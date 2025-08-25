package hs.kr.entrydsm.application.global.grpc.client.schedule

import hs.kr.entrydsm.application.global.grpc.dto.schedule.InternalScheduleResponse
import hs.kr.entrydsm.application.global.grpc.dto.schedule.ScheduleType
import hs.kr.entrydsm.casper.schedule.proto.ScheduleServiceGrpc
import hs.kr.entrydsm.casper.schedule.proto.ScheduleServiceProto
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Component
class ScheduleGrpcClient {
    @GrpcClient("schedule-service")
    lateinit var channel: Channel

    suspend fun getScheduleByType(type: String): InternalScheduleResponse {
        val scheduleStub = ScheduleServiceGrpc.newStub(channel)

        val request =
            ScheduleServiceProto.TypeRequest.newBuilder()
                .setType(ScheduleServiceProto.Type.valueOf(type.uppercase()))
                .build()

        val response =
            suspendCancellableCoroutine { continuation ->
                scheduleStub.getScheduleByType(
                    request,
                    object : StreamObserver<ScheduleServiceProto.GetScheduleResponse> {
                        override fun onNext(value: ScheduleServiceProto.GetScheduleResponse) {
                            continuation.resume(value)
                        }

                        override fun onError(t: Throwable) {
                            continuation.resumeWithException(t)
                        }

                        override fun onCompleted() {}
                    },
                )
            }

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        val scheduleType = toInternal(response.type)
        val date = LocalDateTime.parse(response.date, formatter)

        return InternalScheduleResponse(scheduleType, date)
    }

    private fun toInternal(type: ScheduleServiceProto.Type): ScheduleType {
        return when (type) {
            ScheduleServiceProto.Type.START_DATE -> ScheduleType.START_DATE
            ScheduleServiceProto.Type.FIRST_ANNOUNCEMENT -> ScheduleType.FIRST_ANNOUNCEMENT
            ScheduleServiceProto.Type.INTERVIEW -> ScheduleType.INTERVIEW
            ScheduleServiceProto.Type.SECOND_ANNOUNCEMENT -> ScheduleType.SECOND_ANNOUNCEMENT
            ScheduleServiceProto.Type.END_DATE -> ScheduleType.END_DATE
            else -> ScheduleType.START_DATE
        }
    }
}
