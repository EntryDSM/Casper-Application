package hs.kr.entrydsm.application.domain.application.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationScoreJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.CalculationResultJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.CalculationStepJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.enums.ScoreType
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationScoreJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.CalculationResultJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.CalculationStepJpaRepository
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class ApplicationPersistenceService(
    private val applicationRepository: ApplicationJpaRepository,
    private val scoreRepository: ApplicationScoreJpaRepository,
    private val calculationResultRepository: CalculationResultJpaRepository,
    private val calculationStepRepository: CalculationStepJpaRepository,
    private val objectMapper: ObjectMapper,
) {
    fun saveApplication(
        userId: UUID,
        applicationData: Map<String, Any>,
    ): ApplicationJpaEntity {
        val receiptCode = generateReceiptCode()

        val entity =
            ApplicationJpaEntity(
                applicationId = UUID.randomUUID(),
                userId = userId,
                receiptCode = receiptCode,
                applicantName = extractStringValue(applicationData, "applicantName") ?: "Unknown",
                applicantTel = extractStringValue(applicationData, "applicantTel") ?: "",
                parentName = extractStringValue(applicationData, "parentName"),
                parentTel = extractStringValue(applicationData, "parentTel"),
                birthDate = extractStringValue(applicationData, "birthDate"),
                applicationType = hs.kr.entrydsm.domain.application.values.ApplicationType.valueOf(extractStringValue(applicationData, "applicationType") ?: "COMMON"),
                educationalStatus = extractStringValue(applicationData, "educationalStatus") ?: "UNKNOWN",
                status = ApplicationStatus.SUBMITTED,
                submittedAt = LocalDateTime.now(),
                reviewedAt = null,
                isDaejeon = null,
                isOutOfHeadcount = null,
                photoPath = null,
                parentRelation = null,
                postalCode = null,
                detailAddress = null,
                studyPlan = null,
                selfIntroduce = null,
                veteransNumber = null,
                schoolCode = null,
            )

        return applicationRepository.save(entity)
    }

    fun saveScores(
        applicationId: UUID,
        scores: Map<String, Any>,
    ) {
        val scoreEntities = flattenAndCreateScoreEntities(applicationId, scores)
        scoreRepository.saveAll(scoreEntities)
    }

    fun saveCalculationResult(
        applicationId: UUID,
        calculationResult: CalculationResult,
    ): CalculationResultJpaEntity {
        val calculationId = UUID.randomUUID()

        val steps = extractStepsFromResult(calculationResult)
        val formulaStepsJson = objectMapper.writeValueAsString(steps)

        val entity =
            CalculationResultJpaEntity(
                calculationId = calculationId,
                applicationId = applicationId,
                totalScore = BigDecimal.valueOf(calculationResult.result?.toString()?.toDoubleOrNull() ?: 0.0),
                formulaSteps = formulaStepsJson,
                executedAt = LocalDateTime.now(),
                executionTimeMs = calculationResult.executionTimeMs,
            )

        val savedEntity = calculationResultRepository.save(entity)

        val stepEntities = createStepEntities(calculationId, steps)
        calculationStepRepository.saveAll(stepEntities)

        return savedEntity
    }

    private fun generateReceiptCode(): Long {
        val maxCode = applicationRepository.findMaxReceiptCode()
        return maxCode + 1
    }

    private fun flattenAndCreateScoreEntities(
        applicationId: UUID,
        scores: Map<String, Any>,
    ): List<ApplicationScoreJpaEntity> {
        val entities = mutableListOf<ApplicationScoreJpaEntity>()

        fun flatten(
            map: Map<String, Any>,
            prefix: String = "",
        ) {
            map.forEach { (key, value) ->
                val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
                when (value) {
                    is Map<*, *> -> flatten(value as Map<String, Any>, fullKey)
                    else -> {
                        entities.add(
                            ApplicationScoreJpaEntity(
                                scoreId = UUID.randomUUID(),
                                applicationId = applicationId,
                                scoreKey = fullKey,
                                scoreValue = value.toString(),
                                scoreType = determineScoreType(value),
                            ),
                        )
                    }
                }
            }
        }

        flatten(scores)
        return entities
    }

    private fun determineScoreType(value: Any): ScoreType {
        return when (value) {
            is Number -> ScoreType.NUMBER
            is Boolean -> ScoreType.BOOLEAN
            else -> ScoreType.STRING
        }
    }

    private fun extractStepsFromResult(calculationResult: CalculationResult): List<StepInfo> {
        return calculationResult.steps.mapIndexed { index, step ->
            StepInfo(
                stepOrder = index + 1,
                stepName = "Step ${index + 1}",
                formula = step,
                result = calculationResult.result?.toString()?.toDoubleOrNull() ?: 0.0,
                variables = calculationResult.variables,
                executionTimeMs = calculationResult.executionTimeMs / calculationResult.steps.size,
            )
        }
    }

    private fun createStepEntities(
        calculationId: UUID,
        steps: List<StepInfo>,
    ): List<CalculationStepJpaEntity> {
        return steps.map { step ->
            CalculationStepJpaEntity(
                stepId = UUID.randomUUID(),
                calculationId = calculationId,
                stepOrder = step.stepOrder,
                stepName = step.stepName,
                formula = step.formula,
                result = BigDecimal.valueOf(step.result),
                variablesUsed = objectMapper.writeValueAsString(step.variables),
                executionTimeMs = step.executionTimeMs,
            )
        }
    }

    data class StepInfo(
        val stepOrder: Int,
        val stepName: String,
        val formula: String,
        val result: Double,
        val variables: Map<String, Any>,
        val executionTimeMs: Long,
    )

    private fun extractStringValue(
        data: Map<String, Any>,
        key: String,
    ): String? {
        return try {
            when (val value = data[key]) {
                is String -> value
                else -> value?.toString()
            }
        } catch (e: Exception) {
            null
        }
    }
}
