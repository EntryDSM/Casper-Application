package hs.kr.entrydsm.application.domain.formula.domain.repository

import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaExecutionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FormulaExecutionJpaRepository : JpaRepository<FormulaExecutionJpaEntity, String> {
    
    @Query("SELECT e FROM FormulaExecutionJpaEntity e LEFT JOIN FETCH e.executionSteps WHERE e.formulaSetId = :formulaSetId ORDER BY e.executedAt DESC")
    fun findByFormulaSetIdWithSteps(@Param("formulaSetId") formulaSetId: String): List<FormulaExecutionJpaEntity>
    
    @Query("SELECT e FROM FormulaExecutionJpaEntity e LEFT JOIN FETCH e.executionSteps WHERE e.id = :id")
    fun findByIdWithSteps(@Param("id") id: String): FormulaExecutionJpaEntity?
}