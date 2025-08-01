package hs.kr.entrydsm.domain.calculator.policies

import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 계산 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 계산 과정에서 적용되는
 * 비즈니스 규칙과 정책을 캡슐화합니다. 보안, 성능, 정확성과
 * 관련된 계산 정책을 중앙 집중식으로 관리하여 일관성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "Calculation",
    description = "계산 과정의 비즈니스 규칙과 정책을 관리",
    domain = "calculator",
    scope = Scope.DOMAIN
)
class CalculationPolicy {

    companion object {
        private const val MAX_EXPRESSION_LENGTH = 10000
        private const val MAX_CALCULATION_TIME_MS = 30000L
        private const val MAX_MEMORY_USAGE_MB = 100
        private const val MAX_RECURSION_DEPTH = 100
        private const val MAX_VARIABLES_PER_SESSION = 1000
        private const val MAX_SESSION_DURATION_HOURS = 24
        private const val MAX_CALCULATIONS_PER_MINUTE = 1000
        private const val MAX_CONCURRENT_CALCULATIONS = 10
        
        // 허용된 표현식 패턴
        private val ALLOWED_EXPRESSION_PATTERNS = listOf(
            Regex("^[\\d\\s+\\-*/().,a-zA-Z_]+$"), // 기본 수식 패턴
            Regex("^[\\w\\s+\\-*/.(),=<>!&|^%]+$")   // 확장 수식 패턴
        )
        
        // 금지된 패턴들
        private val FORBIDDEN_PATTERNS = listOf(
            Regex("\\beval\\b", RegexOption.IGNORE_CASE),
            Regex("\\bexec\\b", RegexOption.IGNORE_CASE),
            Regex("\\bsystem\\b", RegexOption.IGNORE_CASE),
            Regex("\\bprocess\\b", RegexOption.IGNORE_CASE),
            Regex("\\bfile\\b", RegexOption.IGNORE_CASE),
            Regex("\\bimport\\b", RegexOption.IGNORE_CASE),
            Regex("__.*__") // Python dunder methods
        )
    }

    private val sessionMetrics = mutableMapOf<String, SessionMetrics>()
    private val rateLimiters = mutableMapOf<String, RateLimiter>()

