package hs.kr.entrydsm.domain.score.model

import hs.kr.entrydsm.domain.score.model.types.Achievement
import hs.kr.entrydsm.domain.score.model.types.Field
import hs.kr.entrydsm.domain.score.model.types.Subject
import java.util.UUID

data class Score(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,

    val field: Field,

    val subject: Subject?,
    val achievement: Achievement?,

    val score: Short
)