package hs.kr.entrydsm.application.domain.application.event.dto

import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID

data class SubmitApplicationEvent(
    val receiptCode: Long,
    val userId: UUID,
    val educationalStatus: EducationalStatus,
    val graduationDate: YearMonth,
    val gradeNumber: String,
    val classNumber: String,
    val studentNumber: String,
    val schoolCode: String,
    val teacherName: String,
    val schoolPhone: String,
    val scoreData: ApplicationScoreData
)

data class ApplicationScoreData(
    val koreanGrade: String,
    val socialGrade: String,
    val historyGrade: String,
    val mathGrade: String,
    val scienceGrade: String,
    val englishGrade: String,
    val techAndHomeGrade: String,
    val gedKorean: BigDecimal,
    val gedSocial: BigDecimal,
    val gedOpt: BigDecimal,
    //val gedHistory: BigDecimal,
    val gedMath: BigDecimal,
    val gedScience: BigDecimal,
    val gedEnglish: BigDecimal,
    val absence: Int,
    val tardiness: Int,
    val earlyLeave: Int,
    val classExit: Int,
    val volunteer: Int,
    val algorithmAward: Boolean,
    val infoProcessingCert: Boolean
)