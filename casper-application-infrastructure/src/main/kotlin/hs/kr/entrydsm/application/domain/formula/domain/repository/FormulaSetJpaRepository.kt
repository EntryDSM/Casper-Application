package hs.kr.entrydsm.application.domain.formula.domain.repository

import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaSetJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.enums.FormulaSetStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FormulaSetJpaRepository : JpaRepository<FormulaSetJpaEntity, UUID> {
    
    fun findAllByStatus(status: FormulaSetStatus): List<FormulaSetJpaEntity>
    
    @Query("SELECT f FROM FormulaSetJpaEntity f WHERE f.applicationType = :applicationType AND f.educationalStatus = :educationalStatus AND (:region IS NULL OR f.region = :region) AND f.status = 'ACTIVE'")
    fun findByApplicationCriteria(
        @Param("applicationType") applicationType: String,
        @Param("educationalStatus") educationalStatus: String,
        @Param("region") region: String?
    ): FormulaSetJpaEntity?
    
    fun findAllByApplicationTypeAndEducationalStatusAndStatus(
        applicationType: String,
        educationalStatus: String,
        status: FormulaSetStatus
    ): List<FormulaSetJpaEntity>
}