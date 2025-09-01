package hs.kr.entrydsm.application.domain.application.presentation.dto.response

data class ValidationResponse(
    val success: Boolean,
    val data: ValidationData
) {
    data class ValidationData(
        val valid: Boolean,
        val errors: List<String>,
        val missingFields: List<String>,
        val extraFields: List<String>
    )
}