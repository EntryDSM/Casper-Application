package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.spi.QueryStaticsCountPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetStaticsCountResponse
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase

@ReadOnlyUseCase
class QueryStaticsCountUseCase(
    private val queryStaticsCountPort: QueryStaticsCountPort
) {
    fun execute(): List<GetStaticsCountResponse> {
        return ApplicationType.values().flatMap { it ->
            // 대전 true false를 나누어 처리한다
            listOf(false, true).map { isDaejeon ->
                val count = queryStaticsCountPort.queryStaticsCount(it, isDaejeon)
                GetStaticsCountResponse(it, isDaejeon, count.count)
            }
        }
    }
}