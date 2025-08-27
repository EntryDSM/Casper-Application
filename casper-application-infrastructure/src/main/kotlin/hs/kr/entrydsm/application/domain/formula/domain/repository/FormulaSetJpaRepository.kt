package hs.kr.entrydsm.application.domain.formula.domain.repository

import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaSetJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.enum.FormulaTypeEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FormulaSetJpaRepository : JpaRepository<FormulaSetJpaEntity, String> {
    
    fun findByTypeAndIsActiveTrue(type: FormulaTypeEnum): List<FormulaSetJpaEntity>
    
    fun findByIsActiveTrue(): List<FormulaSetJpaEntity>
    
    @Query("SELECT fs FROM FormulaSetJpaEntity fs LEFT JOIN FETCH fs.formulas WHERE fs.id = :id")
    fun findByIdWithFormulas(id: String): FormulaSetJpaEntity?
    
    @Query("SELECT fs FROM FormulaSetJpaEntity fs LEFT JOIN FETCH fs.formulas WHERE fs.type = :type AND fs.isActive = true")
    fun findByTypeWithFormulas(type: FormulaTypeEnum): List<FormulaSetJpaEntity>
    
    @Query("SELECT fs FROM FormulaSetJpaEntity fs LEFT JOIN FETCH fs.formulas WHERE fs.applicationType = :applicationType AND fs.educationalStatus = :educationalStatus AND fs.isDaejeon = :isDaejeon AND fs.isActive = true")
    fun findByApplicationCriteriaWithFormulas(applicationType: String, educationalStatus: String, isDaejeon: Boolean): FormulaSetJpaEntity?
}