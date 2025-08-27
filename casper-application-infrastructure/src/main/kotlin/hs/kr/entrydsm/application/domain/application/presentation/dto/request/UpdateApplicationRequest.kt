package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.domain.application.usecase.UpdateApplicationCommand
import hs.kr.entrydsm.domain.application.values.Sex
import java.time.LocalDate
import java.util.UUID

data class UpdateApplicationRequest(
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
    val photoPath: String? = null,
    val studyPlan: String? = null,
    val selfIntroduce: String? = null,
    val veteransNumber: String? = null
) {
    fun toCommand(userId: UUID): UpdateApplicationCommand {
        return UpdateApplicationCommand(
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
            photoPath = photoPath,
            studyPlan = studyPlan,
            selfIntroduce = selfIntroduce,
            veteransNumber = veteransNumber
        )
    }
}