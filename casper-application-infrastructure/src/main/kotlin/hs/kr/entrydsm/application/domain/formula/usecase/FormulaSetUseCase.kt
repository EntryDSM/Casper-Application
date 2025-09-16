package hs.kr.entrydsm.application.domain.formula.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaSetJpaEntity
import hs.kr.entrydsm.application.domain.formula.domain.entity.enums.FormulaSetStatus
import hs.kr.entrydsm.application.domain.formula.domain.repository.FormulaSetJpaRepository
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.CreateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.FormulaExecutionRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.UpdateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.*
import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class FormulaSetUseCase(
    private val formulaSetRepository: FormulaSetJpaRepository,
    private val calculator: Calculator,
    private val objectMapper: ObjectMapper,
) {
    fun createFormulaSet(request: CreateFormulaSetRequest): FormulaSetResponse {
        val formulaSteps =
            request.formulas.map { step ->
                mapOf(
                    "step" to step.step,
                    "name" to step.name,
                    "expression" to step.expression,
                    "resultVariable" to step.resultVariable,
                )
            }

        val entity =
            FormulaSetJpaEntity(
                formulaSetId = UUID.randomUUID(),
                name = request.name,
                description = request.description,
                applicationType = request.applicationType,
                educationalStatus = request.educationalStatus,
                region = request.region,
                formulas = objectMapper.writeValueAsString(formulaSteps),
                constants = objectMapper.writeValueAsString(request.constants),
                status = FormulaSetStatus.ACTIVE,
            )

        val savedEntity = formulaSetRepository.save(entity)

        return FormulaSetResponse(
            success = true,
            data =
                FormulaSetResponse.FormulaSetData(
                    formulaSetId = savedEntity.formulaSetId.toString(),
                    name = savedEntity.name,
                    description = savedEntity.description ?: "",
                    applicationType = savedEntity.applicationType,
                    educationalStatus = savedEntity.educationalStatus,
                    region = savedEntity.region,
                    totalFormulas = request.formulas.size,
                    status = savedEntity.status.toString(),
                    createdAt = savedEntity.createdAt,
                    updatedAt = savedEntity.updatedAt,
                ),
        )
    }

    fun updateFormulaSet(
        formulaSetId: String,
        request: UpdateFormulaSetRequest,
    ): FormulaSetResponse {
        val uuid = UUID.fromString(formulaSetId)
        val entity =
            formulaSetRepository.findById(uuid)
                .orElseThrow { IllegalArgumentException("수식 집합을 찾을 수 없습니다: $formulaSetId") }

        val formulaSteps =
            request.formulas.map { step ->
                mapOf(
                    "step" to step.step,
                    "name" to step.name,
                    "expression" to step.expression,
                    "resultVariable" to step.resultVariable,
                )
            }

        val updatedEntity =
            FormulaSetJpaEntity(
                formulaSetId = entity.formulaSetId,
                name = request.name,
                description = request.description,
                applicationType = entity.applicationType,
                educationalStatus = entity.educationalStatus,
                region = entity.region,
                formulas = objectMapper.writeValueAsString(formulaSteps),
                constants = objectMapper.writeValueAsString(request.constants),
                status = entity.status,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )

        val savedEntity = formulaSetRepository.save(updatedEntity)

        return FormulaSetResponse(
            success = true,
            data =
                FormulaSetResponse.FormulaSetData(
                    formulaSetId = savedEntity.formulaSetId.toString(),
                    name = savedEntity.name,
                    description = savedEntity.description ?: "",
                    applicationType = savedEntity.applicationType,
                    educationalStatus = savedEntity.educationalStatus,
                    region = savedEntity.region,
                    totalFormulas = request.formulas.size,
                    status = savedEntity.status.toString(),
                    createdAt = savedEntity.createdAt,
                    updatedAt = savedEntity.updatedAt,
                ),
        )
    }

    @Transactional(readOnly = true)
    fun getFormulaSetList(): FormulaSetListResponse {
        val entities = formulaSetRepository.findAllByStatus(FormulaSetStatus.ACTIVE)

        return FormulaSetListResponse(
            success = true,
            data =
                FormulaSetListResponse.FormulaSetListData(
                    formulaSets =
                        entities.map { entity ->
                            val formulas = objectMapper.readValue(entity.formulas, List::class.java) as List<Map<String, Any>>
                            FormulaSetListResponse.FormulaSetSummary(
                                formulaSetId = entity.formulaSetId.toString(),
                                name = entity.name,
                                applicationType = entity.applicationType,
                                educationalStatus = entity.educationalStatus,
                                region = entity.region,
                                totalFormulas = formulas.size,
                                status = entity.status.toString(),
                                createdAt = entity.createdAt,
                            )
                        },
                    total = entities.size,
                ),
        )
    }

    @Transactional(readOnly = true)
    fun getFormulaSetDetail(formulaSetId: String): FormulaSetDetailResponse {
        val uuid = UUID.fromString(formulaSetId)
        val entity =
            formulaSetRepository.findById(uuid)
                .orElseThrow { IllegalArgumentException("수식 집합을 찾을 수 없습니다: $formulaSetId") }

        val formulas = objectMapper.readValue(entity.formulas, List::class.java) as List<Map<String, Any>>
        val constants = objectMapper.readValue(entity.constants, Map::class.java) as Map<String, Double>

        return FormulaSetDetailResponse(
            success = true,
            data =
                FormulaSetDetailResponse.FormulaSetDetailData(
                    formulaSetId = entity.formulaSetId.toString(),
                    name = entity.name,
                    description = entity.description ?: "",
                    applicationType = entity.applicationType,
                    educationalStatus = entity.educationalStatus,
                    region = entity.region,
                    formulas =
                        formulas.map { formula ->
                            FormulaSetDetailResponse.FormulaStepDetail(
                                step = formula["step"] as Int,
                                name = formula["name"] as String,
                                expression = formula["expression"] as String,
                                resultVariable = formula["resultVariable"] as String,
                            )
                        },
                    constants = constants,
                    status = entity.status.toString(),
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                ),
        )
    }

    fun deleteFormulaSet(formulaSetId: String) {
        val uuid = UUID.fromString(formulaSetId)
        val entity =
            formulaSetRepository.findById(uuid)
                .orElseThrow { IllegalArgumentException("수식 집합을 찾을 수 없습니다: $formulaSetId") }

        val archivedEntity =
            FormulaSetJpaEntity(
                formulaSetId = entity.formulaSetId,
                name = entity.name,
                description = entity.description,
                applicationType = entity.applicationType,
                educationalStatus = entity.educationalStatus,
                region = entity.region,
                formulas = entity.formulas,
                constants = entity.constants,
                status = FormulaSetStatus.ARCHIVED,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )

        formulaSetRepository.save(archivedEntity)
    }

    fun executeFormulas(
        formulaSetId: String,
        request: FormulaExecutionRequest,
    ): FormulaExecutionResponse {
        val uuid = UUID.fromString(formulaSetId)
        val entity =
            formulaSetRepository.findById(uuid)
                .orElseThrow { IllegalArgumentException("수식 집합을 찾을 수 없습니다: $formulaSetId") }

        val formulas = objectMapper.readValue(entity.formulas, List::class.java) as List<Map<String, Any>>
        val constants = objectMapper.readValue(entity.constants, Map::class.java) as Map<String, Double>

        val variables = request.variables.toMutableMap()
        constants.forEach { (key, value) -> variables[key] = value }

        val executionSteps = mutableListOf<FormulaExecutionResponse.ExecutionStepDetail>()
        var finalResult = 0.0
        val startTime = System.currentTimeMillis()

        try {
            // 각 수식을 순차적으로 실행
            formulas.sortedBy { (it["step"] as Number).toInt() }.forEach { formula ->
                val expression = formula["expression"] as String
                val resultVar = formula["resultVariable"] as String
                val stepName = formula["name"] as String

                val calculationRequest =
                    CalculationRequest(
                        formula = expression,
                        variables = variables,
                    )

                val result = calculator.calculate(calculationRequest)

                if (result.isSuccess()) {
                    val stepResult = result.asDouble() ?: 0.0
                    finalResult = stepResult

                    // 다음 단계를 위해 변수에 추가
                    variables[resultVar] = stepResult

                    executionSteps.add(
                        FormulaExecutionResponse.ExecutionStepDetail(
                            step = (formula["step"] as Number).toInt(),
                            name = stepName,
                            result = stepResult,
                            executionTimeMs = result.executionTimeMs,
                        ),
                    )
                } else {
                    throw RuntimeException("수식 실행 실패: $expression - ${result.errors.joinToString(", ")}")
                }
            }

            val totalExecutionTime = System.currentTimeMillis() - startTime
            val executionId = UUID.randomUUID().toString()

            return FormulaExecutionResponse(
                success = true,
                data =
                    FormulaExecutionResponse.ExecutionData(
                        executionId = executionId,
                        finalResult = finalResult,
                        executionTimeMs = totalExecutionTime,
                        steps = executionSteps,
                    ),
            )
        } catch (e: Exception) {
            val totalExecutionTime = System.currentTimeMillis() - startTime
            val executionId = UUID.randomUUID().toString()

            return FormulaExecutionResponse(
                success = false,
                data =
                    FormulaExecutionResponse.ExecutionData(
                        executionId = executionId,
                        finalResult = 0.0,
                        executionTimeMs = totalExecutionTime,
                        steps =
                            listOf(
                                FormulaExecutionResponse.ExecutionStepDetail(
                                    step = 0,
                                    name = "오류",
                                    result = 0.0,
                                    executionTimeMs = totalExecutionTime,
                                ),
                            ),
                    ),
            )
        }
    }
}
