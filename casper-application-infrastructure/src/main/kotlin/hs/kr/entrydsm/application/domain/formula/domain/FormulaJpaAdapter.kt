package hs.kr.entrydsm.application.domain.formula.domain

import hs.kr.entrydsm.application.domain.formula.domain.entity.ExecutionStatusEnum
import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaExecutionJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaSetJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.enums.FormulaSetStatus
import hs.kr.entrydsm.application.domain.formula.domain.repository.FormulaExecutionJpaRepository
import hs.kr.entrydsm.application.domain.formula.domain.repository.FormulaSetJpaRepository
import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.formula.entities.*
import hs.kr.entrydsm.domain.formula.spi.FormulaPort
import hs.kr.entrydsm.domain.formula.values.FormulaExecutionId
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Component
class FormulaJpaAdapter(
    private val formulaSetRepository: FormulaSetJpaRepository,
    private val formulaExecutionRepository: FormulaExecutionJpaRepository,
    private val objectMapper: ObjectMapper,
    private val calculator: Calculator
) : FormulaPort {
    
    override fun save(formulaSet: FormulaSet): FormulaSet {
        val formulasJson = objectMapper.writeValueAsString(formulaSet.formulas.map { formula ->
            mapOf(
                "formulaId" to formula.id,
                "name" to formula.name,
                "expression" to formula.expression,
                "order" to formula.order,
                "resultVariable" to formula.resultVariable,
                "description" to formula.description
            )
        })
        
        val constantsJson = "{}"
        
        val entity = FormulaSetJpaEntity(
            formulaSetId = UUID.fromString(formulaSet.id.value),
            name = formulaSet.name,
            description = formulaSet.description,
            applicationType = formulaSet.applicationType ?: "",
            educationalStatus = formulaSet.educationalStatus ?: "",
            region = if (formulaSet.isDaejeon == true) "DAEJEON" else null,
            formulas = formulasJson,
            constants = constantsJson,
            status = if (formulaSet.isActive) FormulaSetStatus.ACTIVE else FormulaSetStatus.INACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedEntity = formulaSetRepository.save(entity)
        return mapToDomain(savedEntity)
    }
    
    override fun findById(id: FormulaSetId): FormulaSet? {
        val entity = formulaSetRepository.findById(UUID.fromString(id.value))
            .orElse(null) ?: return null
            
        return mapToDomain(entity)
    }
    
    override fun findByType(type: FormulaType): List<FormulaSet> {
        val entities = formulaSetRepository.findAllByStatus(FormulaSetStatus.ACTIVE)
        return entities.map { mapToDomain(it) }.filter { it.type == type }
    }
    
    override fun findAll(): List<FormulaSet> {
        val entities = formulaSetRepository.findAllByStatus(FormulaSetStatus.ACTIVE)
        return entities.map { mapToDomain(it) }
    }
    
    override fun delete(id: FormulaSetId) {
        formulaSetRepository.deleteById(UUID.fromString(id.value))
    }
    
    override fun existsById(id: FormulaSetId): Boolean {
        return formulaSetRepository.existsById(UUID.fromString(id.value))
    }
    
    override fun saveExecution(execution: FormulaExecution): FormulaExecution {
        val executionStepsJson = objectMapper.writeValueAsString(execution.executionSteps.map { step ->
            mapOf(
                "stepOrder" to step.stepOrder,
                "formulaId" to step.formulaId,
                "formulaExpression" to step.formulaExpression,
                "resultVariableName" to step.resultVariableName,
                "resultValue" to step.resultValue,
                "executedAt" to step.executedAt.toString()
            )
        })
        
        val inputVariablesJson = objectMapper.writeValueAsString(execution.inputVariables)
        
        val entity = FormulaExecutionJpaEntity(
            executionId = UUID.fromString(execution.id.value),
            formulaSetId = UUID.fromString(execution.formulaSetId.value),
            inputVariables = inputVariablesJson,
            executionSteps = executionStepsJson,
            finalResult = BigDecimal.valueOf(execution.finalResult),
            executedAt = execution.executedAt,
            status = when (execution.status) {
                ExecutionStatus.SUCCESS -> ExecutionStatusEnum.SUCCESS
                ExecutionStatus.FAILED -> ExecutionStatusEnum.FAILED
                ExecutionStatus.PARTIAL -> ExecutionStatusEnum.PARTIAL
            }
        )
        
        val savedEntity = formulaExecutionRepository.save(entity)
        return mapExecutionToDomain(savedEntity)
    }
    
    override fun findExecutionById(id: FormulaExecutionId): FormulaExecution? {
        val entity = formulaExecutionRepository.findById(UUID.fromString(id.value))
            .orElse(null) ?: return null
            
        return mapExecutionToDomain(entity)
    }
    
    override fun findExecutionsByFormulaSetId(formulaSetId: FormulaSetId): List<FormulaExecution> {
        val entities = formulaExecutionRepository.findAllByFormulaSetIdOrderByExecutedAtDesc(
            UUID.fromString(formulaSetId.value)
        )
        return entities.map { mapExecutionToDomain(it) }
    }
    
    override fun findByApplicationCriteria(
        applicationType: String,
        educationalStatus: String,
        isDaejeon: Boolean
    ): FormulaSet? {
        val region = if (isDaejeon) "DAEJEON" else null
        val entity = formulaSetRepository.findByApplicationCriteria(
            applicationType = applicationType,
            educationalStatus = educationalStatus,
            region = region
        )
        
        return entity?.let { mapToDomain(it) }
    }
    
    override fun executeFormulas(
        formulaSetId: FormulaSetId,
        executionId: FormulaExecutionId,
        variables: Map<String, Any>
    ): FormulaExecution? {
        val formulaSet = findById(formulaSetId) ?: return null
        
        val executionSteps = mutableListOf<ExecutionStep>()
        var currentVariables = variables.toMutableMap()
        var finalResult = 0.0
        
        try {
            // 각 수식을 순차적으로 실행
            formulaSet.formulas.sortedBy { it.order }.forEach { formula ->
                val calculationRequest = CalculationRequest(
                    formula = formula.expression,
                    variables = currentVariables
                )
                
                val result = calculator.calculate(calculationRequest)
                
                if (result.isSuccess()) {
                    val stepResult = result.result?.toString()?.toDoubleOrNull() ?: 0.0
                    
                    // 실행 단계 기록
                    executionSteps.add(
                        ExecutionStep(
                            stepOrder = formula.order,
                            formulaId = formula.id,
                            formulaExpression = formula.expression,
                            resultVariableName = formula.resultVariable ?: "step_${formula.order}",
                            resultValue = stepResult,
                            executedAt = LocalDateTime.now()
                        )
                    )
                    
                    // 다음 단계를 위해 변수에 추가
                    formula.resultVariable?.let { resultVar ->
                        if (resultVar.isNotBlank()) {
                            currentVariables[resultVar] = stepResult
                        }
                    }
                    
                    finalResult = stepResult // 마지막 결과를 최종 결과로 사용
                } else {




                    throw RuntimeException("수식 실행 실패: ${formula.expression}")
                }
            }
            
            // 실행 결과 생성 및 저장
            val execution = FormulaExecution.create(
                formulaSetId = formulaSetId,
                inputVariables = variables.mapValues { (_, value) ->
                    when (value) {
                        is Number -> value.toDouble()
                        is String -> value.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                },
                executionSteps = executionSteps,
                finalResult = finalResult
            )
            
            return saveExecution(execution)
            
        } catch (e: Exception) {
            // 실패한 경우 실패 상태로 기록
            val failedExecution = FormulaExecution(
                executionId = executionId,
                formulaSetId = formulaSetId,
                inputVariables = variables.mapValues { (_, value) ->
                    when (value) {
                        is Number -> value.toDouble()
                        is String -> value.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                },
                executionSteps = executionSteps,
                finalResult = 0.0,
                status = ExecutionStatus.FAILED
            )
            
            return saveExecution(failedExecution)
        }
    }
    
    private fun mapToDomain(entity: FormulaSetJpaEntity): FormulaSet {
        val formulasList: List<Map<String, Any>> = try {
            objectMapper.readValue(entity.formulas)
        } catch (e: Exception) {
            emptyList()
        }
        
        val formulas = formulasList.map { formulaMap ->
            Formula(
                formulaId = formulaMap["formulaId"] as? String ?: UUID.randomUUID().toString(),
                name = formulaMap["name"] as String,
                expression = formulaMap["expression"] as String,
                order = (formulaMap["order"] as Number).toInt(),
                resultVariable = formulaMap["resultVariable"] as? String,
                description = formulaMap["description"] as? String
            )
        }
        
        return FormulaSet(
            formulaSetId = FormulaSetId(entity.formulaSetId.toString()),
            name = entity.name,
            type = FormulaType.TOTAL_SCORE,
            formulas = formulas,
            description = entity.description,
            isActive = entity.status == FormulaSetStatus.ACTIVE,
            applicationType = entity.applicationType.takeIf { it.isNotBlank() },
            educationalStatus = entity.educationalStatus.takeIf { it.isNotBlank() },
            isDaejeon = entity.region == "DAEJEON"
        )
    }
    
    private fun mapExecutionToDomain(entity: FormulaExecutionJpaEntity): FormulaExecution {
        val executionStepsList: List<Map<String, Any>> = try {
            objectMapper.readValue(entity.executionSteps)
        } catch (e: Exception) {
            emptyList()
        }
        
        val inputVariables: Map<String, Double> = try {
            objectMapper.readValue(entity.inputVariables)
        } catch (e: Exception) {
            emptyMap()
        }
        
        val executionSteps = executionStepsList.map { stepMap ->
            ExecutionStep(
                stepOrder = (stepMap["stepOrder"] as Number).toInt(),
                formulaId = stepMap["formulaId"] as String,
                formulaExpression = stepMap["formulaExpression"] as String,
                resultVariableName = stepMap["resultVariableName"] as String,
                resultValue = (stepMap["resultValue"] as Number).toDouble(),
                executedAt = LocalDateTime.parse(stepMap["executedAt"] as String)
            )
        }
        
        return FormulaExecution(
            executionId = FormulaExecutionId(entity.executionId.toString()),
            formulaSetId = FormulaSetId(entity.formulaSetId.toString()),
            inputVariables = inputVariables,
            executionSteps = executionSteps,
            finalResult = entity.finalResult.toDouble(),
            executedAt = entity.executedAt,
            status = when (entity.status) {
                ExecutionStatusEnum.SUCCESS -> ExecutionStatus.SUCCESS
                ExecutionStatusEnum.FAILED -> ExecutionStatus.FAILED
                ExecutionStatusEnum.PARTIAL -> ExecutionStatus.PARTIAL
            }
        )
    }
}