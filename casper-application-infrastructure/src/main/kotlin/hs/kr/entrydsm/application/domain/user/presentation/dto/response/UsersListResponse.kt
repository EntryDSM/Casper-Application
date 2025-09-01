package hs.kr.entrydsm.application.domain.user.presentation.dto.response

import java.time.LocalDateTime

data class UsersListResponse(
    val success: Boolean,
    val data: UsersData
) {
    data class UsersData(
        val users: List<UserSummary>,
        val total: Int
    )
    
    data class UserSummary(
        val userId: String,
        val name: String,
        val phoneNumber: String,
        val email: String?,
        val createdAt: LocalDateTime
    )
}