package hs.kr.entrydsm.application.domain.formula.presentation.dto.response

data class FormulaExecutionResponse(
    val success: Boolean,
    val data: ExecutionData
) {
    data class ExecutionData(
        val executionId: String,
        val finalResult: Double,
        val executionTimeMs: Long,
        val steps: List<ExecutionStepDetail>
    )
    
    data class ExecutionStepDetail(
        val step: Int,
        val name: String,
        val result: Double,
        val executionTimeMs: Long
    )
}