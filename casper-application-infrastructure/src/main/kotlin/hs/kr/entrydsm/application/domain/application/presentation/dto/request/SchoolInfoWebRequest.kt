package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.NotBlank

data class SchoolInfoWebRequest(
    @field:NotBlank(message = "학교 코드는 필수입니다")
    val schoolCode: String,
    @field:NotBlank(message = "학교명은 필수입니다")
    val schoolName: String,
    @field:NotBlank(message = "학교 전화번호는 필수입니다")
    val schoolPhone: String,
    @field:NotBlank(message = "담임 교사 이름은 필수입니다")
    val teacherName: String,
)
