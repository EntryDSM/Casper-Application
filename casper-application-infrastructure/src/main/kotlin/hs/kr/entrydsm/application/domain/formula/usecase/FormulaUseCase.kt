package hs.kr.entrydsm.application.domain.formula.usecase

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.exceptions.CalculatorException
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.formula.entities.ExecutionStep
import hs.kr.entrydsm.domain.formula.entities.FormulaExecution
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.spi.FormulaPort
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Formula UseCase (Application Service)
 * 
 * 수식 집합 관리와 계산 로직을 연결하는 Application Service
 * 기존 Calculator.calculateMultiStep()와 연동
 */
@Service
class FormulaUseCase(
    private val formulaPort: FormulaPort,
    private val calculator: Calculator
) {
    fun createFormulaSet(formulaSet: FormulaSet): FormulaSet {
        return formulaPort.save(formulaSet)
    }
    
    fun getFormulaSet(id: FormulaSetId): FormulaSet {
        return formulaPort.findById(id) 
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
    }
    
    fun getFormulaSetsByType(type: FormulaType): List<FormulaSet> {
        return formulaPort.findByType(type)
    }
    
    fun getAllFormulaSets(): List<FormulaSet> {
        return formulaPort.findAll()
    }
    
    fun updateFormulaSet(formulaSet: FormulaSet): FormulaSet {
        if (!formulaPort.existsById(formulaSet.id)) {
            throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        }
        return formulaPort.save(formulaSet)
    }
    
    fun deleteFormulaSet(id: FormulaSetId) {
        if (!formulaPort.existsById(id)) {
            throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        }
        formulaPort.delete(id)
    }
    
    /**
     * 수식 집합을 사용하여 다단계 계산 수행 및 결과 저장
     * 트랜잭션을 통한 원자성 보장
     */
    @Transactional
    fun executeFormulas(
        formulaSetId: FormulaSetId, 
        variables: Map<String, Double>
    ): Map<String, Double> {
        val formulaSet = getFormulaSet(formulaSetId)
        
        var currentVariables = variables.mapValues { it.value as Any }.toMutableMap()
        val results = mutableListOf<CalculationResult>()
        val executionSteps = mutableListOf<ExecutionStep>()
        val executionTime = LocalDateTime.now()
        
        formulaSet.formulas.sortedBy { it.order }.forEachIndexed { index, formula ->
            try {
                val variableName = formula.resultVariable 
                    ?: throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
                
                val request = CalculationRequest(formula.expression, currentVariables)
                val result = calculator.calculate(request)
                results.add(result)
                
                currentVariables[variableName] = result.result ?: 0.0
                
                val executionStep = ExecutionStep(
                    stepOrder = index + 1,
                    formulaId = formula.id,
                    formulaExpression = formula.expression,
                    resultVariableName = variableName,
                    resultValue = result.result as? Double ?: 0.0,
                    executedAt = executionTime
                )
                executionSteps.add(executionStep)
                
            } catch (e: Exception) {
                throw CalculatorException.stepExecutionError(index + 1, e)
            }
        }
        
        val finalResult = results.lastOrNull()?.result as? Double ?: 0.0
        
        val formulaExecution = FormulaExecution.create(
            formulaSetId = formulaSetId,
            inputVariables = variables,
            executionSteps = executionSteps,
            finalResult = finalResult
        )
        
        formulaPort.saveExecution(formulaExecution)
        
        val resultMap = mutableMapOf<String, Double>()
        
        formulaSet.formulas.sortedBy { it.order }.forEachIndexed { index, formula ->
            val result = results[index]
            val variableName = formula.resultVariable!!
            resultMap[variableName] = result.result as? Double ?: 0.0
        }

        if (results.isNotEmpty()) {
            resultMap["result"] = finalResult
        }
        
        return resultMap
    }
}