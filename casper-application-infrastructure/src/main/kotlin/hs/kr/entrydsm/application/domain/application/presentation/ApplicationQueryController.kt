package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.response.*
import hs.kr.entrydsm.application.domain.application.usecase.ApplicationQueryUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ApplicationQueryController(
    private val applicationQueryUseCase: ApplicationQueryUseCase
) {
    
    @GetMapping("/applications/{applicationId}")
    fun getApplication(
        @PathVariable applicationId: String?
    ): ResponseEntity<ApplicationDetailResponse> {
        return try {
            if (applicationId.isNullOrBlank()) {
                throw IllegalArgumentException("원서 ID가 필요합니다")
            }
            
            try {
                java.util.UUID.fromString(applicationId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 원서 ID 형식입니다")
            }
            
            val response = applicationQueryUseCase.getApplicationById(applicationId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/applications")
    fun getApplications(
        @RequestParam(required = false) applicationType: String?,
        @RequestParam(required = false) educationalStatus: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApplicationListResponse> {
        return try {
            if (page < 0) {
                throw IllegalArgumentException("페이지 번호는 0 이상이어야 합니다")
            }
            if (size <= 0 || size > 100) {
                throw IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다")
            }
            
            val response = applicationQueryUseCase.getApplications(applicationType, educationalStatus, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/users/{userId}/applications")
    fun getUserApplications(
        @PathVariable userId: String?
    ): ResponseEntity<ApplicationListResponse> {
        return try {
            if (userId.isNullOrBlank()) {
                throw IllegalArgumentException("사용자 ID가 필요합니다")
            }
            
            try {
                java.util.UUID.fromString(userId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 사용자 ID 형식입니다")
            }
            
            val response = applicationQueryUseCase.getUserApplications(userId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/applications/{applicationId}/scores")
    fun getApplicationScores(
        @PathVariable applicationId: String?
    ): ResponseEntity<ApplicationScoresResponse> {
        return try {
            if (applicationId.isNullOrBlank()) {
                throw IllegalArgumentException("원서 ID가 필요합니다")
            }
            
            try {
                java.util.UUID.fromString(applicationId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 원서 ID 형식입니다")
            }
            
            val response = applicationQueryUseCase.getApplicationScores(applicationId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/applications/{applicationId}/calculations")
    fun getCalculationResult(
        @PathVariable applicationId: String?
    ): ResponseEntity<CalculationResponse> {
        return try {
            if (applicationId.isNullOrBlank()) {
                throw IllegalArgumentException("원서 ID가 필요합니다")
            }
            
            try {
                java.util.UUID.fromString(applicationId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 원서 ID 형식입니다")
            }
            
            val response = applicationQueryUseCase.getCalculationResult(applicationId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/applications/{applicationId}/calculations/history")
    fun getCalculationHistory(
        @PathVariable applicationId: String?
    ): ResponseEntity<CalculationHistoryResponse> {
        return try {
            if (applicationId.isNullOrBlank()) {
                throw IllegalArgumentException("원서 ID가 필요합니다")
            }
            
            try {
                java.util.UUID.fromString(applicationId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 원서 ID 형식입니다")
            }
            
            val response = applicationQueryUseCase.getCalculationHistory(applicationId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}