package hs.kr.entrydsm.application.domain.user.usecase.result

import java.time.LocalDateTime

data class UserDetailResult(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val birthDate: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)