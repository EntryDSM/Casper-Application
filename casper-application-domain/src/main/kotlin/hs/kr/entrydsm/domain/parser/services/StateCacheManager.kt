package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.parser.entities.CompressedLRState
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * LR 파서의 상태 캐싱 및 메모리 최적화를 관리하는 서비스입니다.
 *
 * 동일한 상태를 재사용하여 메모리 사용량을 줄이고 성능을 향상시키며,
 * LALR 상태 병합과 압축을 통해 파서 테이블의 크기를 최적화합니다.
 * POC 코드의 상태 캐싱 시스템을 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Service(
    name = "StateCacheManager",
    type = ServiceType.DOMAIN_SERVICE
)
class StateCacheManager {

    // 상태 캐시: 상태 집합 -> 상태 ID 매핑
    private val stateCache = ConcurrentHashMap<Set<LRItem>, Int>()
    private val reverseStateCache = ConcurrentHashMap<Int, Set<LRItem>>() // 역방향 조회용

    // 압축된 상태 캐시: 시그니처 -> 상태 ID 매핑
    private val compressedStateCache = ConcurrentHashMap<String, Int>()
    private val reverseCompressedStateCache = ConcurrentHashMap<Int, String>() // 역방향 조회용

    // 상태별 참조 카운트
    private val referenceCount = ConcurrentHashMap<Int, AtomicLong>()

    // 상태 생성 통계
    private val creationStats = CacheStatistics()

    // 메모리 사용량 추적
    private val memoryTracker = MemoryTracker()

    /**
     * 주어진 상태 집합에 대해 캐시된 상태 ID를 반환하거나 새로 등록합니다.
     *
     * @param state 상태 집합
     * @param stateId 새로 할당할 상태 ID (캐시 미스인 경우)
     * @return 캐시된 상태 ID 또는 새로 등록된 상태 ID
     */
    fun getOrCacheState(state: Set<LRItem>, stateId: Int): CacheResult {
        val existingId = stateCache[state]

        return if (existingId != null) {
            // 캐시 히트
            incrementReference(existingId)
            creationStats.recordHit()
            CacheResult.hit(existingId)
        } else {
            // 캐시 미스 - 새 상태 등록
            stateCache[state] = stateId
            reverseStateCache[stateId] = state // 역방향 캐시 추가
            referenceCount[stateId] = AtomicLong(1)
            memoryTracker.recordStateCreation(state)
            creationStats.recordMiss()
            CacheResult.miss(stateId)
        }
    }

    /**
     * 압축된 상태에 대해 캐시된 상태 ID를 반환하거나 새로 등록합니다.
     *
     * @param compressedState 압축된 상태
     * @param stateId 새로 할당할 상태 ID (캐시 미스인 경우)
     * @return 캐시된 상태 ID 또는 새로 등록된 상태 ID
     */
    fun getOrCacheCompressedState(
        compressedState: CompressedLRState,
        stateId: Int
    ): CacheResult {
        val signature = compressedState.signature
        val existingId = compressedStateCache[signature]

        return if (existingId != null) {
            // 캐시 히트
            incrementReference(existingId)
            creationStats.recordCompressedHit()
            CacheResult.hit(existingId)
        } else {
            // 캐시 미스 - 새 상태 등록
            compressedStateCache[signature] = stateId
            reverseCompressedStateCache[stateId] = signature // 역방향 캐시 추가
            referenceCount[stateId] = AtomicLong(1)
            memoryTracker.recordCompressedStateCreation(compressedState)
            creationStats.recordCompressedMiss()
            CacheResult.miss(stateId)
        }
    }

    /**
     * LALR 병합 가능한 상태를 찾습니다.
     *
     * @param newState 새로운 상태
     * @return 병합 가능한 상태 ID 또는 null
     */
    fun findMergeableState(newState: CompressedLRState): Int? {
        val signature = newState.signature

        for ((cachedSignature, stateId) in compressedStateCache) {
            if (cachedSignature != signature) continue

            // 동일한 시그니처를 가진 상태를 찾았으므로 병합 가능성 확인
            // 실제로는 더 정교한 LALR 병합 조건 검사가 필요
            return stateId
        }

        return null
    }

    /**
     * 상태의 참조 카운트를 증가시킵니다.
     *
     * @param stateId 상태 ID
     */
    fun incrementReference(stateId: Int) {
        referenceCount.computeIfAbsent(stateId) { AtomicLong(0) }.incrementAndGet()
    }

