package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetInformationResponse
import hs.kr.entrydsm.application.domain.file.spi.GenerateFileUrlPort
import hs.kr.entrydsm.application.domain.file.usecase.`object`.PathList
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@ReadOnlyUseCase
class GetInformationUseCase(
    private val securityPort: SecurityPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val generateFileUrlPort: GenerateFileUrlPort
) {
    fun execute(): GetInformationResponse {
        val userId = securityPort.getCurrentUserId()
        val application = queryApplicationPort.queryApplicationByUserId(userId)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        return application.run {
            GetInformationResponse(
                sex = sex,
                birthDate = birthDate,
                photoPath = photoPath?.let { generateFileUrlPort.generateFileUrl(it, PathList.PHOTO) },
                applicantName = applicantName,
                applicantTel = applicantTel,
                parentName = parentName,
                parentTel = parentTel,
                streetAddress = streetAddress,
                postalCode = postalCode,
                detailAddress = detailAddress,
                parentRelation = parentRelation
            )
        }
    }
}
