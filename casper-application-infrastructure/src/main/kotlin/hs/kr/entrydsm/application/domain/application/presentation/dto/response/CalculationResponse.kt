package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import java.time.LocalDateTime

data class CalculationResponse(
    val success: Boolean,
    val data: CalculationData
) {
    data class CalculationData(
        val calculationId: String,
        val applicationId: String,
        val totalScore: Double,
        val breakdown: Map<String, Double>,
        val formulaExecution: FormulaExecutionDetail,
        val executedAt: LocalDateTime,
        val executionTimeMs: Long
    )
    
    data class FormulaExecutionDetail(
        val steps: List<CalculationStepDetail>
    )
    
    data class CalculationStepDetail(
        val stepOrder: Int,
        val stepName: String,
        val formula: String,
        val result: Double,
        val variables: Map<String, Any>,
        val executionTimeMs: Long
    )
}