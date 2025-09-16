package hs.kr.entrydsm.domain.status.aggregates

import hs.kr.entrydsm.domain.status.values.ApplicationStatus

/**
 * 원서 상태 정보의 캐시를 나타내는 도메인 모델입니다.
 * 
 * 자주 조회되는 상태 정보를 Redis 등의 캐시 스토리지에 저장하여
 * 조회 성능을 향상시키기 위해 사용됩니다.
 * 
 * @property receiptCode 접수번호
 * @property examCode 시험 코드 (nullable)
 * @property applicationStatus 현재 원서의 전형 상태
 * @property isFirstRoundPass 1차 전형 합격 여부
 * @property isSecondRoundPass 2차 전형 합격 여부
 * @property ttl 캐시 만료 시간 (Time To Live, 초 단위)
 */
data class StatusCache(
    val receiptCode: Long,
    val examCode: String?,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean,
    val isSecondRoundPass: Boolean,
    val ttl: Long
)
