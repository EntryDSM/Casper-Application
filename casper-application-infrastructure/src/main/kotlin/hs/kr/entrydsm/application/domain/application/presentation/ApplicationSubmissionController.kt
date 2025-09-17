package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationSubmissionResponse
import hs.kr.entrydsm.application.domain.application.usecase.CompleteApplicationUseCase
import hs.kr.entrydsm.application.global.document.application.ApplicationSubmissionApiDocument
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1")
class ApplicationSubmissionController(
    private val completeApplicationUseCase: CompleteApplicationUseCase,
) : ApplicationSubmissionApiDocument {
    @PostMapping("/applications")
    override fun submitApplication(
        @RequestBody request: ApplicationSubmissionRequest?,
    ): ResponseEntity<ApplicationSubmissionResponse> {
        return try {
            if (request == null) {
                return createErrorResponse("요청 데이터가 없습니다", HttpStatus.BAD_REQUEST)
            }

            if (request.userId.isBlank()) {
                return createErrorResponse("사용자 ID가 필요합니다", HttpStatus.BAD_REQUEST)
            }

            if (request.application.isEmpty()) {
                return createErrorResponse("원서 정보가 필요합니다", HttpStatus.BAD_REQUEST)
            }

            if (request.scores.isEmpty()) {
                return createErrorResponse("성적 정보가 필요합니다", HttpStatus.BAD_REQUEST)
            }

            try {
                java.util.UUID.fromString(request.userId)
            } catch (e: IllegalArgumentException) {
                return createErrorResponse("올바르지 않은 사용자 ID 형식입니다", HttpStatus.BAD_REQUEST)
            }

            val applicationType = request.application["applicationType"]
            val educationalStatus = request.application["educationalStatus"]

            if (applicationType == null || applicationType.toString().isBlank()) {
                return createErrorResponse("전형 타입이 필요합니다", HttpStatus.BAD_REQUEST)
            }

            if (educationalStatus == null || educationalStatus.toString().isBlank()) {
                return createErrorResponse("학력 상태가 필요합니다", HttpStatus.BAD_REQUEST)
            }

            val response = completeApplicationUseCase.execute(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            createErrorResponse(e.message ?: "잘못된 요청 파라미터입니다", HttpStatus.BAD_REQUEST)
        } catch (e: NoSuchElementException) {
            createErrorResponse(e.message ?: "요청한 리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND)
        } catch (e: ClassCastException) {
            createErrorResponse("데이터 타입이 올바르지 않습니다: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: NullPointerException) {
            createErrorResponse("필수 값이 누락되었습니다", HttpStatus.BAD_REQUEST)
        } catch (e: NumberFormatException) {
            createErrorResponse("숫자 형식이 올바르지 않습니다: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: RuntimeException) {
            println("RuntimeException in application submission: ${e.message}")
            e.printStackTrace()
            createErrorResponse(e.message ?: "실행 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            println("Exception in application submission: ${e.message}")
            e.printStackTrace()
            createErrorResponse("서버 내부 오류가 발생했습니다: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun createErrorResponse(
        message: String,
        status: HttpStatus,
    ): ResponseEntity<ApplicationSubmissionResponse> {
        val errorResponse =
            ApplicationSubmissionResponse(
                success = false,
                data =
                    ApplicationSubmissionResponse.SubmissionData(
                        application =
                            ApplicationSubmissionResponse.ApplicationInfo(
                                applicationId = "",
                                receiptCode = 0L,
                                applicantName = "",
                                applicationType = "",
                                educationalStatus = "",
                                status =
                                    when (status) {
                                        HttpStatus.BAD_REQUEST -> "VALIDATION_ERROR"
                                        HttpStatus.NOT_FOUND -> "NOT_FOUND"
                                        else -> "INTERNAL_ERROR"
                                    },
                                submittedAt = LocalDateTime.now(),
                            ),
                        calculation =
                            ApplicationSubmissionResponse.CalculationInfo(
                                calculationId = "",
                                totalScore = 0.0,
                                breakdown = mapOf("error" to 0.0),
                                formulaExecution =
                                    ApplicationSubmissionResponse.FormulaExecutionInfo(
                                        executedAt = LocalDateTime.now(),
                                        executionTimeMs = 0L,
                                        steps =
                                            listOf(
                                                ApplicationSubmissionResponse.FormulaStepInfo(
                                                    stepName = "오류",
                                                    formula = "ERROR",
                                                    result = 0.0,
                                                    variables = mapOf("error" to message),
                                                ),
                                            ),
                                    ),
                            ),
                    ),
            )
        return ResponseEntity.status(status).body(errorResponse)
    }
}
