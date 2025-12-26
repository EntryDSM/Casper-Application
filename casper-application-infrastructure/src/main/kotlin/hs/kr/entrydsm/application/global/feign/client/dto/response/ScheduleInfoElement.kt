package hs.kr.entrydsm.application.global.feign.client.dto.response

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import java.time.LocalDateTime

data class ScheduleInfoElement (
    val type: ScheduleType,
    val date: LocalDateTime
)