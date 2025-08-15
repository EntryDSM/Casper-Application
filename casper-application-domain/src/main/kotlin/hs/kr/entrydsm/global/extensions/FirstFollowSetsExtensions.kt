package hs.kr.entrydsm.global.extensions

import hs.kr.entrydsm.domain.parser.values.FirstFollowSets

/**
 * FirstFollowSets 클래스를 위한 통계 및 디버깅 확장 함수들입니다.
 * 
 * 이 확장 함수들은 개발 도구 및 디버깅 목적으로 사용되며,
 * 핵심 도메인 로직과 분리하여 관심사를 명확히 합니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */

/**
 * 계산된 FIRST 집합의 통계 정보를 반환합니다.
 *
 * @return FIRST 집합 통계 맵
 */
fun FirstFollowSets.getFirstStats(): Map<String, Any> {
    val firstSets = this.javaClass.getDeclaredField(Field.FIRST_SETS).apply { isAccessible = true }.get(this) as Map<*, *>
    
    return mapOf(
        "totalSymbols" to firstSets.size,
        "nonEmptyFirstSets" to firstSets.values.count { (it as Set<*>).isNotEmpty() },
        "averageFirstSetSize" to if (firstSets.isNotEmpty()) {
            firstSets.values.map { (it as Set<*>).size }.average()
        } else 0.0,
        "maxFirstSetSize" to (firstSets.values.maxOfOrNull { (it as Set<*>).size } ?: 0)
    )
}

/**
 * 계산된 FOLLOW 집합의 통계 정보를 반환합니다.
 *
 * @return FOLLOW 집합 통계 맵
 */
fun FirstFollowSets.getFollowStats(): Map<String, Any> {
    val followSets = this.javaClass.getDeclaredField(Field.FOLLOW_SETS).apply { isAccessible = true }.get(this) as Map<*, *>
    
    return mapOf(
        "totalSymbols" to followSets.size,
        "nonEmptyFollowSets" to followSets.values.count { (it as Set<*>).isNotEmpty() },
        "averageFollowSetSize" to if (followSets.isNotEmpty()) {
            followSets.values.map { (it as Set<*>).size }.average()
        } else 0.0,
        "maxFollowSetSize" to (followSets.values.maxOfOrNull { (it as Set<*>).size } ?: 0)
    )
}

object Field {
    const val FIRST_SETS = "firstSets"
    const val FOLLOW_SETS = "followSets"
}