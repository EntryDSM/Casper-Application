package hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request

data class ExtraScoreRequest(
    val hasCertificate: Boolean,
    val hasCompetitionPrize: Boolean
)