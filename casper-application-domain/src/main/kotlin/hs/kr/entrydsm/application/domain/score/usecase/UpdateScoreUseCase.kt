package hs.kr.entrydsm.application.domain.score.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.score.exception.ScoreExceptions
import hs.kr.entrydsm.application.domain.score.service.CalculateExtraScoreService
import hs.kr.entrydsm.application.domain.score.spi.CommandScorePort
import hs.kr.entrydsm.application.domain.score.spi.QueryScorePort
import hs.kr.entrydsm.application.domain.score.spi.ScoreQueryApplicationCasePort
import hs.kr.entrydsm.application.domain.score.spi.ScoreQueryApplicationPort
import hs.kr.entrydsm.application.global.annotation.UseCase

@UseCase
class UpdateScoreUseCase(
    private val commandScorePort: CommandScorePort,
    private val scoreQueryApplicationPort: ScoreQueryApplicationPort,
    private val scoreQueryApplicationCasePort: ScoreQueryApplicationCasePort,
    private val queryScorePort: QueryScorePort,
    private val calculateExtraScoreService: CalculateExtraScoreService
) {
    fun execute(receiptCode: Long) {
        val application = scoreQueryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val applicationCase = scoreQueryApplicationCasePort.queryApplicationCaseByApplication(application)
            ?: throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()

        val score = queryScorePort.queryScoreByReceiptCode(receiptCode)
            ?: throw ScoreExceptions.ScoreNotFoundException()

        val extraScore = calculateExtraScoreService.calculateScore(applicationCase, application.isCommon())

        commandScorePort.save(
            score.updateScore(
                applicationCase,
                application.isCommon(),
                extraScore
            )
        )
    }
}
