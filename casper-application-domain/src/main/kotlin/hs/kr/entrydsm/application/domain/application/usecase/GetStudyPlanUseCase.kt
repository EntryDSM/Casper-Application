package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetStudyPlanResponse
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@ReadOnlyUseCase
class GetStudyPlanUseCase(
    private val securityPort: SecurityPort,
    private val queryApplicationPort: QueryApplicationPort,
) {
    fun execute(): GetStudyPlanResponse {
        val userId = securityPort.getCurrentUserId()
        val application = queryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        return GetStudyPlanResponse(application.studyPlan)
    }
}
