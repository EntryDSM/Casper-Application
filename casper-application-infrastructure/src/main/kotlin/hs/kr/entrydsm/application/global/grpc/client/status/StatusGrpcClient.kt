package hs.kr.entrydsm.application.global.grpc.client.status

import com.google.protobuf.Empty
import hs.kr.entrydsm.application.global.grpc.dto.status.ApplicationStatus
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusListResponse
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusResponse
import hs.kr.entrydsm.casper.status.proto.StatusServiceGrpc
import hs.kr.entrydsm.casper.status.proto.StatusServiceProto
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 상태 서비스와의 gRPC 통신을 담당하는 클라이언트 클래스입니다.
 *
 * @property channel gRPC 통신을 위한 채널 (status-service로 자동 주입됨)
 */
@Component
class StatusGrpcClient {

    @GrpcClient("status-service")
    lateinit var channel: Channel

    /**
     * 모든 상태 리스트를 비동기적으로 조회합니다.
     * gRPC 비동기 스트리밍을 사용하여 상태 서비스로부터 전체 상태 정보를 가져옵니다.
     *
     * @return 조회된 상태 정보 리스트를 담은 [InternalStatusListResponse] 객체
     * @throws io.grpc.StatusRuntimeException gRPC 서버에서 오류가 발생한 경우
     * @throws java.util.concurrent.CancellationException 코루틴이 취소된 경우
     */
    suspend fun getStatusList(): InternalStatusListResponse {
        val statusStub = StatusServiceGrpc.newStub(channel)

        val request = Empty.getDefaultInstance()

        val response = suspendCancellableCoroutine { continuation ->
            statusStub.getStatusList(request, object : StreamObserver<StatusServiceProto.GetStatusListResponse>{
                override fun onNext(value: StatusServiceProto.GetStatusListResponse) {
                    continuation.resume(value)
                }

                override fun onError(t: Throwable) {
                    continuation.resumeWithException(t)
                }

                override fun onCompleted() {}

            })
        }

        return InternalStatusListResponse(
            statusList = response.statusListList.map { statusElement ->
                InternalStatusResponse(
                    id = statusElement.id,
                    applicationStatus = mapProtoApplicationStatus(statusElement.applicationStatus),
                    examCode = statusElement.examCode.takeIf { it.isNotBlank() },
                    isFirstRoundPass = statusElement.isFirstRoundPass,
                    isSecondRoundPass = statusElement.isSecondRoundPass,
                    receiptCode = statusElement.receiptCode
                )
            }
        )
    }

    /**
     * 접수번호로 특정 상태를 비동기적으로 조회합니다.
     * gRPC 비동기 스트리밍을 사용하여 상태 서비스로부터 해당 접수번호의 상태 정보를 가져옵니다.
     *
     * @param receiptCode 조회할 접수번호
     * @return 조회된 상태 정보를 담은 [InternalStatusResponse] 객체
     * @throws io.grpc.StatusRuntimeException gRPC 서버에서 오류가 발생한 경우
     * @throws java.util.concurrent.CancellationException 코루틴이 취소된 경우
     */
    suspend fun getStatusByReceiptCode(receiptCode: Long): InternalStatusResponse {
        val statusStub = StatusServiceGrpc.newStub(channel)

        val request = StatusServiceProto.GetStatusByReceiptCodeRequest.newBuilder()
            .setReceiptCode(receiptCode)
            .build()

        val response = suspendCancellableCoroutine { continuation ->
            statusStub.getStatusByReceiptCode(request, object : StreamObserver<StatusServiceProto.GetStatusByReceiptCodeResponse>{
                override fun onNext(value: StatusServiceProto.GetStatusByReceiptCodeResponse) {
                    continuation.resume(value)
                }

                override fun onError(t: Throwable) {
                    continuation.resumeWithException(t)
                }

                override fun onCompleted() {}
            })
        }
        return InternalStatusResponse(
            id = response.status.id,
            applicationStatus = mapProtoApplicationStatus(response.status.applicationStatus),
            examCode = response.status.examCode.takeIf { it.isNotBlank() },
            isFirstRoundPass = response.status.isFirstRoundPass,
            isSecondRoundPass = response.status.isSecondRoundPass,
            receiptCode = response.status.receiptCode
        )
    }

    /**
     * 수험번호를 비동기적으로 업데이트합니다.
     * gRPC 비동기 스트리밍을 사용하여 상태 서비스에 수험번호 업데이트를 요청합니다.
     *
     * @param receiptCode 접수번호
     * @param examCode 새로운 수험번호
     * @throws io.grpc.StatusRuntimeException gRPC 서버에서 오류가 발생한 경우
     * @throws java.util.concurrent.CancellationException 코루틴이 취소된 경우
     */
    suspend fun updateExamCode(receiptCode: Long, examCode: String) {
        val statusStub = StatusServiceGrpc.newStub(channel)

        val request = StatusServiceProto.GetExamCodeRequest.newBuilder()
            .setReceiptCode(receiptCode)
            .setExamCode(examCode)
            .build()

        suspendCancellableCoroutine {  continuation ->
            statusStub.updateExamCode(request, object : StreamObserver<Empty>{
                override fun onNext(value: Empty) {
                    continuation.resume(Unit)
                }

                override fun onError(t: Throwable) {
                    continuation.resumeWithException(t)
                }

                override fun onCompleted() {}
            })
        }
    }


    /**
     * gRPC 프로토콜 지원 상태를 도메인 지원 상태로 변환합니다.
     *
     * @param protoApplicationStatus 변환할 gRPC 프로토콜 지원 상태
     * @return 도메인 지원 상태
     */
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