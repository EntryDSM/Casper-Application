package hs.kr.entrydsm.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.util.UUID

/**
 * 원서 조회 UseCase
 */
interface GetApplicationUseCase {
    fun execute(query: GetApplicationQuery): Application
}

sealed class GetApplicationQuery {
    data class ByUserId(val userId: UUID) : GetApplicationQuery()
    data class ByReceiptCode(val receiptCode: ReceiptCode) : GetApplicationQuery()
}

class GetApplicationUseCaseImpl(
    private val applicationPort: ApplicationPort
) : GetApplicationUseCase {
    
    override fun execute(query: GetApplicationQuery): Application {
        return when (query) {
            is GetApplicationQuery.ByUserId -> {
                applicationPort.queryApplicationByUserId(query.userId)
                    ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
            }
            is GetApplicationQuery.ByReceiptCode -> {
                applicationPort.queryApplicationByReceiptCode(query.receiptCode)
                    ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
            }
        }
    }
}