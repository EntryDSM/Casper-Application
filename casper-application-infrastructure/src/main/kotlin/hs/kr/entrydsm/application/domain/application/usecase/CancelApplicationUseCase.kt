package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationCannotCancelException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.interfaces.ApplicationDeleteEventContract
import hs.kr.entrydsm.domain.application.interfaces.CancelApplicationContract
import hs.kr.entrydsm.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@UseCase
@Transactional
class CancelApplicationUseCase(
    private val applicationContract: ApplicationContract,
    private val applicationDeleteEventContract: ApplicationDeleteEventContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract
) : CancelApplicationContract {
    override fun cancelApplication(
        userId: UUID,
    ) {
        val application =
            applicationContract.getApplicationByUserId(userId)
                ?: throw ApplicationNotFoundException()

        val status = applicationQueryStatusContract.queryStatusByReceiptCode(application.receiptCode)
            ?: StatusExceptions.StatusNotFoundException()

        if (status == ApplicationStatus.NOT_APPLIED || status == ApplicationStatus.WRITING) {
            throw ApplicationCannotCancelException()
        }

        applicationDeleteEventContract.deleteStatus(application.receiptCode)
        applicationContract.delete(application)
    }
}
