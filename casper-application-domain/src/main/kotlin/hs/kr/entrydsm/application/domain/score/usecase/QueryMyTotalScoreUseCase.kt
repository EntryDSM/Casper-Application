package hs.kr.entrydsm.application.domain.score.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.score.exception.ScoreExceptions
import hs.kr.entrydsm.application.domain.score.spi.QueryScorePort
import hs.kr.entrydsm.application.domain.score.usecase.dto.response.QueryTotalScoreResponse
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@ReadOnlyUseCase
class QueryMyTotalScoreUseCase(
    private val queryApplicationPort: QueryApplicationPort,
    private val securityPort: SecurityPort,
    private val queryScorePort: QueryScorePort
) {
    fun execute(): QueryTotalScoreResponse {
        val userId = securityPort.getCurrentUserId()
        val applicationId = queryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()
        val totalScore = queryScorePort.queryTotalScore(applicationId.receiptCode)
            ?: throw ScoreExceptions.ScoreNotFoundException()
        return QueryTotalScoreResponse(totalScore)
     }
}
