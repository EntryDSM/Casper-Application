package hs.kr.entrydsm.application.domain.user.usecase.result

data class CreateUserResult(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val birthDate: String?
)