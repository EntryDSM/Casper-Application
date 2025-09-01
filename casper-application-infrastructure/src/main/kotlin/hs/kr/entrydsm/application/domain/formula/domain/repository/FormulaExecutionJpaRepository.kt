package hs.kr.entrydsm.application.domain.formula.domain.repository

import hs.kr.entrydsm.application.domain.formula.domain.entity.ExecutionStatusEnum
import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaExecutionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface FormulaExecutionJpaRepository : JpaRepository<FormulaExecutionJpaEntity, UUID> {
    
    fun findAllByFormulaSetId(formulaSetId: UUID): List<FormulaExecutionJpaEntity>
    
    fun findAllByFormulaSetIdOrderByExecutedAtDesc(formulaSetId: UUID): List<FormulaExecutionJpaEntity>
    
    fun findAllByStatus(status: ExecutionStatusEnum): List<FormulaExecutionJpaEntity>
    
    @Query("SELECT f FROM FormulaExecutionJpaEntity f WHERE f.formulaSetId = :formulaSetId AND f.status = :status ORDER BY f.executedAt DESC")
    fun findByFormulaSetIdAndStatus(
        @Param("formulaSetId") formulaSetId: UUID,
        @Param("status") status: ExecutionStatusEnum
    ): List<FormulaExecutionJpaEntity>
    
    @Query("SELECT f FROM FormulaExecutionJpaEntity f WHERE f.executedAt BETWEEN :startDate AND :endDate ORDER BY f.executedAt DESC")
    fun findByExecutedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<FormulaExecutionJpaEntity>
    
    fun countByFormulaSetId(formulaSetId: UUID): Long
    
    fun countByStatus(status: ExecutionStatusEnum): Long
}