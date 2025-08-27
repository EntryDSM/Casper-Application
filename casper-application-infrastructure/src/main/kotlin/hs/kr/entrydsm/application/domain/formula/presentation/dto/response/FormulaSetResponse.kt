package hs.kr.entrydsm.application.domain.formula.presentation.dto.response

import hs.kr.entrydsm.domain.formula.entities.Formula
import hs.kr.entrydsm.domain.formula.entities.FormulaSet

data class FormulaSetResponse(
    val id: String,
    val name: String,
    val type: String,
    val formulas: List<FormulaResponse>,
    val description: String? = null,
    val isActive: Boolean
) {
    companion object {
        fun from(formulaSet: FormulaSet): FormulaSetResponse {
            return FormulaSetResponse(
                id = formulaSet.id.value,
                name = formulaSet.name,
                type = formulaSet.type.name,
                formulas = formulaSet.formulas.map { FormulaResponse.from(it) },
                description = formulaSet.description,
                isActive = formulaSet.isActive
            )
        }
    }
}

data class FormulaResponse(
    val id: String,
    val name: String,
    val expression: String,
    val order: Int,
    val resultVariable: String? = null,
    val description: String? = null
) {
    companion object {
        fun from(formula: Formula): FormulaResponse {
            return FormulaResponse(
                id = formula.id,
                name = formula.name,
                expression = formula.expression,
                order = formula.order,
                resultVariable = formula.resultVariable,
                description = formula.description
            )
        }
    }
}