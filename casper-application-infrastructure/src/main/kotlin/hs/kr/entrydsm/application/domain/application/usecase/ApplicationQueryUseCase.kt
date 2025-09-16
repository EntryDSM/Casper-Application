package hs.kr.entrydsm.application.domain.application.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationScoreJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.CalculationResultJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.CalculationStepJpaRepository
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ApplicationQueryUseCase(
    private val applicationRepository: ApplicationJpaRepository,
    private val scoreRepository: ApplicationScoreJpaRepository,
    private val calculationResultRepository: CalculationResultJpaRepository,
    private val calculationStepRepository: CalculationStepJpaRepository,
    private val objectMapper: ObjectMapper,
) {
    fun getApplicationById(applicationId: String): ApplicationDetailResponse {
        val uuid = UUID.fromString(applicationId)
        val application =
            applicationRepository.findById(uuid)
                .orElseThrow { IllegalArgumentException("원서를 찾을 수 없습니다: $applicationId") }

        return ApplicationDetailResponse(
            success = true,
            data =
                ApplicationDetailResponse.ApplicationDetailData(
                    applicationId = application.applicationId.toString(),
                    userId = application.userId.toString(),
                    receiptCode = application.receiptCode,
                    applicantName = application.applicantName,
                    applicantTel = application.applicantTel,
                    parentName = application.parentName,
                    parentTel = application.parentTel,
                    birthDate = application.birthDate,
                    applicationType = application.applicationType,
                    educationalStatus = application.educationalStatus,
                    status = application.status.toString(),
                    submittedAt = application.submittedAt,
                    reviewedAt = application.reviewedAt,
                    createdAt = application.createdAt,
                    updatedAt = application.updatedAt,
                ),
        )
    }

    fun getApplications(
        applicationType: String?,
        educationalStatus: String?,
        page: Int = 0,
        size: Int = 20,
    ): ApplicationListResponse {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending())

        val applications =
            when {
                applicationType != null && educationalStatus != null -> {
                    applicationRepository.findByApplicationTypeAndEducationalStatus(applicationType, educationalStatus)
                }
                else -> applicationRepository.findAll()
            }

        val totalElements = applications.size
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        val pagedApplications =
            if (startIndex < totalElements) {
                applications.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

        return ApplicationListResponse(
            success = true,
            data =
                ApplicationListResponse.ApplicationListData(
                    applications =
                        pagedApplications.map { app ->
                            ApplicationListResponse.ApplicationSummary(
                                applicationId = app.applicationId.toString(),
                                receiptCode = app.receiptCode,
                                applicantName = app.applicantName,
                                applicationType = app.applicationType,
                                educationalStatus = app.educationalStatus,
                                status = app.status.toString(),
                                submittedAt = app.submittedAt,
                            )
                        },
                    total = totalElements,
                    page = page,
                    size = size,
                    totalPages = (totalElements + size - 1) / size,
                ),
        )
    }

    fun getUserApplications(userId: String): ApplicationListResponse {
        val uuid = UUID.fromString(userId)
        val applications = applicationRepository.findAllByUserId(uuid)

        return ApplicationListResponse(
            success = true,
            data =
                ApplicationListResponse.ApplicationListData(
                    applications =
                        applications.map { app ->
                            ApplicationListResponse.ApplicationSummary(
                                applicationId = app.applicationId.toString(),
                                receiptCode = app.receiptCode,
                                applicantName = app.applicantName,
                                applicationType = app.applicationType,
                                educationalStatus = app.educationalStatus,
                                status = app.status.toString(),
                                submittedAt = app.submittedAt,
                            )
                        },
                    total = applications.size,
                    page = 0,
                    size = applications.size,
                    totalPages = 1,
                ),
        )
    }

    fun getApplicationScores(applicationId: String): ApplicationScoresResponse {
        val uuid = UUID.fromString(applicationId)
        val scoreEntities = scoreRepository.findAllByApplicationId(uuid)

        val scores = reconstructNestedScores(scoreEntities)

        return ApplicationScoresResponse(
            success = true,
            data =
                ApplicationScoresResponse.ScoresData(
                    applicationId = applicationId,
                    scores = scores,
                ),
        )
    }

    fun getCalculationResult(applicationId: String): CalculationResponse {
        val uuid = UUID.fromString(applicationId)
        val calculationResult =
            calculationResultRepository.findLatestByApplicationId(uuid)
                ?: throw IllegalArgumentException("계산 결과를 찾을 수 없습니다: $applicationId")

        val steps = calculationStepRepository.findAllByCalculationIdOrderByStepOrder(calculationResult.calculationId)
        val formulaSteps = objectMapper.readValue(calculationResult.formulaSteps, List::class.java) as List<Map<String, Any>>

        return CalculationResponse(
            success = true,
            data =
                CalculationResponse.CalculationData(
                    calculationId = calculationResult.calculationId.toString(),
                    applicationId = applicationId,
                    totalScore = calculationResult.totalScore.toDouble(),
                    breakdown = extractBreakdownFromSteps(steps),
                    formulaExecution =
                        CalculationResponse.FormulaExecutionDetail(
                            steps =
                                steps.map { step ->
                                    CalculationResponse.CalculationStepDetail(
                                        stepOrder = step.stepOrder,
                                        stepName = step.stepName,
                                        formula = step.formula,
                                        result = step.result.toDouble(),
                                        variables =
                                            if (step.variablesUsed != null) {
                                                objectMapper.readValue(step.variablesUsed, Map::class.java) as Map<String, Any>
                                            } else {
                                                emptyMap()
                                            },
                                        executionTimeMs = step.executionTimeMs,
                                    )
                                },
                        ),
                    executedAt = calculationResult.executedAt,
                    executionTimeMs = calculationResult.executionTimeMs,
                ),
        )
    }

    fun getCalculationHistory(applicationId: String): CalculationHistoryResponse {
        val uuid = UUID.fromString(applicationId)
        val calculationResults = calculationResultRepository.findAllByApplicationIdOrderByExecutedAtDesc(uuid)

        return CalculationHistoryResponse(
            success = true,
            data =
                CalculationHistoryResponse.HistoryData(
                    applicationId = applicationId,
                    calculations =
                        calculationResults.map { result ->
                            CalculationHistoryResponse.CalculationSummary(
                                calculationId = result.calculationId.toString(),
                                totalScore = result.totalScore.toDouble(),
                                executedAt = result.executedAt,
                                executionTimeMs = result.executionTimeMs,
                            )
                        },
                ),
        )
    }

    private fun reconstructNestedScores(
        scoreEntities: List<hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationScoreJpaEntity>,
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        scoreEntities.forEach { scoreEntity ->
            val keys = scoreEntity.scoreKey.split(".")
            var currentMap = result

            for (i in 0 until keys.size - 1) {
                val key = keys[i]
                if (currentMap[key] == null) {
                    currentMap[key] = mutableMapOf<String, Any>()
                }
                currentMap = currentMap[key] as MutableMap<String, Any>
            }

            val finalKey = keys.last()
            val value =
                when (scoreEntity.scoreType) {
                    hs.kr.entrydsm.application.domain.application.domain.entity.enums.ScoreType.NUMBER ->
                        scoreEntity.scoreValue.toDoubleOrNull() ?: scoreEntity.scoreValue
                    hs.kr.entrydsm.application.domain.application.domain.entity.enums.ScoreType.BOOLEAN ->
                        scoreEntity.scoreValue.toBooleanStrictOrNull() ?: scoreEntity.scoreValue
                    else -> scoreEntity.scoreValue
                }
            currentMap[finalKey] = value
        }

        return result
    }

    private fun extractBreakdownFromSteps(
        steps: List<hs.kr.entrydsm.application.domain.application.domain.entity.CalculationStepJpaEntity>,
    ): Map<String, Double> {
        return steps.associate { step ->
            step.stepName to step.result.toDouble()
        }
    }
}
