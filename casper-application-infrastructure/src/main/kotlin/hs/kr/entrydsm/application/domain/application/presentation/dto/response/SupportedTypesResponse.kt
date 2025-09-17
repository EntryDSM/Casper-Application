package hs.kr.entrydsm.application.domain.application.presentation.dto.response

data class SupportedTypesResponse(
    val success: Boolean,
    val data: TypesData,
) {
    data class TypesData(
        val applicationTypes: List<TypeInfo>,
        val educationalStatuses: List<TypeInfo>,
    )

    data class TypeInfo(
        val code: String,
        val name: String,
    )
}
