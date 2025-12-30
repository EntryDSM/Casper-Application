package hs.kr.entrydsm.application.domain.application.usecase.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.model.types.Sex
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class SubmitApplicationRequest(
    val applicantName: String,
    val applicantTel: String,
    val applicationType: ApplicationType,
    val educationalStatus: EducationalStatus,
    val birthDate: LocalDate,
    val applicantGender: Sex,
    val streetAddress: String,
    val postalCode: String,
    val detailAddress: String,
    val isDaejeon: Boolean,
    val parentName: String,
    val parentTel: String,
    val parentRelation: String,
    val guardianGender: String,
    val schoolCode: String,
    val schoolName: String,
    val studentId: String,
    val schoolPhone: String,
    val teacherName: String,
    val nationalMeritChild: Boolean,
    val specialAdmissionTarget: Boolean,
    val graduationDate: YearMonth,
    val studyPlan: String,
    val selfIntroduce: String,

    // 요청 형식 이렇게 바꿔서 기존 프론트에서 구현된 원서 제출 api의 request body 수정해야함.
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

    val absence: Int,
    val tardiness: Int,
    val earlyLeave: Int,
    val classExit: Int,
    val volunteer: Int,

    val algorithmAward: Boolean,
    val infoProcessingCert: Boolean
)
