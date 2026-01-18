package hs.kr.entrydsm.application.domain.school.usecase.dto.response

data class SchoolResponse(
    val code: String,
    val name: String,
    val information: String,
    val address: String
)