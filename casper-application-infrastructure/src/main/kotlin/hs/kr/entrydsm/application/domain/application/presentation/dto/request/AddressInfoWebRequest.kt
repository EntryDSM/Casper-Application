package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class AddressInfoWebRequest(
    @field:NotNull(message = "대전 지역 여부는 필수입니다")
    val isDaejeon: Boolean,

    @field:NotBlank(message = "주소는 필수입니다")
    val streetAddress: String,

    @field:NotBlank(message = "상세 주소는 필수입니다")
    val detailAddress: String,

    @field:NotBlank(message = "우편번호는 필수입니다")
    val postalCode: String
)