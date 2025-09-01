package hs.kr.entrydsm.application.domain.user.domain.repository

import hs.kr.entrydsm.application.domain.user.domain.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserJpaEntity, UUID> {
    fun findByPhoneNumber(phoneNumber: String): UserJpaEntity?
    fun findByEmail(email: String): UserJpaEntity?
}