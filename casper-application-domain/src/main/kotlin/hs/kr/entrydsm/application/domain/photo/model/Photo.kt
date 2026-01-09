package hs.kr.entrydsm.application.domain.photo.model

import hs.kr.entrydsm.application.global.annotation.Aggregate
import java.util.UUID

@Aggregate
data class Photo(
    val userId: UUID,
    var photoPath: String
)
