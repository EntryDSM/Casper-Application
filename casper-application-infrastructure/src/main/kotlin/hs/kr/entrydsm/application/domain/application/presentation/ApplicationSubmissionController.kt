package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CreateApplicationResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ScoreCalculationResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CancelApplicationResponse
import hs.kr.entrydsm.application.domain.application.usecase.CompleteApplicationUseCase
import hs.kr.entrydsm.domain.application.interfaces.CancelApplicationContract
import hs.kr.entrydsm.application.global.document.application.ApplicationSubmissionApiDocument
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/applications")
class ApplicationSubmissionController(
    private val completeApplicationUseCase: CompleteApplicationUseCase,
    private val cancelApplicationContract: CancelApplicationContract,
) : ApplicationSubmissionApiDocument {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    override fun createApplication(
        @RequestHeader("Request-User-Id") userId: String,
        @Valid @RequestBody request: CreateApplicationRequest
    ): ResponseEntity<CreateApplicationResponse> {
        return try {
            val userUuid = UUID.fromString(userId)
            
            // CreateApplicationRequest를 ApplicationSubmissionRequest로 변환
            val submissionRequest = convertToSubmissionRequest(request)
            
            // CompleteApplicationUseCase 실행
            val submissionResponse = completeApplicationUseCase.execute(userUuid, submissionRequest)
            
            // CreateApplicationResponse로 변환
            val response = CreateApplicationResponse(
                success = submissionResponse.success,
                data = CreateApplicationResponse.ApplicationData(
                    applicationId = UUID.fromString(submissionResponse.data.application.applicationId),
                    receiptCode = submissionResponse.data.application.receiptCode,
                    applicantName = submissionResponse.data.application.applicantName,
                    applicationType = submissionResponse.data.application.applicationType,
                    educationalStatus = submissionResponse.data.application.educationalStatus,
                    status = submissionResponse.data.application.status,
                    submittedAt = submissionResponse.data.application.submittedAt,
                    createdAt = submissionResponse.data.application.submittedAt
                ),
                message = "원서가 성공적으로 제출되었습니다."
            )
            
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.error("원서 제출 실패 - IllegalArgumentException: userId=$userId", e)
            ResponseEntity.badRequest().body(
                CreateApplicationResponse(
                    success = false,
                    data = null,
                    message = e.message
                )
            )
        } catch (e: Exception) {
            logger.error("원서 제출 실패 - Exception: userId=$userId", e)
            ResponseEntity.internalServerError().body(
                CreateApplicationResponse(
                    success = false,
                    data = null,
                    message = "원서 제출 중 오류가 발생했습니다: ${e.javaClass.simpleName} - ${e.message}"
                )
            )
        }
    }

    private fun convertToSubmissionRequest(request: CreateApplicationRequest): ApplicationSubmissionRequest {
        val applicationData = mutableMapOf<String, Any>()
        val scoresData = mutableMapOf<String, Any>()
        
        // 기본 정보
        applicationData["applicantName"] = request.applicantName
        applicationData["applicantTel"] = request.applicantTel
        applicationData["applicationType"] = request.applicationType
        applicationData["educationalStatus"] = request.educationalStatus
        
        // 선택적 정보
        request.birthDate?.let { applicationData["birthDate"] = it }
        request.parentName?.let { applicationData["parentName"] = it }
        request.parentTel?.let { applicationData["parentTel"] = it }
        request.streetAddress?.let { applicationData["streetAddress"] = it }
        request.isDaejeon?.let { applicationData["isDaejeon"] = it }
        
        // 성적 정보
        request.korean_3_1?.let { scoresData["korean_3_1"] = it }
        request.math_3_1?.let { scoresData["math_3_1"] = it }
        request.english_3_1?.let { scoresData["english_3_1"] = it }
        request.science_3_1?.let { scoresData["science_3_1"] = it }
        request.social_3_1?.let { scoresData["social_3_1"] = it }
        request.history_3_1?.let { scoresData["history_3_1"] = it }
        request.tech_3_1?.let { scoresData["tech_3_1"] = it }
        
        request.korean_3_2?.let { scoresData["koreanGrade"] = it }
        request.math_3_2?.let { scoresData["mathGrade"] = it }
        request.english_3_2?.let { scoresData["englishGrade"] = it }
        request.science_3_2?.let { scoresData["scienceGrade"] = it }
        request.social_3_2?.let { scoresData["socialGrade"] = it }
        request.history_3_2?.let { scoresData["historyGrade"] = it }
        request.tech_3_2?.let { scoresData["techAndHomeGrade"] = it }
        
        // 출결 및 봉사 정보
        request.absence?.let { scoresData["attendanceScore"] = 30 - (it * 1) } // 출석점수 계산
        request.volunteer?.let { scoresData["volunteerScore"] = it } 
        
        // 검정고시 성적
        request.gedKorean?.let { scoresData["qualificationKorean"] = it }
        request.gedMath?.let { scoresData["qualificationMath"] = it }
        request.gedEnglish?.let { scoresData["qualificationEnglish"] = it }
        request.gedScience?.let { scoresData["qualificationScience"] = it }
        request.gedSocial?.let { scoresData["qualificationSocial"] = it }
        request.gedTech?.let { scoresData["qualificationOpt"] = it }
        
        // 가산점
        var extraScore = 0
        if (request.algorithmAward == true) extraScore += 3
        if (request.infoProcessingCert == true) extraScore += 2
        if (extraScore > 0) scoresData["extraScore"] = extraScore
        
        return ApplicationSubmissionRequest(
            application = applicationData,
            scores = scoresData
        )
    }

    @DeleteMapping("/{receiptCode}")
    override fun cancelApplication(
        @RequestHeader("Request-User-Id") userId: String,
        @PathVariable receiptCode: Long,
    ): ResponseEntity<CancelApplicationResponse> {
        return try {
            val userUuid = UUID.fromString(userId)
            cancelApplicationContract.cancelApplication(userUuid, receiptCode)
            
            ResponseEntity.ok(
                CancelApplicationResponse(
                    success = true,
                    message = "원서 접수가 취소되었습니다."
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                CancelApplicationResponse(
                    success = false,
                    message = e.message ?: "원서 취소 중 오류가 발생했습니다."
                )
            )
        }
    }
}
