package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.GetApplicationStatusResponse
import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.SecurityAdapter
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import hs.kr.entrydsm.domain.user.interfaces.UserContract

@ReadOnlyUseCase
class GetMyApplicationStatusUseCase(
    private val securityAdapter: SecurityAdapter,
    private val applicationContract: ApplicationContract,
    private val userContract: UserContract,
    private val statusContract: StatusContract
) {
    fun execute(): GetApplicationStatusResponse {
        val userId = securityAdapter.getCurrentUserId()

        val application = applicationContract.getApplicationByUserId(userId)
            ?: throw ApplicationNotFoundException()

        val status = statusContract.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        val user = userContract.queryUserByUserId(userId)

        val phoneNumber = if (user.isParent) application.parentTel else application.applicantTel
        val name = if ( user.isParent && application.applicantName == null)
            application.parentName else application.applicantName

        return GetApplicationStatusResponse(
            receiptCode = application.receiptCode,
            phoneNumber = phoneNumber,
            name = name,
            isSubmitted = status.isSubmitted,
            isPrintedArrived = status.isPrintsArrived,
            selfIntroduce = application.selfIntroduce,
            studyPlan = application.studyPlan,
            applicationType = application.applicationType.name
        )
    }
}