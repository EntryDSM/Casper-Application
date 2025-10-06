package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import java.time.LocalDateTime

data class ApplicationListResponse(
    val success: Boolean,
    val data: ApplicationListData,
) {
    data class ApplicationListData(
        val applications: List<ApplicationSummary>,
        val total: Int,
        val page: Int,
        val size: Int,
        val totalPages: Int,
    )

    data class ApplicationSummary(
        val applicationId: String,
        val receiptCode: Long,
        val applicantName: String,
        val applicationType: String,
        val educationalStatus: String,
        val status: String,
        val submittedAt: LocalDateTime,
        val isDaejeon: Boolean,
        val isSubmitted: Boolean,
        val isArrived: Boolean,
    )
}
