package hs.kr.entrydsm.application.global.grpc.client.schedule

import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import hs.kr.entrydsm.application.global.extension.executeGrpcCallWithResilience
import hs.kr.entrydsm.application.global.grpc.dto.schedule.InternalScheduleResponse
import hs.kr.entrydsm.casper.schedule.proto.ScheduleServiceGrpc
import hs.kr.entrydsm.casper.schedule.proto.ScheduleServiceProto
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Component
class ScheduleGrpcClient(
    @Qualifier("scheduleGrpcRetry") private val retry: Retry,
    @Qualifier("scheduleGrpcCircuitBreaker") private val circuitBreaker: CircuitBreaker,
) {
    @GrpcClient("schedule-grpc")
    lateinit var channel: Channel

    suspend fun getScheduleByType(type: String): InternalScheduleResponse {
        return executeGrpcCallWithResilience(
            retry = retry,
            circuitBreaker = circuitBreaker,
            fallback = {
                InternalScheduleResponse(
                    type =
                        toInternal(
                            runCatching { ScheduleServiceProto.Type.valueOf(type.uppercase()) }
                                .getOrDefault(ScheduleServiceProto.Type.START_DATE),
                        ),
                    date = LocalDateTime.now(),
                )
            },
        ) {
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

            InternalScheduleResponse(scheduleType, date)
        }
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