package hs.kr.entrydsm.application.domain.application.presentation.dto.request

data class ValidateScoreDataRequest(
    val applicationType: String,
    val educationalStatus: String,
    val scoreData: Map<String, Any>
)