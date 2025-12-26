package hs.kr.entrydsm.application.domain.application.usecase.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType

data class UpdateApplicationTypeRequest(
    val applicationType: ApplicationType,
    val applicationRemark: ApplicationRemark?,
    val isDaejeon: Boolean,
    val isOutOfHeadcount: Boolean,
    val veteransNumber: Int?
)
