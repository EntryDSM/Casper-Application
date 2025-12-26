package hs.kr.entrydsm.application.domain.application.service

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryUserPort
import hs.kr.entrydsm.application.global.annotation.DomainService
import java.util.UUID

@DomainService
class CheckTelService(
    private val applicationQueryUserPort: ApplicationQueryUserPort,
) {
    fun checkParentPutApplicantTel(
        userId: UUID,
        applicantTel: String,
    ): String {
        val user = applicationQueryUserPort.queryUserByUserId(userId)
        return if (user.isParent) Application.DEFAULT_TEL else applicantTel
    }
}
