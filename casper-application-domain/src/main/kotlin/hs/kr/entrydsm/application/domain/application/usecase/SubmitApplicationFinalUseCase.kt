package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.*
import hs.kr.entrydsm.application.domain.graduationInfo.exception.GraduationInfoExceptions
import hs.kr.entrydsm.application.domain.score.exception.ScoreExceptions
import hs.kr.entrydsm.application.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class SubmitApplicationFinalUseCase(
    private val securityPort: SecurityPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val applicationQueryUserPort: ApplicationQueryUserPort,
    private val applicationQueryGraduationInfoPort: ApplicationQueryGraduationInfoPort,
    private val applicationQueryStatusPort: ApplicationQueryStatusPort,
    private val applicationQueryScorePort: ApplicationQueryScorePort,
    private val applicationEventPort: ApplicationEventPort
) {
   fun execute(){
       val userId = securityPort.getCurrentUserId()

       val application = queryApplicationPort.queryApplicationByUserId(userId)
           ?: throw ApplicationExceptions.ApplicationNotFoundException()
       val graduationInfo = applicationQueryGraduationInfoPort.queryGraduationInfoByApplication(application)
           ?: throw GraduationInfoExceptions.GraduationNotFoundException()

       val score = applicationQueryScorePort.queryScoreByReceiptCode(application.receiptCode)

       if(graduationInfo.hasEmptyInfo() || score == null || application.hasEmptyInfo()) {
            throw ApplicationExceptions.ApplicationProcessNotComplete()
       }

       val status = applicationQueryStatusPort.queryStatusByReceiptCode(application.receiptCode)
           ?: throw StatusExceptions.StatusNotFoundException()

       if(status.isSubmitted) {
            throw StatusExceptions.AlreadySubmittedException()
       }

       applicationEventPort.submitApplicationFinal(application.receiptCode)
   }
}