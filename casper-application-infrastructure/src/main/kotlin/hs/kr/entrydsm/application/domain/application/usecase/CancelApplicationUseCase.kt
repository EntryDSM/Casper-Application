package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationCannotCancelException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.interfaces.CancelApplicationContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
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
                ?: throw ApplicationNotFoundException()

        if (application.status != ApplicationStatus.SUBMITTED) {
            throw ApplicationCannotCancelException()
        }

        applicationContract.updateApplicationStatus(application.receiptCode, ApplicationStatus.WRITING)
    }
}
