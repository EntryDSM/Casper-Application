package hs.kr.entrydsm.application.domain.application.model

data class Applicant(
    val receiptCode: Long,
    val name: String?,
    val isDaejeon: Boolean?,
    val applicationType: String?,
    val educationalStatus: String?,
    val isArrived: Boolean?,
)
