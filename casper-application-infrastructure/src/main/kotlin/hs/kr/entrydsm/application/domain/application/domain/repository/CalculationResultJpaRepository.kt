package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.CalculationResultJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CalculationResultJpaRepository : JpaRepository<CalculationResultJpaEntity, UUID> {
    
    fun findAllByApplicationIdOrderByExecutedAtDesc(applicationId: UUID): List<CalculationResultJpaEntity>
    
    @Query("SELECT c FROM CalculationResultJpaEntity c WHERE c.applicationId = :applicationId ORDER BY c.executedAt DESC LIMIT 1")
    fun findLatestByApplicationId(@Param("applicationId") applicationId: UUID): CalculationResultJpaEntity?
}