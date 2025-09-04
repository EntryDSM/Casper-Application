package hs.kr.entrydsm.application.domain.status.domain.entity

import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

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
