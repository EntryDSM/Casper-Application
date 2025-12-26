package hs.kr.entrydsm.application.domain.schedule.domain

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import hs.kr.entrydsm.application.domain.schedule.model.Schedule
import hs.kr.entrydsm.application.domain.schedule.spi.SchedulePortApplication
import hs.kr.entrydsm.application.global.feign.client.ScheduleClient
import org.springframework.stereotype.Component

@Component

class SchedulePersistenceAdapterApplication(
    private val scheduleClient: ScheduleClient
): SchedulePortApplication {
    override fun queryByScheduleType(scheduleType: ScheduleType): Schedule? {
        return scheduleClient.queryScheduleByType(scheduleType.name)?.let {
            Schedule(
                scheduleType = it.type,
                date = it.date
            )
        }
    }
}