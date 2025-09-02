package hs.kr.entrydsm.domain.status.aggregates

import hs.kr.entrydsm.domain.status.values.ApplicationStatus

data class StatusCache(
    val receiptCode: Long,
    val examCode: String?,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean,
    val isSecondRoundPass: Boolean,
    val ttl: Long
)