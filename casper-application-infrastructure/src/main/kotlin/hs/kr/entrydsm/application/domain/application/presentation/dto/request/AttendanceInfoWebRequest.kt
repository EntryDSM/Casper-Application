package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.PositiveOrZero

data class AttendanceInfoWebRequest(
    @PositiveOrZero
    val absence: Int?,
    @PositiveOrZero
    val tardiness: Int?,
    @PositiveOrZero
    val earlyLeave: Int?,
    @PositiveOrZero
    val classExit: Int?,
    @PositiveOrZero
    val volunteer: Int?
)
