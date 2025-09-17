package hs.kr.entrydsm.application.domain.admin.usecase.result

data class CreateEducationalStatusResult(
    val statusId: String,
    val code: String,
    val name: String,
)
