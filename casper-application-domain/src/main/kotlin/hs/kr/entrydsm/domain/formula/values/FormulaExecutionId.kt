package hs.kr.entrydsm.domain.formula.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.ValueObject
import java.util.UUID

/**
 * 수식 실행 ID 값 객체
 */
data class FormulaExecutionId(
    val value: String
) : ValueObject {
    
    init {
        if (value.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    override fun getId(): String = value
    override fun getType(): String = "FormulaExecutionId"
    override fun isValid(): Boolean = value.isNotBlank()

    companion object {
        fun generate(): FormulaExecutionId = FormulaExecutionId(UUID.randomUUID().toString())
        
        fun from(value: String): FormulaExecutionId = FormulaExecutionId(value)
    }
}