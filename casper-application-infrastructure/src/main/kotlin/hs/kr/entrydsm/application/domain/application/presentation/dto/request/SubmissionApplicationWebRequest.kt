package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.model.types.Sex
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class SubmissionApplicationWebRequest(

    @field:NotBlank
    @field:Size(max = 10)
    val applicantName: String,

    @field:NotBlank
    @field:Pattern(
        regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$",
        message = "전화번호 형식이 올바르지 않습니다"
    )
    val applicantTel: String,

    @field:NotNull
    val applicationType: ApplicationType,

    @field:NotNull
    val educationalStatus: EducationalStatus,

    @field:NotNull
    @field:Past
    val birthDate: LocalDate,

    @field:NotNull
    val applicantGender: Sex,

    @field:NotBlank
    val streetAddress: String,

    @field:NotBlank
    val postalCode: String,

    @field:NotBlank
    val detailAddress: String,

    @field:NotNull
    val isDaejeon: Boolean,

    @field:NotBlank
    @field:Size(max = 20)
    val parentName: String,

    @field:NotBlank
    @field:Pattern(
        regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$"
    )
    val parentTel: String,

    @field:NotBlank
    val parentRelation: String,

    @field:NotBlank
    val guardianGender: String,

    @field:NotBlank
    val schoolCode: String,

    @field:NotBlank
    val schoolName: String,

    @field:NotBlank
    val studentId: String,

    @field:NotBlank
    val schoolPhone: String,

    @field:NotBlank
    val teacherName: String,

    @field:NotNull
    val nationalMeritChild: Boolean,

    @field:NotNull
    val specialAdmissionTarget: Boolean,

    @field:NotNull
    val graduationDate: YearMonth,

    @field:NotBlank
    @field:Size(max = 1500)
    val studyPlan: String,

    @field:NotBlank
    @field:Size(max = 1500)
    val selfIntroduce: String,

    @field:NotBlank
    val koreanGrade: String,

    @field:NotBlank
    val socialGrade: String,

    @field:NotBlank
    val historyGrade: String,

    @field:NotBlank
    val mathGrade: String,

    @field:NotBlank
    val scienceGrade: String,

    @field:NotBlank
    val englishGrade: String,

    @field:NotBlank
    val techAndHomeGrade: String,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedKorean: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedSocial: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedHistory: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedMath: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedScience: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedTech: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val gedEnglish: BigDecimal = BigDecimal.ZERO,

    @field:Min(0)
    val absence: Int,

    @field:Min(0)
    val tardiness: Int,

    @field:Min(0)
    val earlyLeave: Int,

    @field:Min(0)
    val classExit: Int,

    @field:Min(0)
    val volunteer: Int,

    @field:NotNull
    val algorithmAward: Boolean,

    @field:NotNull
    val infoProcessingCert: Boolean
)
