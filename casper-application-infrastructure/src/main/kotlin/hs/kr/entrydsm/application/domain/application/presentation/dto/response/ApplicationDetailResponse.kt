package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import java.time.LocalDateTime

data class ApplicationDetailResponse(
    val success: Boolean,
    val data: ApplicationDetailData,
) {
    data class ApplicationDetailData(
        val applicationId: String,
        val userId: String,
        val receiptCode: Long,
        val applicantName: String,
        val applicantTel: String,
        val parentName: String?,
        val parentTel: String?,
        val birthDate: String?,
        val applicationType: String,
        val educationalStatus: String,
        val status: String,
        val submittedAt: LocalDateTime,
        val reviewedAt: LocalDateTime?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val photoUrl: String?,
        val studyPlan: String?,
        val selfIntroduce: String?,
        val isDaejeon: Boolean,
        // 점수 정보
        val scores: ScoreInfo?
    )

    data class ScoreInfo(
        val totalScore: Double?,
        val subjectScore: Double?,
        val attendanceScore: Double?,
        val volunteerScore: Double?,
        val bonusScore: Double?
    )
}
