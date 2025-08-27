package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.entities.User
import hs.kr.entrydsm.domain.application.services.ScoreCalculationResult
import java.time.LocalDateTime
import java.util.UUID

/**
 * 통합 원서 작성 완료 응답 DTO
 */
data class CompleteApplicationResponse(
    // 사용자 정보
    val user: UserInfo,
    
    // 원서 정보
    val application: ApplicationInfo,
    
    // 성적 정보
    val score: ScoreInfo,
    
    // 계산 결과
    val calculation: CalculationInfo,
    
    // 처리 상태
    val status: String = "COMPLETED",
    val processedAt: LocalDateTime = LocalDateTime.now()
) {
    data class UserInfo(
        val userId: UUID,
        val phoneNumber: String,
        val name: String,
        val isParent: Boolean
    )
    
    data class ApplicationInfo(
        val receiptCode: Long,
        val applicantName: String,
        val applicationType: String,
        val educationalStatus: String,
        val isDaejeon: Boolean,
        val isSubmitted: Boolean = true
    )
    
    data class ScoreInfo(
        val receiptCode: Long,
        val attendanceScore: Int?,
        val volunteerScore: Double?,
        val totalScore: Double,
        val extraScore: Double
    )
    
    data class CalculationInfo(
        val finalScore: Double,
        val detailScores: Map<String, Double>,
        val executionId: String,
        val variableMapping: Map<String, String>,
        val status: String = "SUCCESS"
    )
    
    companion object {
        fun from(
            user: User,
            application: Application,
            score: Score,
            calculationResult: ScoreCalculationResult
        ): CompleteApplicationResponse {
            return CompleteApplicationResponse(
                user = UserInfo(
                    userId = user.id,
                    phoneNumber = user.phoneNumber,
                    name = user.name,
                    isParent = user.isParent
                ),
                application = ApplicationInfo(
                    receiptCode = application.getId().value,
                    applicantName = application.applicantName,
                    applicationType = application.applicationType.name,
                    educationalStatus = application.educationalStatus.name,
                    isDaejeon = application.isDaejeon,
                    isSubmitted = true
                ),
                score = ScoreInfo(
                    receiptCode = score.getId().value,
                    attendanceScore = score.attendanceScore,
                    volunteerScore = score.volunteerScore?.toDouble(),
                    totalScore = score.totalScore.toDouble(),
                    extraScore = score.extraScore.toDouble()
                ),
                calculation = CalculationInfo(
                    finalScore = calculationResult.finalScore,
                    detailScores = calculationResult.detailScores,
                    executionId = calculationResult.formulaExecution.id.value,
                    variableMapping = calculationResult.variableMapping
                )
            )
        }
    }
}