package hs.kr.entrydsm.application.domain.applicationCase.domain.entity.vo

import jakarta.persistence.Embeddable

@Embeddable
data class ExtraScoreItem(
    val hasCertificate: Boolean,
    val hasCompetitionPrize: Boolean,
)
