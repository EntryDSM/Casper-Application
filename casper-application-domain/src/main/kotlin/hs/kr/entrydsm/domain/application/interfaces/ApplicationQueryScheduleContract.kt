package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.schedule.aggregates.Schedule
import hs.kr.entrydsm.domain.schedule.values.ScheduleType

interface ApplicationQueryScheduleContract {
    suspend fun queryByScheduleType(scheduleType: ScheduleType): Schedule?
}