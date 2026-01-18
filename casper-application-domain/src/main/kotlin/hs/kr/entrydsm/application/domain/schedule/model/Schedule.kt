package hs.kr.entrydsm.application.domain.schedule.model

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import hs.kr.entrydsm.application.global.annotation.Aggregate
import java.time.LocalDateTime

@Aggregate
data class Schedule(
    val scheduleType: ScheduleType,
    val date: LocalDateTime
)