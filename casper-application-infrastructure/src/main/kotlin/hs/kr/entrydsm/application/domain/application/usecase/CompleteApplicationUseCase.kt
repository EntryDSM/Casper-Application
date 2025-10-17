package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationSubmissionResponse
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 입학원서 제출 UseCase
 *
 * 원서 제출 요청을 처리하고 응답을 생성합니다.
 * 단일 트랜잭션으로 모든 데이터를 저장합니다.
 */
@Service
@Transactional
class CompleteApplicationUseCase(
    private val applicationPersistenceService: ApplicationPersistenceService,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 입학원서를 제출합니다.
     *
     * @param userId 사용자 ID
     * @param request 원서 제출 요청
     * @return 원서 제출 응답
     */
    suspend fun execute(
        userId: UUID,
        request: ApplicationSubmissionRequest,
    ): ApplicationSubmissionResponse {
        logger.info("원서 제출 요청: userId=$userId")

        try {
            // 원서 생성 (검증, 점수 계산, 저장 모두 포함)
            val savedApplication =
                applicationPersistenceService.createApplication(
                    userId = userId,
                    applicationData = request.application,
                    scoresData = request.scores,
                )

            logger.info("=== 원서 제출 완료 ===")
            logger.info("userId: $userId")
            logger.info("applicationId: ${savedApplication.applicationId}")
            logger.info("receiptCode: ${savedApplication.receiptCode}")
            logger.info("applicantName: ${savedApplication.applicantName}")
            logger.info("applicationType: ${savedApplication.applicationType}")

            // 응답 생성
            return buildSuccessResponse(savedApplication)
        } catch (e: Exception) {
            logger.error("원서 제출 실패: userId=$userId", e)
            throw e
        }
    }

    /**
     * 성공 응답을 생성합니다.
     */
    private fun buildSuccessResponse(
        application: hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity,
    ): ApplicationSubmissionResponse {
        val status = applicationQueryStatusContract.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        return ApplicationSubmissionResponse(
            success = true,
            data =
                ApplicationSubmissionResponse.SubmissionData(
                    application =
                        ApplicationSubmissionResponse.ApplicationInfo(
                            applicationId = application.applicationId.toString(),
                            receiptCode = application.receiptCode,
                            applicantName = application.applicantName,
                            applicationType = application.applicationType.name,
                            educationalStatus = application.educationalStatus.name,
                            status = status.applicationStatus.name,
                            submittedAt = application.submittedAt,
                        ),
                    calculation =
                        ApplicationSubmissionResponse.CalculationInfo(
                            calculationId = application.applicationId.toString(),
                            totalScore = application.totalScore?.toDouble() ?: 0.0,
                            breakdown =
                                mapOf(
                                    "subjectScore" to (application.subjectScore?.toDouble() ?: 0.0),
                                    "attendanceScore" to (application.attendanceScore?.toDouble() ?: 0.0),
                                    "volunteerScore" to (application.volunteerScore?.toDouble() ?: 0.0),
                                    "bonusScore" to (application.bonusScore?.toDouble() ?: 0.0),
                                    "totalScore" to (application.totalScore?.toDouble() ?: 0.0),
                                ),
                            formulaExecution =
                                ApplicationSubmissionResponse.FormulaExecutionInfo(
                                    executedAt = application.calculatedAt ?: application.submittedAt,
                                    executionTimeMs = application.calculationTimeMs ?: 0L,
                                    steps = createDetailedSteps(application),
                                ),
                        ),
                ),
        )
    }

    /**
     * 점수 계산 단계 정보를 생성합니다.
     */
    private fun createDetailedSteps(
        application: hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity,
    ): List<ApplicationSubmissionResponse.FormulaStepInfo> {
        val applicationType = application.applicationType
        val educationalStatus = application.educationalStatus

        return listOf(
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "교과성적 계산",
                formula =
                    when (educationalStatus) {
                        EducationalStatus.PROSPECTIVE_GRADUATE -> "졸업예정자: 3-1학기(50%) + 2-2학기(25%) + 2-1학기(25%)"
                        EducationalStatus.GRADUATE -> "졸업자: 3-2학기(25%) + 3-1학기(25%) + 2-2학기(25%) + 2-1학기(25%)"
                        EducationalStatus.QUALIFICATION_EXAM -> "검정고시: 평균 / 100 × 80"
                    } + " × ${applicationType.baseScoreMultiplier}",
                result = application.subjectScore?.toDouble() ?: 0.0,
                variables =
                    mapOf(
                        "applicationType" to applicationType.displayName,
                        "educationalStatus" to educationalStatus.displayName,
                        "multiplier" to applicationType.baseScoreMultiplier,
                    ),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "출석점수 계산",
                formula = "15점 만점, 환산결석 = 결석 + (지각+조퇴+결과)/3",
                result = application.attendanceScore?.toDouble() ?: 0.0,
                variables = mapOf("maxScore" to 15.0),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "봉사활동점수 계산",
                formula = "15시간 이상: 15점, 14시간 이하: 시간수 = 점수",
                result = application.volunteerScore?.toDouble() ?: 0.0,
                variables = mapOf("maxScore" to 15.0),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "가산점 계산",
                formula = "알고리즘경진대회(3점) + 정보처리기능사(특별전형 6점)",
                result = application.bonusScore?.toDouble() ?: 0.0,
                variables =
                    mapOf(
                        "algorithmAward" to 3.0,
                        "infoProcessingCert" to if (applicationType != ApplicationType.COMMON) 6.0 else 0.0,
                    ),
            ),
            ApplicationSubmissionResponse.FormulaStepInfo(
                stepName = "최종점수 계산",
                formula = "교과성적(${application.subjectScore?.toDouble() ?: 0.0}) + 출석점수(${application.attendanceScore?.toDouble() ?: 0.0}) + 봉사점수(${application.volunteerScore?.toDouble() ?: 0.0}) + 가산점(${application.bonusScore?.toDouble() ?: 0.0})",
                result = application.totalScore?.toDouble() ?: 0.0,
                variables =
                    mapOf(
                        "subjectScore" to (application.subjectScore?.toDouble() ?: 0.0),
                        "attendanceScore" to (application.attendanceScore?.toDouble() ?: 0.0),
                        "volunteerScore" to (application.volunteerScore?.toDouble() ?: 0.0),
                        "bonusScore" to (application.bonusScore?.toDouble() ?: 0.0),
                        "totalScore" to (application.totalScore?.toDouble() ?: 0.0),
                    ),
            ),
        )
    }
}
