package hs.kr.entrydsm.application.domain.schedule.domain

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import hs.kr.entrydsm.application.domain.schedule.model.Schedule
import hs.kr.entrydsm.application.domain.schedule.spi.SchedulePortApplication
import hs.kr.entrydsm.application.global.grpc.client.schedule.ScheduleGrpcClient
import org.springframework.stereotype.Component

@Component

class SchedulePersistenceAdapterApplication(
    private val scheduleGrpcClient: ScheduleGrpcClient
): SchedulePortApplication {
    override suspend fun queryByScheduleType(scheduleType: ScheduleType): Schedule? {
        return scheduleGrpcClient.getScheduleByType(scheduleType.name).let {
            Schedule(
                scheduleType = it.type,
                date = it.date
            )
        }
    }
}