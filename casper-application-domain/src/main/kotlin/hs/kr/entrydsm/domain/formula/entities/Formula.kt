package hs.kr.entrydsm.domain.formula.entities

import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.EntityBase
import java.util.UUID

/**
 * 개별 수식 엔티티
 * 
 * 기존 Calculator.calculateMultiStep()에서 사용하는 수식 문자열을 
 * 메타데이터와 함께 관리하는 엔티티
 */
@Entity(aggregateRoot = FormulaSet::class, context = "formula")
data class Formula(
    private val formulaId: String,
    val name: String,
    val expression: String,
    val order: Int,
    val resultVariable: String? = null, 
    val description: String? = null
) : EntityBase<String>() {

    val id: String 
        @JvmName("getFormulaId")
        get() = formulaId

    init {
        if (formulaId.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (name.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (expression.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (order <= 0) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    override fun getId(): String = formulaId
    override fun getType(): String = "Formula"
    override fun isValid(): Boolean = formulaId.isNotBlank() && name.isNotBlank() && expression.isNotBlank() && order > 0

    companion object {
        fun create(
            name: String,
            expression: String,
            order: Int = 1,
            resultVariable: String? = null,
            description: String? = null
        ): Formula {
            return Formula(
                formulaId = UUID.randomUUID().toString(),
                name = name,
                expression = expression,
                order = order,
                resultVariable = resultVariable,
                description = description
            )
        }
    }
}