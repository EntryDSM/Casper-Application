package hs.kr.entrydsm.application.global.grpc.dto.schedule

import java.time.LocalDateTime

data class InternalScheduleResponse(
    val type: ScheduleType,
    val date: LocalDateTime,
)
