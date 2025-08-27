package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.domain.application.usecase.CreateApplicationCommand
import hs.kr.entrydsm.domain.application.values.*
import java.time.LocalDate
import java.util.UUID

data class CreateApplicationRequest(
    val userId: UUID,
    val applicantName: String,
    val applicantTel: String,
    val parentName: String,
    val parentTel: String,
    val sex: String, // "MALE" or "FEMALE"
    val birthDate: LocalDate,
    val streetAddress: String,
    val postalCode: String,
    val detailAddress: String,
    val isDaejeon: Boolean,
    val applicationType: String, // "COMMON", "MEISTER", "SOCIAL"
    val applicationRemark: String, // "NONE", "ONE_PARENT", etc.
    val educationalStatus: String // "PROSPECTIVE_GRADUATE", "GRADUATE", "QUALIFICATION_EXAM"
) {
    fun toCommand(): CreateApplicationCommand {
        return CreateApplicationCommand(
            userId = userId,
            applicantName = applicantName,
            applicantTel = applicantTel,
            parentName = parentName,
            parentTel = parentTel,
            sex = Sex.valueOf(sex),
            birthDate = birthDate,
            streetAddress = streetAddress,
            postalCode = postalCode,
            detailAddress = detailAddress,
            isDaejeon = isDaejeon,
            applicationType = ApplicationType.valueOf(applicationType),
            applicationRemark = ApplicationRemark.valueOf(applicationRemark),
            educationalStatus = EducationalStatus.valueOf(educationalStatus)
        )
    }
}