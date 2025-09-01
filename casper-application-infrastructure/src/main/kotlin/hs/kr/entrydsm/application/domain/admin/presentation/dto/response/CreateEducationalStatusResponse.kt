package hs.kr.entrydsm.application.domain.admin.presentation.dto.response

data class CreateEducationalStatusResponse(
    val success: Boolean,
    val data: StatusData
) {
    data class StatusData(
        val statusId: String,
        val code: String,
        val name: String
    )
}