package hs.kr.entrydsm.domain.application.aggregates

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.application.values.ApplicationSubmissionStatus
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Aggregate(context = "application")
data class Application(
    val applicationId: UUID,
    val userId: UUID,
    val receiptCode: Long,
    val applicantName: String,
    val applicantTel: String,
    val parentName: String?,
    val parentTel: String?,
    val birthDate: String?,
    val applicationType: ApplicationType,
    val educationalStatus: EducationalStatus,
    val status: ApplicationStatus,
    val submissionStatus: ApplicationSubmissionStatus,
    val streetAddress: String?,
    val submittedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    val isDaejeon: Boolean?,
    val isOutOfHeadcount: Boolean?,
    val photoPath: String?,
    val parentRelation: String?,
    val postalCode: String?,
    val detailAddress: String?,
    val studyPlan: String?,

    val selfIntroduce: String?,
    val veteransNumber: Int?,
    val schoolCode: String?,
    
    // Basic Info Fields
    val nationalMeritChild: Boolean?,
    val specialAdmissionTarget: Boolean?,
    val graduationDate: String?,
    
    // Personal Info Fields
    val applicantGender: String?,
    
    // Guardian Info Fields
    val guardianName: String?,
    val guardianNumber: String?,
    val guardianGender: String?,
    
    // School Info Fields
    val schoolName: String?,
    val studentId: String?,
    val schoolPhone: String?,
    val teacherName: String?,
    
    // Grade 3-1 Score Fields
    val korean_3_1: Int?,
    val social_3_1: Int?,
    val history_3_1: Int?,
    val math_3_1: Int?,
    val science_3_1: Int?,
    val tech_3_1: Int?,
    val english_3_1: Int?,
    
    // Grade 2-2 Score Fields
    val korean_2_2: Int?,
    val social_2_2: Int?,
    val history_2_2: Int?,
    val math_2_2: Int?,
    val science_2_2: Int?,
    val tech_2_2: Int?,
    val english_2_2: Int?,
    
    // Grade 2-1 Score Fields
    val korean_2_1: Int?,
    val social_2_1: Int?,
    val history_2_1: Int?,
    val math_2_1: Int?,
    val science_2_1: Int?,
    val tech_2_1: Int?,
    val english_2_1: Int?,
    
    // Grade 3-2 Score Fields (for graduates)
    val korean_3_2: Int?,
    val social_3_2: Int?,
    val history_3_2: Int?,
    val math_3_2: Int?,
    val science_3_2: Int?,
    val tech_3_2: Int?,
    val english_3_2: Int?,
    
    // GED Score Fields
    val gedKorean: Int?,
    val gedSocial: Int?,
    val gedHistory: Int?,
    val gedMath: Int?,
    val gedScience: Int?,
    val gedTech: Int?,
    val gedEnglish: Int?,
    
    // Additional Personal Info Fields
    val specialNotes: String?,
    
    // Attendance & Service Fields
    val absence: Int?,
    val tardiness: Int?,
    val earlyLeave: Int?,
    val classExit: Int?,
    val unexcused: Int?,
    val volunteer: Int?,
    val algorithmAward: Boolean?,
    val infoProcessingCert: Boolean?,
    
    // Score Calculation Fields
    val totalScore: BigDecimal?
) {
    
    /**
     * 입학전형 점수를 계산하여 totalScore를 업데이트합니다.
     */
    fun calculateAndUpdateScore(): Application {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        val calculatedScore = scoreService.calculateTotalScore(this)
        return this.copy(totalScore = calculatedScore)
    }
    
    /**
     * 점수 상세 정보를 반환합니다.
     */
    fun getScoreDetails(): Map<String, BigDecimal> {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        return scoreService.getScoreDetails(this)
    }
    
    /**
     * 교과 성적을 계산합니다.
     */
    fun calculateSubjectScore(): BigDecimal {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        return scoreService.calculateSubjectScore(this)
    }
    
    /**
     * 출석 점수를 계산합니다.
     */
    fun calculateAttendanceScore(): BigDecimal {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        return scoreService.calculateAttendanceScore(this)
    }
    
    /**
     * 봉사활동 점수를 계산합니다.
     */
    fun calculateVolunteerScore(): BigDecimal {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        return scoreService.calculateVolunteerScore(this)
    }
    
    /**
     * 가산점을 계산합니다.
     */
    fun calculateBonusScore(): BigDecimal {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        return scoreService.calculateBonusScore(this)
    }
    
    /**
     * 전형별 최대 점수를 반환합니다.
     */
    fun getMaxScore(): BigDecimal {
        val scoreService = hs.kr.entrydsm.domain.application.services.ScoreCalculationService()
        return scoreService.getMaxScore(this.applicationType)
    }
    
    /**
     * 점수 백분율을 계산합니다.
     */
    fun getScorePercentage(): Double {
        val current = totalScore ?: calculateAndUpdateScore().totalScore ?: BigDecimal.ZERO
        val max = getMaxScore()
        return if (max > BigDecimal.ZERO) {
            current.divide(max, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal("100")).toDouble()
        } else 0.0
    }
}
