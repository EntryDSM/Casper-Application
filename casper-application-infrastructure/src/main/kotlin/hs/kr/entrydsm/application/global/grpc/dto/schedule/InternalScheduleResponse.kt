package hs.kr.entrydsm.application.global.grpc.dto.schedule

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import java.time.LocalDateTime

data class InternalScheduleResponse(
    val type: ScheduleType,
    val date: LocalDateTime,
)