package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.*
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.*
import hs.kr.entrydsm.application.domain.application.usecase.ApplicationUseCase
import hs.kr.entrydsm.domain.application.usecase.*
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Application Web Adapter (Controller)
 */
@RestController
@RequestMapping("/api/applications")
class WebApplicationAdapter(
    private val applicationUseCase: ApplicationUseCase
) {
    
    /**
     * 원서 생성
     */
    @PostMapping
    fun createApplication(
        @RequestBody request: CreateApplicationRequest
    ): ResponseEntity<ApplicationResponse> {
        val command = request.toCommand()
        val application = applicationUseCase.execute(command)
        return ResponseEntity.ok(ApplicationResponse.from(application))
    }
    
    /**
     * 원서 조회 (사용자 ID로)
     */
    @GetMapping("/user/{userId}")
    fun getApplicationByUserId(
        @PathVariable userId: UUID
    ): ResponseEntity<ApplicationResponse> {
        val query = GetApplicationQuery.ByUserId(userId)
        val application = applicationUseCase.execute(query)
        return ResponseEntity.ok(ApplicationResponse.from(application))
    }
    
    /**
     * 원서 조회 (접수 번호로)
     */
    @GetMapping("/{receiptCode}")
    fun getApplicationByReceiptCode(
        @PathVariable receiptCode: Long
    ): ResponseEntity<ApplicationResponse> {
        val query = GetApplicationQuery.ByReceiptCode(ReceiptCode.from(receiptCode))
        val application = applicationUseCase.execute(query)
        return ResponseEntity.ok(ApplicationResponse.from(application))
    }
    
    /**
     * 원서 수정
     */
    @PutMapping("/user/{userId}")
    fun updateApplication(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateApplicationRequest
    ): ResponseEntity<ApplicationResponse> {
        val command = request.toCommand(userId)
        val application = applicationUseCase.execute(command)
        return ResponseEntity.ok(ApplicationResponse.from(application))
    }
    
    /**
     * 원서 최종 제출 + 성적 자동 계산
     */
    @PostMapping("/user/{userId}/submit")
    fun submitApplication(
        @PathVariable userId: UUID
    ): ResponseEntity<ApplicationSubmissionResponse> {
        val command = SubmitApplicationCommand(userId)
        val submittedApplication = applicationUseCase.execute(command)
        
        // 제출된 원서의 성적 자동 계산
        val calculateCommand = CalculateScoreCommand(receiptCode = submittedApplication.id)
        val calculationResult = applicationUseCase.execute(calculateCommand)
        
        return ResponseEntity.ok(
            ApplicationSubmissionResponse.from(submittedApplication, calculationResult)
        )
    }
    
    /**
     * 모든 원서 조회
     */
    @GetMapping
    fun getAllApplications(): ResponseEntity<List<ApplicationResponse>> {
        val applications = applicationUseCase.getAllApplications()
        val responses = applications.map { ApplicationResponse.from(it) }
        return ResponseEntity.ok(responses)
    }
    
    /**
     * 사용자 생성
     */
    @PostMapping("/users")
    fun createUser(
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = applicationUseCase.createUser(
            phoneNumber = request.phoneNumber,
            name = request.name,
            isParent = request.isParent
        )
        return ResponseEntity.ok(UserResponse.from(user))
    }
    
    /**
     * 성적 생성/수정
     */
    @PostMapping("/user/{userId}/score")
    fun createScore(
        @PathVariable userId: UUID,
        @RequestBody request: CreateScoreRequest
    ): ResponseEntity<ScoreResponse> {
        val command = request.toCommand(userId)
        val score = applicationUseCase.execute(command)
        return ResponseEntity.ok(ScoreResponse.from(score))
    }
    
    /**
     * 성적 조회
     */
    @GetMapping("/{receiptCode}/score")
    fun getScore(
        @PathVariable receiptCode: Long
    ): ResponseEntity<ScoreResponse> {
        val score = applicationUseCase.getScore(ReceiptCode.from(receiptCode))
        return if (score != null) {
            ResponseEntity.ok(ScoreResponse.from(score))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 성적 계산 실행
     * 원서와 성적 정보를 바탕으로 최종 점수를 계산
     */
    @PostMapping("/{receiptCode}/calculate-score")
    fun calculateScore(
        @PathVariable receiptCode: Long
    ): ResponseEntity<ScoreCalculationResponse> {
        val command = CalculateScoreCommand(receiptCode = ReceiptCode.from(receiptCode))
        val result = applicationUseCase.execute(command)
        return ResponseEntity.ok(ScoreCalculationResponse.from(result))
    }
    
    /**
     * 통합 원서 작성 + 성적 입력 + 자동 계산
     * 기존 사용자 조회 → 원서 작성 → 성적 입력 → 자동 계산을 한 번에 처리
     */
    @PostMapping("/user/{userId}/complete")
    fun createCompleteApplication(
        @PathVariable userId: UUID,
        @RequestBody request: CreateApplicationWithScoreRequest
    ): ResponseEntity<CompleteApplicationResponse> {
        val result = applicationUseCase.createCompleteApplication(
            userId = userId,
            applicationCommand = request.toApplicationCommand(userId),
            scoreCommand = request.toScoreCommand(userId)
        )
        
        return ResponseEntity.ok(
            CompleteApplicationResponse.from(
                result.user,
                result.application,
                result.score,
                result.calculationResult
            )
        )
    }
    
}