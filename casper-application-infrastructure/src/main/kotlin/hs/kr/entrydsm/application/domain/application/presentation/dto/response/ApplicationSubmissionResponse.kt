package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.services.ScoreCalculationResult
import java.time.LocalDateTime

/**
 * 원서 제출 + 성적 계산 통합 응답 DTO
 */
data class ApplicationSubmissionResponse(
    val application: ApplicationResponse,
    val scoreCalculation: ScoreCalculationResponse,
    val submissionTimestamp: LocalDateTime = LocalDateTime.now(),
    val status: String = "SUBMITTED_AND_CALCULATED"
) {
    companion object {
        fun from(
            application: Application, 
            calculationResult: ScoreCalculationResult
        ): ApplicationSubmissionResponse {
            return ApplicationSubmissionResponse(
                application = ApplicationResponse.from(application),
                scoreCalculation = ScoreCalculationResponse.from(calculationResult)
            )
        }
    }
}