package hs.kr.entrydsm.application.domain.application.usecase.dto.request

data class SchoolInfo(
    val schoolCode: String,
    val schoolName: String,
    val schoolPhone: String,
    val teacherName: String
)