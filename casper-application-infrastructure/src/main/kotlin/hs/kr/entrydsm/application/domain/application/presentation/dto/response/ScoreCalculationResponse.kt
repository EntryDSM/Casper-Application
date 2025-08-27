package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import hs.kr.entrydsm.domain.application.services.ScoreCalculationResult
import hs.kr.entrydsm.domain.application.values.ReceiptCode

/**
 * 성적 계산 결과 응답 DTO
 */
data class ScoreCalculationResponse(
    val receiptCode: Long,
    val finalScore: Double,
    val detailScores: Map<String, Double>,
    val executionId: String,
    val variableMapping: Map<String, String>,
    val calculationStatus: String = "SUCCESS"
) {
    companion object {
        fun from(result: ScoreCalculationResult): ScoreCalculationResponse {
            return ScoreCalculationResponse(
                receiptCode = result.receiptCode.value,
                finalScore = result.finalScore,
                detailScores = result.detailScores,
                executionId = result.formulaExecution.getId().value,
                variableMapping = result.variableMapping
            )
        }
    }
}