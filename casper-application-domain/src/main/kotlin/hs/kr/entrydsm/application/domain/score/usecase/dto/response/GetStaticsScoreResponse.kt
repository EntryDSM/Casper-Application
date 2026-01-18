package hs.kr.entrydsm.application.domain.score.usecase.dto.response

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import java.math.BigDecimal

data class GetStaticsScoreResponse(
    val isDaejeon: Boolean,
    val applicationType: ApplicationType,
    var totalScore: List<Int>
)
