package hs.kr.entrydsm.application.domain.formula.presentation.dto.response

import java.time.LocalDateTime

data class FormulaSetListResponse(
    val success: Boolean,
    val data: FormulaSetListData
) {
    data class FormulaSetListData(
        val formulaSets: List<FormulaSetSummary>,
        val total: Int
    )
    
    data class FormulaSetSummary(
        val formulaSetId: String,
        val name: String,
        val applicationType: String,
        val educationalStatus: String,
        val region: String?,
        val totalFormulas: Int,
        val status: String,
        val createdAt: LocalDateTime
    )
}