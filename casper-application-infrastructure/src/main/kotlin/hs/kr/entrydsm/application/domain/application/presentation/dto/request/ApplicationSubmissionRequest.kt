package hs.kr.entrydsm.application.domain.application.presentation.dto.request

data class ApplicationSubmissionRequest(
    val application: Map<String, Any>,
    val scores: Map<String, Any>,
)