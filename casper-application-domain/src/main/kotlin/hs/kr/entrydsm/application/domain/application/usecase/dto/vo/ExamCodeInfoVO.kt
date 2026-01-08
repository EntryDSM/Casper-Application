package hs.kr.entrydsm.application.domain.application.usecase.dto.vo

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType

data class ExamCodeInfoVO(
    val receiptCode: Long,
    val applicationType: ApplicationType,
    val distance: Int,
    var examCode: String? = null
)