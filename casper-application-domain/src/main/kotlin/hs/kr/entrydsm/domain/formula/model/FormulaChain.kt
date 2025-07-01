package hs.kr.entrydsm.domain.formula.model

import hs.kr.entrydsm.domain.application.model.types.Admission
import hs.kr.entrydsm.domain.score.model.types.Field
import java.util.UUID
import javax.swing.plaf.synth.Region

data class FormulaChain(
    val id: UUID = UUID.randomUUID(),

    val name: String,
    val description: String,

    val admission: Admission,
    val region: Region,

    val firstStepId: UUID,
    val lastStepId: UUID,
    val totalSteps: Int,

    val expectedFields: Set<Field>,
    val estimatedDurationMs: Long,
    val isActive: Boolean,
)