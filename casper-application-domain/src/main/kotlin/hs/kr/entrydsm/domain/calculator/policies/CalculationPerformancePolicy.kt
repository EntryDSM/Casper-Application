package hs.kr.entrydsm.domain.calculator.policies

import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.calculator.values.MultiStepCalculationRequest
import hs.kr.entrydsm.domain.calculator.values.PerformanceRecommendation
import hs.kr.entrydsm.domain.calculator.values.RecommendationType
import hs.kr.entrydsm.domain.calculator.values.RecommendationPriority
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope
import hs.kr.entrydsm.global.constants.ErrorCodes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.*
import java.util.concurrent.TimeoutException

/**
 * POC 코드의 성능 관리 기능을 DDD Policy 패턴으로 구현한 클래스입니다.
 *
 * POC 코드의 CalculatorService에서 제공하는 캐싱(@Cacheable), 성능 측정(measureTimeMillis),
 * 실행 시간 추적, 메모리 관리 등의 기능을 정책으로 캡슐화합니다.
 * Spring Boot의 @Cacheable과 동일한 기능을 DDD 정책으로 구현했습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
@Policy(
    name = "CalculationPerformance",
    description = "POC 코드 기반의 계산 성능 관리 정책",
    domain = "calculator",
    scope = Scope.DOMAIN
)
class CalculationPerformancePolicy {

    companion object {
        // POC 코드의 CalculatorProperties 기본값들
        private const val DEFAULT_CACHE_TTL_SECONDS = 3600L
        private const val DEFAULT_MAX_EXECUTION_TIME_MS = 30000L
        private const val DEFAULT_MAX_MEMORY_MB = 100
        private const val DEFAULT_MAX_CACHE_SIZE = 1000
        
        // 성능 임계값들
        private const val SLOW_CALCULATION_THRESHOLD_MS = 1000L
        private const val VERY_SLOW_CALCULATION_THRESHOLD_MS = 5000L
        private const val MEMORY_WARNING_THRESHOLD_MB = 80
        
        // 통계 추적
        private val totalCalculations = AtomicLong(0)
        private val cachedCalculations = AtomicLong(0)
        private val slowCalculations = AtomicLong(0)
        private val failedCalculations = AtomicLong(0)
        private val totalExecutionTime = AtomicLong(0)
    }

    // POC 코드의 @Cacheable 기능을 구현하는 캐시
    private val calculationCache = ConcurrentHashMap<String, CachedResult>()
    private val multiStepCache = ConcurrentHashMap<String, CachedMultiStepResult>()
    
    // 성능 모니터링을 위한 메트릭
    private val executionTimes = mutableListOf<Long>()
    private val memoryUsage = mutableListOf<Long>()

    /**
     * POC 코드의 CalculatorService.calculate에 해당하는 성능 정책 적용
     */
    fun applyPerformancePolicy(
        request: CalculationRequest,
        enableCaching: Boolean = true,
        timeout: Long = DEFAULT_MAX_EXECUTION_TIME_MS,
        operation: () -> CalculationResult
    ): CalculationResult {
        totalCalculations.incrementAndGet()
        
        // 캐시 확인 (POC의 @Cacheable 기능)
        if (enableCaching) {
            val cacheKey = generateCacheKey(request)
            val cachedResult = calculationCache[cacheKey]
            
            if (cachedResult != null && !cachedResult.isExpired()) {
                cachedCalculations.incrementAndGet()
                return cachedResult.result.copy(
                    executionTimeMs = 0L,
                    metadata = cachedResult.result.metadata + mapOf(
                        "cached" to true,
                        "cacheHit" to true
                    )
                )
            }
        }
        
        // 메모리 사용량 체크
        val beforeMemory = getUsedMemoryMB()
        if (beforeMemory > MEMORY_WARNING_THRESHOLD_MB) {
            return createMemoryLimitError(request, beforeMemory)
        }
        
        // 타임아웃과 성능 측정 (POC의 measureTimeMillis 기능)
        var result: CalculationResult
        val executionTime = measureTimeMillis {
            try {
                result = executeWithTimeout(operation, timeout)
            } catch (e: Exception) {
                failedCalculations.incrementAndGet()
                result = createExecutionError(request, e)
            }
        }
        
        // 실행 시간 업데이트 (POC 코드의 executionTimeMs 설정)
        result = result.copy(executionTimeMs = executionTime)
        
        // 성능 통계 업데이트
        updatePerformanceMetrics(executionTime, getUsedMemoryMB() - beforeMemory)
        
        // 느린 계산 감지
        if (executionTime > SLOW_CALCULATION_THRESHOLD_MS) {
            slowCalculations.incrementAndGet()
            handleSlowCalculation(request, executionTime)
        }
        
        // 캐시 저장 (성공한 경우만)
        if (enableCaching && result.isSuccess()) {
            val cacheKey = generateCacheKey(request)
            calculationCache[cacheKey] = CachedResult(result, System.currentTimeMillis())
            
            // 캐시 크기 관리
            if (calculationCache.size > DEFAULT_MAX_CACHE_SIZE) {
                evictOldestCacheEntries()
            }
        }
        
        return result
    }

    /**
     * POC 코드의 CalculatorService.calculateMultiStep에 해당하는 성능 정책 적용
     */
    fun applyMultiStepPerformancePolicy(
        request: MultiStepCalculationRequest,
        enableCaching: Boolean = true,
        timeout: Long = DEFAULT_MAX_EXECUTION_TIME_MS,
        operation: () -> CalculationResult
    ): CalculationResult {
        totalCalculations.incrementAndGet()
        
        // 다단계 계산 캐시 확인
        if (enableCaching) {
            val cacheKey = generateMultiStepCacheKey(request)
            val cachedResult = multiStepCache[cacheKey]
            
            if (cachedResult != null && !cachedResult.isExpired()) {
                cachedCalculations.incrementAndGet()
                return cachedResult.result.copy(
                    executionTimeMs = 0L,
                    metadata = cachedResult.result.metadata + mapOf("cached" to true)
                )
            }
        }
        
        // 단계별 성능 모니터링
        val stepExecutionTimes = mutableListOf<Long>()
        var totalResult: CalculationResult
        
        val totalExecutionTime = measureTimeMillis {
            try {
                totalResult = executeMultiStepWithTimeout(request, operation, timeout) { stepTime ->
                    stepExecutionTimes.add(stepTime)
                }
            } catch (e: Exception) {
                failedCalculations.incrementAndGet()
                totalResult = createMultiStepExecutionError(request, e)
            }
        }
        
        // 결과 업데이트
        totalResult = totalResult.copy(
            executionTimeMs = totalExecutionTime,
            metadata = totalResult.metadata + mapOf("stepExecutionTimes" to stepExecutionTimes)
        )
        
        // 성능 통계 업데이트
        updatePerformanceMetrics(totalExecutionTime, 0L)
        
        // 캐시 저장
        if (enableCaching && totalResult.isSuccess()) {
            val cacheKey = generateMultiStepCacheKey(request)
            multiStepCache[cacheKey] = CachedMultiStepResult(totalResult, System.currentTimeMillis())
        }
        
        return totalResult
    }

    /**
     * 계산 성능이 정책을 만족하는지 검증
     */
    fun meetsPerformanceRequirements(
        executionTime: Long,
        memoryUsage: Long,
        cacheHitRate: Double
    ): Boolean {
        return executionTime < DEFAULT_MAX_EXECUTION_TIME_MS &&
               memoryUsage < DEFAULT_MAX_MEMORY_MB &&
               cacheHitRate >= 0.8 // 80% 이상의 캐시 히트율 요구
    }

    /**
     * 성능 최적화 권장사항 제공
     */
    fun getPerformanceRecommendations(): List<PerformanceRecommendation> {
        val recommendations = mutableListOf<PerformanceRecommendation>()
        
        val avgExecutionTime = if (executionTimes.isNotEmpty()) {
            executionTimes.average()
        } else 0.0
        
        val cacheHitRate = if (totalCalculations.get() > 0) {
            cachedCalculations.get().toDouble() / totalCalculations.get()
        } else 0.0
        
        if (avgExecutionTime > SLOW_CALCULATION_THRESHOLD_MS) {
            recommendations.add(
                PerformanceRecommendation(
                    type = RecommendationType.EXECUTION_TIME,
                    message = "평균 실행 시간이 ${avgExecutionTime.toLong()}ms입니다. 수식 복잡도를 줄이거나 캐싱을 활성화하세요.",
                    priority = if (avgExecutionTime > VERY_SLOW_CALCULATION_THRESHOLD_MS) 
                        RecommendationPriority.HIGH else RecommendationPriority.MEDIUM
                )
            )
        }
        
        if (cacheHitRate < 0.5) {
            recommendations.add(
                PerformanceRecommendation(
                    type = RecommendationType.CACHE_HIT_RATE,
                    message = "캐시 히트율이 ${(cacheHitRate * 100).toInt()}%입니다. 캐시 설정을 최적화하세요.",
                    priority = RecommendationPriority.MEDIUM
                )
            )
        }
        
        val avgMemory = if (memoryUsage.isNotEmpty()) {
            memoryUsage.average()
        } else 0.0
        
        if (avgMemory > MEMORY_WARNING_THRESHOLD_MB) {
            recommendations.add(
                PerformanceRecommendation(
                    type = RecommendationType.MEMORY_USAGE,
                    message = "평균 메모리 사용량이 ${avgMemory.toLong()}MB입니다. 메모리 사용을 최적화하세요.",
                    priority = RecommendationPriority.HIGH
                )
            )
        }
        
        return recommendations
    }

    /**
     * 캐시 통계 정보 반환 (POC 코드의 파서 정보 조회와 유사)
     */
    fun getCacheStatistics(): Map<String, Any> {
        val cacheHitRate = if (totalCalculations.get() > 0) {
            cachedCalculations.get().toDouble() / totalCalculations.get()
        } else 0.0
        
        return mapOf(
            "totalCalculations" to totalCalculations.get(),
            "cachedCalculations" to cachedCalculations.get(),
            "cacheHitRate" to cacheHitRate,
            "cacheSize" to calculationCache.size,
            "multiStepCacheSize" to multiStepCache.size,
            "slowCalculations" to slowCalculations.get(),
            "failedCalculations" to failedCalculations.get(),
            "averageExecutionTime" to if (executionTimes.isNotEmpty()) executionTimes.average() else 0.0,
            "totalExecutionTime" to totalExecutionTime.get()
        )
    }

    /**
     * 캐시 정리 정책 적용
     */
    fun applyCacheEvictionPolicy() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = mutableListOf<String>()
        
        // 만료된 캐시 항목 찾기
        calculationCache.forEach { (key, cachedResult) ->
            if (cachedResult.isExpired(currentTime)) {
                expiredKeys.add(key)
            }
        }
        
        // 만료된 항목 제거
        expiredKeys.forEach { key ->
            calculationCache.remove(key)
        }
        
        // 다단계 캐시도 동일하게 처리
        val expiredMultiStepKeys = mutableListOf<String>()
        multiStepCache.forEach { (key, cachedResult) ->
            if (cachedResult.isExpired(currentTime)) {
                expiredMultiStepKeys.add(key)
            }
        }
        
        expiredMultiStepKeys.forEach { key ->
            multiStepCache.remove(key)
        }
    }

    // Private helper methods

    private fun generateCacheKey(request: CalculationRequest): String {
        return "${request.formula}:${request.variables.hashCode()}"
    }

    private fun generateMultiStepCacheKey(request: MultiStepCalculationRequest): String {
        return "${request.steps.hashCode()}:${request.variables.hashCode()}"
    }

    private fun executeWithTimeout(operation: () -> CalculationResult, timeout: Long): CalculationResult {
        return try {
            runBlocking {
                withTimeout(timeout) {
                    operation()
                }
            }
        } catch (e: TimeoutCancellationException) {
            CalculationResult(
                formula = "timeout",
                result = null,
                executionTimeMs = timeout,
                errors = listOf("계산이 제한 시간(${timeout}ms)을 초과했습니다"),
                metadata = mapOf(
                    "timeout" to true,
                    "timeoutMs" to timeout
                )
            )
        }
    }

    private fun executeMultiStepWithTimeout(
        request: MultiStepCalculationRequest,
        operation: () -> CalculationResult,
        timeout: Long,
        stepTimeCallback: (Long) -> Unit
    ): CalculationResult {
        return try {
            runBlocking {
                withTimeout(timeout) {
                    val startTime = System.currentTimeMillis()
                    var totalStepTime = 0L
                    
                    // 각 단계별 시간 측정 시뮬레이션
                    request.steps.forEachIndexed { index, step ->
                        val stepStartTime = System.currentTimeMillis()
                        
                        // 단계별 실행 시간 체크 (전체 타임아웃의 일부)
                        val remainingTime = timeout - totalStepTime
                        if (remainingTime <= 0) {
                            throw CancellationException("Step $index exceeded overall timeout")
                        }
                        
                        // 단계 실행 시간 측정 (실제로는 각 단계를 실행)
                        delay(1) // 실행 시뮬레이션
                        
                        val stepExecutionTime = System.currentTimeMillis() - stepStartTime
                        totalStepTime += stepExecutionTime
                        
                        // 단계별 시간 콜백 호출
                        stepTimeCallback(stepExecutionTime)
                        
                        // 단계별 로깅
                        if (stepExecutionTime > SLOW_CALCULATION_THRESHOLD_MS / request.steps.size) {
                            println("Slow step detected: Step $index (${step.formula.substring(0, minOf(step.formula.length, 50))}) took ${stepExecutionTime}ms")
                        }
                    }
                    
                    // 실제 연산 실행
                    operation()
                }
            }
        } catch (e: TimeoutCancellationException) {
            CalculationResult(
                formula = request.steps.joinToString("; ") { it.formula },
                result = null,
                executionTimeMs = timeout,
                errors = listOf("다단계 계산이 제한 시간(${timeout}ms)을 초과했습니다"),
                metadata = mapOf(
                    "timeout" to true,
                    "timeoutMs" to timeout,
                    "multiStep" to true,
                    "totalSteps" to request.steps.size
                )
            )
        }
    }

    private fun updatePerformanceMetrics(executionTime: Long, memoryDelta: Long) {
        synchronized(executionTimes) {
            executionTimes.add(executionTime)
            if (executionTimes.size > 1000) {
                executionTimes.removeAt(0) // 오래된 데이터 제거
            }
        }
        
        synchronized(memoryUsage) {
            memoryUsage.add(memoryDelta)
            if (memoryUsage.size > 1000) {
                memoryUsage.removeAt(0)
            }
        }
        
        totalExecutionTime.addAndGet(executionTime)
    }

    private fun handleSlowCalculation(request: CalculationRequest, executionTime: Long) {
        // 느린 계산에 대한 로깅이나 알림 처리
        // 실제 구현에서는 로거 사용
        println("Slow calculation detected: ${request.formula} took ${executionTime}ms")
    }

    private fun evictOldestCacheEntries() {
        synchronized(calculationCache) {
            val sortedEntries = calculationCache.entries.sortedBy { it.value.timestamp }
            val toRemove = sortedEntries.take(calculationCache.size - DEFAULT_MAX_CACHE_SIZE + 100)
            
            toRemove.forEach { entry ->
                calculationCache.remove(entry.key)
            }
        }
    }

    private fun getUsedMemoryMB(): Long {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        return (totalMemory - freeMemory) / (1024 * 1024)
    }

    private fun createMemoryLimitError(request: CalculationRequest, memoryUsage: Long): CalculationResult {
        return CalculationResult(
            formula = request.formula,
            result = null,
            executionTimeMs = 0L,
            errors = listOf("메모리 사용량 한계 초과: ${memoryUsage}MB"),
            metadata = mapOf("errorCode" to ErrorCodes.Calculator.MEMORY_LIMIT_EXCEEDED)
        )
    }

    private fun createExecutionError(request: CalculationRequest, exception: Exception): CalculationResult {
        return CalculationResult(
            formula = request.formula,
            result = null,
            executionTimeMs = 0L,
            errors = listOf("실행 오류: ${exception.message}"),
            metadata = mapOf("errorCode" to ErrorCodes.Calculator.CALCULATION_FAILED)
        )
    }

    private fun createMultiStepExecutionError(
        request: MultiStepCalculationRequest,
        exception: Exception
    ): CalculationResult {
        return CalculationResult(
            result = null,
            executionTimeMs = 0L,
            formula = "MultiStep",
            errors = listOf("다단계 계산 실행 오류: ${exception.message}"),
            metadata = mapOf("errorCode" to ErrorCodes.Calculator.CALCULATION_FAILED)
        )
    }

    // Data classes

    private data class CachedResult(
        val result: CalculationResult,
        val timestamp: Long,
        val ttl: Long = DEFAULT_CACHE_TTL_SECONDS * 1000
    ) {
        fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
            return currentTime - timestamp > ttl
        }
    }

    private data class CachedMultiStepResult(
        val result: CalculationResult,
        val timestamp: Long,
        val ttl: Long = DEFAULT_CACHE_TTL_SECONDS * 1000
    ) {
        fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
            return currentTime - timestamp > ttl
        }
    }


    /**
     * 정책의 설정 정보를 반환합니다.
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "name" to "CalculationPerformancePolicy",
        "based_on" to "POC_CalculatorService_Performance",
        "defaultCacheTtlSeconds" to DEFAULT_CACHE_TTL_SECONDS,
        "defaultMaxExecutionTimeMs" to DEFAULT_MAX_EXECUTION_TIME_MS,
        "defaultMaxMemoryMB" to DEFAULT_MAX_MEMORY_MB,
        "defaultMaxCacheSize" to DEFAULT_MAX_CACHE_SIZE,
        "slowCalculationThresholdMs" to SLOW_CALCULATION_THRESHOLD_MS,
        "memoryWarningThresholdMB" to MEMORY_WARNING_THRESHOLD_MB,
        "features" to listOf("caching", "performance_monitoring", "timeout_handling", "memory_management")
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "CalculationPerformancePolicy",
        "totalCalculations" to totalCalculations.get(),
        "cachedCalculations" to cachedCalculations.get(),
        "slowCalculations" to slowCalculations.get(),
        "failedCalculations" to failedCalculations.get(),
        "totalExecutionTime" to totalExecutionTime.get(),
        "cacheSize" to calculationCache.size,
        "multiStepCacheSize" to multiStepCache.size,
        "pocCompatibility" to true
    )
}

// 임시 데이터 클래스들 (실제로는 별도 파일에 정의되어야 함)
data class CalculationResult(
    val success: Boolean,
    val executionTimeMs: Long,
    val errors: List<String> = emptyList(),
    val errorCode: String? = null,
    val stepResults: List<Any> = emptyList(),
    val finalVariables: Map<String, Any> = emptyMap(),
    val stepExecutionTimes: List<Long> = emptyList(),
    val cached: Boolean = false
)