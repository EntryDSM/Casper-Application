package hs.kr.entrydsm.application.domain.status.domain.entity

import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

/**
 * Redis에 저장되는 상태 캐시 엔티티 클래스입니다.
 *
 * 자주 조회되는 원서 상태 정보를 Redis에 캐싱하여 조회 성능을 향상시킵니다.
 * TTL(Time To Live) 설정을 통해 캐시 데이터의 자동 만료를 지원합니다.
 *
 * @property receiptCode 접수번호 (Primary Key)
 * @property examCode 시험 코드 (nullable)
 * @property applicationStatus 현재 원서의 전형 상태
 * @property isFirstRoundPass 1차 전형 합격 여부
 * @property isSecondRoundPass 2차 전형 합격 여부
 * @property ttl 캐시 만료 시간 (초 단위)
 */
@RedisHash("status_cache")
class StatusCacheRedisEntity(
    @Id
    val receiptCode: Long,
    val examCode: String?,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean,
    val isSecondRoundPass: Boolean,
    @TimeToLive
    val ttl: Long,
)
