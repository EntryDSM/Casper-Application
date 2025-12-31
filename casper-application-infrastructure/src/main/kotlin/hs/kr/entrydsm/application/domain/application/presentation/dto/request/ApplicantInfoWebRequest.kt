package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.Sex
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ApplicantInfoWebRequest(
    @field:NotBlank(message = "지원자 이름은 필수입니다")
    @field:Size(max = 10, message = "지원자 이름은 10자 이하여야 합니다")
    val applicantName: String,

    @field:NotBlank(message = "지원자 전화번호는 필수입니다")
    val applicantTel: String,

    @field:NotNull(message = "생년월일은 필수입니다")
    val birthDate: LocalDate,

    @field:NotNull(message = "지원자 성별은 필수입니다")
    val applicantGender: Sex,

    @field:NotBlank(message = "보호자 이름은 필수입니다")
    @field:Size(max = 10, message = "보호자 이름은 10자 이하여야 합니다")
    val parentName: String,

    @field:NotBlank(message = "보호자 전화번호는 필수입니다")
    val parentTel: String,

    @field:NotBlank(message = "보호자 관계는 필수입니다")
    val parentRelation: String,

    @field:NotBlank(message = "보호자 성별은 필수입니다")
    val guardianGender: String
)