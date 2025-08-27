package hs.kr.entrydsm.application.domain.formula.domain.repository

import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FormulaJpaRepository : JpaRepository<FormulaJpaEntity, String> {
    
    fun findByFormulaSetIdOrderByOrderNumber(formulaSetId: String): List<FormulaJpaEntity>
    
    @Query("SELECT f FROM FormulaJpaEntity f WHERE f.formulaSetId = :formulaSetId ORDER BY f.orderNumber")
    fun findFormulasInSet(formulaSetId: String): List<FormulaJpaEntity>
    
    fun existsByFormulaSetIdAndOrderNumber(formulaSetId: String, orderNumber: Int): Boolean
}