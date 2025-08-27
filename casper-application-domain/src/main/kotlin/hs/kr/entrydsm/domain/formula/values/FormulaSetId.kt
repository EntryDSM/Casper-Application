package hs.kr.entrydsm.domain.formula.values

import hs.kr.entrydsm.global.interfaces.ValueObject
import java.util.UUID

/**
 * 수식 집합 식별자 값 객체
 */
data class FormulaSetId(
    val value: String
) : ValueObject {

    init {
        require(value.isNotBlank()) { "FormulaSetId cannot be blank" }
    }

    override fun getId(): String = value
    override fun getType(): String = "FormulaSetId"
    override fun isValid(): Boolean = value.isNotBlank()

    companion object {
        fun generate(): FormulaSetId = FormulaSetId(UUID.randomUUID().toString())
        fun from(value: String): FormulaSetId = FormulaSetId(value)
    }
}