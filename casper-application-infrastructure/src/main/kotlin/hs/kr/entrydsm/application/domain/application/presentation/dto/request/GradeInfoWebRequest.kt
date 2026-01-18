package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal

data class GradeInfoWebRequest(
    @field:NotNull(message = "국어 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val koreanGrade: String,
    @field:NotNull(message = "사회 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val socialGrade: String,
    @field:NotNull(message = "역사 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val historyGrade: String,
    @field:NotNull(message = "수학 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val mathGrade: String,
    @field:NotNull(message = "과학 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val scienceGrade: String,
    @field:NotNull(message = "영어 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val englishGrade: String,
    @field:NotNull(message = "기술·가정 성적은 필수입니다")
    @Pattern(regexp = "[A-E,X]{4}")
    val techAndHomeGrade: String,
    @field:DecimalMin(value = "0.0", message = "GED 국어 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 국어 점수는 100점 이하여야 합니다")
    val gedKorean: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", message = "GED 사회 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 사회 점수는 100점 이하여야 합니다")
    val gedSocial: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", message = "GED 수학 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 수학 점수는 100점 이하여야 합니다")
    val gedMath: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", message = "GED 과학 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 과학 점수는 100점 이하여야 합니다")
    val gedScience: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", message = "GED 영어 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 영어 점수는 100점 이하여야 합니다")
    val gedEnglish: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", message = "GED 역사 점수는 0점 이상이어야 합니다")
    @field:DecimalMax(value = "100.0", message = "GED 역사 점수는 100점 이하여야 합니다")
    val gedHistory: BigDecimal = BigDecimal.ZERO,
)
