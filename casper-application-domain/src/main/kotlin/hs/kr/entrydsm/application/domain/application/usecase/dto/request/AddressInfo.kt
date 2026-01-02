package hs.kr.entrydsm.application.domain.application.usecase.dto.request

data class AddressInfo(
    val isDaejeon: Boolean,
    val streetAddress: String,
    val detailAddress: String,
    val postalCode: String
)