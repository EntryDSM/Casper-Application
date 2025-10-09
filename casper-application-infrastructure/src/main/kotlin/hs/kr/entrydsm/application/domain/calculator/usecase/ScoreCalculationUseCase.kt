package hs.kr.entrydsm.application.domain.calculator.usecase

import hs.kr.entrydsm.application.domain.application.calculator.ScoreCalculator
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.request.CalculateScoreRequest
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.response.CalculateScoreResponse
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(ScoreCalculationUseCase::class.java)

    fun calculateScore(request: CalculateScoreRequest): CalculateScoreResponse {
        val applicationType = ApplicationType.fromString(request.applicationType)
        val educationalStatus = EducationalStatus.fromString(request.educationalStatus)

        val scoresMap = request.scores.toMap()

        logger.info("=== 성적 계산 요청 ===")
        logger.info("전형 타입: $applicationType")
        logger.info("학력 상태: $educationalStatus")
        logger.info("입력 점수 맵: $scoresMap")

        val result =
            scoreCalculator.calculateScore(
                applicationType = applicationType,
                educationalStatus = educationalStatus,
                scores = scoresMap,
            )

        logger.info("=== 성적 계산 결과 ===")
        logger.info("교과성적: ${result.subjectScore}")
        logger.info("출석점수: ${result.attendanceScore}")
        logger.info("봉사점수: ${result.volunteerScore}")
        logger.info("가산점: ${result.bonusScore}")
        logger.info("총점: ${result.totalScore}")

        return CalculateScoreResponse(
            success = true,
            data =
                CalculateScoreResponse.ScoreResultData(
                    subjectScore = String.format("%.2f", result.subjectScore).toDouble(),
                    attendanceScore = String.format("%.2f", result.attendanceScore).toDouble(),
                    volunteerScore = String.format("%.2f", result.volunteerScore).toDouble(),
                    bonusScore = String.format("%.2f", result.bonusScore).toDouble(),
                    totalScore = String.format("%.2f", result.totalScore).toDouble(),
                ),
        )
    }
}
