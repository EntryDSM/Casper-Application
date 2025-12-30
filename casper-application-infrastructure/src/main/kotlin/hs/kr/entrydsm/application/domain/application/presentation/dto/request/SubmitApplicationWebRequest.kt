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
import javax.validation.constraints.Size

data class SubmitApplicationWebRequest(

    @field:NotBlank(message = "지원자 이름은 필수입니다")
    @field:Size(max = 10, message = "지원자 이름은 10자 이하여야 합니다")
    val applicantName: String,

    @field:NotBlank(message = "지원자 전화번호는 필수입니다")
    val applicantTel: String,

    @field:NotNull(message = "지원 유형은 필수입니다")
    val applicationType: ApplicationType,

    @field:NotNull(message = "학력 상태는 필수입니다")
    val educationalStatus: EducationalStatus,

    @field:NotNull(message = "생년월일은 필수입니다")
    val birthDate: LocalDate,

    @field:NotNull(message = "지원자 성별은 필수입니다")
    val applicantGender: Sex,

    @field:NotBlank(message = "주소는 필수입니다")
    val streetAddress: String,

    @field:NotBlank(message = "우편번호는 필수입니다")
    val postalCode: String,

    @field:NotBlank(message = "상세 주소는 필수입니다")
    val detailAddress: String,

    @field:NotNull(message = "대전 지역 여부는 필수입니다")
    val isDaejeon: Boolean,

    @field:NotBlank(message = "보호자 이름은 필수입니다")
    @field:Size(max = 10, message = "보호자 이름은 10자 이하여야 합니다")
    val parentName: String,

    @field:NotBlank(message = "보호자 전화번호는 필수입니다")
    val parentTel: String,

    @field:NotBlank(message = "보호자 관계는 필수입니다")
    val parentRelation: String,

    @field:NotBlank(message = "보호자 성별은 필수입니다")
    val guardianGender: String,

    @field:NotBlank(message = "학교 코드는 필수입니다")
    val schoolCode: String,

    @field:NotBlank(message = "학교명은 필수입니다")
    val schoolName: String,

    @field:NotBlank(message = "학번은 필수입니다")
    @field:Size(min = 4, max = 4, message = "학번은 4자리여야 합니다")
    val studentId: String,

    @field:NotBlank(message = "학교 전화번호는 필수입니다")
    val schoolPhone: String,

    @field:NotBlank(message = "담임 교사 이름은 필수입니다")
    val teacherName: String,

    @field:NotNull(message = "국가유공자 자녀 여부는 필수입니다")
    val nationalMeritChild: Boolean,

    @field:NotNull(message = "특별전형 대상 여부는 필수입니다")
    val specialAdmissionTarget: Boolean,

    @field:NotNull(message = "졸업(예정)일은 필수입니다")
    val graduationDate: YearMonth,

    @field:NotBlank(message = "학업 계획서는 필수입니다")
    @field:Size(max = 1600, message = "학업 계획서는 1500자 이하여야 합니다")
    val studyPlan: String,

    @field:NotBlank(message = "자기소개서는 필수입니다")
    @field:Size(max = 1600, message = "자기소개서는 1500자 이하여야 합니다")
    val selfIntroduce: String,

    @field:NotNull(message = "국어 성적은 필수입니다")
    val koreanGrade: String,

    @field:NotNull(message = "사회 성적은 필수입니다")
    val socialGrade: String,

    @field:NotNull(message = "역사 성적은 필수입니다")
    val historyGrade: String,

    @field:NotNull(message = "수학 성적은 필수입니다")
    val mathGrade: String,

    @field:NotNull(message = "과학 성적은 필수입니다")
    val scienceGrade: String,

    @field:NotNull(message = "영어 성적은 필수입니다")
    val englishGrade: String,

    @field:NotNull(message = "기술·가정 성적은 필수입니다")
    val techAndHomeGrade: String,

    @field:DecimalMin(value = "0.0", message = "GED 국어 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 국어 점수는 100점 이하여야 합니다")
    val gedKorean: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin(value = "0.0", message = "GED 사회 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 사회 점수는 100점 이하여야 합니다")
    val gedSocial: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin(value = "0.0", message = "GED 영어 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 영어 점수는 100점 이하여야 합니다")
    val gedEnglish: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin(value = "0.0", message = "GED 수학 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 수학 점수는 100점 이하여야 합니다")
    val gedMath: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin(value = "0.0", message = "GED 과학 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 과학 점수는 100점 이하여야 합니다")
    val gedScience: BigDecimal = BigDecimal.ZERO,

    @field:DecimalMin(value = "0.0", message = "GED 역사 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 역사 점수는 100점 이하여야 합니다")
    val gedHistory: BigDecimal = BigDecimal.ZERO,

    @field:Min(value = 0, message = "결석 횟수는 0 이상이어야 합니다")
    val absence: Int,

    @field:Min(value = 0, message = "지각 횟수는 0 이상이어야 합니다")
    val tardiness: Int,

    @field:Min(value = 0, message = "조퇴 횟수는 0 이상이어야 합니다")
    val earlyLeave: Int,

    @field:Min(value = 0, message = "결과 횟수는 0 이상이어야 합니다")
    val classExit: Int,

    @field:Min(value = 0, message = "봉사 시간은 0 이상이어야 합니다")
    val volunteer: Int,

    @field:NotNull(message = "알고리즘 수상 여부는 필수입니다")
    val algorithmAward: Boolean,

    @field:NotNull(message = "자격증 여부는 필수입니다")
    val infoProcessingCert: Boolean
)
