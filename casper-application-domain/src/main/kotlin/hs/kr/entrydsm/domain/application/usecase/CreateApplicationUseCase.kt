package hs.kr.entrydsm.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.User
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.values.*
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.time.LocalDate
import java.util.UUID

/**
 * 원서 생성 UseCase
 */
interface CreateApplicationUseCase {
    fun execute(command: CreateApplicationCommand): Application
}

data class CreateApplicationCommand(
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
    val applicationType: ApplicationType,
    val applicationRemark: ApplicationRemark,
    val educationalStatus: EducationalStatus
)

class CreateApplicationUseCaseImpl(
    private val applicationPort: ApplicationPort
) : CreateApplicationUseCase {
    
    override fun execute(command: CreateApplicationCommand): Application {
        // 이미 원서를 제출했는지 확인
        if (applicationPort.existsByUserId(command.userId)) {
            throw DomainException(ErrorCodes.Common.RESOURCE_ALREADY_EXISTS)
        }
        
        // 사용자 정보 확인
        val user = applicationPort.queryUserById(command.userId)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        
        // 접수 번호 생성 (간단한 예시 - 실제로는 시퀀스나 다른 방식 사용)
        val receiptCode = generateReceiptCode()
        
        // 원서 생성
        val application = Application.create(
            receiptCode = receiptCode,
            userId = command.userId,
            applicantName = command.applicantName,
            applicantTel = command.applicantTel,
            parentName = command.parentName,
            parentTel = command.parentTel,
            sex = command.sex,
            birthDate = command.birthDate,
            streetAddress = command.streetAddress,
            postalCode = command.postalCode,
            detailAddress = command.detailAddress,
            isDaejeon = command.isDaejeon,
            applicationType = command.applicationType,
            applicationRemark = command.applicationRemark,
            educationalStatus = command.educationalStatus
        )
        
        return applicationPort.save(application)
    }
    
    private fun generateReceiptCode(): ReceiptCode {
        // 실제 구현에서는 DB 시퀀스나 다른 방식으로 유일한 번호 생성
        val nextCode = System.currentTimeMillis() % 100000 + 1001L
        return ReceiptCode.from(nextCode)
    }
}