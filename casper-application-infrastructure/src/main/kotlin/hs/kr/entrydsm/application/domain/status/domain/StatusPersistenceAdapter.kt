package hs.kr.entrydsm.application.domain.status.domain

import hs.kr.entrydsm.application.domain.status.domain.repository.StatusCacheRepository
import hs.kr.entrydsm.application.domain.status.model.Status
import hs.kr.entrydsm.application.domain.status.model.StatusCache
import hs.kr.entrydsm.application.domain.status.spi.StatusPort
import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import org.springframework.stereotype.Component

@Component
class StatusPersistenceAdapter(
    private val statusCacheRepository: StatusCacheRepository,
    private val statusGrpcClient: StatusGrpcClient,
) : StatusPort {
    override suspend fun queryStatusByReceiptCode(receiptCode: Long): Status? {
        return statusGrpcClient.getStatusByReceiptCode(receiptCode)?.let {
            Status(
                id = it.id,
                applicationStatus = it.applicationStatus,
                examCode = it.examCode,
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

    override suspend fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) {
        statusGrpcClient.updateExamCode(receiptCode, examCode)
    }
}
