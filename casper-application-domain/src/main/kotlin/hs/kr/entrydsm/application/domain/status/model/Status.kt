package hs.kr.entrydsm.application.domain.status.model

import hs.kr.entrydsm.application.global.annotation.Aggregate
import java.time.LocalDateTime

@Aggregate
data class Status(
    val id: Long? = 0,
    val applicationStatus: ApplicationStatus,
//    val isPrintsArrived: Boolean = false,
//    val isSubmitted: Boolean = false,
    val examCode: String? = null,
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
