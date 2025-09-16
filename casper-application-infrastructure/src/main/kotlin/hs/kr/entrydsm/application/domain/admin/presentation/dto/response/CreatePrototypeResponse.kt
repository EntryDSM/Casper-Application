package hs.kr.entrydsm.application.domain.admin.presentation.dto.response

data class CreatePrototypeResponse(
    val success: Boolean,
    val data: PrototypeData,
) {
    data class PrototypeData(
        val prototypeId: String,
        val applicationType: String,
        val educationalStatus: String,
        val region: String?,
    )
}
