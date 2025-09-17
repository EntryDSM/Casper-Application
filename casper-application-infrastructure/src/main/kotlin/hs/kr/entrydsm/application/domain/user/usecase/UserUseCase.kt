package hs.kr.entrydsm.application.domain.user.usecase

import hs.kr.entrydsm.application.domain.user.domain.entity.UserJpaEntity
import hs.kr.entrydsm.application.domain.user.domain.repository.UserJpaRepository
import hs.kr.entrydsm.application.domain.user.usecase.result.CreateUserResult
import hs.kr.entrydsm.application.domain.user.usecase.result.UserDetailResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class UserUseCase(
    private val userJpaRepository: UserJpaRepository,
) {
    fun createUser(
        name: String,
        phoneNumber: String,
        email: String?,
        birthDate: String?,
    ): CreateUserResult {
        val user =
            UserJpaEntity(
                userId = UUID.randomUUID(),
                name = name,
                phoneNumber = phoneNumber,
                email = email,
                birthDate = birthDate,
            )

        val saved = userJpaRepository.save(user)
        return CreateUserResult(
            userId = saved.userId.toString(),
            name = saved.name,
            phoneNumber = saved.phoneNumber,
            email = saved.email,
            birthDate = saved.birthDate,
        )
    }

    @Transactional(readOnly = true)
    fun getUserById(userId: String): UserDetailResult? {
        val userEntity =
            userJpaRepository.findById(UUID.fromString(userId))
                .orElse(null) ?: return null

        return UserDetailResult(
            userId = userEntity.userId.toString(),
            name = userEntity.name,
            phoneNumber = userEntity.phoneNumber,
            email = userEntity.email,
            birthDate = userEntity.birthDate,
            createdAt = userEntity.createdAt,
            updatedAt = userEntity.updatedAt,
        )
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserDetailResult> {
        return userJpaRepository.findAll().map { userEntity ->
            UserDetailResult(
                userId = userEntity.userId.toString(),
                name = userEntity.name,
                phoneNumber = userEntity.phoneNumber,
                email = userEntity.email,
                birthDate = userEntity.birthDate,
                createdAt = userEntity.createdAt,
                updatedAt = userEntity.updatedAt,
            )
        }
    }
}
