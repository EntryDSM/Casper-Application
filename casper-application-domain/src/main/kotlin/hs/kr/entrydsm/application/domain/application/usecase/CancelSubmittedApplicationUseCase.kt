package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryStatusPort
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.spi.CommandApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.CommandGraduationInfoPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.QueryGraduationInfoPort
import hs.kr.entrydsm.application.domain.score.spi.CommandScorePort
import hs.kr.entrydsm.application.domain.score.spi.QueryScorePort
import hs.kr.entrydsm.application.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class CancelSubmittedApplicationUseCase(
    private val securityPort: SecurityPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val applicationQueryStatusPort: ApplicationQueryStatusPort,
    private val applicationEventPort: ApplicationEventPort,
    private val commandApplicationPort: CommandApplicationPort,
    private val queryApplicationCasePort: QueryApplicationCasePort,
    private val commandApplicationCasePort: CommandApplicationCasePort,
    private val queryGraduationInfoPort: QueryGraduationInfoPort,
    private val commandGraduationInfoPort: CommandGraduationInfoPort,
    private val queryScorePort: QueryScorePort,
    private val commandScorePort: CommandScorePort
) {

    suspend fun execute() {
        val userId = securityPort.getCurrentUserId()
        val application = queryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val status = applicationQueryStatusPort.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        if (!status.isSubmitted) {
            throw StatusExceptions.NotSubmitException()
        }

        applicationEventPort.cancelSubmittedApplication(application.receiptCode)

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
