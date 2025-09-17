package hs.kr.entrydsm.domain.formula.entities

import hs.kr.entrydsm.domain.formula.values.FormulaExecutionId
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.EntityBase
import java.time.LocalDateTime
import java.util.UUID

/**
 * 수식 실행 이력 엔티티
 * 
 * 수식 집합의 실행 결과와 중간 변수들을 저장
 */
@Entity(aggregateRoot = FormulaExecution::class, context = "formula")
data class FormulaExecution(
    private val executionId: FormulaExecutionId,
    val formulaSetId: FormulaSetId,
    val inputVariables: Map<String, Double>,
    val executionSteps: List<ExecutionStep>,
    val finalResult: Double,
    val executedAt: LocalDateTime = LocalDateTime.now(),
    val status: ExecutionStatus = ExecutionStatus.SUCCESS
) : EntityBase<FormulaExecutionId>() {

    val id: FormulaExecutionId
        @JvmName("getExecutionId")
        get() = executionId

    init {
        if (executionSteps.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (inputVariables.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    override fun getId(): FormulaExecutionId = executionId
    override fun getType(): String = "FormulaExecution"
    override fun isValid(): Boolean = executionSteps.isNotEmpty() && inputVariables.isNotEmpty()
    
    /**
     * 특정 변수의 결과값 조회
     */
    fun getResult(variableName: String): Double? {
        return when (variableName) {
            "final_score" -> finalResult
            else -> executionSteps.find { it.resultVariableName == variableName }?.resultValue
        }
    }
    
    /**
     * 모든 실행 결과 조회
     */
    fun getAllResults(): Map<String, Double> {
        return buildMap {
            // 모든 실행 단계의 결과값
            executionSteps.forEach { step ->
                put(step.resultVariableName, step.resultValue)
            }
            // 최종 결과값
            put("final_score", finalResult)
        }
    }
    
    /**
     * 입력 변수 조회
     */
    fun getInputVariable(variableName: String): Double? {
        return inputVariables[variableName]
    }
    
    /**
     * 실행 단계별 결과 조회
     */
    fun getStepResult(stepOrder: Int): ExecutionStep? {
        return executionSteps.find { it.stepOrder == stepOrder }
    }

    companion object {
        fun create(
            formulaSetId: FormulaSetId,
            inputVariables: Map<String, Double>,
            executionSteps: List<ExecutionStep>,
            finalResult: Double
        ): FormulaExecution {
            return FormulaExecution(
                executionId = FormulaExecutionId.generate(),
                formulaSetId = formulaSetId,
                inputVariables = inputVariables,
                executionSteps = executionSteps,
                finalResult = finalResult
            )
        }
    }
}

/**
 * 개별 수식 실행 단계
 */
data class ExecutionStep(
    val stepOrder: Int,
    val formulaId: String,
    val formulaExpression: String,
    val resultVariableName: String,
    val resultValue: Double,
    val executedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 실행 상태
 */
enum class ExecutionStatus {
    SUCCESS,
    FAILED,
    PARTIAL
}