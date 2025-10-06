package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationDetailResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationListResponse
import hs.kr.entrydsm.application.domain.application.usecase.ApplicationQueryUseCase
import hs.kr.entrydsm.application.global.document.application.ApplicationQueryApiDocument
import hs.kr.entrydsm.application.global.pdf.generator.ApplicationPdfGenerator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ApplicationQueryController(
    private val applicationQueryUseCase: ApplicationQueryUseCase,
    private val applicationPdfGenerator: ApplicationPdfGenerator,
) : ApplicationQueryApiDocument {
    @GetMapping("/applications/{applicationId}")
    override fun getApplication(
        @PathVariable applicationId: String?,
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
    override fun getApplications(
        @RequestParam(required = false) applicationType: String?,
        @RequestParam(required = false) educationalStatus: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
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

    @GetMapping("/applications/{applicationId}/pdf")
    override fun generateApplicationPdf(
        @PathVariable applicationId: String?,
    ): ResponseEntity<ByteArray> {
        return try {
            if (applicationId.isNullOrBlank()) {
                throw IllegalArgumentException("원서 ID가 필요합니다")
            }

            try {
                java.util.UUID.fromString(applicationId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 원서 ID 형식입니다")
            }

            // ApplicationQueryUseCase를 통해 Application 조회
            val response = applicationQueryUseCase.getApplicationById(applicationId)
            val application = response.data ?: throw IllegalArgumentException("원서를 찾을 수 없습니다")

            // 도메인 모델로 변환하여 점수 계산
            val applicationUuid = java.util.UUID.fromString(applicationId)
            val applicationModel = applicationQueryUseCase.getApplicationDomainModel(applicationUuid)
            val updatedApplication = applicationModel.calculateAndUpdateScore()
            val scoreDetails = updatedApplication.getScoreDetails()

            // Application 도메인으로부터 PDF 생성
            val pdfBytes = applicationPdfGenerator.generate(updatedApplication, scoreDetails)

            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=application_${applicationId}.pdf")
                .body(pdfBytes)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
