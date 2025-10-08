package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.interfaces.CancelApplicationContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.global.exception.ErrorCode
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@UseCase
@Transactional
class CancelApplicationUseCase(
    private val applicationContract: ApplicationContract,
) : CancelApplicationContract {
    override fun cancelApplication(
        userId: UUID,
    ) {
        val application =
            applicationContract.getApplicationByUserId(userId)
                ?: throw ApplicationException(ErrorCode.APPLICATION_NOT_FOUND)

        if (application.userId != userId) {
            throw ApplicationException(ErrorCode.APPLICATION_ACCESS_DENIED)
        }

        if (application.status != ApplicationStatus.SUBMITTED) {
            throw ApplicationException(ErrorCode.APPLICATION_CANNOT_CANCEL)
        }

        applicationContract.updateApplicationStatus(application.receiptCode, ApplicationStatus.WRITING)
    }
}

class ApplicationException(
    private val errorCode: ErrorCode,
) : RuntimeException(errorCode.description) {
    val code: String = errorCode.code
}
