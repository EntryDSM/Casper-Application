package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import hs.kr.entrydsm.domain.application.entities.Application
import java.time.LocalDate
import java.util.UUID

data class ApplicationResponse(
    val receiptCode: Long,
    val userId: UUID,
    val applicantName: String,
    val applicantTel: String,
    val parentName: String,
    val parentTel: String,
    val sex: String,
    val birthDate: LocalDate,
    val streetAddress: String,
    val postalCode: String,
    val detailAddress: String,
    val isDaejeon: Boolean,
    val applicationType: String,
    val applicationRemark: String,
    val educationalStatus: String,
    val photoPath: String?,
    val studyPlan: String?,
    val selfIntroduce: String?,
    val veteransNumber: String?
) {
    companion object {
        fun from(application: Application): ApplicationResponse {
            return ApplicationResponse(
                receiptCode = application.id.value,
                userId = application.userId,
                applicantName = application.applicantName,
                applicantTel = application.applicantTel,
                parentName = application.parentName,
                parentTel = application.parentTel,
                sex = application.sex.name,
                birthDate = application.birthDate,
                streetAddress = application.streetAddress,
                postalCode = application.postalCode,
                detailAddress = application.detailAddress,
                isDaejeon = application.isDaejeon,
                applicationType = application.applicationType.name,
                applicationRemark = application.applicationRemark.name,
                educationalStatus = application.educationalStatus.name,
                photoPath = application.photoPath,
                studyPlan = application.studyPlan,
                selfIntroduce = application.selfIntroduce,
                veteransNumber = application.veteransNumber
            )
        }
    }
}