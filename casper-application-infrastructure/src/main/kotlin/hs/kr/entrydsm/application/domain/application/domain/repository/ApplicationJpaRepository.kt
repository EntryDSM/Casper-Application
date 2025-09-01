package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ApplicationJpaRepository : JpaRepository<ApplicationJpaEntity, UUID> {
    
    fun findAllByUserId(userId: UUID): List<ApplicationJpaEntity>
    
    @Query("SELECT a FROM ApplicationJpaEntity a WHERE a.applicationType = :applicationType AND a.educationalStatus = :educationalStatus")
    fun findByApplicationTypeAndEducationalStatus(
        @Param("applicationType") applicationType: String,
        @Param("educationalStatus") educationalStatus: String
    ): List<ApplicationJpaEntity>
    
    @Query("SELECT a FROM ApplicationJpaEntity a WHERE a.receiptCode = :receiptCode")
    fun findByReceiptCode(@Param("receiptCode") receiptCode: Long): ApplicationJpaEntity?
    
    @Query("SELECT COALESCE(MAX(a.receiptCode), 0) FROM ApplicationJpaEntity a")
    fun findMaxReceiptCode(): Long
}