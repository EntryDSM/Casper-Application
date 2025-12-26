package hs.kr.entrydsm.application.domain.applicationCase.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase
import hs.kr.entrydsm.application.domain.applicationCase.spi.ApplicationCaseQueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.response.GetExtraScoreResponse
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.response.GetQualificationCaseResponse
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class GetQualificationCaseUseCase(
    private val securityPort: SecurityPort,
    private val applicationCaseQueryApplicationPort: ApplicationCaseQueryApplicationPort,
    private val queryApplicationCasePort: QueryApplicationCasePort
) {
    fun execute(): GetQualificationCaseResponse {
        val userId = securityPort.getCurrentUserId()

        val application = applicationCaseQueryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val qualificationCase = queryApplicationCasePort.queryApplicationCaseByApplication(application)

        if (qualificationCase !is QualificationCase) throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()

        return GetQualificationCaseResponse(
            koreanGrade = qualificationCase.koreanGrade,
            socialGrade = qualificationCase.socialGrade,
            mathGrade = qualificationCase.mathGrade,
            scienceGrade = qualificationCase.scienceGrade,
            englishGrade = qualificationCase.englishGrade,
            optGrade = qualificationCase.optGrade,
            extraScore = qualificationCase.extraScoreItem.run {
                GetExtraScoreResponse(
                    hasCertificate = hasCertificate,
                    hasCompetitionPrize = hasCompetitionPrize
                )
            }
        )
    }
}
