package hs.kr.entrydsm.application.domain.status.model

import hs.kr.entrydsm.application.domain.status.enums.ApplicationStatus

data class StatusCache(
    val receiptCode: Long,
    val examCode: String?,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean,
    val isSecondRoundPass: Boolean,
    val ttl: Long
)