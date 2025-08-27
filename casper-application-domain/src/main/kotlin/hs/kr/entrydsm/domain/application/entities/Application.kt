package hs.kr.entrydsm.domain.application.entities

import hs.kr.entrydsm.domain.application.values.*
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.AggregateRoot
import java.time.LocalDate
import java.util.UUID

/**
 * 원서 접수 애그리게이트 루트
 */
@Entity(aggregateRoot = Application::class, context = "application")
data class Application(
    private val receiptCode: ReceiptCode,
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
    val educationalStatus: EducationalStatus,
    val photoPath: String? = null,
    val studyPlan: String? = null,
    val selfIntroduce: String? = null,
    val veteransNumber: String? = null
) : AggregateRoot<ReceiptCode>() {

    val id: ReceiptCode
        @JvmName("getReceiptCode")
        get() = receiptCode

    init {
        if (applicantName.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (applicantTel.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (parentName.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (parentTel.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (streetAddress.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (postalCode.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (detailAddress.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    override fun getId(): ReceiptCode = receiptCode
    override fun getType(): String = "Application"
    override fun checkInvariants(): Boolean {
        return applicantName.isNotBlank() &&
               applicantTel.isNotBlank() &&
               parentName.isNotBlank() &&
               parentTel.isNotBlank() &&
               streetAddress.isNotBlank() &&
               postalCode.isNotBlank() &&
               detailAddress.isNotBlank()
    }

    /**
     * 원서 완성도 검증 (필수 정보 누락 체크)
     */
    fun hasEmptyInfo(): Boolean {
        return applicantName.isBlank() ||
               applicantTel.isBlank() ||
               parentName.isBlank() ||
               parentTel.isBlank() ||
               streetAddress.isBlank() ||
               postalCode.isBlank() ||
               detailAddress.isBlank() ||
               (isSocial() && applicationRemark == ApplicationRemark.NOTHING)
    }

    /**
     * 전형별 판별 메소드들
     */
    fun isCommon(): Boolean = applicationType == ApplicationType.COMMON
    fun isSocial(): Boolean = applicationType == ApplicationType.SOCIAL
    fun isMeister(): Boolean = applicationType == ApplicationType.MEISTER

    /**
     * 학력별 판별 메소드들
     */
    fun isQualificationExam(): Boolean = educationalStatus == EducationalStatus.QUALIFICATION_EXAM
    fun isGraduate(): Boolean = educationalStatus == EducationalStatus.GRADUATE
    fun isProspectiveGraduate(): Boolean = educationalStatus == EducationalStatus.PROSPECTIVE_GRADUATE

    /**
     * 개인정보 업데이트
     */
    fun updatePersonalInfo(
        applicantName: String,
        applicantTel: String,
        parentName: String,
        parentTel: String,
        sex: Sex,
        birthDate: LocalDate,
        streetAddress: String,
        postalCode: String,
        detailAddress: String,
        isDaejeon: Boolean
    ): Application {
        return copy(
            applicantName = applicantName,
            applicantTel = applicantTel,
            parentName = parentName,
            parentTel = parentTel,
            sex = sex,
            birthDate = birthDate,
            streetAddress = streetAddress,
            postalCode = postalCode,
            detailAddress = detailAddress,
            isDaejeon = isDaejeon
        )
    }

    /**
     * 추가 정보 업데이트
     */
    fun updateAdditionalInfo(
        photoPath: String?,
        studyPlan: String?,
        selfIntroduce: String?,
        veteransNumber: String?
    ): Application {
        return copy(
            photoPath = photoPath,
            studyPlan = studyPlan,
            selfIntroduce = selfIntroduce,
            veteransNumber = veteransNumber
        )
    }

    companion object {
        fun create(
            receiptCode: ReceiptCode,
            userId: UUID,
            applicantName: String,
            applicantTel: String,
            parentName: String,
            parentTel: String,
            sex: Sex,
            birthDate: LocalDate,
            streetAddress: String,
            postalCode: String,
            detailAddress: String,
            isDaejeon: Boolean,
            applicationType: ApplicationType,
            applicationRemark: ApplicationRemark,
            educationalStatus: EducationalStatus
        ): Application {
            return Application(
                receiptCode = receiptCode,
                userId = userId,
                applicantName = applicantName,
                applicantTel = applicantTel,
                parentName = parentName,
                parentTel = parentTel,
                sex = sex,
                birthDate = birthDate,
                streetAddress = streetAddress,
                postalCode = postalCode,
                detailAddress = detailAddress,
                isDaejeon = isDaejeon,
                applicationType = applicationType,
                applicationRemark = applicationRemark,
                educationalStatus = educationalStatus
            )
        }
    }
}