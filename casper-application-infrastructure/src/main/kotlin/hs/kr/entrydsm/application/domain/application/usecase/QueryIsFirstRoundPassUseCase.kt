package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.domain.pass.presentation.dto.response.QueryIsFirstRoundPassResponse
import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.SecurityAdapter
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.interfaces.ApplicationQueryScheduleContract
import hs.kr.entrydsm.domain.schedule.exception.ScheduleExceptions
import hs.kr.entrydsm.domain.schedule.values.ScheduleType
import hs.kr.entrydsm.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import java.time.LocalDateTime

@ReadOnlyUseCase
class QueryIsFirstRoundPassUseCase(
    private val securityAdapter: SecurityAdapter,
    private val queryApplicationContract: ApplicationContract,
    private val applicationQueryScheduleContract: ApplicationQueryScheduleContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract
) {
    suspend fun execute(): QueryIsFirstRoundPassResponse {

        val userId = securityAdapter.getCurrentUserId()
        val application = queryApplicationContract.getApplicationByUserId(userId)
            ?: throw ApplicationNotFoundException()

        val firstAnnounce = applicationQueryScheduleContract.queryByScheduleType(ScheduleType.FIRST_ANNOUNCEMENT)
            ?: throw ScheduleExceptions.ScheduleNotFoundException()

        if (LocalDateTime.now().isBefore(firstAnnounce.date))
            throw ScheduleExceptions.AdmissionUnavailableException()

        val status = applicationQueryStatusContract.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        return QueryIsFirstRoundPassResponse(status.isFirstRoundPass)
    }
}