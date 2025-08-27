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

/**
 * Schedule Service와 gRPC 통신을 하는 클라이언트입니다.
 */
@Component
class ScheduleGrpcClient {
    @GrpcClient("schedule-service")
    lateinit var channel: Channel

    /**
     * 일정 종류를 통해 일정을 조회합니다.
     * @param type 일정 종류
     * @return 일정 정보
     */
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

    /**
     * gRPC DTO를 내부 DTO로 변환합니다.
     * @param type gRPC DTO
     * @return 내부 DTO
     */
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
