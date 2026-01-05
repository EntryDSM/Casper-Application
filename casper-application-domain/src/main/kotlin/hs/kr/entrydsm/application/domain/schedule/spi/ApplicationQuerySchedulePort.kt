package hs.kr.entrydsm.application.domain.schedule.spi

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import hs.kr.entrydsm.application.domain.schedule.model.Schedule

interface ApplicationQuerySchedulePort {
    suspend fun queryByScheduleType(scheduleType: ScheduleType): Schedule?
}