package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.spi.CommandApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.CommandGraduationInfoPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.QueryGraduationInfoPort
import hs.kr.entrydsm.application.domain.score.spi.CommandScorePort
import hs.kr.entrydsm.application.domain.score.spi.QueryScorePort
import hs.kr.entrydsm.application.global.annotation.UseCase

@UseCase
class DeleteApplicationUseCase(
    private val queryApplicationPort: QueryApplicationPort,
    private val commandApplicationPort: CommandApplicationPort,
    private val queryApplicationCasePort: QueryApplicationCasePort,
    private val commandApplicationCasePort: CommandApplicationCasePort,
    private val queryGraduationInfoPort: QueryGraduationInfoPort,
    private val commandGraduationInfoPort: CommandGraduationInfoPort,
    private val queryScorePort: QueryScorePort,
    private val commandScorePort: CommandScorePort
) {
    fun execute(receiptCode: Long) {
        val application = queryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        queryApplicationCasePort.queryApplicationCaseByApplication(application)?.let {
            commandApplicationCasePort.delete(it)
        }
        queryGraduationInfoPort.queryGraduationInfoByApplication(application)?.let {
            commandGraduationInfoPort.delete(it)
        }
        queryScorePort.queryScoreByReceiptCode(application.receiptCode)?.let {
            commandScorePort.delete(it)
        }
        commandApplicationPort.delete(application)
    }
}