package hs.kr.entrydsm.application.domain.application.presentation.dto.request

data class CreateUserRequest(
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean = false
)