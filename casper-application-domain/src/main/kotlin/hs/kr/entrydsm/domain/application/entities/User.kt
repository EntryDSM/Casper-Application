package hs.kr.entrydsm.domain.application.entities

import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.AggregateRoot
import java.util.UUID

/**
 * 사용자 애그리게이트 루트
 */
@Entity(aggregateRoot = User::class, context = "application")
data class User(
    private val userId: UUID,
    val phoneNumber: String,
    val name: String,
    val isParent: Boolean = false
) : AggregateRoot<UUID>() {

    val id: UUID
        @JvmName("getUserId")
        get() = userId

    init {
        if (phoneNumber.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (name.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (!isValidPhoneNumber(phoneNumber)) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    override fun getId(): UUID = userId
    override fun getType(): String = "User"
    override fun checkInvariants(): Boolean {
        return phoneNumber.isNotBlank() && 
               name.isNotBlank() && 
               isValidPhoneNumber(phoneNumber)
    }

    /**
     * 전화번호 형식 검증
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = "^01[0-9]{8,9}$".toRegex()
        return phoneRegex.matches(phoneNumber.replace("-", ""))
    }

    /**
     * 사용자 정보 업데이트
     */
    fun updateInfo(phoneNumber: String, name: String): User {
        return copy(
            phoneNumber = phoneNumber,
            name = name
        )
    }

    /**
     * 학부모 여부 변경
     */
    fun changeParentStatus(isParent: Boolean): User {
        return copy(isParent = isParent)
    }

    companion object {
        fun create(
            userId: UUID,
            phoneNumber: String,
            name: String,
            isParent: Boolean = false
        ): User {
            return User(
                userId = userId,
                phoneNumber = phoneNumber,
                name = name,
                isParent = isParent
            )
        }

        fun generate(
            phoneNumber: String,
            name: String,
            isParent: Boolean = false
        ): User {
            return User(
                userId = UUID.randomUUID(),
                phoneNumber = phoneNumber,
                name = name,
                isParent = isParent
            )
        }
    }
}