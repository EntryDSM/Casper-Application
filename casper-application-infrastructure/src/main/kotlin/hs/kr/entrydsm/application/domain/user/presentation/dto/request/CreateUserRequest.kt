package hs.kr.entrydsm.application.domain.user.presentation.dto.request

data class CreateUserRequest(
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val birthDate: String?,
)
