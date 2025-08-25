package hs.kr.entrydsm.application.domain.status

import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.application.global.grpc.client.status.dto.request.InternalUpdateExamCodeRequest
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import org.springframework.stereotype.Component

@Component
class StatusPersistenceAdapter(
    private val statusGrpcClient: StatusGrpcClient,
) : StatusContract {
    override suspend fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) {
        val response = InternalUpdateExamCodeRequest(receiptCode, examCode)
        statusGrpcClient.updateExamCode(response)
    }
}
