package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryStatusPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.QueryIsSecondRoundPassResponse
import hs.kr.entrydsm.application.domain.applicationCase.spi.ApplicationCaseQueryApplicationPort
import hs.kr.entrydsm.application.domain.schedule.enums.ScheduleType
import hs.kr.entrydsm.application.domain.schedule.exception.ScheduleExceptions
import hs.kr.entrydsm.application.domain.schedule.spi.ApplicationQuerySchedulePort
import hs.kr.entrydsm.application.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort
import java.time.LocalDateTime

@ReadOnlyUseCase
class QueryIsSecondRoundPassUseCase (
    private val securityPort: SecurityPort,
    private val queryApplicationPort: ApplicationCaseQueryApplicationPort,
    private val applicationQueryStatusPort: ApplicationQueryStatusPort,
    private val applicationQuerySchedulePort: ApplicationQuerySchedulePort
) {
    suspend fun execute(): QueryIsSecondRoundPassResponse {
        val userId = securityPort.getCurrentUserId()
        val application = queryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val secondAnnounce = applicationQuerySchedulePort.queryByScheduleType(ScheduleType.SECOND_ANNOUNCEMENT)
            ?: throw ScheduleExceptions.ScoreNotFoundException()

        if (LocalDateTime.now().isBefore(secondAnnounce.date))
            throw ScheduleExceptions.AdmissionUnavailableException()

        val status = applicationQueryStatusPort.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        return QueryIsSecondRoundPassResponse(status.isSecondRoundPass)
    }
}