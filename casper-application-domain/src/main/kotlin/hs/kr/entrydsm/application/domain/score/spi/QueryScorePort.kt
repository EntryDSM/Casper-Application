package hs.kr.entrydsm.application.domain.score.spi

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.score.model.Score
import hs.kr.entrydsm.application.domain.score.usecase.dto.response.QueryTotalScoreResponse
import java.math.BigDecimal

interface QueryScorePort {
    fun queryScoreByReceiptCode(receiptCode: Long): Score?

    fun queryTotalScore(receiptCode: Long): BigDecimal?

    suspend fun queryScoreByApplicationTypeAndIsDaejeon(applicationType: ApplicationType, isDaejeon: Boolean): List<Score?>
}
