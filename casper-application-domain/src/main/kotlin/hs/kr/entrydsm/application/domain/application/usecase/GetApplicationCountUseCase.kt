package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationCountResponse
import hs.kr.entrydsm.application.global.annotation.UseCase

@UseCase
class GetApplicationCountUseCase(
    private val queryApplicationPort: QueryApplicationPort,
) {
    suspend fun execute(
        applicationType: ApplicationType,
        isDaejeon: Boolean,
    ): GetApplicationCountResponse {
        return queryApplicationPort.
        queryApplicationCountByApplicationTypeAndIsDaejeon(applicationType, isDaejeon)
    }
}
