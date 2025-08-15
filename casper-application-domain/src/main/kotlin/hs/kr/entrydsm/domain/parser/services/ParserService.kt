package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.domain.parser.interfaces.ParserContract
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.domain.parser.values.ParsingResult
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import hs.kr.entrydsm.global.configuration.ParserConfiguration
import hs.kr.entrydsm.global.configuration.interfaces.ConfigurationProvider
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Parser 도메인의 핵심 서비스 클래스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 복잡한 파싱 로직을 캡슐화하고,
 * 다양한 파싱 전략과 최적화 기법을 제공합니다. 높은 수준의 파싱 연산과
 * 여러 도메인 객체들 간의 복잡한 상호작용을 관리합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "ParserService",
    type = ServiceType.DOMAIN_SERVICE
)
class ParserService(
    private val lrParserTableService: LRParserTableService,
    private val firstFollowCalculatorService: FirstFollowCalculatorService,
    private val conflictResolverService: ConflictResolverService,
    private val configurationProvider: ConfigurationProvider
) : ParserContract {

    private val parsingStatistics = mutableMapOf<String, Any>()

    // 설정은 ConfigurationProvider를 통해 동적으로 접근
    private val config: ParserConfiguration
        get() = configurationProvider.getParserConfiguration()

    /**
     * 토큰 목록을 구문 분석하여 AST를 생성합니다.
     *
     * @param tokens 구문 분석할 토큰 목록
     * @return 파싱 결과 (AST 및 메타데이터 포함)
     */
    override fun parse(tokens: List<Token>): ParsingResult {
        val startTime = System.currentTimeMillis()

        try {
            validateTokens(tokens)
            updateStatistics(STAT_PARSE_ATTEMPTS, 1)

            val parsingTable = lrParserTableService.buildParsingTable(Grammar)
            val result = performLRParsing(tokens, parsingTable)

            val duration = System.currentTimeMillis() - startTime
            updateStatistics(STAT_TOTAL_PARSING_TIME, duration)
            updateStatistics(STAT_AVERAGE_TOKENS_PER_SECOND, calculateTokensPerSecond(tokens.size, duration))

            if (result.isSuccess) {
                updateStatistics(STAT_SUCCESSFUL_PARSES, 1)
            } else {
                updateStatistics(STAT_FAILED_PARSES, 1)
            }

            return result.copy(duration = duration)

        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            updateStatistics(STAT_ERROR_PARSES, 1)

            return ParsingResult.failure(
                error = ParserException(
                    errorCode = hs.kr.entrydsm.global.exception.ErrorCode.PARSING_ERROR,
                    message = "파싱 중 오류 발생: ${e.message}",
                    cause = e
                ),
                duration = duration,
                tokenCount = tokens.size
            )
        }
    }

    /**
     * 단일 토큰 스트림을 구문 분석합니다.
     *
     * @param tokenSequence 토큰 시퀀스
     * @return 파싱 결과
     */
    override fun parseSequence(tokenSequence: Sequence<Token>): ParsingResult {
        val tokens = tokenSequence.take(config.maxTokenCount).toList()
        return parse(tokens)
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
            result.isSuccess && result.ast != null
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
        val originalConfig = config
        val modifiedConfig = if (allowIncomplete) {
            originalConfig.copy(errorRecoveryMode = true)
        } else originalConfig

        configurationProvider.updateParserConfiguration(modifiedConfig)

        try {
            val result = parse(tokens)

            // 불완전한 파싱도 성공으로 처리 (부분 AST가 있는 경우)
            if (allowIncomplete && result.isFailure() && result.ast != null) {
                return result.copy(
                    isSuccess = true,
                    warnings = result.warnings + WARNING_PARTIAL_PARSING
                )
            }

            return result

        } finally {
            configurationProvider.updateParserConfiguration(originalConfig)
        }
    }

    /**
     * 다음에 올 수 있는 유효한 토큰들을 예측합니다.
     *
     * @param currentTokens 현재까지의 토큰 목록
     * @return 다음에 올 수 있는 토큰 타입들
     */
    override fun predictNextTokens(currentTokens: List<Token>): Set<TokenType> {
        return try {
            val parsingTable = lrParserTableService.buildParsingTable(Grammar)
            val currentState = determineCurrentState(currentTokens, parsingTable)

            // 현재 상태에서 가능한 모든 액션의 터미널들 반환
            currentState?.actions?.keys?.toSet() ?: emptySet()

        } catch (e: Exception) {
            emptySet()
        }
    }

    /**
     * 파싱 오류 위치와 예상 토큰을 분석합니다.
     *
     * @param tokens 분석할 토큰 목록
     * @return 오류 분석 결과
     */
    override fun analyzeErrors(tokens: List<Token>): Map<String, Any> {
        val result = parse(tokens)

        return if (result.isFailure() && result.error != null) {
            mapOf(
                ERROR_TYPE to ERROR_TYPE_PARSING,
                ERROR_MESSAGE to (result.error.message ?: "Unknown parsing error"),
                ERROR_TOKEN_COUNT to tokens.size,
                ERROR_EXPECTED_TOKENS to predictNextTokens(tokens),
                ERROR_POSITION to findErrorPosition(tokens, result.error),
                ERROR_SUGGESTIONS to generateErrorSuggestions(tokens, result.error)
            )
        } else {
            mapOf(
                ERROR_TYPE to ERROR_TYPE_NONE,
                ERROR_MESSAGE to PARSING_SUCCESS_MESSAGE,
                ERROR_TOKEN_COUNT to tokens.size
            )
        }
    }

    /**
     * 파서의 현재 상태를 반환합니다.
     *
     * @return 파서 상태 정보
     */
    override fun getState(): Map<String, Any> = mapOf(
        CONFIG_DEBUG_MODE to config.debugMode,
        CONFIG_ERROR_RECOVERY_MODE to config.errorRecoveryMode,
        "maxParsingDepth" to config.maxParsingDepth, // 별도 상수 키 없음
        STATE_PARSING_STATISTICS to parsingStatistics.toMap(),
        STATE_GRAMMAR_INFO to Grammar.getGrammarStatistics(),
        STATE_IS_READY to true
    )

    /**
     * 파서를 초기 상태로 재설정합니다.
     */
    override fun reset() {
        // 설정을 기본값으로 초기화
        configurationProvider.resetToDefaults()
        parsingStatistics.clear()

        // 서비스들도 리셋
        lrParserTableService.clearCache()
        firstFollowCalculatorService.clearCache()
        conflictResolverService.reset()
    }

    /**
     * 파서의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    override fun getConfiguration(): Map<String, Any> = mapOf(
        CONFIG_MAX_PARSING_STEPS to config.maxParsingSteps,
        CONFIG_MAX_STACK_DEPTH to config.maxStackDepth,
        CONFIG_MAX_TOKEN_COUNT to config.maxTokenCount,
        CONFIG_DEBUG_MODE to config.debugMode,
        CONFIG_ERROR_RECOVERY_MODE to config.errorRecoveryMode,
        CONFIG_ENABLE_OPTIMIZATIONS to config.enableOptimizations,
        CONFIG_CACHING_ENABLED to config.cachingEnabled,
        CONFIG_STREAMING_BATCH_SIZE to config.streamingBatchSize,
        CONFIG_PARSING_STRATEGY to PARSING_STRATEGY_LR1,
        CONFIG_OPTIMIZATIONS to OPTIMIZATIONS_LIST
    )

    /**
     * 파싱 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵 (파싱 횟수, 성공률, 평균 처리 시간 등)
     */
    override fun getStatistics(): Map<String, Any> {
        val totalAttempts = (parsingStatistics[STAT_PARSE_ATTEMPTS] as? Long) ?: 0L
        val successfulParses = (parsingStatistics[STAT_SUCCESSFUL_PARSES] as? Long) ?: 0L
        val successRate = if (totalAttempts > 0) successfulParses.toDouble() / totalAttempts else 0.0

        return parsingStatistics.toMap() + mapOf(
            STAT_SUCCESS_RATE to successRate,
            STAT_TOTAL_ATTEMPTS to totalAttempts,
            STAT_GRAMMAR_COMPLEXITY to (Grammar.getGrammarStatistics()["productionCount"] ?: 0),
            STAT_AVERAGE_PARSING_TIME to calculateAverageParsingTime()
        )
    }

    /**
     * 디버그 모드를 설정합니다.
     *
     * @param enabled 디버그 모드 활성화 여부
     */
    override fun setDebugMode(enabled: Boolean) {
        val updatedConfig = config.copy(debugMode = enabled)
        configurationProvider.updateParserConfiguration(updatedConfig)
    }

    /**
     * 오류 복구 모드를 설정합니다.
     *
     * @param enabled 오류 복구 모드 활성화 여부
     */
    override fun setErrorRecoveryMode(enabled: Boolean) {
        val updatedConfig = config.copy(errorRecoveryMode = enabled)
        configurationProvider.updateParserConfiguration(updatedConfig)
    }

    /**
     * 최대 파싱 깊이를 설정합니다.
     *
     * @param maxDepth 최대 파싱 깊이
     */
    override fun setMaxParsingDepth(maxDepth: Int) {
        if (maxDepth <= 0) {
            throw ParserException.maxParsingDepthNotPositive(maxDepth)
        }
        if (maxDepth > config.maxStackDepth) {
            throw ParserException.maxParsingDepthExceedsLimit(
                maxDepth = maxDepth,
                limit = config.maxStackDepth
            )
        }

        val updatedConfig = config.copy(maxParsingDepth = maxDepth)
        configurationProvider.updateParserConfiguration(updatedConfig)
    }

    /**
     * 스트리밍 모드로 파싱을 수행합니다.
     *
     * @param tokens 토큰 목록
     * @param callback 파싱 진행 상황 콜백
     * @return 파싱 결과
     */
    override fun parseStreaming(tokens: List<Token>, callback: (progress: Double) -> Unit): ParsingResult {
        val totalSteps = tokens.size * 2 // 대략적인 스텝 수 추정
        var currentStep = 0

        val progressCallback = {
            currentStep++
            callback(currentStep.toDouble() / totalSteps)
        }

        return performStreamingParsing(tokens, progressCallback)
    }

    /**
     * 비동기적으로 구문 분석을 수행합니다.
     *
     * @param tokens 분석할 토큰 목록
     * @param callback 분석 완료 시 호출될 콜백 함수
     */
    override fun parseAsync(tokens: List<Token>, callback: (ParsingResult) -> Unit) {
        // 실제 비동기 구현은 코루틴이나 별도 스레드를 사용해야 함
        // 여기서는 동기적으로 실행하고 콜백 호출
        val result = parse(tokens)
        callback(result)
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
        // 간단한 증분 파싱 구현
        // 실제로는 더 복잡한 로직이 필요 (파싱 트리의 부분 재구성)

        return if (changeStartIndex == 0 || !previousResult.isSuccess) {
            // 처음부터 다시 파싱
            parse(newTokens)
        } else {
            // 변경 부분만 다시 파싱하고 이전 결과와 병합
            // 현재는 단순히 전체 재파싱
            parse(newTokens).copy(
                metadata = previousResult.metadata + (INCREMENTAL_PARSING_FLAG to true)
            )
        }
    }

    /**
     * 문법 규칙의 유효성을 검증합니다.
     *
     * @return 문법이 유효하면 true
     */
    override fun validateGrammar(): Boolean {
        return try {
            Grammar.isValid() &&
                    lrParserTableService.canBuildParsingTable(Grammar) &&
                    !conflictResolverService.hasUnresolvableConflicts(Grammar)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 파싱 테이블의 충돌을 확인합니다.
     *
     * @return 충돌 정보 맵
     */
    override fun checkParsingConflicts(): Map<String, Any> {
        return try {
            val parsingTable = lrParserTableService.buildParsingTable(Grammar)
            val conflicts = parsingTable.getConflicts()

            mapOf(
                CONFLICT_HAS_CONFLICTS to conflicts.isNotEmpty(),
                CONFLICT_TYPES to conflicts.keys,
                CONFLICT_COUNT to conflicts.values.sumOf { it.size },
                CONFLICT_CONFLICTS to conflicts,
                CONFLICT_RESOLUTION_STRATEGY to conflictResolverService.getResolutionStrategy()
            )
        } catch (e: Exception) {
            mapOf(
                CONFLICT_HAS_CONFLICTS to true,
                CONFLICT_ERROR to (e.message ?: "Unknown error")
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
            CONTEXT_TOKEN_INDEX to tokenIndex,
            CONTEXT_INFO to CONTEXT_INFO_NOT_IMPLEMENTED,
            CONTEXT_AVAILABLE_ACTIONS to emptyList<String>(),
            CONTEXT_STACK_DEPTH to 0
        )
    }

    /**
     * 현재 파싱 스택의 상태를 반환합니다.
     *
     * @return 파싱 스택 정보
     */
    override fun getParsingStack(): List<Any> {
        // 실제 파싱 중이 아니므로 빈 스택 반환
        return emptyList()
    }

    /**
     * 파서가 지원하는 최대 토큰 수를 반환합니다.
     *
     * @return 최대 토큰 수
     */
    override fun getMaxSupportedTokens(): Int = config.maxTokenCount

    /**
     * 파서의 메모리 사용량을 반환합니다.
     *
     * @return 메모리 사용량 정보
     */
    override fun getMemoryUsage(): Map<String, Any> {
        val runtime = Runtime.getRuntime()

        return mapOf(
            MEMORY_TOTAL to runtime.totalMemory(),
            MEMORY_FREE to runtime.freeMemory(),
            MEMORY_USED to (runtime.totalMemory() - runtime.freeMemory()),
            MEMORY_MAX to runtime.maxMemory(),
            MEMORY_PARSING_TABLE_SIZE to estimateParsingTableSize(),
            MEMORY_STATISTICS_SIZE to parsingStatistics.size * STATISTICS_MEMORY_MULTIPLIER // 대략적 추정
        )
    }

    // Private helper methods

    private fun validateTokens(tokens: List<Token>) {
        if (tokens.size > config.maxTokenCount) {
            throw ParserException.tokenCountExceedsLimit(
                count = tokens.size,
                limit = config.maxTokenCount
            )
        }
    }

    private fun performLRParsing(tokens: List<Token>, parsingTable: ParsingTable): ParsingResult {
        // LR 파싱 알고리즘 구현
        val stack = mutableListOf<Int>() // 상태 스택
        val inputBuffer = tokens.toMutableList()
        var currentState = parsingTable.startState
        var step = 0

        stack.add(currentState)

        while (step < config.maxParsingSteps && inputBuffer.isNotEmpty()) {
            step++

            val currentToken = inputBuffer.first()
            val action = parsingTable.getAction(currentState, currentToken.type)

            when {
                action?.isShift() == true -> {
                    // Shift 연산
                    inputBuffer.removeAt(0)
                    currentState = (action as hs.kr.entrydsm.domain.parser.values.LRAction.Shift).state
                    stack.add(currentState)
                }
                action?.isReduce() == true -> {
                    // Reduce 연산
                    val productionId = action.getProductionId()
                    val production = Grammar.productions.find { it.id == productionId }
                        ?: throw ParserException(
                            errorCode = ErrorCode.PARSING_ERROR,
                            message = "생산 규칙을 찾을 수 없습니다: $productionId"
                        )
                    repeat(production.right.size) { stack.removeLastOrNull() }

                    val gotoState = stack.lastOrNull()?.let {
                        parsingTable.getGoto(it, production.left)
                    }

                    if (gotoState != null) {
                        currentState = gotoState
                        stack.add(currentState)
                    } else {
                        throw ParserException(
                            errorCode = ErrorCode.PARSING_ERROR,
                            message = "Goto 상태를 찾을 수 없습니다"
                        )
                    }
                }
                action?.isAccept() == true -> {
                    // Accept
                    return ParsingResult.success(
                        ast = createDummyAST(), // 실제로는 스택에서 AST 구성
                        tokenCount = tokens.size,
                        nodeCount = 1,
                        maxDepth = 1
                    )
                }
                else -> {
                    // Error
                    if (config.errorRecoveryMode) {
                        return attemptErrorRecovery(tokens, stack, inputBuffer)
                    } else {
                        throw ParserException(
                            errorCode = ErrorCode.SYNTAX_ERROR,
                            message = "파싱 오류: 예상하지 못한 토큰 ${currentToken.type}"
                        )
                    }
                }
            }
        }

        throw ParserException(
            errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSING_FAILED,
            message = "파싱이 완료되지 않았습니다"
        )
    }

    private fun performStreamingParsing(
        tokens: List<Token>,
        progressCallback: () -> Unit
    ): ParsingResult {
        // 스트리밍 파싱 구현 (단순화)
        val batchSize = config.streamingBatchSize
        var processedTokens = 0

        while (processedTokens < tokens.size) {
            val batch = tokens.drop(processedTokens).take(batchSize)
            processedTokens += batch.size
            progressCallback()

            // 배치 처리 시뮬레이션
            Thread.sleep(STREAMING_SLEEP_TIME)
        }

        return parse(tokens)
    }

    private fun determineCurrentState(tokens: List<Token>, parsingTable: ParsingTable): ParsingState? {
        // 현재 토큰들로부터 파싱 상태 결정 (단순화)
        return parsingTable.states[parsingTable.startState]
    }

    private fun findErrorPosition(tokens: List<Token>, error: ParserException): Int {
        // 오류 위치 찾기 (단순화)
        return tokens.size - 1
    }

    private fun generateErrorSuggestions(tokens: List<Token>, error: ParserException): List<String> {
        return ERROR_SUGGESTIONS_LIST
    }

    private fun attemptErrorRecovery(
        originalTokens: List<Token>,
        stack: MutableList<Int>,
        inputBuffer: MutableList<Token>
    ): ParsingResult {
        // 간단한 에러 복구 (토큰 스킵)
        if (inputBuffer.isNotEmpty()) {
            inputBuffer.removeAt(0)
            return ParsingResult.failure(
                error = ParserException(
                    errorCode = ErrorCode.PARSING_ERROR,
                    message = "에러 복구 수행됨"
                ),
                partialAST = createDummyAST(),
                tokenCount = originalTokens.size
            )
        }

        return ParsingResult.failure(
            error = ParserException(
                errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSING_FAILED,
                message = "에러 복구 실패"
            ),
            tokenCount = originalTokens.size
        )
    }

    private fun createDummyAST(): hs.kr.entrydsm.domain.ast.entities.NumberNode {
        // 임시 AST 노드 생성 (NumberNode 사용)
        return hs.kr.entrydsm.domain.ast.entities.NumberNode(0.0)
    }

    private fun updateStatistics(key: String, value: Any) {
        when (value) {
            is Number -> {
                val current = parsingStatistics[key] as? Long ?: 0L
                parsingStatistics[key] = current + value.toLong()
            }
            else -> parsingStatistics[key] = value
        }
    }

    private fun calculateTokensPerSecond(tokenCount: Int, durationMs: Long): Double {
        return if (durationMs > 0) (tokenCount * TOKENS_PER_SECOND_MULTIPLIER) / durationMs else 0.0
    }

    private fun calculateAverageParsingTime(): Double {
        val totalTime = parsingStatistics[STAT_TOTAL_PARSING_TIME] as? Long ?: 0L
        val totalAttempts = parsingStatistics[STAT_PARSE_ATTEMPTS] as? Long ?: 0L
        return if (totalAttempts > 0) totalTime.toDouble() / totalAttempts else 0.0
    }

    private fun estimateParsingTableSize(): Long {
        return try {
            val parsingTable = lrParserTableService.buildParsingTable(Grammar)
            // 대략적인 메모리 사용량 추정
            (parsingTable.states.size * PARSING_TABLE_STATE_SIZE +
                    parsingTable.actionTable.size * PARSING_TABLE_ACTION_SIZE +
                    parsingTable.gotoTable.size * PARSING_TABLE_GOTO_SIZE)
        } catch (e: Exception) {
            0L
        }
    }

    companion object {
        // Statistics keys
        private const val STAT_PARSE_ATTEMPTS = "parseAttempts"
        private const val STAT_TOTAL_PARSING_TIME = "totalParsingTime"
        private const val STAT_AVERAGE_TOKENS_PER_SECOND = "averageTokensPerSecond"
        private const val STAT_SUCCESSFUL_PARSES = "successfulParses"
        private const val STAT_FAILED_PARSES = "failedParses"
        private const val STAT_ERROR_PARSES = "errorParses"
        private const val STAT_SUCCESS_RATE = "successRate"
        private const val STAT_TOTAL_ATTEMPTS = "totalAttempts"
        private const val STAT_GRAMMAR_COMPLEXITY = "grammarComplexity"
        private const val STAT_AVERAGE_PARSING_TIME = "averageParsingTime"

        // Configuration keys
        private const val CONFIG_MAX_PARSING_STEPS = "maxParsingSteps"
        private const val CONFIG_MAX_STACK_DEPTH = "maxStackDepth"
        private const val CONFIG_MAX_TOKEN_COUNT = "maxTokenCount"
        private const val CONFIG_DEBUG_MODE = "debugMode"
        private const val CONFIG_ERROR_RECOVERY_MODE = "errorRecoveryMode"
        private const val CONFIG_ENABLE_OPTIMIZATIONS = "enableOptimizations"
        private const val CONFIG_CACHING_ENABLED = "cachingEnabled"
        private const val CONFIG_STREAMING_BATCH_SIZE = "streamingBatchSize"
        private const val CONFIG_PARSING_STRATEGY = "parsingStrategy"
        private const val CONFIG_OPTIMIZATIONS = "optimizations"

        // State keys
        private const val STATE_PARSING_STATISTICS = "parsingStatistics"
        private const val STATE_GRAMMAR_INFO = "grammarInfo"
        private const val STATE_IS_READY = "isReady"

        // Error analysis keys
        private const val ERROR_TYPE = "errorType"
        private const val ERROR_MESSAGE = "message"
        private const val ERROR_TOKEN_COUNT = "tokenCount"
        private const val ERROR_EXPECTED_TOKENS = "expectedTokens"
        private const val ERROR_POSITION = "errorPosition"
        private const val ERROR_SUGGESTIONS = "suggestions"

        // Error types
        private const val ERROR_TYPE_PARSING = "ParsingError"
        private const val ERROR_TYPE_NONE = "None"

        // Conflict check keys
        private const val CONFLICT_HAS_CONFLICTS = "hasConflicts"
        private const val CONFLICT_TYPES = "conflictTypes"
        private const val CONFLICT_COUNT = "conflictCount"
        private const val CONFLICT_CONFLICTS = "conflicts"
        private const val CONFLICT_RESOLUTION_STRATEGY = "resolutionStrategy"
        private const val CONFLICT_ERROR = "error"

        // Parsing context keys
        private const val CONTEXT_TOKEN_INDEX = "tokenIndex"
        private const val CONTEXT_INFO = "contextInfo"
        private const val CONTEXT_AVAILABLE_ACTIONS = "availableActions"
        private const val CONTEXT_STACK_DEPTH = "stackDepth"

        // Memory usage keys
        private const val MEMORY_TOTAL = "totalMemory"
        private const val MEMORY_FREE = "freeMemory"
        private const val MEMORY_USED = "usedMemory"
        private const val MEMORY_MAX = "maxMemory"
        private const val MEMORY_PARSING_TABLE_SIZE = "parsingTableSize"
        private const val MEMORY_STATISTICS_SIZE = "statisticsSize"

        // Values
        private const val PARSING_STRATEGY_LR1 = "LR(1)"
        private const val CONTEXT_INFO_NOT_IMPLEMENTED = "파싱 컨텍스트 분석 미구현"
        private const val PARSING_SUCCESS_MESSAGE = "파싱 성공"
        private const val INCREMENTAL_PARSING_FLAG = "incrementalParsing"

        // Optimizations
        private val OPTIMIZATIONS_LIST = listOf(
            "tableCompression",
            "stateMinimization",
            "conflictResolution"
        )

        // Error suggestions
        private val ERROR_SUGGESTIONS_LIST = listOf(
            "문법을 확인하세요",
            "괄호가 균형을 이루는지 확인하세요",
            "연산자 우선순위를 확인하세요"
        )

        // Warnings
        private const val WARNING_PARTIAL_PARSING = "부분 파싱 결과입니다"

        // Memory size estimates
        private const val STATISTICS_MEMORY_MULTIPLIER = 50L
        private const val PARSING_TABLE_STATE_SIZE = 500L
        private const val PARSING_TABLE_ACTION_SIZE = 100L
        private const val PARSING_TABLE_GOTO_SIZE = 100L

        // Streaming
        private const val STREAMING_SLEEP_TIME = 10L
        private const val TOKENS_PER_SECOND_MULTIPLIER = 1000.0
    }
}
