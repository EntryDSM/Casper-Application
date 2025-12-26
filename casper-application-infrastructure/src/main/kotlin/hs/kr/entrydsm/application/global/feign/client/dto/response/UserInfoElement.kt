package hs.kr.entrydsm.application.global.feign.client.dto.response

import hs.kr.entrydsm.application.global.security.jwt.UserRole
import java.util.UUID

data class UserInfoElement(
    val id: UUID,
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean,
    val role: UserRole,
)
