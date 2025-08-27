package hs.kr.entrydsm.application.domain.formula.domain.mapper

import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaSetJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.enum.FormulaTypeEnum
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import hs.kr.entrydsm.domain.formula.entities.Formula
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import org.springframework.stereotype.Component

@Component
class FormulaSetMapper : GenericMapper<FormulaSetJpaEntity, FormulaSet> {
    
    override fun toDomain(entity: FormulaSetJpaEntity?): FormulaSet? {
        if (entity == null) return null
        return FormulaSet(
            formulaSetId = FormulaSetId(entity.id),
            name = entity.name,
            type = FormulaType.valueOf(entity.type.name),
            formulas = entity.formulas.map { mapFormula(it) },
            description = entity.description,
            isActive = entity.isActive
        )
    }
    
    override fun toDomainNotNull(entity: FormulaSetJpaEntity): FormulaSet {
        println("DEBUG: entity.formulas.size = ${entity.formulas.size}")
        entity.formulas.forEach { println("DEBUG: formula = ${it.id}, ${it.name}") }
        
        return FormulaSet(
            formulaSetId = FormulaSetId(entity.id),
            name = entity.name,
            type = FormulaType.valueOf(entity.type.name),
            formulas = entity.formulas.map { mapFormula(it) },
            description = entity.description,
            isActive = entity.isActive
        )
    }
    
    override fun toEntity(model: FormulaSet): FormulaSetJpaEntity {
        val entity = FormulaSetJpaEntity(
            id = model.id.value,
            name = model.name,
            type = FormulaTypeEnum.valueOf(model.type.name),
            description = model.description,
            isActive = model.isActive,
            formulas = model.formulas.map { formula ->
                FormulaJpaEntity(
                    id = formula.id,
                    formulaSetId = model.id.value,
                    name = formula.name,
                    expression = formula.expression,
                    orderNumber = formula.order,
                    resultVariable = formula.resultVariable,
                    description = formula.description,
                    formulaSet = null
                )
            }
        )
        return entity
    }
    
    fun mapFormula(entity: FormulaJpaEntity): Formula {
        return Formula(
            formulaId = entity.id,
            name = entity.name,
            expression = entity.expression,
            order = entity.orderNumber,
            resultVariable = entity.resultVariable,
            description = entity.description
        )
    }
}