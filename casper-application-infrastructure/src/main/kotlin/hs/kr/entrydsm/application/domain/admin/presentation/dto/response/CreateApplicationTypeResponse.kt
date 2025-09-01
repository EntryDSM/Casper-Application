package hs.kr.entrydsm.application.domain.admin.presentation.dto.response

data class CreateApplicationTypeResponse(
    val success: Boolean,
    val data: TypeData
) {
    data class TypeData(
        val typeId: String,
        val code: String,
        val name: String
    )
}