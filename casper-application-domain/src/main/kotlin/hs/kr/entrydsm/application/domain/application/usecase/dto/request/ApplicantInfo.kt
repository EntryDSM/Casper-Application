package hs.kr.entrydsm.application.domain.application.usecase.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.Sex
import java.time.LocalDate

data class ApplicantInfo(
    val applicantName: String,
    val applicantTel: String,
    val birthDate: LocalDate,
    val applicantGender: Sex,
    val parentName: String,
    val parentTel: String,
    val parentRelation: String,
)