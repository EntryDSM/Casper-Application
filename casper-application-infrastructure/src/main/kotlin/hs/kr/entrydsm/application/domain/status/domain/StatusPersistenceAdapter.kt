package hs.kr.entrydsm.application.domain.status.domain

import hs.kr.entrydsm.application.domain.status.domain.repository.StatusCacheRepository
import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.status.aggregates.StatusCache
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class StatusPersistenceAdapter(
    private val statusGrpcClient: StatusGrpcClient,
    private val statusCacheRepository: StatusCacheRepository,
) : StatusContract {
    override fun queryStatusByReceiptCode(receiptCode: Long): Status? =
        runBlocking {
            statusGrpcClient.getStatusByReceiptCode(receiptCode)?.let {
                Status(
                    id = it.id,
                    examCode = it.examCode,
                    applicationStatus = it.applicationStatus,
                    isFirstRoundPass = it.isFirstRoundPass,
                    isSecondRoundPass = it.isSecondRoundPass,
                    receiptCode = it.receiptCode,
                )
            }
        }

    override fun queryStatusByReceiptCodeInCache(receiptCode: Long): StatusCache? {
        return statusCacheRepository.findById(receiptCode)
            .map {
                StatusCache(
                    receiptCode = it.receiptCode,
                    applicationStatus = it.applicationStatus,
                    examCode = it.examCode,
                    isFirstRoundPass = it.isFirstRoundPass,
                    isSecondRoundPass = it.isSecondRoundPass,
                    ttl = it.ttl,
                )
            }.orElse(null)
    }

    override fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) = runBlocking {
        statusGrpcClient.updateExamCode(receiptCode, examCode)
    }
}
