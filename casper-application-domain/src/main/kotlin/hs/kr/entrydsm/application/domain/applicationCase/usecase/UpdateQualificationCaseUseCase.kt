package hs.kr.entrydsm.application.domain.applicationCase.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.applicationCase.event.spi.ApplicationCaseEventPort
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.vo.ExtraScoreItem
import hs.kr.entrydsm.application.domain.applicationCase.spi.ApplicationCaseQueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.spi.CommandApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateQualificationCaseRequest
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class UpdateQualificationCaseUseCase(
    private val securityPort: SecurityPort,
    private val applicationCaseQueryApplicationPort: ApplicationCaseQueryApplicationPort,
    private val commandApplicationCasePort: CommandApplicationCasePort,
    private val queryApplicationCasePort: QueryApplicationCasePort,
    private val qualificationEventPort: ApplicationCaseEventPort,
) {
    fun execute(request: UpdateQualificationCaseRequest) {
        val userId = securityPort.getCurrentUserId()
        val application = applicationCaseQueryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val qualificationCase =
            queryApplicationCasePort.queryApplicationCaseByApplication(application)

        if(qualificationCase !is QualificationCase) throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()

        commandApplicationCasePort.save(
            qualificationCase.copy(
                koreanGrade = request.koreanGrade,
                scienceGrade = request.scienceGrade,
                socialGrade = request.socialGrade,
                mathGrade = request.mathGrade,
                englishGrade = request.englishGrade,
                optGrade = request.optGrade,
                extraScoreItem = ExtraScoreItem(
                    hasCertificate = request.extraScore.hasCertificate,
                    hasCompetitionPrize = request.extraScore.hasCompetitionPrize
                )
            ),
        )

        qualificationEventPort.updateQualificationCase(qualificationCase)
    }
}
