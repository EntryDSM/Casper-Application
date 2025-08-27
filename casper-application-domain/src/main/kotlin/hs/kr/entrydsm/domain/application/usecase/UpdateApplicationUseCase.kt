package hs.kr.entrydsm.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.domain.application.values.Sex
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.time.LocalDate
import java.util.UUID

/**
 * 원서 수정 UseCase
 */
interface UpdateApplicationUseCase {
    fun execute(command: UpdateApplicationCommand): Application
}

data class UpdateApplicationCommand(
    val userId: UUID,
    val applicantName: String,
    val applicantTel: String,
    val parentName: String,
    val parentTel: String,
    val sex: Sex,
    val birthDate: LocalDate,
    val streetAddress: String,
    val postalCode: String,
    val detailAddress: String,
    val isDaejeon: Boolean,
    val photoPath: String? = null,
    val studyPlan: String? = null,
    val selfIntroduce: String? = null,
    val veteransNumber: String? = null
)

class UpdateApplicationUseCaseImpl(
    private val applicationPort: ApplicationPort
) : UpdateApplicationUseCase {
    
    override fun execute(command: UpdateApplicationCommand): Application {
        // 기존 원서 조회
        val existingApplication = applicationPort.queryApplicationByUserId(command.userId)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        
        // 개인정보 업데이트
        val updatedApplication = existingApplication
            .updatePersonalInfo(
                applicantName = command.applicantName,
                applicantTel = command.applicantTel,
                parentName = command.parentName,
                parentTel = command.parentTel,
                sex = command.sex,
                birthDate = command.birthDate,
                streetAddress = command.streetAddress,
                postalCode = command.postalCode,
                detailAddress = command.detailAddress,
                isDaejeon = command.isDaejeon
            )
            .updateAdditionalInfo(
                photoPath = command.photoPath,
                studyPlan = command.studyPlan,
                selfIntroduce = command.selfIntroduce,
                veteransNumber = command.veteransNumber
            )
        
        return applicationPort.save(updatedApplication)
    }
}