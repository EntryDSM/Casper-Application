package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import java.time.LocalDateTime

data class CalculationHistoryResponse(
    val success: Boolean,
    val data: HistoryData
) {
    data class HistoryData(
        val applicationId: String,
        val calculations: List<CalculationSummary>
    )
    
    data class CalculationSummary(
        val calculationId: String,
        val totalScore: Double,
        val executedAt: LocalDateTime,
        val executionTimeMs: Long
    )
}