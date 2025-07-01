package hs.kr.entrydsm.domain.formula.model

import hs.kr.entrydsm.domain.application.model.types.Admission
import hs.kr.entrydsm.domain.application.model.types.Region
import java.util.UUID

data class Formula (
    val id: UUID = UUID.randomUUID(),

    val name: String,
    val description: String,
    val formula: String,

    val admission: Admission,
    val region: Region,

    val nextStepId : UUID?,

    val executionCondition: Int,
    val isOptional: Boolean,

    val requiredValues: Set<String>,
    val isActive: Boolean = true,
)