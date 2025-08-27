package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ApplicationJpaRepository : JpaRepository<ApplicationJpaEntity, Long> {
    
    fun findByUserId(userId: ByteArray): ApplicationJpaEntity?
    
    fun existsByUserId(userId: ByteArray): Boolean
    
    fun deleteByUserId(userId: ByteArray)
    
    @Query("SELECT a FROM ApplicationJpaEntity a ORDER BY a.receiptCode DESC")
    fun findAllOrderByReceiptCodeDesc(): List<ApplicationJpaEntity>
}