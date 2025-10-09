package hs.kr.entrydsm.application.domain.application.presentation.dto.response

data class GetApplicationStatusResponse(
    val receiptCode: Long,
    val phoneNumber: String?,
    val name: String?,
    val isSubmitted: Boolean,
    val isPrintedArrived: Boolean,
    val selfIntroduce: String?,
    val studyPlan: String?,
    val applicationType: String
)
