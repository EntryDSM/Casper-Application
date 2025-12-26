package hs.kr.entrydsm.application.domain.application.usecase.dto.response

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import java.time.LocalDate
import java.time.YearMonth

data class GetApplicationTypeResponse(
    val educationalStatus: EducationalStatus?,
    val applicationType: ApplicationType?,
    val isDaejeon: Boolean?,
    val applicationRemark: ApplicationRemark?,
    val isOutOfHeadCount: Boolean?,
    val graduatedDate: YearMonth?,
    val veteransNumber: Int?
)
