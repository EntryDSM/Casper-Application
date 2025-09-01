package hs.kr.entrydsm.domain.formula.spi

import hs.kr.entrydsm.domain.formula.entities.FormulaExecution
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.values.FormulaExecutionId
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType

/**
 * Formula 도메인을 위한 SPI (Service Provider Interface)
 * 
 * Port-Adapter 패턴의 Port 역할
 * Infrastructure 계층에서 구현됨
 */
interface FormulaPort {
    
    fun save(formulaSet: FormulaSet): FormulaSet
    
    fun findById(id: FormulaSetId): FormulaSet?
    
    fun findByType(type: FormulaType): List<FormulaSet>
    
    fun findAll(): List<FormulaSet>
    
    fun delete(id: FormulaSetId)
    
    fun existsById(id: FormulaSetId): Boolean
    
    fun saveExecution(execution: FormulaExecution): FormulaExecution
    
    fun findExecutionById(id: FormulaExecutionId): FormulaExecution?
    
    fun findExecutionsByFormulaSetId(formulaSetId: FormulaSetId): List<FormulaExecution>
    
    /**
     * Application 조건에 맞는 FormulaSet 찾기
     */
    fun findByApplicationCriteria(
        applicationType: String,
        educationalStatus: String,
        isDaejeon: Boolean
    ): FormulaSet?
    
    /**
     * 수식 실행
     */
    fun executeFormulas(
        formulaSetId: FormulaSetId,
        executionId: FormulaExecutionId,
        variables: Map<String, Any>
    ): FormulaExecution?
}