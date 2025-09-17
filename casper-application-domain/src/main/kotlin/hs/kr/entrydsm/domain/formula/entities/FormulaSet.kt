package hs.kr.entrydsm.domain.formula.entities


import hs.kr.entrydsm.domain.formula.values.FormulaType
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.global.interfaces.AggregateRoot as AggregateRootInterface

/**
 * 수식 집합 애그리게이트 루트
 * 
 * 다중 단계 계산을 위한 수식들의 집합을 관리
 * Calculator.calculateMultiStep() 메서드와 연동
 */
data class FormulaSet(
    private val formulaSetId: FormulaSetId,
    val name: String,
    val type: FormulaType,
    val formulas: List<Formula>,
    val description: String? = null,
    val isActive: Boolean = true,
    val applicationType: String? = null,
    val educationalStatus: String? = null,
    val isDaejeon: Boolean? = null
) : AggregateRootInterface<FormulaSetId>() {

    val id: FormulaSetId 
        @JvmName("getFormulaSetId")
        get() = formulaSetId

    init {
        if (name.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (formulas.isEmpty()) throw DomainException(ErrorCode.BUSINESS_RULE_VIOLATION)
        validateFormulaOrder()
        validateResultVariables()
    }

    private fun validateFormulaOrder() {
        val orders = formulas.map { it.order }.sorted()
        if (orders != (1..orders.size).toList()) {
            throw DomainException(ErrorCode.BUSINESS_RULE_VIOLATION) as Throwable
        }
    }

    private fun validateResultVariables() {
        val resultVariables = formulas.mapNotNull { it.resultVariable }
        val duplicateVariables = resultVariables.groupBy { it }
            .filter { it.value.size > 1 }
            .keys
        
        if (duplicateVariables.isNotEmpty()) {
            throw DomainException(ErrorCode.BUSINESS_RULE_VIOLATION)
        }
        
        val formulasWithoutVariable = formulas.filter { it.resultVariable == null }
        if (formulasWithoutVariable.isNotEmpty()) {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
    }

    override fun getId(): FormulaSetId = formulaSetId
    override fun getType(): String = "FormulaSet"
    override fun checkInvariants(): Boolean = name.isNotBlank() && formulas.isNotEmpty()


    fun addFormula(formula: Formula): FormulaSet {
        val newFormulas = formulas + formula
        return copy(formulas = newFormulas)
    }

    fun removeFormula(formulaId: String): FormulaSet {
        val newFormulas = formulas.filter { it.id != formulaId }
        if (newFormulas.isEmpty()) {
            throw DomainException(ErrorCode.BUSINESS_RULE_VIOLATION)
        }
        return copy(formulas = newFormulas)
    }

    fun updateFormula(formulaId: String, updatedFormula: Formula): FormulaSet {
        val newFormulas = formulas.map { 
            if (it.id == formulaId) updatedFormula else it 
        }
        return copy(formulas = newFormulas)
    }

    companion object {
        fun create(
            name: String,
            type: FormulaType,
            formulas: List<Formula>,
            description: String? = null
        ): FormulaSet {
            return FormulaSet(
                formulaSetId = FormulaSetId.generate(),
                name = name,
                type = type,
                formulas = formulas,
                description = description
            )
        }
    }
}