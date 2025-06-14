package hs.kr.entrydsm.domain.formula.model

import hs.kr.entrydsm.domain.application.model.types.Admission

data class Formula (
    val admission: Admission,
    val formula: String,
    val step: Short,
)