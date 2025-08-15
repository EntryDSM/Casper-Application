package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.global.extensions.*

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.domain.parser.interfaces.GrammarProvider
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.ParsingResult
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.domain.parser.values.ParserState
import hs.kr.entrydsm.domain.parser.values.ParsingTraceEntry
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * 실제 LR 파싱을 수행하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 LR(1) 파싱 알고리즘의
 * 핵심 구현을 담당합니다. 토큰 스트림을 AST로 변환하는 과정에서
 * 파싱 테이블을 활용한 Shift/Reduce 연산을 수행하며,
 * 에러 복구와 디버깅 기능을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "RealLRParserService",
    type = ServiceType.DOMAIN_SERVICE
)
class RealLRParserService(
    private val grammarProvider: GrammarProvider,
    private val parsingTable: ParsingTable
) {

    companion object {
        private const val MAX_PARSING_STEPS = 100000
        private const val MAX_STACK_SIZE = 10000
        private const val MAX_ERROR_RECOVERY_ATTEMPTS = 100
    }

    // 파싱 상태를 캡슐화한 값 객체
    private var parserState = ParserState()
    
    // 파싱 설정
    private var enableErrorRecovery = true
    private var enableDebugging = false
    private var maxStackSize = MAX_STACK_SIZE

    /**
     * 토큰 목록을 LR 파싱하여 AST를 생성합니다.
     *
     * @param tokens 파싱할 토큰 목록
     * @return 파싱 결과
     */
    fun parse(tokens: List<Token>): ParsingResult {
        val startTime = System.currentTimeMillis()
        
        try {
            initializeParsing(tokens)
            val ast = performLRParsing()
            val duration = System.currentTimeMillis() - startTime
            
            return ParsingResult.success(
                ast = ast,
                duration = duration,
                tokenCount = tokens.size,
                nodeCount = calculateNodeCount(ast),
                maxDepth = calculateASTDepth(ast),
                metadata = createParsingMetadata()
            )
            
        } catch (e: ParserException) {
            val duration = System.currentTimeMillis() - startTime
            
            return ParsingResult.failure(
                error = e,
                duration = duration,
                tokenCount = tokens.size,
                metadata = createParsingMetadata()
            )
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            
            return ParsingResult.failure(
                error = ParserException(
                    errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSING_FAILED,
                    message = "파싱 중 예상치 못한 오류 발생",
                    cause = e
                ),
                duration = duration,
                tokenCount = tokens.size,
                metadata = createParsingMetadata()
            )
        }
    }

    /**
     * 단일 파싱 단계를 수행합니다.
     *
     * @return 파싱이 완료되면 true, 계속해야 하면 false
     */
    fun parseStep(): Boolean {
        if (parserState.currentPosition > parserState.inputTokens.size) {
            throw ParserException(
                errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSER_STATE_ERROR,
                message = "파싱이 이미 완료되었습니다"
            )
        }
        
        val currentToken = getCurrentToken()
        val currentState = parserState.stateStack.lastOrNull() ?: throw ParserException(
            errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSER_STATE_ERROR,
            message = "스택이 비어있습니다"
        )
        val action = parsingTable.getAction(currentState, currentToken.type)
        
        if (enableDebugging) {
            recordTrace(currentState, currentToken, action)
        }
        
        when {
            action == null -> {
                if (enableErrorRecovery) {
                    performErrorRecovery(currentToken)
                    return false
                } else {
                    throw ParserException(
                        errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.UNEXPECTED_EOF,
                        message = "예상하지 못한 토큰: ${currentToken.type} at position ${parserState.currentPosition}"
                    )
                }
            }
            action.isShift() -> {
                performShift(action, currentToken)
                return false
            }
            action.isReduce() -> {
                performReduce(action)
                return false
            }
            action.isAccept() -> {
                return true // 파싱 완료
            }
            else -> {
                throw ParserException(
                    errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSER_STATE_ERROR,
                    message = "알 수 없는 액션: $action"
                )
            }
        }
    }

    /**
     * 현재 파싱 상태를 반환합니다.
     *
     * @return 파싱 상태 정보
     */
    fun getCurrentState(): Map<String, Any> = mapOf(
        "currentPosition" to parserState.currentPosition,
        "inputSize" to parserState.inputTokens.size,
        "stackSize" to parserState.stateStack.size,
        "currentStateId" to (parserState.stateStack.lastOrNull() ?: -1),
        "currentToken" to (getCurrentToken().type.name),
        "parsingSteps" to parserState.parsingSteps,
        "shiftOperations" to parserState.shiftOperations,
        "reduceOperations" to parserState.reduceOperations,
        "errorRecoveryAttempts" to parserState.errorRecoveryAttempts
    )

    /**
     * 파싱 추적 정보를 반환합니다.
     *
     * @return 파싱 추적 목록
     */
    fun getParsingTrace(): List<ParsingTraceEntry> = parserState.parsingTrace.toList()

    /**
     * 파서를 초기화합니다.
     */
    fun reset() {
        parserState.stateStack.clear()
        parserState.astStack.clear()
        parserState.inputTokens.clear()
        parserState.currentPosition = 0
        parserState.parsingSteps = 0
        parserState.shiftOperations = 0
        parserState.reduceOperations = 0
        parserState.errorRecoveryAttempts = 0
        parserState.parsingTrace.clear()
    }

    /**
     * 에러 복구 모드를 설정합니다.
     *
     * @param enabled 에러 복구 활성화 여부
     */
    fun setErrorRecoveryEnabled(enabled: Boolean) {
        this.enableErrorRecovery = enabled
    }

    /**
     * 디버깅 모드를 설정합니다.
     *
     * @param enabled 디버깅 활성화 여부
     */
    fun setDebuggingEnabled(enabled: Boolean) {
        this.enableDebugging = enabled
    }

    /**
     * 최대 스택 크기를 설정합니다.
     *
     * @param maxSize 최대 스택 크기
     */
    fun setMaxStackSize(maxSize: Int) {
        if (maxSize <= 0) {
            throw ParserException.maxStackSizeNotPositive(maxSize)
        }
        this.maxStackSize = maxSize
    }

    /**
     * 파싱을 초기화합니다.
     */
    private fun initializeParsing(tokens: List<Token>) {
        reset()
        parserState.inputTokens.addAll(tokens)
        parserState.inputTokens.add(Token(TokenType.DOLLAR, "$", hs.kr.entrydsm.global.values.Position.of(0))) // EOF 토큰 추가
        parserState.stateStack.add(parsingTable.startState)
        parserState.currentPosition = 0
        parserState.parsingSteps = 0
    }

    /**
     * LR 파싱을 수행합니다.
     */
    private fun performLRParsing(): ASTNode {
        while (parserState.parsingSteps < MAX_PARSING_STEPS) {
            if (parserState.stateStack.size > maxStackSize) {
                throw ParserException(
                    errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSER_STATE_ERROR,
                    message = "스택 오버플로우: ${parserState.stateStack.size} > $maxStackSize"
                )
            }
            
            if (parseStep()) {
                // 파싱 완료
                return parserState.astStack.lastOrNull() ?: createEmptyAST()
            }
            
            parserState.parsingSteps++
        }
        
        throw ParserException(
            errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSING_FAILED,
            message = "파싱이 최대 단계 수를 초과했습니다: $MAX_PARSING_STEPS"
        )
    }

    /**
     * Shift 연산을 수행합니다.
     */
    private fun performShift(action: LRAction, token: Token) {
        val nextState = (action as hs.kr.entrydsm.domain.parser.values.LRAction.Shift).state
        parserState.stateStack.add(nextState)
        parserState.astStack.add(createLeafNode(token))
        parserState.currentPosition++
        parserState.shiftOperations++
        
        if (enableDebugging) {
            println("SHIFT: state ${parserState.stateStack[parserState.stateStack.size - 2]} -> $nextState, token: ${token.type}")
        }
    }

    /**
     * Reduce 연산을 수행합니다.
     */
    private fun performReduce(action: LRAction) {
        val productionId = action.getProductionId()
        val production = grammarProvider.getProductionById(productionId)
        
        // 스택에서 심볼들 제거
        val children = mutableListOf<ASTNode?>()
        repeat(production.right.size) {
            if (parserState.stateStack.isNotEmpty()) parserState.stateStack.removeLastOrNull()
            children.add(0, parserState.astStack.removeLastOrNull()) // 역순으로 추가
        }
        
        // AST 노드 생성
        val astNode = production.buildAST(children.filterNotNull() as List<Any>) as? hs.kr.entrydsm.domain.ast.entities.ASTNode
        parserState.astStack.add(astNode)
        
        // Goto 연산
        val currentState = parserState.stateStack.lastOrNull() ?: throw ParserException(
            errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSER_STATE_ERROR,
            message = "Reduce 후 스택이 비어있습니다"
        )
        val gotoState = parsingTable.getGoto(currentState, production.left)
            ?: throw ParserException(
            errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSER_STATE_ERROR,
            message = "Goto 상태를 찾을 수 없습니다: state $currentState, symbol ${production.left}"
        )
        
        parserState.stateStack.add(gotoState)
        parserState.reduceOperations++
        
        if (enableDebugging) {
            println("REDUCE: production $productionId (${production.left} -> ${production.right.joinToString(" ")})")
            println("       goto state $gotoState")
        }
    }

    /**
     * 에러 복구를 수행합니다.
     */
    private fun performErrorRecovery(currentToken: Token) {
        parserState.errorRecoveryAttempts++
        
        if (parserState.errorRecoveryAttempts > MAX_ERROR_RECOVERY_ATTEMPTS) {
            throw ParserException(
                errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSING_FAILED,
                message = "에러 복구 시도 횟수 초과: $MAX_ERROR_RECOVERY_ATTEMPTS"
            )
        }
        
        // 간단한 에러 복구: 현재 토큰 스킵
        parserState.currentPosition++
        
        if (enableDebugging) {
            println("ERROR RECOVERY: skipping token ${currentToken.type} at position ${parserState.currentPosition - 1}")
        }
    }

    /**
     * 현재 토큰을 반환합니다.
     */
    private fun getCurrentToken(): Token {
        return if (parserState.currentPosition < parserState.inputTokens.size) {
            parserState.inputTokens[parserState.currentPosition]
        } else {
            parserState.inputTokens.last() // EOF 토큰
        }
    }

    /**
     * 리프 AST 노드를 생성합니다.
     */
    private fun createLeafNode(token: Token): hs.kr.entrydsm.domain.ast.entities.NumberNode {
        return hs.kr.entrydsm.domain.ast.entities.NumberNode(
            if (token.type == hs.kr.entrydsm.domain.lexer.entities.TokenType.NUMBER) {
                token.value.toDoubleOrNull() ?: 0.0
            } else {
                0.0 // 기본값
            }
        )
    }

    /**
     * 빈 AST 노드를 생성합니다.
     */
    private fun createEmptyAST(): hs.kr.entrydsm.domain.ast.entities.NumberNode {
        return hs.kr.entrydsm.domain.ast.entities.NumberNode(0.0)
    }

    /**
     * AST의 노드 개수를 계산합니다.
     */
    private fun calculateNodeCount(ast: ASTNode): Int {
        return 1 + ast.getChildren().sumOf { calculateNodeCount(it) }
    }

    /**
     * AST의 깊이를 계산합니다.
     */
    private fun calculateASTDepth(ast: ASTNode): Int {
        return if (ast.getChildren().isEmpty()) {
            1
        } else {
            1 + (ast.getChildren().maxOfOrNull { calculateASTDepth(it) } ?: 0)
        }
    }

    /**
     * 파싱 메타데이터를 생성합니다.
     */
    private fun createParsingMetadata(): Map<String, Any> = mapOf(
        "parsingSteps" to parserState.parsingSteps,
        "shiftOperations" to parserState.shiftOperations,
        "reduceOperations" to parserState.reduceOperations,
        "errorRecoveryAttempts" to parserState.errorRecoveryAttempts,
        "maxStackDepth" to parserState.stateStack.size,
        "finalPosition" to parserState.currentPosition,
        "parsingTraceSize" to parserState.parsingTrace.size,
        "enableErrorRecovery" to enableErrorRecovery,
        "enableDebugging" to enableDebugging
    )

    /**
     * 파싱 추적 정보를 기록합니다.
     */
    private fun recordTrace(state: Int, token: Token, action: LRAction?) {
        val actionStr = action?.getActionType() ?: "ERROR"
        val traceEntry = if (action?.isShift() == true) {
            ParsingTraceEntry.shift(state, token, parserState.stateStack.lastOrNull() ?: 0, parserState.parsingSteps)
        } else {
            ParsingTraceEntry(
                step = parserState.parsingSteps,
                action = actionStr,
                state = state,
                token = token,
                production = null,
                stackSnapshot = parserState.stateStack.toList()
            )
        }
        parserState.parsingTrace.add(traceEntry)
    }


    /**
     * 서비스의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxParsingSteps" to MAX_PARSING_STEPS,
        "maxStackSize" to maxStackSize,
        "maxErrorRecoveryAttempts" to MAX_ERROR_RECOVERY_ATTEMPTS,
        "enableErrorRecovery" to enableErrorRecovery,
        "enableDebugging" to enableDebugging,
        "grammarComplexity" to grammarProvider.calculateComplexity(),
        "parsingTableSize" to parsingTable.getSizeInfo()
    )

    /**
     * 서비스 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "serviceName" to "RealLRParserService",
        "currentSessionStats" to getCurrentState(),
        "totalTraceEntries" to parserState.parsingTrace.size,
        "operationDistribution" to mapOf(
            "shift" to parserState.shiftOperations,
            "reduce" to parserState.reduceOperations,
            "errorRecovery" to parserState.errorRecoveryAttempts
        )
    )

    /**
     * 파싱 추적을 문자열로 출력합니다.
     *
     * @return 파싱 추적 문자열
     */
    fun dumpParsingTrace(): String = buildString {
        appendLine("=== LR 파싱 추적 정보 ===")
        appendLine("총 단계: ${parserState.parsingSteps}")
        appendLine("Shift 연산: ${parserState.shiftOperations}")
        appendLine("Reduce 연산: ${parserState.reduceOperations}")
        appendLine("에러 복구: ${parserState.errorRecoveryAttempts}")
        appendLine()
        
        parserState.parsingTrace.forEach { entry ->
            appendLine(entry.toString())
        }
    }
}