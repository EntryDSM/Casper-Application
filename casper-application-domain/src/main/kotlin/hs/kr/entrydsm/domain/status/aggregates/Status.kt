package hs.kr.entrydsm.domain.status.aggregates

import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

@Aggregate(context = "status")
data class Status(
    val id: Long? = 0,
    val examCode: String? = null,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean = false,
    val isSecondRoundPass: Boolean = false,
    val receiptCode: Long,
)