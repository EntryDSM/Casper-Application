package hs.kr.entrydsm.domain.user.aggregates

import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.util.UUID

@Aggregate(context = "user")
data class User(
    val id: UUID,
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean
)
