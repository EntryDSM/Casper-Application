package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.exception.ApplicationValidationException
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationDetailResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationListResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.UpdateApplicationArrivalResponse
import hs.kr.entrydsm.application.domain.application.usecase.ApplicationQueryUseCase
import hs.kr.entrydsm.application.domain.application.usecase.UpdateApplicationArrivalUseCase
import hs.kr.entrydsm.application.domain.pdf.presentation.dto.request.PreviewPdfRequest
import hs.kr.entrydsm.application.domain.pdf.usecase.GetPreviewApplicationPdfUseCase
import hs.kr.entrydsm.application.global.document.application.ApplicationQueryApiDocument
import hs.kr.entrydsm.application.global.pdf.generator.ApplicationPdfGenerator
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class ApplicationQueryController(
    private val applicationQueryUseCase: ApplicationQueryUseCase,
    private val applicationPdfGenerator: ApplicationPdfGenerator,
    private val updateApplicationArrivalUseCase: UpdateApplicationArrivalUseCase,
    private val getPreviewApplicationPdfUseCase: GetPreviewApplicationPdfUseCase,
) : ApplicationQueryApiDocument {
    @GetMapping("/applications/{applicationId}")
    override fun getApplication(
        @PathVariable applicationId: String?,
    ): ResponseEntity<ApplicationDetailResponse> {
        if (applicationId.isNullOrBlank()) {
            throw ApplicationValidationException("원서 ID가 필요합니다")
        }

        try {
            UUID.fromString(applicationId)
        } catch (e: IllegalArgumentException) {
            throw ApplicationValidationException("올바르지 않은 원서 ID 형식입니다")
        }

        val response = applicationQueryUseCase.getApplicationById(applicationId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/applications")
    override fun getApplications(
        @RequestParam(required = false) applicationType: String?,
        @RequestParam(required = false) educationalStatus: String?,
        @RequestParam(required = false) isDaejeon: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApplicationListResponse> {
        if (page < 0) {
            throw ApplicationValidationException("페이지 번호는 0 이상이어야 합니다")
        }
        if (size <= 0 || size > 100) {
            throw ApplicationValidationException("페이지 크기는 1~100 사이여야 합니다")
        }

        val response = applicationQueryUseCase.getApplications(applicationType, educationalStatus, isDaejeon, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/applications/pdf")
    override fun generateApplicationPdf(): ResponseEntity<ByteArray> {
        val application = applicationQueryUseCase.getCurrentUserApplication()
        val pdfBytes = applicationPdfGenerator.generate(application)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "attachment; filename=application_${application.applicationId}.pdf")
            .body(pdfBytes)
    }

    @PostMapping("/applications/pdf/preview")
    override fun previewApplicationPdf(
        @RequestBody request: PreviewPdfRequest,
    ): ResponseEntity<ByteArray> {
        // 프론트에서 받은 임시 데이터로 미리보기 PDF 생성
        val pdfBytes = getPreviewApplicationPdfUseCase.execute(request)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=preview_application.pdf")
            .body(pdfBytes)
    }

    @PatchMapping("/applications/{applicationId}/arrival")
    override fun updateArrivalStatus(
        @PathVariable applicationId: String,
        @RequestParam isArrived: Boolean,
    ): ResponseEntity<UpdateApplicationArrivalResponse> {
        if (applicationId.isBlank()) {
            throw ApplicationValidationException("원서 ID가 필요합니다")
        }

        val uuid =
            try {
                UUID.fromString(applicationId)
            } catch (e: IllegalArgumentException) {
                throw ApplicationValidationException("올바르지 않은 원서 ID 형식입니다")
            }

        updateApplicationArrivalUseCase.updateArrivalStatus(uuid, isArrived)

        return ResponseEntity.ok(
            UpdateApplicationArrivalResponse(
                success = true,
                message = "학교 도착 여부가 업데이트되었습니다.",
            ),
        )
    }
}
