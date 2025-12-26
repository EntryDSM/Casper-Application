package hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.response

data class GetExtraScoreResponse(
    val hasCertificate: Boolean,
    val hasCompetitionPrize: Boolean
)
