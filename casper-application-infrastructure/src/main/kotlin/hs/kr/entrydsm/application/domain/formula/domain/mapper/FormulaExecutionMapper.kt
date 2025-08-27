package hs.kr.entrydsm.application.domain.formula.domain.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hs.kr.entrydsm.application.domain.formula.domain.entity.ExecutionStepJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaExecutionJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.enum.ExecutionStatusEnum
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import hs.kr.entrydsm.domain.formula.entities.ExecutionStatus
import hs.kr.entrydsm.domain.formula.entities.ExecutionStep
import hs.kr.entrydsm.domain.formula.entities.FormulaExecution
import hs.kr.entrydsm.domain.formula.values.FormulaExecutionId
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import org.springframework.stereotype.Component

@Component
class FormulaExecutionMapper(
    private val objectMapper: ObjectMapper
) : GenericMapper<FormulaExecutionJpaEntity, FormulaExecution> {
    
    override fun toDomain(entity: FormulaExecutionJpaEntity?): FormulaExecution? {
        if (entity == null) return null
        return FormulaExecution(
            executionId = FormulaExecutionId(entity.id),
            formulaSetId = FormulaSetId(entity.formulaSetId),
            inputVariables = objectMapper.readValue<Map<String, Double>>(entity.inputVariables),
            executionSteps = entity.executionSteps.map { mapExecutionStep(it) },
            finalResult = entity.finalResult,
            executedAt = entity.executedAt,
            status = ExecutionStatus.valueOf(entity.status.name)
        )
    }
    
    override fun toDomainNotNull(entity: FormulaExecutionJpaEntity): FormulaExecution {
        return FormulaExecution(
            executionId = FormulaExecutionId(entity.id),
            formulaSetId = FormulaSetId(entity.formulaSetId),
            inputVariables = objectMapper.readValue<Map<String, Double>>(entity.inputVariables),
            executionSteps = entity.executionSteps.map { mapExecutionStep(it) },
            finalResult = entity.finalResult,
            executedAt = entity.executedAt,
            status = ExecutionStatus.valueOf(entity.status.name)
        )
    }
    
    override fun toEntity(model: FormulaExecution): FormulaExecutionJpaEntity {
        val entity = FormulaExecutionJpaEntity(
            id = model.id.value,
            formulaSetId = model.formulaSetId.value,
            inputVariables = objectMapper.writeValueAsString(model.inputVariables),
            finalResult = model.finalResult,
            executedAt = model.executedAt,
            status = ExecutionStatusEnum.valueOf(model.status.name),
            executionSteps = model.executionSteps.map { step ->
                ExecutionStepJpaEntity(
                    executionId = model.id.value,
                    stepOrder = step.stepOrder,
                    formulaId = step.formulaId,
                    formulaExpression = step.formulaExpression,
                    resultVariableName = step.resultVariableName,
                    resultValue = step.resultValue,
                    executedAt = step.executedAt,
                    execution = null
                )
            }
        )
        return entity
    }
    
    private fun mapExecutionStep(entity: ExecutionStepJpaEntity): ExecutionStep {
        return ExecutionStep(
            stepOrder = entity.stepOrder,
            formulaId = entity.formulaId,
            formulaExpression = entity.formulaExpression,
            resultVariableName = entity.resultVariableName,
            resultValue = entity.resultValue,
            executedAt = entity.executedAt
        )
    }
}