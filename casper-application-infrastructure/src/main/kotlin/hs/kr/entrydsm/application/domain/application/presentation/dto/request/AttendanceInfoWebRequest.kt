package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import javax.validation.constraints.Min

data class AttendanceInfoWebRequest(
    @field:Min(value = 0, message = "결석 횟수는 0 이상이어야 합니다")
    val absence: Int,

    @field:Min(value = 0, message = "지각 횟수는 0 이상이어야 합니다")
    val tardiness: Int,

    @field:Min(value = 0, message = "조퇴 횟수는 0 이상이어야 합니다")
    val earlyLeave: Int,

    @field:Min(value = 0, message = "결과 횟수는 0 이상이어야 합니다")
    val classExit: Int,

    @field:Min(value = 0, message = "봉사 시간은 0 이상이어야 합니다")
    val volunteer: Int
)