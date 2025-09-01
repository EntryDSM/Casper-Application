package hs.kr.entrydsm.application.domain.admin.usecase.result

data class CreateApplicationTypeResult(
    val typeId: String,
    val code: String,
    val name: String
)