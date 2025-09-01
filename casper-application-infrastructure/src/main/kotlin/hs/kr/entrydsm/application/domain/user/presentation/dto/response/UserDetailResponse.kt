package hs.kr.entrydsm.application.domain.user.presentation.dto.response

import java.time.LocalDateTime

data class UserDetailResponse(
    val success: Boolean,
    val data: UserDetailData
) {
    data class UserDetailData(
        val userId: String,
        val name: String,
        val phoneNumber: String,
        val email: String?,
        val birthDate: String?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
}