package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.spi.ApplicationPdfGeneratorPort
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryStatusPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.domain.graduationInfo.exception.GraduationInfoExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.spi.QueryGraduationInfoPort
import hs.kr.entrydsm.application.domain.photo.exception.PhotoExceptions
import hs.kr.entrydsm.application.domain.photo.spi.QueryPhotoPort
import hs.kr.entrydsm.application.domain.score.exception.ScoreExceptions
import hs.kr.entrydsm.application.domain.score.spi.QueryScorePort
import hs.kr.entrydsm.application.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@ReadOnlyUseCase
class GetFinalApplicationPdfUseCase (
    private val securityPort: SecurityPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val queryScorePort: QueryScorePort,
    private val applicationPdfGeneratorPort: ApplicationPdfGeneratorPort,
    private val queryGraduationInfoPort: QueryGraduationInfoPort,
    private val queryApplicationCasePort: QueryApplicationCasePort,
    private val queryStatusPort: ApplicationQueryStatusPort,
    private val queryPhotoPort: QueryPhotoPort
) {

    suspend fun getFinalApplicationPdf(): ByteArray {
        val userId = securityPort.getCurrentUserId()
        val application = queryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val status = queryStatusPort.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        if (status.isNotSubmitted) {
            throw StatusExceptions.NotSubmitException()
        }

        validatePrintableApplicant(application)

        val photo = queryPhotoPort.queryPhotoByUserId(userId)
            ?: throw PhotoExceptions.PhotoNotFoundException()

        val calculatedScore = queryScorePort.queryScoreByReceiptCode(application.receiptCode)
            ?: throw ScoreExceptions.ScoreNotFoundException()

        val graduationInfo = queryGraduationInfoPort.queryGraduationInfoByApplication(application)
            ?: throw GraduationInfoExceptions.GraduationNotFoundException()

        val applicationCase = queryApplicationCasePort.queryApplicationCaseByApplication(application)
            ?: throw ApplicationCaseExceptions.ApplicationCaseNotFoundException()

        return applicationPdfGeneratorPort.generate(
            application = application,
            score = calculatedScore,
            photo = photo,
            graduationInfo = graduationInfo,
            applicationCase = applicationCase
        )
    }

    private fun validatePrintableApplicant(application: Application) {
        if (application.isEducationalStatusEmpty())
            throw GraduationInfoExceptions.EducationalStatusUnmatchedException()
    }
}