    /**
     * 상태의 참조 카운트를 감소시킵니다.
     *
     * @param stateId 상태 ID
     * @return 감소 후 참조 카운트
     */
    fun decrementReference(stateId: Int): Long {
        val counter = referenceCount[stateId] ?: return 0
        val newCount = counter.decrementAndGet()

        if (newCount <= 0) {
            // 참조가 없으면 정리 대상으로 마킹
            markForCleanup(stateId)
        }

        return newCount
    }

    /**
     * 특정 상태를 정리 대상으로 마킹합니다.
     *
     * @param stateId 정리할 상태 ID
     */
    private fun markForCleanup(stateId: Int) {
        // 실제 구현에서는 지연 정리 큐에 추가하거나
        // 주기적인 가비지 컬렉션을 통해 정리
        memoryTracker.recordStateCleanup(stateId)
    }

    /**
     * 메모리 압박 시 캐시를 정리합니다.
     *
     * @param memoryPressureLevel 메모리 압박 수준 (0.0 ~ 1.0)
     * @return 정리된 상태 개수
     */
    fun performMemoryCleanup(memoryPressureLevel: Double = 0.8): Int {
        if (memoryPressureLevel < 0.5) {
            return 0 // 메모리 압박이 심하지 않음
        }

        var cleanedCount = 0
        val lowReferenceStates = mutableListOf<Int>()

        // 참조 카운트가 낮은 상태들을 찾아서 정리
        for ((stateId, counter) in referenceCount) {
            if (counter.get() <= 1) {
                lowReferenceStates.add(stateId)
            }
        }

        // 정리 수행
        for (stateId in lowReferenceStates.take((lowReferenceStates.size * memoryPressureLevel).toInt())) {
            cleanupState(stateId)
            cleanedCount++
        }

        creationStats.recordCleanup(cleanedCount)
        return cleanedCount
    }

    /**
     * 특정 상태를 캐시에서 제거합니다.
     *
     * @param stateId 제거할 상태 ID
     */
    private fun cleanupState(stateId: Int) {
        // 역방향 캐시를 사용하여 O(1) 시간 복잡도로 제거
        reverseStateCache.remove(stateId)?.let { stateCache.remove(it) }
        reverseCompressedStateCache.remove(stateId)?.let { compressedStateCache.remove(it) }

        referenceCount.remove(stateId)
        memoryTracker.recordStateCleanup(stateId)
    }

    /**
     * 캐시 통계를 반환합니다.
     *
     * @return 캐시 통계 정보
     */
    fun getCacheStatistics(): Map<String, Any> {
        val hitRate = creationStats.getHitRate()
        val compressedHitRate = creationStats.getCompressedHitRate()

        return mapOf(
            "totalStates" to stateCache.size,
            "compressedStates" to compressedStateCache.size,
            "totalReferences" to referenceCount.values.sumOf { it.get() },
            "hitRate" to hitRate,
            "compressedHitRate" to compressedHitRate,
            "overallHitRate" to (hitRate + compressedHitRate) / 2,
            "memoryStats" to memoryTracker.getMemoryStatistics(),
            "cacheEfficiency" to calculateCacheEfficiency()
        )
    }

    /**
     * 캐시 효율성을 계산합니다.
     *
     * @return 캐시 효율성 (0.0 ~ 1.0)
     */
    private fun calculateCacheEfficiency(): Double {
        val totalRequests = creationStats.hits + creationStats.misses
        if (totalRequests == 0L) return 0.0

        val hitRate = creationStats.hits.toDouble() / totalRequests
        val memoryEfficiency = 1.0 - (stateCache.size.toDouble() / maxOf(1, totalRequests))

        return (hitRate + memoryEfficiency) / 2
    }

    /**
     * 캐시를 완전히 초기화합니다.
     */
    fun clearCache() {
        stateCache.clear()
        reverseStateCache.clear()
        compressedStateCache.clear()
        reverseCompressedStateCache.clear()
        referenceCount.clear()
        creationStats.reset()
        memoryTracker.reset()
    }

