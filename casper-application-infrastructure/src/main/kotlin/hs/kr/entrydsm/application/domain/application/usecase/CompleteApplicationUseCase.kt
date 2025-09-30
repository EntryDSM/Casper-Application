package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.calculator.ScoreCalculator
import hs.kr.entrydsm.application.domain.application.enums.ApplicationType
import hs.kr.entrydsm.application.domain.application.enums.EducationalStatus
import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationSubmissionResponse
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CompleteApplicationUseCase(
    private val scoreCalculator: ScoreCalculator,
    private val applicationPersistenceService: ApplicationPersistenceService,
) {
    fun execute(userId: UUID, request: ApplicationSubmissionRequest): ApplicationSubmissionResponse {
        // 요청 데이터 검증
        validateApplicationData(request.application)
        
        // Enum 변환
        val applicationType = ApplicationType.fromString(request.application["applicationType"] as String)
        val educationalStatus = EducationalStatus.fromString(request.application["educationalStatus"] as String)

        // 원서 저장
        val applicationEntity = applicationPersistenceService.saveApplication(
            userId = userId,
            applicationData = request.application,
        )

        // 성적 저장
        applicationPersistenceService.saveScores(
            applicationId = applicationEntity.applicationId,
            scores = request.scores,
        )

        // 2026학년도 대덕소프트웨어마이스터고 입학전형 점수 계산
        val scoreResult = scoreCalculator.calculateScore(applicationType, educationalStatus, request.scores)
        
        // 계산 결과 생성
        val calculationResult = CalculationResult.success(
            result = scoreResult.totalScore,
            executionTimeMs = 50L,
            formula = "2026학년도 대덕소프트웨어마이스터고 입학전형 점수 계산"
        )

        // 계산 결과 저장
        val calculationEntity = applicationPersistenceService.saveCalculationResult(
            applicationId = applicationEntity.applicationId,
            calculationResult = calculationResult,
        )

        return ApplicationSubmissionResponse(
            success = true,
            data = ApplicationSubmissionResponse.SubmissionData(
                application = ApplicationSubmissionResponse.ApplicationInfo(
                    applicationId = applicationEntity.applicationId.toString(),
                    receiptCode = applicationEntity.receiptCode,
                    applicantName = applicationEntity.applicantName,
                    applicationType = applicationEntity.applicationType,
                    educationalStatus = applicationEntity.educationalStatus,
                    status = applicationEntity.status.toString(),
                    submittedAt = applicationEntity.submittedAt,
                ),
                calculation = ApplicationSubmissionResponse.CalculationInfo(
                    calculationId = calculationEntity.calculationId.toString(),
                    totalScore = scoreResult.totalScore,
                    breakdown = mapOf(
                        "subjectScore" to scoreResult.subjectScore,
                        "attendanceScore" to scoreResult.attendanceScore,
                        "volunteerScore" to scoreResult.volunteerScore,
                        "bonusScore" to scoreResult.bonusScore,
                        "totalScore" to scoreResult.totalScore
                    ),
                    formulaExecution = ApplicationSubmissionResponse.FormulaExecutionInfo(
                        executedAt = calculationEntity.executedAt,
                        executionTimeMs = calculationEntity.executionTimeMs,
                        steps = createDetailedSteps(applicationType, educationalStatus, scoreResult),
                    ),
                ),
            ),
        )
    }

    private fun validateApplicationData(applicationData: Map<String, Any>) {
        val requiredFields = listOf("applicantName", "applicationType", "educationalStatus")
        
        requiredFields.forEach { field ->
            if (!applicationData.containsKey(field) || applicationData[field]?.toString().isNullOrBlank()) {
                throw IllegalArgumentException("필수 필드가 누락되었습니다: $field")
            }
        }
    }

    private fun createDetailedSteps(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        scoreResult: ScoreCalculator.ScoreResult
    ): List<ApplicationSubmissionResponse.FormulaStepInfo> {
        return listOf(
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "교과성적 계산",
                formula = when (educationalStatus) {
                    EducationalStatus.PROSPECTIVE_GRADUATE -> "졸업예정자: 3-1학기(50%) + 2-2학기(25%) + 2-1학기(25%)"
                    EducationalStatus.GRADUATE -> "졸업자: 3-2학기(25%) + 3-1학기(25%) + 2-2학기(25%) + 2-1학기(25%)"
                    EducationalStatus.QUALIFICATION_EXAM -> "검정고시: 입학전형위원회 결정 환산점수"
                } + " × ${applicationType.baseScoreMultiplier}",
                result = scoreResult.subjectScore,
                variables = mapOf(
                    "applicationType" to applicationType.displayName,
                    "educationalStatus" to educationalStatus.displayName,
                    "multiplier" to applicationType.baseScoreMultiplier
                ),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "출석점수 계산",
                formula = "15점 만점, 환산결석 = 결석 + (지각+조퇴+결과)/3",
                result = scoreResult.attendanceScore,
                variables = mapOf("maxScore" to 15.0),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "봉사활동점수 계산",
                formula = "15시간 이상: 15점, 14시간 이하: 시간수 = 점수",
                result = scoreResult.volunteerScore,
                variables = mapOf("maxScore" to 15.0),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "가산점 계산",
                formula = "알고리즘경진대회(3점) + 정보처리기능사(특별전형 6점)",
                result = scoreResult.bonusScore,
                variables = mapOf(
                    "algorithmAward" to 3.0,
                    "infoProcessingCert" to if (applicationType != ApplicationType.COMMON) 6.0 else 0.0
                ),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "최종점수 계산",
                formula = "교과성적(${scoreResult.subjectScore}) + 출석점수(${scoreResult.attendanceScore}) + 봉사점수(${scoreResult.volunteerScore}) + 가산점(${scoreResult.bonusScore})",
                result = scoreResult.totalScore,
                variables = mapOf(
                    "subjectScore" to scoreResult.subjectScore,
                    "attendanceScore" to scoreResult.attendanceScore,
                    "volunteerScore" to scoreResult.volunteerScore,
                    "bonusScore" to scoreResult.bonusScore,
                    "totalScore" to scoreResult.totalScore
                ),
            ),
        )
    }
}