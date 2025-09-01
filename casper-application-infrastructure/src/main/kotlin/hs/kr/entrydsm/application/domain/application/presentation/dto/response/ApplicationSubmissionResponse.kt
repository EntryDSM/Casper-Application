package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import java.time.LocalDateTime

data class ApplicationSubmissionResponse(
    val success: Boolean,
    val data: SubmissionData
) {
    data class SubmissionData(
        val application: ApplicationInfo,
        val calculation: CalculationInfo
    )
    
    data class ApplicationInfo(
        val applicationId: String,
        val receiptCode: Long,
        val applicantName: String,
        val applicationType: String,
        val educationalStatus: String,
        val status: String,
        val submittedAt: LocalDateTime
    )
    
    data class CalculationInfo(
        val calculationId: String,
        val totalScore: Double,
        val breakdown: Map<String, Double>,
        val formulaExecution: FormulaExecutionInfo
    )
    
    data class FormulaExecutionInfo(
        val executedAt: LocalDateTime,
        val executionTimeMs: Long,
        val steps: List<FormulaStepInfo>
    )
    
    data class FormulaStepInfo(
        val stepName: String,
        val formula: String,
        val result: Double,
        val variables: Map<String, Any>
    )
}