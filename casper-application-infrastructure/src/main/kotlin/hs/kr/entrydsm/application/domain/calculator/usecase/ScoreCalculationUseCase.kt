package hs.kr.entrydsm.application.domain.calculator.usecase

import hs.kr.entrydsm.application.domain.application.calculator.ScoreCalculator
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.request.CalculateScoreRequest
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.response.CalculateScoreResponse
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.springframework.stereotype.Service

/**
 * 성적 계산 UseCase
 *
 * 인증 없이 성적 계산 기능을 제공합니다.
 */
@Service
class ScoreCalculationUseCase(
    private val scoreCalculator: ScoreCalculator,
) {
    fun calculateScore(request: CalculateScoreRequest): CalculateScoreResponse {
        val applicationType = ApplicationType.fromString(request.applicationType)
        val educationalStatus = EducationalStatus.fromString(request.educationalStatus)

        val result =
            scoreCalculator.calculateScore(
                applicationType = applicationType,
                educationalStatus = educationalStatus,
                scores = request.scores.toMap(),
            )

        val scorePercentage =
            if (result.totalScore > 0) {
                (result.totalScore / 300.0) * 100.0
            } else {
                0.0
            }

        return CalculateScoreResponse(
            success = true,
            data =
                CalculateScoreResponse.ScoreResultData(
                    subjectScore = String.format("%.2f", result.subjectScore).toDouble(),
                    attendanceScore = String.format("%.2f", result.attendanceScore).toDouble(),
                    volunteerScore = String.format("%.2f", result.volunteerScore).toDouble(),
                    bonusScore = String.format("%.2f", result.bonusScore).toDouble(),
                    totalScore = String.format("%.2f", result.totalScore).toDouble(),
                    scorePercentage = String.format("%.2f", scorePercentage).toDouble(),
                ),
        )
    }
}
