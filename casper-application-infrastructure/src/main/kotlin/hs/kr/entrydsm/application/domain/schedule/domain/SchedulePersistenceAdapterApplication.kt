package hs.kr.entrydsm.application.domain.schedule.domain

import hs.kr.entrydsm.application.global.grpc.client.schedule.ScheduleGrpcClient
import hs.kr.entrydsm.domain.application.interfaces.ApplicationQueryScheduleContract
import hs.kr.entrydsm.domain.schedule.aggregates.Schedule
import hs.kr.entrydsm.domain.schedule.values.ScheduleType
import org.springframework.stereotype.Component

@Component
class SchedulePersistenceAdapterApplication(
    private val scheduleGrpcClient: ScheduleGrpcClient
) : ApplicationQueryScheduleContract {
    override suspend fun queryByScheduleType(scheduleType: ScheduleType): Schedule? {
        return scheduleGrpcClient.getScheduleByType(scheduleType.name).let {
            Schedule(
                scheduleType = it.type,
                date = it.date
            )
        }
    }
}