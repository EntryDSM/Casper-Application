package hs.kr.entrydsm.domain.examcode.values

import hs.kr.entrydsm.domain.application.values.ApplicationType

data class ExamCodeInfo(
    val receiptCode: Long,
    val applicationType: ApplicationType,
    val distance: Int,
    var examCode: String? = null
)