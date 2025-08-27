package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, ByteArray> {
    
    fun findByUserId(userId: ByteArray): UserJpaEntity?
    
    fun findByPhoneNumber(phoneNumber: String): UserJpaEntity?
    
    fun existsByUserId(userId: ByteArray): Boolean
    
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    
    fun deleteByUserId(userId: ByteArray)
}