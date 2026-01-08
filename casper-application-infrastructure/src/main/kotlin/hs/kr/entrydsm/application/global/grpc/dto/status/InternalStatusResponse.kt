package hs.kr.entrydsm.application.global.grpc.dto.status

import hs.kr.entrydsm.application.domain.status.enums.ApplicationStatus

data class InternalStatusResponse(
    val id: Long,
    val applicationStatus: ApplicationStatus,
    val examCode: String? = null,
    val isFirstRoundPass: Boolean = false,
    val isSecondRoundPass: Boolean = false,
    val receiptCode: Long,
)