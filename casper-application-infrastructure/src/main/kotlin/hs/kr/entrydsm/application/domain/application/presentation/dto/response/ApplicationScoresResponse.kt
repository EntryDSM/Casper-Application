package hs.kr.entrydsm.application.domain.application.presentation.dto.response

data class ApplicationScoresResponse(
    val success: Boolean,
    val data: ScoresData,
) {
    data class ScoresData(
        val applicationId: String,
        val scores: Map<String, Any>,
    )
}
