package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.global.annotation.UseCase

@UseCase
class DeleteApplicationUseCase(
    private val queryApplicationPort: QueryApplicationPort,
    private val commendApplicationPort: CommandApplicationPort
) {
    fun execute(receiptCode: Long) {
        val application = queryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()
        commendApplicationPort.delete(application)
    }
}