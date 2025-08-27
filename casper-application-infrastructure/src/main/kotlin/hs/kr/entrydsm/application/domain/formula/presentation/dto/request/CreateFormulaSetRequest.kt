package hs.kr.entrydsm.application.domain.formula.presentation.dto.request

import hs.kr.entrydsm.domain.formula.entities.Formula
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.values.FormulaType

data class CreateFormulaSetRequest(
    val name: String,
    val type: String,
    val formulas: List<FormulaRequest>,
    val description: String? = null
) {
    fun toDomain(): FormulaSet {
        val domainFormulas = formulas.map { 
            Formula.create(
                name = it.name,
                expression = it.expression,
                order = it.order,
                resultVariable = it.resultVariable,
                description = it.description
            )
        }
        
        return FormulaSet.create(
            name = name,
            type = FormulaType.valueOf(type),
            formulas = domainFormulas,
            description = description
        )
    }
}

data class FormulaRequest(
    val name: String,
    val expression: String,
    val order: Int,
    val resultVariable: String? = null,  // 이 단계 결과를 저장할 변수명 (예: "grade_avg", "total_score")
    val description: String? = null
)