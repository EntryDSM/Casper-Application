package hs.kr.entrydsm.domain.parser.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.domain.parser.interfaces.ParserContract
import hs.kr.entrydsm.domain.parser.services.ParserService
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.ParsingResult
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

/**
 * 파싱 컨텍스트를 관리하는 애그리게이트 루트입니다.
 *
 * DDD Aggregate 패턴을 적용하여 파싱 과정의 전체 컨텍스트와 상태를 
 * 일관성 있게 관리합니다. 파싱 스택, 현재 상태, 입력 버퍼, 에러 복구 등
 * 파싱과 관련된 모든 정보를 통합적으로 처리합니다.
 *
 * @property grammar 사용할 문법
 * @property parsingTable 파싱 테이블
 * @property parserService 파서 서비스
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Aggregate(context = "parser")
class ParsingContextAggregate(
    private val grammar: Grammar,
    private val parsingTable: ParsingTable,
    private val parserService: ParserService
) : ParserContract {

    companion object {
        private const val MAX_STACK_SIZE = 10000
        private const val MAX_ERROR_RECOVERY_ATTEMPTS = 100
        private const val MAX_PARSING_STEPS = 100000
    }

    // 파싱 상태
    private val stateStack = mutableListOf<Int>()
    private val astStack = mutableListOf<ASTNode?>()
    private var currentState: Int = parsingTable.startState
    private var inputTokens = mutableListOf<Token>()
    private var currentTokenIndex = 0
    
    // 파싱 통계
    private var stepCount = 0
    private var shiftCount = 0
    private var reduceCount = 0
    private var errorRecoveryCount = 0
    
    // 설정
    private var debugMode = false
    private var errorRecoveryMode = true
    private var maxParsingDepth = MAX_STACK_SIZE
    
    // 파싱 기록
    private val parsingTrace = mutableListOf<ParsingStep>()
    private val errorHistory = mutableListOf<ParsingError>()

    /**
     * 파싱 단계를 나타내는 데이터 클래스입니다.
     */
    data class ParsingStep(
        val stepNumber: Int,
        val action: String,
        val currentState: Int,
        val currentToken: TokenType?,
        val stackSize: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * 파싱 에러를 나타내는 데이터 클래스입니다.
     */
    data class ParsingError(
        val errorType: String,
        val message: String,
        val tokenIndex: Int,
        val stackState: List<Int>,
        val recoveryAction: String?,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * 토큰 목록을 구문 분석하여 AST를 생성합니다.
     *
     * @param tokens 구문 분석할 토큰 목록
     * @return 파싱 결과 (AST 및 메타데이터 포함)
     */
    override fun parse(tokens: List<Token>): ParsingResult {
        val startTime = System.currentTimeMillis()
        
        try {
            initializeParsing(tokens)
            val result = performParsing()
            val duration = System.currentTimeMillis() - startTime
            
            return result.copy(
                duration = duration,
                metadata = result.metadata + getParsingMetadata()
            )
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            recordError("ParsingException", e.message ?: "Unknown error", null)
            
            return ParsingResult.failure(
                error = ParserException(
                    errorCode = hs.kr.entrydsm.global.exception.ErrorCode.LR_PARSING_ERROR,
                    message = "파싱 실패: ${e.message}",
                    cause = e
                ),
                duration = duration,
                tokenCount = tokens.size,
                metadata = getParsingMetadata()
            )
        } finally {
            if (debugMode) {
                printParsingTrace()
            }
        }
    }

    /**
     * 단일 토큰 스트림을 구문 분석합니다.
     *
     * @param tokenSequence 토큰 시퀀스
     * @return 파싱 결과
     */
    override fun parseSequence(tokenSequence: Sequence<Token>): ParsingResult {
        return parse(tokenSequence.toList())
    }

    /**
     * 주어진 토큰 목록이 문법적으로 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 목록
     * @return 유효하면 true, 그렇지 않으면 false
     */
    override fun validate(tokens: List<Token>): Boolean {
        return try {
            val result = parse(tokens)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 부분 파싱을 수행합니다 (구문 완성, 에러 복구 등에 사용).
     *
     * @param tokens 부분 토큰 목록
     * @param allowIncomplete 불완전한 구문 허용 여부
     * @return 부분 파싱 결과
     */
    override fun parsePartial(tokens: List<Token>, allowIncomplete: Boolean): ParsingResult {
        val originalErrorRecovery = errorRecoveryMode
        
        try {
            errorRecoveryMode = true
            val result = parse(tokens)
            
            // 불완전한 파싱도 허용하는 경우 부분 결과 반환
            if (allowIncomplete && result.isFailure() && astStack.isNotEmpty()) {
                return ParsingResult.success(
                    ast = astStack.lastOrNull() ?: createEmptyAST(),
                    warnings = listOf("부분 파싱 결과입니다"),
                    tokenCount = tokens.size,
                    metadata = getParsingMetadata() + ("partialParsing" to true)
                )
            }
            
            return result
            
        } finally {
            errorRecoveryMode = originalErrorRecovery
        }
    }

    /**
     * 다음에 올 수 있는 유효한 토큰들을 예측합니다.
     *
     * @param currentTokens 현재까지의 토큰 목록
     * @return 다음에 올 수 있는 토큰 타입들
     */
    override fun predictNextTokens(currentTokens: List<Token>): Set<TokenType> {
        // 현재 상태에서 가능한 액션들의 터미널 심볼들 반환
        val currentParsingState = parsingTable.getState(currentState)
        return currentParsingState.actions.keys.toSet()
    }

    /**
     * 파싱 오류 위치와 예상 토큰을 분석합니다.
     *
     * @param tokens 분석할 토큰 목록
     * @return 오류 분석 결과
     */
    override fun analyzeErrors(tokens: List<Token>): Map<String, Any> {
        val result = parse(tokens)
        
        return mapOf(
            "hasErrors" to result.isFailure(),
            "errorHistory" to errorHistory,
            "expectedTokens" to predictNextTokens(tokens),
            "currentPosition" to currentTokenIndex,
            "stackDepth" to stateStack.size,
            "parsingSteps" to stepCount,
            "errorRecoveryAttempts" to errorRecoveryCount
        )
    }

    /**
     * 파서의 현재 상태를 반환합니다.
     *
     * @return 파서 상태 정보
     */
    override fun getState(): Map<String, Any> = mapOf(
        "currentState" to currentState,
        "stackSize" to stateStack.size,
        "currentTokenIndex" to currentTokenIndex,
        "inputTokensRemaining" to (inputTokens.size - currentTokenIndex),
        "stepCount" to stepCount,
        "shiftCount" to shiftCount,
        "reduceCount" to reduceCount,
        "errorRecoveryCount" to errorRecoveryCount,
        "debugMode" to debugMode,
        "errorRecoveryMode" to errorRecoveryMode,
        "maxParsingDepth" to maxParsingDepth
    )

    /**
     * 파서를 초기 상태로 재설정합니다.
     */
    override fun reset() {
        stateStack.clear()
        astStack.clear()
        currentState = parsingTable.startState
        inputTokens.clear()
        currentTokenIndex = 0
        stepCount = 0
        shiftCount = 0
        reduceCount = 0
        errorRecoveryCount = 0
        parsingTrace.clear()
        errorHistory.clear()
        debugMode = false
        errorRecoveryMode = true
        maxParsingDepth = MAX_STACK_SIZE
    }

    /**
     * 파서의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    override fun getConfiguration(): Map<String, Any> = mapOf(
        "maxStackSize" to MAX_STACK_SIZE,
        "maxErrorRecoveryAttempts" to MAX_ERROR_RECOVERY_ATTEMPTS,
        "maxParsingSteps" to MAX_PARSING_STEPS,
        "grammarInfo" to grammar.getGrammarStatistics(),
        "parsingTableSize" to parsingTable.getSizeInfo()
    )

    /**
     * 파싱 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵 (파싱 횟수, 성공률, 평균 처리 시간 등)
     */
    override fun getStatistics(): Map<String, Any> = mapOf(
        "aggregateName" to "ParsingContextAggregate",
        "currentSessionStats" to mapOf(
            "stepCount" to stepCount,
            "shiftCount" to shiftCount,
            "reduceCount" to reduceCount,
            "errorRecoveryCount" to errorRecoveryCount,
            "maxStackDepth" to stateStack.size,
            "parsingTraceSize" to parsingTrace.size,
            "errorHistorySize" to errorHistory.size
        )
    )

    /**
     * 디버그 모드를 설정합니다.
     *
     * @param enabled 디버그 모드 활성화 여부
     */
    override fun setDebugMode(enabled: Boolean) {
        debugMode = enabled
    }

    /**
     * 오류 복구 모드를 설정합니다.
     *
     * @param enabled 오류 복구 모드 활성화 여부
     */
    override fun setErrorRecoveryMode(enabled: Boolean) {
        errorRecoveryMode = enabled
    }

    /**
     * 최대 파싱 깊이를 설정합니다.
     *
     * @param maxDepth 최대 파싱 깊이
     */
    override fun setMaxParsingDepth(maxDepth: Int) {
        require(maxDepth > 0) { "최대 파싱 깊이는 양수여야 합니다: $maxDepth" }
        require(maxDepth <= MAX_STACK_SIZE) { "최대 파싱 깊이가 한계를 초과했습니다: $maxDepth > $MAX_STACK_SIZE" }
        
        this.maxParsingDepth = maxDepth
    }

    /**
     * 스트리밍 모드로 파싱을 수행합니다.
     *
     * @param tokens 토큰 목록
     * @param callback 파싱 진행 상황 콜백
     * @return 파싱 결과
     */
    override fun parseStreaming(tokens: List<Token>, callback: (progress: Double) -> Unit): ParsingResult {
        return parserService.parseStreaming(tokens, callback)
    }

    /**
     * 비동기적으로 구문 분석을 수행합니다.
     *
     * @param tokens 분석할 토큰 목록
     * @param callback 분석 완료 시 호출될 콜백 함수
     */
    override fun parseAsync(tokens: List<Token>, callback: (ParsingResult) -> Unit) {
        parserService.parseAsync(tokens, callback)
    }

    /**
     * 증분 파싱을 수행합니다.
     * 기존 파싱 결과를 재활용하여 성능을 향상시킵니다.
     *
     * @param previousResult 이전 파싱 결과
     * @param newTokens 새로운 토큰 목록
     * @param changeStartIndex 변경 시작 위치
     * @return 증분 파싱 결과
     */
    override fun incrementalParse(
        previousResult: ParsingResult,
        newTokens: List<Token>,
        changeStartIndex: Int
    ): ParsingResult {
        return parserService.incrementalParse(previousResult, newTokens, changeStartIndex)
    }

    /**
     * 문법 규칙의 유효성을 검증합니다.
     *
     * @return 문법이 유효하면 true
     */
    override fun validateGrammar(): Boolean {
        return grammar.isValid() && parsingTable.isLR1Valid()
    }

    /**
     * 파싱 테이블의 충돌을 확인합니다.
     *
     * @return 충돌 정보 맵
     */
    override fun checkParsingConflicts(): Map<String, Any> {
        return parsingTable.getConflicts().let { conflicts ->
            mapOf(
                "hasConflicts" to conflicts.isNotEmpty(),
                "conflictTypes" to conflicts.keys,
                "conflictDetails" to conflicts
            )
        }
    }

    /**
     * 특정 위치에서의 파싱 컨텍스트를 반환합니다.
     *
     * @param tokenIndex 토큰 인덱스
     * @return 파싱 컨텍스트 정보
     */
    override fun getParsingContext(tokenIndex: Int): Map<String, Any> {
        return mapOf(
            "tokenIndex" to tokenIndex,
            "currentState" to currentState,
            "stackStates" to stateStack.toList(),
            "availableActions" to getCurrentAvailableActions(),
            "expectedTokens" to predictNextTokens(inputTokens.take(tokenIndex)),
            "parsingSteps" to parsingTrace.filter { it.stepNumber <= tokenIndex }
        )
    }

    /**
     * 현재 파싱 스택의 상태를 반환합니다.
     *
     * @return 파싱 스택 정보
     */
    override fun getParsingStack(): List<Any> {
        return stateStack.mapIndexed { index, stateId ->
            mapOf(
                "index" to index,
                "stateId" to stateId,
                "astNode" to (astStack.getOrNull(index)?.toString() ?: "null")
            )
        }
    }

    /**
     * 파서가 지원하는 최대 토큰 수를 반환합니다.
     *
     * @return 최대 토큰 수
     */
    override fun getMaxSupportedTokens(): Int = 50000

    /**
     * 파서의 메모리 사용량을 반환합니다.
     *
     * @return 메모리 사용량 정보
     */
    override fun getMemoryUsage(): Map<String, Any> {
        return mapOf(
            "stateStackSize" to (stateStack.size * 4), // Int 크기
            "astStackSize" to (astStack.size * 100), // 대략적 AST 노드 크기
            "inputTokensSize" to (inputTokens.size * 50), // 대략적 토큰 크기
            "parsingTraceSize" to (parsingTrace.size * 100), // 대략적 trace 크기
            "errorHistorySize" to (errorHistory.size * 200), // 대략적 error 크기
            "totalEstimatedSize" to calculateTotalMemoryUsage()
        )
    }

    // Private helper methods

    private fun initializeParsing(tokens: List<Token>) {
        reset()
        inputTokens.addAll(tokens)
        stateStack.add(currentState)
        
        if (debugMode) {
            recordStep("INIT", "파싱 시작")
        }
    }

    private fun performParsing(): ParsingResult {
        while (stepCount < MAX_PARSING_STEPS && currentTokenIndex <= inputTokens.size) {
            if (stateStack.size > maxParsingDepth) {
                throw IllegalStateException("스택 깊이가 최대값을 초과했습니다: ${stateStack.size} > $maxParsingDepth")
            }
            
            val currentToken = getCurrentToken()
            val action = getActionForCurrentState(currentToken?.type ?: TokenType.DOLLAR)
            
            when {
                action == null -> {
                    if (errorRecoveryMode) {
                        performErrorRecovery(currentToken)
                    } else {
                        throw IllegalStateException("예상하지 못한 토큰: ${currentToken?.type}")
                    }
                }
                action is LRAction.Shift -> performShift(action, currentToken!!)
                action is LRAction.Reduce -> performReduce(action)
                action is LRAction.Accept -> return createSuccessResult()
                else -> throw IllegalStateException("알 수 없는 액션: $action")
            }
            
            stepCount++
        }
        
        throw IllegalStateException("파싱이 최대 단계를 초과했습니다")
    }

    private fun getCurrentToken(): Token? {
        return if (currentTokenIndex < inputTokens.size) {
            inputTokens[currentTokenIndex]
        } else {
            null
        }
    }

    private fun getActionForCurrentState(tokenType: TokenType): LRAction? {
        return parsingTable.getAction(currentState, tokenType)
    }

    private fun performShift(action: LRAction.Shift, token: Token) {
        currentState = action.state
        stateStack.add(currentState)
        astStack.add(createLeafAST(token))
        currentTokenIndex++
        shiftCount++
        
        if (debugMode) {
            recordStep("SHIFT", "Shift to state $currentState, token: ${token.type}")
        }
    }

    private fun performReduce(action: LRAction.Reduce) {
        val production = action.production
        val rightSize = production.right.size
        
        // 스택에서 심볼들 제거
        val astChildren = mutableListOf<ASTNode?>()
        repeat(rightSize) {
            if (stateStack.isNotEmpty()) stateStack.removeLastOrNull()
            astChildren.add(0, astStack.removeLastOrNull())
        }
        
        // AST 노드 생성
        val newAST = production.astBuilder.build(astChildren.filterNotNull())
        astStack.add(newAST as? ASTNode)
        
        // Goto 수행
        val gotoState = parsingTable.getGoto(stateStack.lastOrNull() ?: 0, production.left)
        if (gotoState != null) {
            currentState = gotoState
            stateStack.add(currentState)
        } else {
            throw IllegalStateException("Goto 상태를 찾을 수 없습니다: ${production.left}")
        }
        
        reduceCount++
        
        if (debugMode) {
            recordStep("REDUCE", "Reduce by production ${production.id}: ${production.left} -> ${production.right.joinToString(" ")}")
        }
    }

    private fun performErrorRecovery(currentToken: Token?) {
        errorRecoveryCount++
        
        if (errorRecoveryCount > MAX_ERROR_RECOVERY_ATTEMPTS) {
            throw IllegalStateException("에러 복구 시도 횟수가 최대값을 초과했습니다")
        }
        
        // 간단한 에러 복구: 현재 토큰 스킵
        if (currentToken != null) {
            recordError("UnexpectedToken", "예상하지 못한 토큰: ${currentToken.type}", "TokenSkip")
            currentTokenIndex++
        } else {
            recordError("UnexpectedEOF", "예상하지 못한 입력 종료", "EOFHandling")
        }
        
        if (debugMode) {
            recordStep("ERROR_RECOVERY", "에러 복구 수행")
        }
    }

    private fun createSuccessResult(): ParsingResult {
        val finalAST = astStack.lastOrNull() ?: createEmptyAST()
        
        return ParsingResult.success(
            ast = finalAST,
            tokenCount = inputTokens.size,
            nodeCount = calculateNodeCount(finalAST),
            maxDepth = calculateMaxDepth(finalAST),
            metadata = getParsingMetadata()
        )
    }

    private fun createLeafAST(token: Token): ASTNode {
        return hs.kr.entrydsm.domain.ast.entities.VariableNode(token.value)
    }

    private fun createEmptyAST(): ASTNode {
        return hs.kr.entrydsm.domain.ast.entities.NumberNode(0.0)
    }

    private fun recordStep(action: String, description: String) {
        parsingTrace.add(
            ParsingStep(
                stepNumber = stepCount,
                action = action,
                currentState = currentState,
                currentToken = getCurrentToken()?.type,
                stackSize = stateStack.size
            )
        )
    }

    private fun recordError(errorType: String, message: String, recoveryAction: String?) {
        errorHistory.add(
            ParsingError(
                errorType = errorType,
                message = message,
                tokenIndex = currentTokenIndex,
                stackState = stateStack.toList(),
                recoveryAction = recoveryAction
            )
        )
    }

    private fun getCurrentAvailableActions(): List<String> {
        val currentParsingState = parsingTable.getState(currentState)
        return currentParsingState.actions.map { (terminal, action) ->
            "$terminal: ${action.getActionType()}"
        }
    }

    private fun getParsingMetadata(): Map<String, Any> = mapOf(
        "stepCount" to stepCount,
        "shiftCount" to shiftCount,
        "reduceCount" to reduceCount,
        "errorRecoveryCount" to errorRecoveryCount,
        "maxStackDepth" to (stateStack.maxOrNull() ?: 0),
        "finalStackSize" to stateStack.size,
        "parsingTraceSize" to parsingTrace.size,
        "errorHistorySize" to errorHistory.size
    )

    private fun calculateNodeCount(ast: ASTNode): Int {
        return 1 + ast.getChildren().sumOf { calculateNodeCount(it) }
    }

    private fun calculateMaxDepth(ast: ASTNode): Int {
        return if (ast.getChildren().isEmpty()) {
            1
        } else {
            1 + (ast.getChildren().maxOfOrNull { calculateMaxDepth(it) } ?: 0)
        }
    }

    private fun calculateTotalMemoryUsage(): Long {
        return (stateStack.size * 4 +
                astStack.size * 100 +
                inputTokens.size * 50 +
                parsingTrace.size * 100 +
                errorHistory.size * 200).toLong()
    }

    private fun printParsingTrace() {
        println("=== 파싱 추적 정보 ===")
        parsingTrace.forEach { step ->
            println("Step ${step.stepNumber}: ${step.action} - State: ${step.currentState}, Token: ${step.currentToken}, Stack: ${step.stackSize}")
        }
        
        if (errorHistory.isNotEmpty()) {
            println("=== 에러 기록 ===")
            errorHistory.forEach { error ->
                println("${error.errorType}: ${error.message} at token ${error.tokenIndex}")
            }
        }
    }
}