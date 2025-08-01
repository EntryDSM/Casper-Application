package hs.kr.entrydsm.domain.calculator.values

/**
 * 성능 최적화 권장사항을 나타내는 값 객체입니다.
 *
 * @property type 권장사항 타입
 * @property message 권장사항 메시지
 * @property priority 권장사항 우선순위
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
data class PerformanceRecommendation(
    val type: RecommendationType,
    val message: String,
    val priority: RecommendationPriority
)