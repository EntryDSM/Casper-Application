package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetStaticsCountResponse

interface QueryStaticsCountPort {
    fun queryStaticsCount(
        applicationType: ApplicationType,
        isDaejeon: Boolean
    ): GetStaticsCountResponse
}