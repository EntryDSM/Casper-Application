package hs.kr.entrydsm.application.domain.formula.presentation.dto.response

import java.time.LocalDateTime

data class FormulaSetDetailResponse(
    val success: Boolean,
    val data: FormulaSetDetailData,
) {
    data class FormulaSetDetailData(
        val formulaSetId: String,
        val name: String,
        val description: String,
        val applicationType: String,
        val educationalStatus: String,
        val region: String?,
        val formulas: List<FormulaStepDetail>,
        val constants: Map<String, Double>,
        val status: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime?,
    )

    data class FormulaStepDetail(
        val step: Int,
        val name: String,
        val expression: String,
        val resultVariable: String,
    )
}