    /**
     * 캐시 상태 보고서를 생성합니다.
     *
     * @return 상세한 캐시 보고서
     */
    fun generateCacheReport(): String {
        val stats = getCacheStatistics()
        val sb = StringBuilder()

        sb.appendLine("=== 상태 캐시 관리 보고서 ===")
        sb.appendLine("총 상태 수: ${stats["totalStates"]}")
        sb.appendLine("압축된 상태 수: ${stats["compressedStates"]}")
        sb.appendLine("총 참조 수: ${stats["totalReferences"]}")
        sb.appendLine("히트율: ${String.format("%.2f%%", (stats["hitRate"] as Double) * 100)}")
        sb.appendLine("압축 히트율: ${String.format("%.2f%%", (stats["compressedHitRate"] as Double) * 100)}")
        sb.appendLine("전체 히트율: ${String.format("%.2f%%", (stats["overallHitRate"] as Double) * 100)}")
        sb.appendLine("캐시 효율성: ${String.format("%.2f%%", (stats["cacheEfficiency"] as Double) * 100)}")
        sb.appendLine()

        @Suppress("UNCHECKED_CAST")
        val memoryStats = stats["memoryStats"] as Map<String, Any>
        sb.appendLine("=== 메모리 사용량 ===")
        sb.appendLine("추정 메모리: ${memoryStats["estimatedMemoryBytes"]} bytes")
        sb.appendLine("평균 상태 크기: ${memoryStats["averageStateSize"]} items")
        sb.appendLine("메모리 효율성: ${memoryStats["memoryEfficiency"]}")

        return sb.toString()
    }

    companion object {
        /**
         * 싱글톤 인스턴스를 생성합니다.
         */
        fun create(): StateCacheManager = StateCacheManager()
    }
}

/**
 * 캐시 결과를 나타내는 데이터 클래스입니다.
 */
data class CacheResult(
    val stateId: Int,
    val isHit: Boolean
) {
    companion object {
        fun hit(stateId: Int): CacheResult = CacheResult(stateId, true)
        fun miss(stateId: Int): CacheResult = CacheResult(stateId, false)
    }
}

/**
 * 캐시 통계를 관리하는 클래스입니다.
 */
private class CacheStatistics {
    var hits: Long = 0
    var misses: Long = 0
    var compressedHits: Long = 0
    var compressedMisses: Long = 0
    var cleanups: Long = 0

    fun recordHit() { hits++ }
    fun recordMiss() { misses++ }
    fun recordCompressedHit() { compressedHits++ }
    fun recordCompressedMiss() { compressedMisses++ }
    fun recordCleanup(count: Int) { cleanups += count }

    fun getHitRate(): Double {
        val total = hits + misses
        return if (total > 0) hits.toDouble() / total else 0.0
    }

    fun getCompressedHitRate(): Double {
        val total = compressedHits + compressedMisses
        return if (total > 0) compressedHits.toDouble() / total else 0.0
    }

    fun reset() {
        hits = 0
        misses = 0
        compressedHits = 0
        compressedMisses = 0
        cleanups = 0
    }
}

/**
 * 메모리 사용량을 추적하는 클래스입니다.
 */
private class MemoryTracker {
    private var totalStatesCreated: Long = 0
    private var totalItemsCreated: Long = 0
    private var totalStatesDestroyed: Long = 0

    fun recordStateCreation(state: Set<LRItem>) {
        totalStatesCreated++
        totalItemsCreated += state.size
    }

    fun recordCompressedStateCreation(state: CompressedLRState) {
        totalStatesCreated++
        totalItemsCreated += state.getCoreItemCount()
    }

    fun recordStateCleanup(stateId: Int) {
        totalStatesDestroyed++
    }

    fun getMemoryStatistics(): Map<String, Any> {
        val currentStates = totalStatesCreated - totalStatesDestroyed
        val averageStateSize = if (totalStatesCreated > 0) {
            totalItemsCreated.toDouble() / totalStatesCreated
        } else 0.0

        return mapOf(
            "totalStatesCreated" to totalStatesCreated,
            "totalStatesDestroyed" to totalStatesDestroyed,
            "currentStates" to currentStates,
            "totalItemsCreated" to totalItemsCreated,
            "averageStateSize" to averageStateSize,
            "estimatedMemoryBytes" to (currentStates * averageStateSize * 64), // 대략적인 추정
            "memoryEfficiency" to if (totalStatesCreated > 0) {
                (totalStatesCreated - totalStatesDestroyed).toDouble() / totalStatesCreated
            } else 1.0
        )
    }

    fun reset() {
        totalStatesCreated = 0
        totalItemsCreated = 0
        totalStatesDestroyed = 0
    }
}