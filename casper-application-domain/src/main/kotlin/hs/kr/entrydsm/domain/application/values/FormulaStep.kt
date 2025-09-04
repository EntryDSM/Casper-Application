package hs.kr.entrydsm.domain.application.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException

data class FormulaStep(
    val step: Int,
    val name: String,
    val expression: String,
    val resultVariable: String
) {
    init {
        if (step <= 0) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (name.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (expression.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (resultVariable.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    companion object {
        fun create(
            step: Int,
            name: String,
            expression: String,
            resultVariable: String
        ): FormulaStep {
            return FormulaStep(
                step = step,
                name = name,
                expression = expression,
                resultVariable = resultVariable
            )
        }
    }
}