package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationInfoListByStatusIsSubmittedPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationStatusByGenderResponse
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase

@ReadOnlyUseCase
class GetApplicationStatusByGenderUseCase(
    private val queryApplicationInfoListByStatusIsSubmittedPort: QueryApplicationInfoListByStatusIsSubmittedPort,
) {
    suspend fun execute(): GetApplicationStatusByGenderResponse {
        val applicationList =
            queryApplicationInfoListByStatusIsSubmittedPort.queryApplicationInfoListByStatusIsSubmitted(true)

        val maleCount = applicationList.count { it.isMale() }
        val femaleCount = applicationList.count { it.isFemale() }

        return GetApplicationStatusByGenderResponse(
            male = maleCount,
            female = femaleCount,
        )
    }
}
