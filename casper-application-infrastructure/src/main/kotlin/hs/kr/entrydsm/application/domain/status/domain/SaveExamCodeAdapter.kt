package hs.kr.entrydsm.application.domain.status.domain

import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.domain.status.interfaces.SaveExamCodeContract
import org.springframework.stereotype.Component

/**
 * 시험 코드 저장을 위한 어댑터입니다.
 *
 * gRPC를 통해 외부 Status 서비스에 비동기적으로 시험 코드를 저장하는 기능을 제공합니다.
 */
@Component
class SaveExamCodeAdapter(
    private val statusGrpcClient: StatusGrpcClient,
) : SaveExamCodeContract {
    /**
     * 지정된 접수번호의 시험 코드를 비동기적으로 업데이트합니다.
     *
     * Coroutine을 사용하여 비블로킹 방식으로 외부 서비스에
     * 시험 코드 업데이트를 요청합니다.
     *
     * @param receiptCode 시험 코드를 업데이트할 접수번호
     * @param examCode 새로 배정된 시험 코드
     */
    override suspend fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) {
        statusGrpcClient.updateExamCode(receiptCode, examCode)
    }
}
