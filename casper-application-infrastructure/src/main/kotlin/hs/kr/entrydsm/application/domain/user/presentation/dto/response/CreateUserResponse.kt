package hs.kr.entrydsm.application.domain.user.presentation.dto.response

data class CreateUserResponse(
    val success: Boolean,
    val data: UserData
) {
    data class UserData(
        val userId: String,
        val name: String,
        val phoneNumber: String,
        val email: String?,
        val birthDate: String?
    )
}