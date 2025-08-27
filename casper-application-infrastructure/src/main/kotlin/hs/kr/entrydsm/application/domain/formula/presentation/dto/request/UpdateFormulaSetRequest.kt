package hs.kr.entrydsm.application.domain.formula.presentation.dto.request

import hs.kr.entrydsm.domain.formula.entities.Formula
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType

data class UpdateFormulaSetRequest(
    val name: String,
    val type: String,
    val formulas: List<FormulaRequest>,
    val description: String? = null,
    val isActive: Boolean = true
) {
    fun toDomain(id: FormulaSetId): FormulaSet {
        val domainFormulas = formulas.map { 
            Formula.create(
                name = it.name,
                expression = it.expression,
                order = it.order,
                description = it.description
            )
        }
        
        return FormulaSet(
            formulaSetId = id,
            name = name,
            type = FormulaType.valueOf(type),
            formulas = domainFormulas,
            description = description,
            isActive = isActive
        )
    }
}