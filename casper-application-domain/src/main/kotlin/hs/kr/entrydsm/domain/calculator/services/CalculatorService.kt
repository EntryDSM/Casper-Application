package hs.kr.entrydsm.domain.calculator.services

import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.calculator.policies.CalculationPolicy
import hs.kr.entrydsm.domain.calculator.specifications.CalculationValiditySpec
import hs.kr.entrydsm.domain.evaluator.aggregates.ExpressionEvaluator
// Removed unused EvaluatorException and EvaluationResult imports
import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.domain.parser.aggregates.LRParser
import hs.kr.entrydsm.domain.ast.services.TreeOptimizer
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode
import java.time.Instant

/**
 * 계산기의 핵심 비즈니스 로직을 처리하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 계산 요청의 처리와 결과 생성을
 * 담당합니다. 렉싱, 파싱, 평가의 전체 파이프라인을 조율하며
 * 정책과 명세를 적용하여 안전하고 정확한 계산을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "CalculatorService",
    type = hs.kr.entrydsm.global.annotation.service.type.ServiceType.DOMAIN_SERVICE
)
class CalculatorService(
    private val lexer: LexerAggregate,
    private val parser: LRParser,
    private val evaluator: ExpressionEvaluator,
    private val calculationPolicy: CalculationPolicy,
    private val validitySpec: CalculationValiditySpec,
    private val treeOptimizer: TreeOptimizer
) {

    companion object {
        private const val DEFAULT_TIMEOUT_MS = 30000L
        private const val MAX_RETRIES = 3
    }

    private val calculationCache = mutableMapOf<String, CachedResult>()
    private val performanceMetrics = PerformanceMetrics()

    /**
     * 계산 요청을 처리합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션 (선택적)
     * @return 계산 결과
     */
    fun calculate(request: CalculationRequest, session: CalculationSession? = null): CalculationResult {
        val startTime = System.currentTimeMillis()
        
        try {
            performanceMetrics.incrementTotalRequests()
            
            // 1. 요청 유효성 검증
            validateRequest(request, session)
            
            // 2. 정책 검증
            checkPolicy(request, session)
            
            // 3. 캐시 확인
            val cachedResult = retrieveFromCache(request, session)
            if (cachedResult != null) {
                return cachedResult
            }
            
            // 4. 계산 실행
            val result = performCalculation(request, session)
            
            // 5. 결과 캐싱
            cacheResultIfNeeded(request, session, result)
            
            // 6. 메트릭 업데이트
            val executionTime = System.currentTimeMillis() - startTime
            updateMetrics(request, session, executionTime)
            
            return result
            
        } catch (e: DomainException) {
            // 도메인 예외는 이미 적절한 컨텍스트를 포함하고 있음
            performanceMetrics.incrementFailures()
            return createFailureResult(request, e.message ?: "도메인 오류", startTime)
            
        } catch (e: IllegalArgumentException) {
            // 잘못된 인수 예외
            performanceMetrics.incrementFailures()
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "잘못된 계산 인수: ${e.message}",
                cause = e,
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to (session?.sessionId ?: "anonymous")
                )
            )
            
        } catch (e: ArithmeticException) {
            // 산술 연산 예외
            performanceMetrics.incrementFailures()
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "수학 연산 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to (session?.sessionId ?: "anonymous")
                )
            )
            
        } catch (e: Exception) {
            // 예상치 못한 시스템 예외
            performanceMetrics.incrementFailures()
            throw DomainException(
                errorCode = ErrorCode.UNEXPECTED_ERROR,
                message = "계산 실행 중 예상치 못한 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to (session?.sessionId ?: "anonymous"),
                    "exceptionType" to e.javaClass.simpleName
                )
            )
        }
    }

    /**
     * 일괄 계산을 수행합니다.
     *
     * @param requests 계산 요청들
     * @param session 계산 세션
     * @return 계산 결과들
     */
    fun calculateBatch(requests: List<CalculationRequest>, session: CalculationSession? = null): List<CalculationResult> {
        require(requests.isNotEmpty()) { "계산 요청 목록은 비어있을 수 없습니다" }
        
        return requests.map { request ->
            calculate(request, session)
        }
    }

    /**
     * 병렬 계산을 수행합니다.
     *
     * @param requests 계산 요청들
     * @param session 계산 세션
     * @return 계산 결과들
     */
    fun calculateParallel(requests: List<CalculationRequest>, session: CalculationSession? = null): List<CalculationResult> {
        require(requests.isNotEmpty()) { "계산 요청 목록은 비어있을 수 없습니다" }
        
        return requests.parallelStream().map { request ->
            calculate(request, session)
        }.toList()
    }

    /**
     * 표현식의 유효성을 검증합니다.
     *
     * @param expression 검증할 표현식
     * @param variables 변수들
     * @return 유효하면 true
     */
    fun validateExpression(expression: String, variables: Map<String, Any> = emptyMap()): Boolean {
        val request = CalculationRequest(
            formula = expression,
            variables = variables
        )
        return validitySpec.isSatisfiedBy(request)
    }

    /**
     * 표현식을 분석합니다.
     *
     * @param expression 분석할 표현식
     * @return 분석 결과
     */
    fun analyzeExpression(expression: String): Map<String, Any> {
        try {
            val lexingResult = lexer.tokenize(expression)
            val tokens = if (lexingResult.isSuccess) lexingResult.tokens else emptyList()
            val ast = parser.parse(tokens)
            
            return mapOf(
                "tokenCount" to tokens.size,
                "parseTree" to ast.toString(),
                "variables" to extractVariables(expression),
                "functions" to extractFunctions(expression),
                "complexity" to calculationPolicy.calculateTimeout(expression) / 1000,
                "isValid" to validateExpression(expression),
                "riskScore" to validitySpec.calculateRiskScore(expression)
            )
        } catch (e: Exception) {
            return mapOf(
                "error" to (e.message ?: "Unknown error"),
                "isValid" to false
            )
        }
    }

    /**
     * 계산 성능을 최적화합니다.
     *
     * @param expression 최적화할 표현식
     * @return 최적화된 표현식
     */
    fun optimizeExpression(expression: String): String {
        // 간단한 최적화 예시들
        var optimized = expression.trim()
        
        // 중복 공백 제거
        optimized = optimized.replace(Regex("\\s+"), " ")
        
        // 불필요한 괄호 제거 (간단한 경우만)
        optimized = optimized.replace(Regex("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)"), "$1")
        
        // 상수 폴딩은 AST 최적화 단계에서 TreeOptimizer에 위임됨
        // 문자열 기반 최적화 대신 AST 기반 상수 폴딩 사용
        
        return optimized
    }

    /**
     * 캐시를 관리합니다.
     *
     * @param maxSize 최대 캐시 크기
     * @param maxAge 최대 캐시 유지 시간 (밀리초)
     */
    fun manageCaches(maxSize: Int = 1000, maxAge: Long = 3600000) { // 1시간
        val currentTime = System.currentTimeMillis()
        
        // 만료된 캐시 제거
        calculationCache.entries.removeIf { (_, cached) ->
            currentTime - cached.timestamp > maxAge
        }
        
        // 크기 제한
        if (calculationCache.size > maxSize) {
            val sortedEntries = calculationCache.entries.sortedBy { it.value.timestamp }
            val toRemove = sortedEntries.take(calculationCache.size - maxSize)
            toRemove.forEach { calculationCache.remove(it.key) }
        }
    }

    // Private helper methods

    private fun executeCalculation(request: CalculationRequest, session: CalculationSession?): CalculationResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. 렉싱
            val lexingResult = lexer.tokenize(request.formula)
            val tokens = if (lexingResult.isSuccess) lexingResult.tokens else emptyList()
            
            // 2. 파싱
            val ast = parser.parse(tokens)
            
            // 3. AST 최적화 (상수 폴딩 포함)
            val optimizedAst = treeOptimizer.optimize(ast)
            
            // 4. 변수 결합
            val allVariables = mutableMapOf<String, Any>()
            session?.variables?.let { allVariables.putAll(it) }
            allVariables.putAll(request.variables)
            
            // 5. 평가 (최적화된 AST 사용)
            val evaluationResult = evaluateWithRetry(optimizedAst, allVariables)
            
            val executionTime = System.currentTimeMillis() - startTime
            
            return CalculationResult(
                result = evaluationResult,
                executionTimeMs = executionTime,
                formula = request.formula,
                metadata = mapOf(
                    "tokenCount" to tokens.size,
                    "variableCount" to allVariables.size,
                    "astDepth" to calculateASTDepth(optimizedAst),
                    "optimized" to true
                )
            )
            
        } catch (e: Exception) {
            return createFailureResult(request, "계산 오류: ${e.message}", startTime)
        }
    }

    private fun evaluateWithRetry(ast: Any, variables: Map<String, Any>, retries: Int = MAX_RETRIES): Any? {
        repeat(retries) { attempt ->
            try {
                // AST를 실제 ASTNode로 변환하여 평가
                // 여기서는 간단히 evaluator의 evaluate 메서드 호출을 시뮬레이션
                return evaluateAST(ast, variables)
            } catch (e: Exception) {
                if (attempt == retries - 1) {
                    throw e
                }
                // 재시도 전 잠시 대기
                Thread.sleep(100)
            }
        }
        throw RuntimeException("최대 재시도 횟수 초과")
    }

    private fun evaluateAST(ast: Any, variables: Map<String, Any>): Any? {
        return try {
            // AST를 실제 ASTNode로 캐스팅하여 evaluator로 평가
            val astNode = ast as? hs.kr.entrydsm.domain.ast.entities.ASTNode
                ?: throw IllegalArgumentException("Invalid AST node type: ${ast.javaClass.simpleName}")
            
            // 변수와 함께 새로운 evaluator 생성하여 평가
            val evaluatorWithVariables = evaluator.withVariables(variables)
            evaluatorWithVariables.evaluate(astNode)
            
        } catch (e: IllegalArgumentException) {
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "AST 평가 실패: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "variableCount" to variables.size
                )
            )
        } catch (e: ArithmeticException) {
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "수학 연산 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "variableCount" to variables.size
                )
            )
        } catch (e: Exception) {
            throw DomainException(
                errorCode = ErrorCode.UNEXPECTED_ERROR,
                message = "AST 평가 중 예상치 못한 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "variableCount" to variables.size,
                    "exceptionType" to e.javaClass.simpleName
                )
            )
        }
    }

    private fun createFailureResult(request: CalculationRequest, error: String, startTime: Long): CalculationResult {
        val executionTime = System.currentTimeMillis() - startTime
        return CalculationResult(
            result = null,
            executionTimeMs = executionTime,
            formula = request.formula,
            errors = listOf(error)
        )
    }

    private fun generateCacheKey(request: CalculationRequest, session: CalculationSession?): String {
        val variables = (session?.variables ?: emptyMap()) + request.variables
        val variablesHash = variables.entries.sortedBy { it.key }.hashCode()
        return "${request.formula.hashCode()}_${variablesHash}"
    }

    private fun getCachedResult(key: String): CachedResult? {
        return calculationCache[key]?.takeIf { 
            System.currentTimeMillis() - it.timestamp < 3600000 // 1시간 유효
        }
    }

    private fun cacheResult(key: String, result: CalculationResult) {
        if (result.isSuccess() && calculationCache.size < 1000) { // 캐시 크기 제한
            calculationCache[key] = CachedResult(
                result = result.result,
                executionTime = result.executionTimeMs,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun extractVariables(expression: String): Set<String> {
        val pattern = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
        return pattern.findAll(expression)
            .map { it.value }
            .filter { it.uppercase() !in setOf("PI", "E", "TRUE", "FALSE", "SIN", "COS", "TAN", "LOG", "ABS") }
            .toSet()
    }

    private fun extractFunctions(expression: String): Set<String> {
        val pattern = Regex("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(")
        return pattern.findAll(expression)
            .map { it.groupValues[1] }
            .toSet()
    }


    private fun calculateASTDepth(ast: Any): Int {
        // 실제 구현에서는 AST의 실제 구조를 분석
        return ast.toString().count { it == '(' } + 1
    }

    private fun estimateMemoryUsage(expression: String): Int {
        // 간단한 메모리 사용량 추정
        return (expression.length * 0.001).toInt() + 1 // MB
    }

    /**
     * 캐시된 결과를 나타내는 데이터 클래스입니다.
     */
    private data class CachedResult(
        val result: Any?,
        val executionTime: Long,
        val timestamp: Long
    ) {
        fun toCalculationResult(formula: String, startTime: Long): CalculationResult {
            return CalculationResult(
                result = result,
                executionTimeMs = System.currentTimeMillis() - startTime,
                formula = formula,
                metadata = mapOf("fromCache" to true)
            )
        }
    }

    /**
     * 성능 메트릭을 관리하는 클래스입니다.
     */
    private class PerformanceMetrics {
        private var totalRequests = 0L
        private var totalFailures = 0L
        private var totalCacheHits = 0L
        private var totalExecutionTime = 0L
        private var requestCount = 0L

        fun incrementTotalRequests() = synchronized(this) { totalRequests++ }
        fun incrementFailures() = synchronized(this) { totalFailures++ }
        fun incrementCacheHits() = synchronized(this) { totalCacheHits++ }
        
        fun updateExecutionTime(time: Long) = synchronized(this) {
            totalExecutionTime += time
            requestCount++
        }

        fun getMetrics(): Map<String, Any> = synchronized(this) {
            mapOf(
                "totalRequests" to totalRequests,
                "totalFailures" to totalFailures,
                "totalCacheHits" to totalCacheHits,
                "averageExecutionTime" to if (requestCount > 0) totalExecutionTime.toDouble() / requestCount else 0.0,
                "successRate" to if (totalRequests > 0) ((totalRequests - totalFailures).toDouble() / totalRequests) else 0.0,
                "cacheHitRate" to if (totalRequests > 0) (totalCacheHits.toDouble() / totalRequests) else 0.0
            )
        }
    }

    /**
     * 서비스의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "serviceName" to "CalculatorService",
        "defaultTimeoutMs" to DEFAULT_TIMEOUT_MS,
        "maxRetries" to MAX_RETRIES,
        "cacheEnabled" to true,
        "maxCacheSize" to 1000,
        "cacheExpirationMs" to 3600000,
        "parallelProcessingEnabled" to true
    )

    /**
     * 서비스의 성능 통계를 반환합니다.
     *
     * @return 성능 통계 맵
     */
    fun getStatistics(): Map<String, Any> {
        val metrics = performanceMetrics.getMetrics()
        return metrics + mapOf(
            "currentCacheSize" to calculationCache.size,
            "maxCacheSize" to 1000
        )
    }

    /**
     * 서비스 상태를 확인합니다.
     *
     * @return 상태 정보 맵
     */
    fun getStatus(): Map<String, Any> = mapOf(
        "status" to "active",
        "healthCheck" to checkHealth(),
        "lastActivity" to System.currentTimeMillis(),
        "cacheStatus" to mapOf(
            "size" to calculationCache.size,
            "enabled" to true
        )
    )

    /**
     * 요청 유효성을 검증합니다.
     */
    private fun validateRequest(request: CalculationRequest, session: CalculationSession?) {
        if (!validitySpec.isSatisfiedBy(request, session)) {
            val errors = validitySpec.getValidationErrors(request, session)
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "유효성 검증 실패: ${errors.joinToString(", ") { it.message }}",
                context = mapOf(
                    "formula" to request.formula,
                    "errorCount" to errors.size,
                    "errors" to errors.map { it.message }
                )
            )
        }
    }

    /**
     * 계산 정책을 확인합니다.
     */
    private fun checkPolicy(request: CalculationRequest, session: CalculationSession?) {
        if (session != null && !calculationPolicy.isCalculationAllowed(request, session)) {
            throw DomainException(
                errorCode = ErrorCode.BUSINESS_RULE_VIOLATION,
                message = "계산 정책 위반",
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to session.sessionId
                )
            )
        }
    }

    /**
     * 캐시에서 결과를 조회합니다.
     */
    private fun retrieveFromCache(request: CalculationRequest, session: CalculationSession?): CalculationResult? {
        if (session?.settings?.enableCaching != true) return null
        
        val cacheKey = generateCacheKey(request, session)
        val cachedResult = getCachedResult(cacheKey)
        
        return if (cachedResult != null) {
            performanceMetrics.incrementCacheHits()
            cachedResult.toCalculationResult(request.formula, System.currentTimeMillis())
        } else null
    }

    /**
     * 실제 계산을 수행합니다.
     */
    private fun performCalculation(request: CalculationRequest, session: CalculationSession?): CalculationResult {
        return executeCalculation(request, session)
    }

    /**
     * 필요한 경우 결과를 캐시에 저장합니다.
     */
    private fun cacheResultIfNeeded(request: CalculationRequest, session: CalculationSession?, result: CalculationResult) {
        if (session?.settings?.enableCaching == true && result.isSuccess()) {
            val cacheKey = generateCacheKey(request, session)
            cacheResult(cacheKey, result)
        }
    }

    /**
     * 메트릭을 업데이트합니다.
     */
    private fun updateMetrics(request: CalculationRequest, session: CalculationSession?, executionTime: Long) {
        calculationPolicy.updateSessionMetrics(
            session?.sessionId ?: "anonymous",
            executionTime,
            estimateMemoryUsage(request.formula)
        )
        performanceMetrics.updateExecutionTime(executionTime)
    }

    /**
     * 서비스 건강 상태를 확인합니다.
     *
     * @return 건강하면 true
     */
    private fun checkHealth(): Boolean {
        return try {
            // 간단한 계산으로 건강 상태 확인
            val testRequest = CalculationRequest("1+1", emptyMap())
            val result = calculate(testRequest)
            result.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}