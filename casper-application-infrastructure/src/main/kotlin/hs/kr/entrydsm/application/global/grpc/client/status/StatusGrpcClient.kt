package hs.kr.entrydsm.application.global.grpc.client.status

import com.google.protobuf.Empty
import hs.kr.entrydsm.application.domain.status.enums.ApplicationStatus
import hs.kr.entrydsm.application.global.extension.executeGrpcCallWithResilience
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusListResponse
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusResponse
import hs.kr.entrydsm.casper.status.proto.StatusServiceGrpc
import hs.kr.entrydsm.casper.status.proto.StatusServiceProto
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Component
class StatusGrpcClient(
    @Qualifier("statusGrpcRetry") private val retry: Retry,
    @Qualifier("statusGrpcCircuitBreaker") private val circuitBreaker: CircuitBreaker,
) {
    @GrpcClient("status-grpc")
    lateinit var channel: Channel

    suspend fun getStatusList(): InternalStatusListResponse {
        return executeGrpcCallWithResilience(
            retry = retry,
            circuitBreaker = circuitBreaker,
            fallback = {
                InternalStatusListResponse(statusList = emptyList())
            },
        ) {
            val statusStub = StatusServiceGrpc.newStub(channel)

            val request = Empty.getDefaultInstance()

            val response =
                suspendCancellableCoroutine { continuation ->
                    statusStub.getStatusList(
                        request,
                        object : StreamObserver<StatusServiceProto.GetStatusListResponse> {
                            override fun onNext(value: StatusServiceProto.GetStatusListResponse) {
                                continuation.resume(value)
                            }

                            override fun onError(t: Throwable) {
                                continuation.resumeWithException(t)
                            }

                            override fun onCompleted() {}
                        },
                    )
                }

            InternalStatusListResponse(
                statusList =
                    response.statusListList.map { statusElement ->
                        InternalStatusResponse(
                            id = statusElement.id,
                            applicationStatus = mapProtoApplicationStatus(statusElement.applicationStatus),
                            examCode = statusElement.examCode.takeIf { it.isNotBlank() },
                            isFirstRoundPass = statusElement.isFirstRoundPass,
                            isSecondRoundPass = statusElement.isSecondRoundPass,
                            receiptCode = statusElement.receiptCode,
                        )
                    },
            )
        }
    }

    suspend fun getStatusByReceiptCode(receiptCode: Long): InternalStatusResponse? {
        return executeGrpcCallWithResilience(
            retry = retry,
            circuitBreaker = circuitBreaker,
            fallback = {
                // Fallback: 기본 상태 반환
                InternalStatusResponse(
                    id = 0L,
                    applicationStatus = ApplicationStatus.NOT_APPLIED,
                    examCode = null,
                    isFirstRoundPass = false,
                    isSecondRoundPass = false,
                    receiptCode = receiptCode,
                )
            },
        ) {
            val statusStub = StatusServiceGrpc.newStub(channel)
            val request =
                StatusServiceProto.GetStatusByReceiptCodeRequest.newBuilder()
                    .setReceiptCode(receiptCode)
                    .build()

            val response =
                suspendCancellableCoroutine { continuation ->
                    statusStub.getStatusByReceiptCode(
                        request,
                        object : StreamObserver<StatusServiceProto.GetStatusByReceiptCodeResponse> {
                            override fun onNext(value: StatusServiceProto.GetStatusByReceiptCodeResponse) {
                                continuation.resume(value)
                            }

                            override fun onError(t: Throwable) {
                                continuation.resumeWithException(t)
                            }

                            override fun onCompleted() {}
                        },
                    )
                }

            InternalStatusResponse(
                id = response.status.id,
                applicationStatus = mapProtoApplicationStatus(response.status.applicationStatus),
                examCode = response.status.examCode.takeIf { it.isNotBlank() },
                isFirstRoundPass = response.status.isFirstRoundPass,
                isSecondRoundPass = response.status.isSecondRoundPass,
                receiptCode = response.status.receiptCode,
            )
        }
    }

    suspend fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) {
        return executeGrpcCallWithResilience(
            retry = retry,
            circuitBreaker = circuitBreaker,
            fallback = {
                // Fallback: 로깅만 하고 조용히 실패
                println("Failed to update exam code for receiptCode: $receiptCode")
            },
        ) {
            val statusStub = StatusServiceGrpc.newStub(channel)
            val request =
                StatusServiceProto.GetExamCodeRequest.newBuilder()
                    .setReceiptCode(receiptCode)
                    .setExamCode(examCode)
                    .build()

            suspendCancellableCoroutine { continuation ->
                statusStub.updateExamCode(
                    request,
                    object : StreamObserver<Empty> {
                        override fun onNext(value: Empty) {
                            continuation.resume(Unit)
                        }

                        override fun onError(t: Throwable) {
                            continuation.resumeWithException(t)
                        }

                        override fun onCompleted() {}
                    },
                )
            }
        }
    }

    private fun mapProtoApplicationStatus(protoApplicationStatus: StatusServiceProto.ApplicationStatus): ApplicationStatus {
        return when (protoApplicationStatus) {
            StatusServiceProto.ApplicationStatus.NOT_APPLIED -> ApplicationStatus.NOT_APPLIED
            StatusServiceProto.ApplicationStatus.WRITING -> ApplicationStatus.WRITING
            StatusServiceProto.ApplicationStatus.SUBMITTED -> ApplicationStatus.SUBMITTED
            StatusServiceProto.ApplicationStatus.WAITING_DOCUMENTS -> ApplicationStatus.WAITING_DOCUMENTS
            StatusServiceProto.ApplicationStatus.DOCUMENTS_RECEIVED -> ApplicationStatus.DOCUMENTS_RECEIVED
            StatusServiceProto.ApplicationStatus.SCREENING_IN_PROGRESS -> ApplicationStatus.SCREENING_IN_PROGRESS
            StatusServiceProto.ApplicationStatus.RESULT_ANNOUNCED -> ApplicationStatus.RESULT_ANNOUNCED
            else -> ApplicationStatus.NOT_APPLIED // UNSPECIFIED 및 기타 경우
        }
    }
}