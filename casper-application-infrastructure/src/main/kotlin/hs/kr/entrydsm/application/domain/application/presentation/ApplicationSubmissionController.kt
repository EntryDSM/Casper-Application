package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.exception.ApplicationValidationException
import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CancelApplicationResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CreateApplicationResponse
import hs.kr.entrydsm.application.domain.application.usecase.CompleteApplicationUseCase
import hs.kr.entrydsm.application.global.document.application.ApplicationSubmissionApiDocument
import hs.kr.entrydsm.domain.application.interfaces.CancelApplicationContract
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
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
        @Valid @RequestBody request: CreateApplicationRequest,
    ): ResponseEntity<CreateApplicationResponse> = runBlocking {
        val userUuid =
            try {
                UUID.fromString(userId)
            } catch (e: IllegalArgumentException) {
                throw ApplicationValidationException("올바르지 않은 사용자 ID 형식입니다")
            }

        // CreateApplicationRequest를 ApplicationSubmissionRequest로 변환
        val submissionRequest = convertToSubmissionRequest(request)

        // CompleteApplicationUseCase 실행
        val submissionResponse = completeApplicationUseCase.execute(userUuid, submissionRequest)

        // CreateApplicationResponse로 변환
        val response =
            CreateApplicationResponse(
                success = submissionResponse.success,
                data =
                    CreateApplicationResponse.ApplicationData(
                        applicationId = UUID.fromString(submissionResponse.data.application.applicationId),
                        receiptCode = submissionResponse.data.application.receiptCode,
                        applicantName = submissionResponse.data.application.applicantName,
                        applicationType = submissionResponse.data.application.applicationType,
                        educationalStatus = submissionResponse.data.application.educationalStatus,
                        status = submissionResponse.data.application.status,
                        submittedAt = submissionResponse.data.application.submittedAt,
                        createdAt = submissionResponse.data.application.submittedAt,
                    ),
                message = "원서가 성공적으로 제출되었습니다.",
            )

        ResponseEntity.status(HttpStatus.CREATED).body(response)
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
        request.studyPlan?.let { applicationData["studyPlan"] = it }
        request.selfIntroduce?.let { applicationData["selfIntroduce"] = it }
        request.detailAddress?.let { applicationData["detailAddress"] = it }
        request.parentRelation?.let { applicationData["parentRelation"] = it }
        request.postalCode?.let { applicationData["postalCode"] = it }
        request.schoolCode?.let { applicationData["schoolCode"] = it }
        request.applicantGender?.let { applicationData["applicantGender"] = it }
        request.nationalMeritChild?.let { applicationData["nationalMeritChild"] = it }
        request.specialAdmissionTarget?.let { applicationData["specialAdmissionTarget"] = it }
        request.schoolName?.let { applicationData["schoolName"] = it }
        request.studentId?.let { applicationData["studentId"] = it }
        request.graduationDate?.let { applicationData["graduationDate"] = it }
        request.guardianGender?.let { applicationData["guardianGender"] = it }
        request.schoolPhone?.let { applicationData["schoolPhone"] = it }
        request.teacherName?.let { applicationData["teacherName"] = it }

        // 성적 정보 - 3학년 1학기
        request.korean_3_1?.let { scoresData["korean_3_1"] = it }
        request.social_3_1?.let { scoresData["social_3_1"] = it }
        request.history_3_1?.let { scoresData["history_3_1"] = it }
        request.math_3_1?.let { scoresData["math_3_1"] = it }
        request.science_3_1?.let { scoresData["science_3_1"] = it }
        request.tech_3_1?.let { scoresData["tech_3_1"] = it }
        request.english_3_1?.let { scoresData["english_3_1"] = it }

        // 성적 정보 - 3학년 2학기 (졸업생용)
        request.korean_3_2?.let { scoresData["korean_3_2"] = it }
        request.social_3_2?.let { scoresData["social_3_2"] = it }
        request.history_3_2?.let { scoresData["history_3_2"] = it }
        request.math_3_2?.let { scoresData["math_3_2"] = it }
        request.science_3_2?.let { scoresData["science_3_2"] = it }
        request.tech_3_2?.let { scoresData["tech_3_2"] = it }
        request.english_3_2?.let { scoresData["english_3_2"] = it }

        // 성적 정보 - 2학년 2학기
        request.korean_2_2?.let { scoresData["korean_2_2"] = it }
        request.social_2_2?.let { scoresData["social_2_2"] = it }
        request.history_2_2?.let { scoresData["history_2_2"] = it }
        request.math_2_2?.let { scoresData["math_2_2"] = it }
        request.science_2_2?.let { scoresData["science_2_2"] = it }
        request.tech_2_2?.let { scoresData["tech_2_2"] = it }
        request.english_2_2?.let { scoresData["english_2_2"] = it }

        // 성적 정보 - 2학년 1학기
        request.korean_2_1?.let { scoresData["korean_2_1"] = it }
        request.social_2_1?.let { scoresData["social_2_1"] = it }
        request.history_2_1?.let { scoresData["history_2_1"] = it }
        request.math_2_1?.let { scoresData["math_2_1"] = it }
        request.science_2_1?.let { scoresData["science_2_1"] = it }
        request.tech_2_1?.let { scoresData["tech_2_1"] = it }
        request.english_2_1?.let { scoresData["english_2_1"] = it }

        // 출결 정보
        request.absence?.let { scoresData["absence"] = it }
        request.tardiness?.let { scoresData["tardiness"] = it }
        request.earlyLeave?.let { scoresData["earlyLeave"] = it }
        request.classExit?.let { scoresData["classExit"] = it }
        //request.unexcused?.let { scoresData["unexcused"] = it }

        // 봉사활동
        request.volunteer?.let { scoresData["volunteer"] = it }

        // 검정고시 성적
        request.gedKorean?.let { scoresData["gedKorean"] = it }
        request.gedSocial?.let { scoresData["gedSocial"] = it }
        request.gedHistory?.let { scoresData["gedHistory"] = it }
        request.gedMath?.let { scoresData["gedMath"] = it }
        request.gedScience?.let { scoresData["gedScience"] = it }
        request.gedEnglish?.let { scoresData["gedEnglish"] = it }
        request.gedTech?.let { scoresData["gedTech"] = it }

        // 가산점
        request.algorithmAward?.let { scoresData["algorithmAward"] = it }
        request.infoProcessingCert?.let { scoresData["infoProcessingCert"] = it }
        
        var extraScore = 0
        if (request.algorithmAward == true) extraScore += 3
        if (request.infoProcessingCert == true) extraScore += 2
        if (extraScore > 0) scoresData["extraScore"] = extraScore

        return ApplicationSubmissionRequest(
            application = applicationData,
            scores = scoresData,
        )
    }

    @DeleteMapping
    override fun cancelApplication(
        @RequestHeader("Request-User-Id") userId: String,
    ): ResponseEntity<CancelApplicationResponse> {
        val userUuid =
            try {
                UUID.fromString(userId)
            } catch (e: IllegalArgumentException) {
                throw ApplicationValidationException("올바르지 않은 사용자 ID 형식입니다")
            }

        cancelApplicationContract.cancelApplication(userUuid)

        return ResponseEntity.ok(
            CancelApplicationResponse(
                success = true,
                message = "원서 접수가 취소되었습니다.",
            ),
        )
    }
}
