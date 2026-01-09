package hs.kr.entrydsm.application.global.grpc.dto.user

import hs.kr.entrydsm.application.global.security.jwt.UserRole
import java.util.UUID

data class InternalUserResponse(
    val id: UUID,
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean,
    val role: UserRole,
)
