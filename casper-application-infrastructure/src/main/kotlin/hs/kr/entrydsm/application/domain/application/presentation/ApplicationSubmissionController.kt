package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CreateApplicationResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ScoreCalculationResponse
import hs.kr.entrydsm.application.domain.application.usecase.ApplicationSubmissionUseCase
import hs.kr.entrydsm.application.global.document.application.ApplicationSubmissionApiDocument
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 원서 제출 컨트롤러
 * 
 * 원서 생성 등 원서 제출과 관련된 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/applications")
class ApplicationSubmissionController(
    private val applicationSubmissionUseCase: ApplicationSubmissionUseCase
) : ApplicationSubmissionApiDocument {
    
    @PostMapping
    override fun createApplication(
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: CreateApplicationRequest
    ): ResponseEntity<CreateApplicationResponse> {
        return try {
            val userUuid = UUID.fromString(userId)
            val response = applicationSubmissionUseCase.createApplication(userUuid, request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(
                CreateApplicationResponse(
                    success = false,
                    data = null,
                    message = e.message
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                CreateApplicationResponse(
                    success = false,
                    data = null,
                    message = e.message
                )
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                CreateApplicationResponse(
                    success = false,
                    data = null,
                    message = "원서 제출 중 오류가 발생했습니다"
                )
            )
        }
    }
    
    @GetMapping("/{applicationId}/score")
    override fun calculateScore(
        @PathVariable applicationId: String
    ): ResponseEntity<ScoreCalculationResponse> {
        return try {
            val applicationUuid = UUID.fromString(applicationId)
            val application = applicationSubmissionUseCase.getApplicationById(applicationUuid)
                ?: return ResponseEntity.notFound().build()
            
            val updatedApplication = application.calculateAndUpdateScore()
            val scoreDetails = updatedApplication.getScoreDetails()
            
            val scoreData = ScoreCalculationResponse.ScoreData(
                applicationId = applicationId,
                subjectScore = scoreDetails["교과성적"]!!,
                attendanceScore = scoreDetails["출석점수"]!!,
                volunteerScore = scoreDetails["봉사활동점수"]!!,
                bonusScore = scoreDetails["가산점"]!!,
                totalScore = scoreDetails["총점"]!!,
                maxScore = scoreDetails["최대점수"]!!,
                scorePercentage = updatedApplication.getScorePercentage(),
                applicationType = updatedApplication.applicationType.displayName,
                educationalStatus = updatedApplication.educationalStatus.displayName
            )
            
            ResponseEntity.ok(
                ScoreCalculationResponse(
                    success = true,
                    data = scoreData,
                    message = "점수 계산이 완료되었습니다"
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ScoreCalculationResponse(
                    success = false,
                    data = null,
                    message = "유효하지 않은 원서 ID입니다"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                ScoreCalculationResponse(
                    success = false,
                    data = null,
                    message = "점수 계산 중 오류가 발생했습니다"
                )
            )
        }
    }
}