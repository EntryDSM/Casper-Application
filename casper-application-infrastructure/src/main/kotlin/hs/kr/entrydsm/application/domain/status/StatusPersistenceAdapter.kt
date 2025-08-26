package hs.kr.entrydsm.application.domain.status

import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.domain.status.interfaces.StatusUseCase
import org.springframework.stereotype.Component

@Component
class StatusPersistenceAdapter(
    private val statusGrpcClient: StatusGrpcClient,
) : StatusUseCase {
    override suspend fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) {
        statusGrpcClient.updateExamCode(receiptCode, examCode)
    }
}