    /**
     * 계산 요청이 허용되는지 검증합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션
     * @return 허용되면 true
     */
    fun isCalculationAllowed(request: CalculationRequest, session: CalculationSession): Boolean {
        return try {
            validateExpressionSafety(request.formula) &&
            validateExpressionLength(request.formula) &&
            validateSessionLimits(session) &&
            validateRateLimit(session.sessionId) &&
            validateResourceUsage(request, session)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 표현식의 안전성을 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 안전하면 true
     */
    fun isExpressionSafe(expression: String): Boolean {
        return validateExpressionSafety(expression)
    }

    /**
     * 계산 복잡도를 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 허용 가능한 복잡도면 true
     */
    fun isComplexityAcceptable(expression: String): Boolean {
        val complexity = calculateComplexity(expression)
        return complexity <= 10000 // 복잡도 제한
    }

    /**
     * 세션의 리소스 사용량을 검증합니다.
     *
     * @param session 검증할 세션
     * @return 리소스 사용량이 적절하면 true
     */
    fun isResourceUsageAcceptable(session: CalculationSession): Boolean {
        val metrics = getSessionMetrics(session.sessionId)
        return metrics.memoryUsageMB <= MAX_MEMORY_USAGE_MB &&
               metrics.averageCalculationTimeMs <= MAX_CALCULATION_TIME_MS
    }

    /**
     * 계산 결과의 유효성을 검증합니다.
     *
     * @param result 검증할 결과
     * @return 유효하면 true
     */
    fun isResultValid(result: Any?): Boolean {
        return when (result) {
            null -> false
            is Double -> !result.isNaN() && result.isFinite()
            is Float -> !result.isNaN() && result.isFinite()
            is Number -> true
            is Boolean -> true
            is String -> result.length <= 10000
            else -> false
        }
    }

    /**
     * 세션의 보안 정책을 적용합니다.
     *
     * @param session 검증할 세션
     * @return 보안 정책을 만족하면 true
     */
    fun applySecurityPolicy(session: CalculationSession): Boolean {
        return session.variables.size <= MAX_VARIABLES_PER_SESSION &&
               isSessionDurationAcceptable(session) &&
               !containsSuspiciousActivity(session)
    }

    /**
     * 성능 정책을 적용합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션
     * @return 성능 정책을 만족하면 true
     */
    fun applyPerformancePolicy(request: CalculationRequest, session: CalculationSession): Boolean {
        return estimateExecutionTime(request.formula) <= MAX_CALCULATION_TIME_MS &&
               estimateMemoryUsage(request.formula) <= MAX_MEMORY_USAGE_MB &&
               validateConcurrencyLimit(session.sessionId)
    }

    /**
     * 계산 캐싱이 허용되는지 확인합니다.
     *
     * @param expression 표현식
     * @param variables 변수들
     * @return 캐싱 허용되면 true
     */
    fun isCachingAllowed(expression: String, variables: Map<String, Any>): Boolean {
        return expression.length <= 1000 && // 짧은 표현식만 캐싱
               variables.size <= 10 && // 변수가 많지 않을 때만
               !containsRandomFunction(expression) && // 랜덤 함수가 없을 때만
               !containsTimeFunction(expression) // 시간 관련 함수가 없을 때만
    }

    /**
     * 계산 우선순위를 결정합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션
     * @return 우선순위 (높을수록 높은 우선순위)
     */
    fun calculatePriority(request: CalculationRequest, session: CalculationSession): Int {
        var priority = 0
        
        // 사용자 타입에 따른 우선순위
        if (session.userId != null) {
            priority += 10 // 로그인 사용자
        }
        
        // 표현식 복잡도에 따른 우선순위 (단순할수록 높은 우선순위)
        val complexity = calculateComplexity(request.formula)
        priority += maxOf(0, 100 - complexity / 100)
        
        // 세션 활동에 따른 우선순위
        val sessionAge = System.currentTimeMillis() - session.createdAt.toEpochMilli()
        if (sessionAge < 300000) { // 5분 이내 생성된 세션
            priority += 5
        }
        
        return priority
    }

    /**
     * 계산 타임아웃을 결정합니다.
     *
     * @param expression 표현식
     * @return 타임아웃 시간 (밀리초)
     */
    fun calculateTimeout(expression: String): Long {
        val complexity = calculateComplexity(expression)
        val baseTimeout = 1000L // 기본 1초
        val complexityTimeout = (complexity / 100) * 1000L // 복잡도에 따른 추가 시간
        return minOf(baseTimeout + complexityTimeout, MAX_CALCULATION_TIME_MS)
    }

    // Private helper methods

    private fun validateExpressionSafety(expression: String): Boolean {
        // 금지된 패턴 검사
        if (FORBIDDEN_PATTERNS.any { it.containsMatchIn(expression) }) {
            return false
        }
        
        // 허용된 패턴 검사
        if (ALLOWED_EXPRESSION_PATTERNS.none { it.matches(expression) }) {
            return false
        }
        
        return true
    }

    private fun validateExpressionLength(expression: String): Boolean {
        return expression.length <= MAX_EXPRESSION_LENGTH
    }

    private fun validateSessionLimits(session: CalculationSession): Boolean {
        return session.variables.size <= MAX_VARIABLES_PER_SESSION &&
               session.calculations.size <= 10000 // 최대 계산 이력
    }

    private fun validateRateLimit(sessionId: String): Boolean {
        val rateLimiter = rateLimiters.getOrPut(sessionId) {
            RateLimiter(MAX_CALCULATIONS_PER_MINUTE, 60000) // 1분당 최대 계산 수
        }
        return rateLimiter.isAllowed()
    }

    private fun validateResourceUsage(request: CalculationRequest, session: CalculationSession): Boolean {
        val estimatedTime = estimateExecutionTime(request.formula)
        val estimatedMemory = estimateMemoryUsage(request.formula)
        
        return estimatedTime <= MAX_CALCULATION_TIME_MS &&
               estimatedMemory <= MAX_MEMORY_USAGE_MB
    }

    private fun validateConcurrencyLimit(sessionId: String): Boolean {
        val metrics = getSessionMetrics(sessionId)
        return metrics.concurrentCalculations < MAX_CONCURRENT_CALCULATIONS
    }

    private fun isSessionDurationAcceptable(session: CalculationSession): Boolean {
        val durationHours = (System.currentTimeMillis() - session.createdAt.toEpochMilli()) / 3600000
        return durationHours <= MAX_SESSION_DURATION_HOURS
    }

    private fun containsSuspiciousActivity(session: CalculationSession): Boolean {
        val recentFailures = session.calculations.filter { it.isFailure() }
        
        // 최근 계산 수가 많고 실패율이 높으면 의심스러운 활동으로 간주
        return recentFailures.size > 100 && session.calculations.size > 200
    }

    private fun calculateComplexity(expression: String): Int {
        var complexity = expression.length // 기본 복잡도
        
        // 연산자 개수에 따른 복잡도
        complexity += expression.count { it in "+-*/%^" } * 2
        
        // 괄호 깊이에 따른 복잡도
        var depth = 0
        var maxDepth = 0
        for (char in expression) {
            when (char) {
                '(' -> {
                    depth++
                    maxDepth = maxOf(maxDepth, depth)
                }
                ')' -> depth--
            }
        }
        complexity += maxDepth * 10
        
        // 함수 호출에 따른 복잡도
        val functionCount = Regex("[a-zA-Z]\\w*\\(").findAll(expression).count()
        complexity += functionCount * 20
        
        return complexity
    }

    private fun estimateExecutionTime(expression: String): Long {
        val complexity = calculateComplexity(expression)
        return (complexity * 0.1).toLong() // 매우 간단한 추정
    }

    private fun estimateMemoryUsage(expression: String): Int {
        val complexity = calculateComplexity(expression)
        return (complexity * 0.001).toInt() + 1 // 매우 간단한 추정 (MB)
    }

    private fun containsRandomFunction(expression: String): Boolean {
        return Regex("\\b(random|rand)\\b", RegexOption.IGNORE_CASE).containsMatchIn(expression)
    }

    private fun containsTimeFunction(expression: String): Boolean {
        return Regex("\\b(now|time|date)\\b", RegexOption.IGNORE_CASE).containsMatchIn(expression)
    }

    private fun getSessionMetrics(sessionId: String): SessionMetrics {
        return sessionMetrics.getOrPut(sessionId) { SessionMetrics() }
    }

    /**
     * 세션 메트릭을 업데이트합니다.
     *
     * @param sessionId 세션 ID
     * @param executionTime 실행 시간
     * @param memoryUsage 메모리 사용량
     */
    fun updateSessionMetrics(sessionId: String, executionTime: Long, memoryUsage: Int) {
        val metrics = getSessionMetrics(sessionId)
        metrics.updateMetrics(executionTime, memoryUsage)
    }

    /**
     * 세션 메트릭을 나타내는 클래스입니다.
     */
    private data class SessionMetrics(
        var totalCalculations: Long = 0,
        var totalExecutionTimeMs: Long = 0,
        var totalMemoryUsageMB: Long = 0,
        var concurrentCalculations: Int = 0,
        var lastActivity: Long = System.currentTimeMillis()
    ) {
        val averageCalculationTimeMs: Long
            get() = if (totalCalculations > 0) totalExecutionTimeMs / totalCalculations else 0
        
        val memoryUsageMB: Int
            get() = if (totalCalculations > 0) (totalMemoryUsageMB / totalCalculations).toInt() else 0
        
        fun updateMetrics(executionTime: Long, memoryUsage: Int) {
            totalCalculations++
            totalExecutionTimeMs += executionTime
            totalMemoryUsageMB += memoryUsage
            lastActivity = System.currentTimeMillis()
        }
    }

    /**
     * 속도 제한을 관리하는 클래스입니다.
     */
    private data class RateLimiter(
        private val maxRequests: Int,
        private val windowMs: Long
    ) {
        private val requests = mutableListOf<Long>()
        
        fun isAllowed(): Boolean {
            val now = System.currentTimeMillis()
            
            // 윈도우 밖의 요청들 제거
            requests.removeAll { now - it > windowMs }
            
            return if (requests.size < maxRequests) {
                requests.add(now)
                true
            } else {
                false
            }
        }
    }

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxExpressionLength" to MAX_EXPRESSION_LENGTH,
        "maxCalculationTimeMs" to MAX_CALCULATION_TIME_MS,
        "maxMemoryUsageMB" to MAX_MEMORY_USAGE_MB,
        "maxRecursionDepth" to MAX_RECURSION_DEPTH,
        "maxVariablesPerSession" to MAX_VARIABLES_PER_SESSION,
        "maxSessionDurationHours" to MAX_SESSION_DURATION_HOURS,
        "maxCalculationsPerMinute" to MAX_CALCULATIONS_PER_MINUTE,
        "maxConcurrentCalculations" to MAX_CONCURRENT_CALCULATIONS,
        "allowedPatterns" to ALLOWED_EXPRESSION_PATTERNS.size,
        "forbiddenPatterns" to FORBIDDEN_PATTERNS.size
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "CalculationPolicy",
        "activeSessions" to sessionMetrics.size,
        "activeRateLimiters" to rateLimiters.size,
        "securityRules" to listOf("expression_patterns", "resource_limits", "rate_limiting"),
        "performanceRules" to listOf("execution_time", "memory_usage", "concurrency_limits")
    )
}