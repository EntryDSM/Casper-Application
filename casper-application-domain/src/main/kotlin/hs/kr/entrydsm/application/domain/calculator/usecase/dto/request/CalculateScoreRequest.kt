package hs.kr.entrydsm.application.domain.calculator.usecase.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import java.math.BigDecimal

data class CalculateScoreRequest(
    val applicationType: ApplicationType,
    val educationalStatus: EducationalStatus,
    val gradeInfo: GradeInfo,
    val attendanceInfo: AttendanceInfo,
    val awardAndCertificateInfo: AwardAndCertificateInfo
) {
    data class GradeInfo(
        val koreanGrade: String,
        val socialGrade: String,
        val historyGrade: String,
        val mathGrade: String,
        val scienceGrade: String,
        val englishGrade: String,
        val techAndHomeGrade: String,
        val gedKorean: BigDecimal = BigDecimal.ZERO,
        val gedSocial: BigDecimal = BigDecimal.ZERO,
        val gedMath: BigDecimal = BigDecimal.ZERO,
        val gedScience: BigDecimal = BigDecimal.ZERO,
        val gedEnglish: BigDecimal = BigDecimal.ZERO,
        val gedHistory: BigDecimal = BigDecimal.ZERO,
    )

    data class AttendanceInfo(
        val absence: Int,
        val tardiness: Int,
        val earlyLeave: Int,
        val classExit: Int,
        val volunteer: Int,
    )

    data class AwardAndCertificateInfo(
        val algorithmAward: Boolean,
        val infoProcessingCert: Boolean,
    )
}