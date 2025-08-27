package hs.kr.entrydsm.application.domain.formula.domain

import hs.kr.entrydsm.application.domain.formula.domain.entity.enum.FormulaTypeEnum
import hs.kr.entrydsm.application.domain.formula.domain.mapper.FormulaExecutionMapper
import hs.kr.entrydsm.application.domain.formula.domain.mapper.FormulaSetMapper
import hs.kr.entrydsm.application.domain.formula.domain.repository.FormulaExecutionJpaRepository
import hs.kr.entrydsm.application.domain.formula.domain.repository.FormulaSetJpaRepository
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.formula.entities.ExecutionStep
import hs.kr.entrydsm.domain.formula.entities.FormulaExecution
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.spi.FormulaPort
import hs.kr.entrydsm.domain.formula.values.FormulaExecutionId
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Formula 도메인을 위한 Persistence Adapter
 * 
 * Port-Adapter 패턴의 Adapter 구현체
 * equus-application의 Adapter 패턴을 따름
 */
@Component
class FormulaPersistenceAdapter(
    private val formulaSetJpaRepository: FormulaSetJpaRepository,
    private val formulaExecutionJpaRepository: FormulaExecutionJpaRepository,
    private val formulaSetMapper: FormulaSetMapper,
    private val formulaExecutionMapper: FormulaExecutionMapper,
    private val entityManager: EntityManager,
    private val calculator: Calculator
) : FormulaPort {
    
    @Transactional
    override fun save(formulaSet: FormulaSet): FormulaSet {
        val entity = formulaSetMapper.toEntity(formulaSet)
        val savedEntity = formulaSetJpaRepository.save(entity)
        
        entityManager.flush()

        val entityWithFormulas = formulaSetJpaRepository.findByIdWithFormulas(savedEntity.id)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        return formulaSetMapper.toDomainNotNull(entityWithFormulas)
    }
    
    override fun findById(id: FormulaSetId): FormulaSet? {
        val entity = formulaSetJpaRepository.findByIdWithFormulas(id.value)
        return formulaSetMapper.toDomain(entity)
    }
    
    override fun findByType(type: FormulaType): List<FormulaSet> {
        val typeEnum = FormulaTypeEnum.valueOf(type.name)
        val entities = formulaSetJpaRepository.findByTypeWithFormulas(typeEnum)
        return entities.mapNotNull { formulaSetMapper.toDomain(it) }
    }
    
    override fun findAll(): List<FormulaSet> {
        val entities = formulaSetJpaRepository.findByIsActiveTrue()
        return entities.mapNotNull { formulaSetMapper.toDomain(it) }
    }
    
    override fun delete(id: FormulaSetId) {
        formulaSetJpaRepository.deleteById(id.value)
    }
    
    override fun existsById(id: FormulaSetId): Boolean {
        return formulaSetJpaRepository.existsById(id.value)
    }
    
    @Transactional
    override fun saveExecution(execution: FormulaExecution): FormulaExecution {
        val entity = formulaExecutionMapper.toEntity(execution)
        val savedEntity = formulaExecutionJpaRepository.save(entity)
        
        entityManager.flush()
        
        val entityWithSteps = formulaExecutionJpaRepository.findByIdWithSteps(savedEntity.id)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        return formulaExecutionMapper.toDomainNotNull(entityWithSteps)
    }
    
    override fun findExecutionById(id: FormulaExecutionId): FormulaExecution? {
        val entity = formulaExecutionJpaRepository.findByIdWithSteps(id.value)
        return formulaExecutionMapper.toDomain(entity)
    }
    
    override fun findExecutionsByFormulaSetId(formulaSetId: FormulaSetId): List<FormulaExecution> {
        val entities = formulaExecutionJpaRepository.findByFormulaSetIdWithSteps(formulaSetId.value)
        return entities.mapNotNull { formulaExecutionMapper.toDomain(it) }
    }
    
    override fun findByApplicationCriteria(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        isDaejeon: Boolean
    ): FormulaSet? {
        val entity = formulaSetJpaRepository.findByApplicationCriteriaWithFormulas(
            applicationType = applicationType.name,
            educationalStatus = educationalStatus.name,
            isDaejeon = isDaejeon
        )
        return formulaSetMapper.toDomain(entity)
    }
    
    @Transactional
    override fun executeFormulas(
        formulaSetId: FormulaSetId,
        executionId: FormulaExecutionId,
        variables: Map<String, Any>
    ): FormulaExecution? {
        val formulaSet = findById(formulaSetId) 
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        
        val doubleVariables = variables.mapValues { (_, value) ->
            when (value) {
                is Number -> value.toDouble()
                else -> throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
            }
        }
        
        val currentVariables = doubleVariables.toMutableMap()
        val executionSteps = mutableListOf<ExecutionStep>()
        val executionTime = LocalDateTime.now()
        
        formulaSet.formulas.sortedBy { it.order }.forEachIndexed { index, formula ->
            val variableName = formula.resultVariable 
                ?: throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
            
            val request = CalculationRequest(
                formula = formula.expression, 
                variables = currentVariables.mapValues { it.value as Any }
            )
            val result = calculator.calculate(request)
            
            if (!result.isSuccess()) {
                throw DomainException(ErrorCode.BUSINESS_RULE_VIOLATION)
            }
            
            val stepValue = result.asDouble() ?: 0.0
            currentVariables[variableName] = stepValue
            
            val executionStep = ExecutionStep(
                stepOrder = index + 1,
                formulaId = formula.id,
                formulaExpression = formula.expression,
                resultVariableName = variableName,
                resultValue = stepValue,
                executedAt = executionTime
            )
            executionSteps.add(executionStep)
        }
        
        val finalResult = executionSteps.lastOrNull()?.resultValue ?: 0.0
        
        val execution = FormulaExecution.create(
            formulaSetId = formulaSetId,
            inputVariables = doubleVariables,
            executionSteps = executionSteps,
            finalResult = finalResult
        )
        
        return saveExecution(execution)
    }
}