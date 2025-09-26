package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationSubmissionResponse
import hs.kr.entrydsm.domain.application.spi.PrototypePort
import hs.kr.entrydsm.domain.application.values.ApplicationTypeFilter
import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CompleteApplicationUseCase(
    private val prototypePort: PrototypePort,
    private val calculator: Calculator,
    private val applicationPersistenceService: ApplicationPersistenceService,
) {
    fun execute(
        userId: UUID,
        request: ApplicationSubmissionRequest,
    ): ApplicationSubmissionResponse {
        val applicationType = request.application["applicationType"] as String
        val educationalStatus = request.application["educationalStatus"] as String
        val region = request.application["region"] as? String

        val filter =
            ApplicationTypeFilter(
                applicationType = applicationType,
                educationalStatus = educationalStatus,
                region = region,
            )

        val prototype =
            prototypePort.findPrototypeByApplicationType(filter)
                ?: throw IllegalArgumentException("해당하는 프로토타입을 찾을 수 없습니다")

        val validationResult = prototypePort.validateScoreData(prototype, request.scores)
        if (!validationResult.valid) {
            throw IllegalArgumentException("데이터 검증 실패: ${validationResult.errors.joinToString(", ")}")
        }

        val applicationEntity =
            applicationPersistenceService.saveApplication(
                userId = userId,
                applicationData = request.application,
            )

        applicationPersistenceService.saveScores(
            applicationId = applicationEntity.applicationId,
            scores = request.scores,
        )

        // 각 수식을 순차적으로 실행
        val variables = flattenScores(request.scores).toMutableMap()
        prototype.constant.forEach { (key, value) -> variables[key] = value }

        // 디버깅: 변수들을 출력
        println("Variables for calculation: $variables")

        var finalResult = 0.0
        prototype.formula.sortedBy { it.step }.forEach { formula ->
            val calculationRequest =
                CalculationRequest(
                    formula = formula.expression,
                    variables = variables,
                )

            val stepResult = calculator.calculate(calculationRequest)
            if (stepResult.isSuccess()) {
                val resultValue = stepResult.asDouble() ?: 0.0
                finalResult = resultValue
                variables[formula.resultVariable] = resultValue
            } else {
                throw RuntimeException("수식 실행 실패: ${formula.expression} - ${stepResult.errors.joinToString(", ")}")
            }
        }

        // 최종 결과로 CalculationResult 생성
        val calculationResult = CalculationResult.success(finalResult, 0L, "최종 계산 완료")

        val calculationEntity =
            applicationPersistenceService.saveCalculationResult(
                applicationId = applicationEntity.applicationId,
                calculationResult = calculationResult,
            )

        return ApplicationSubmissionResponse(
            success = true,
            data =
                ApplicationSubmissionResponse.SubmissionData(
                    application =
                        ApplicationSubmissionResponse.ApplicationInfo(
                            applicationId = applicationEntity.applicationId.toString(),
                            receiptCode = applicationEntity.receiptCode,
                            applicantName = applicationEntity.applicantName,
                            applicationType = applicationEntity.applicationType.toString(),
                            educationalStatus = applicationEntity.educationalStatus,
                            status = applicationEntity.status.toString(),
                            submittedAt = applicationEntity.submittedAt,
                        ),
                    calculation =
                        ApplicationSubmissionResponse.CalculationInfo(
                            calculationId = calculationEntity.calculationId.toString(),
                            totalScore = calculationResult.result?.toString()?.toDoubleOrNull() ?: 0.0,
                            breakdown = extractBreakdown(calculationResult),
                            formulaExecution =
                                ApplicationSubmissionResponse.FormulaExecutionInfo(
                                    executedAt = calculationEntity.executedAt,
                                    executionTimeMs = calculationEntity.executionTimeMs,
                                    steps =
                                        prototype.formula.mapIndexed { index, formulaStep ->
                                            ApplicationSubmissionResponse.FormulaStepInfo(
                                                stepName = formulaStep.name,
                                                formula = formulaStep.expression,
                                                result = calculationResult.result?.toString()?.toDoubleOrNull() ?: 0.0,
                                                variables = calculationResult.variables,
                                            )
                                        },
                                ),
                        ),
                ),
        )
    }

    private fun flattenScores(scores: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        fun flatten(
            map: Map<String, Any>,
            prefix: String = "",
        ) {
            map.forEach { (key, value) ->
                val currentKey = if (prefix.isEmpty()) key else "$prefix.$key"
                when (value) {
                    is Map<*, *> -> flatten(value as Map<String, Any>, currentKey)
                    else -> {
                        // 중첩된 구조에서는 마지막 키만 사용 (grades.korean_3_1 -> korean_3_1)
                        val finalKey = key
                        result[finalKey] = value
                    }
                }
            }
        }

        flatten(scores)
        return result
    }

    private fun extractBreakdown(calculationResult: CalculationResult): Map<String, Double> {
        return mapOf(
            "finalResult" to (calculationResult.result?.toString()?.toDoubleOrNull() ?: 0.0),
        )
    }
}
