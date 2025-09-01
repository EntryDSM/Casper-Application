package hs.kr.entrydsm.application.domain.formula.presentation.dto.response

import java.time.LocalDateTime

data class FormulaSetResponse(
    val success: Boolean,
    val data: FormulaSetData
) {
    data class FormulaSetData(
        val formulaSetId: String,
        val name: String,
        val description: String,
        val applicationType: String,
        val educationalStatus: String,
        val region: String?,
        val totalFormulas: Int,
        val status: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime?
    )
}