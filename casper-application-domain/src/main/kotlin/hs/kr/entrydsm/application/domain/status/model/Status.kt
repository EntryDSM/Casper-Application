package hs.kr.entrydsm.application.domain.status.model

import hs.kr.entrydsm.application.domain.status.enums.ApplicationStatus
import hs.kr.entrydsm.application.global.annotation.Aggregate

@Aggregate
data class Status(
    val id: Long? = 0,
    val examCode: String? = null,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean = false,
    val isSecondRoundPass: Boolean = false,
    val receiptCode: Long,
) {
    val isSubmitted: Boolean
        get() = applicationStatus != ApplicationStatus.NOT_APPLIED &&
                applicationStatus != ApplicationStatus.WRITING

    val isPrintsArrived: Boolean
        get() = applicationStatus == ApplicationStatus.DOCUMENTS_RECEIVED ||
                applicationStatus == ApplicationStatus.SCREENING_IN_PROGRESS ||
                applicationStatus == ApplicationStatus.RESULT_ANNOUNCED
}
