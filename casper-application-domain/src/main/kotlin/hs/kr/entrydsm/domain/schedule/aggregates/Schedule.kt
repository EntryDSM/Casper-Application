package hs.kr.entrydsm.domain.schedule.aggregates

import hs.kr.entrydsm.domain.schedule.values.ScheduleType
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.time.LocalDateTime

@Aggregate(context = "schedule")
data class Schedule(
    val scheduleType: ScheduleType,
    val date: LocalDateTime
)
