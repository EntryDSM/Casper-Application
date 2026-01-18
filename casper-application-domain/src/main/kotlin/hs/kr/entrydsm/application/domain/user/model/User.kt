package hs.kr.entrydsm.application.domain.user.model

import hs.kr.entrydsm.application.global.annotation.Aggregate
import java.util.UUID

@Aggregate
data class User(
    val id: UUID,
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean,
)
