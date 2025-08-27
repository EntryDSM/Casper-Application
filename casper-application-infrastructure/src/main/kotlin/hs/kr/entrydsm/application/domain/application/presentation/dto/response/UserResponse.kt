package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import hs.kr.entrydsm.domain.application.entities.User
import java.util.UUID

data class UserResponse(
    val userId: UUID,
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                userId = user.id,
                phoneNumber = user.phoneNumber,
                name = user.name,
                isParent = user.isParent
            )
        }
    }
}