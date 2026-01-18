package hs.kr.entrydsm.application.domain.application.usecase.dto.request

data class AttendanceInfo(
    val absence: Int?,
    val tardiness: Int?,
    val earlyLeave: Int?,
    val classExit: Int?,
    val volunteer: Int?
)