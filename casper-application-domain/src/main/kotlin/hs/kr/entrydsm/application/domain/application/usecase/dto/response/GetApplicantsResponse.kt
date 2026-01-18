package hs.kr.entrydsm.application.domain.application.usecase.dto.response

data class GetApplicantsResponse(
    val applicants: List<ApplicantDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int,
) {
    data class ApplicantDto(
        val receiptCode: Long,
        val applicantName: String?,
        val applicationType: String?,
        val educationalStatus: String?,
        val isDaejeon: Boolean?,
        val isArrived: Boolean?,
    )
}
