package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.presentation.mapper.toApplicationRequest
import hs.kr.entrydsm.application.domain.application.usecase.GetFinalApplicationPdfUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetPreviewApplicationPdfUseCase
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/pdf")
class WebApplicationPdfAdapter(
    private val getPreviewApplicationPdfUseCase: GetPreviewApplicationPdfUseCase,
    private val getFinalApplicationPdfUseCase: GetFinalApplicationPdfUseCase,
) {
    companion object {
        const val FILE_NAME = "entry"
    }

    @PostMapping("/preview")
    fun previewPdf(@RequestBody @Valid request: ApplicationWebRequest): ResponseEntity<ByteArray> {
        val pdfBytes = getPreviewApplicationPdfUseCase.execute(request.toApplicationRequest())
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .body(pdfBytes)
    }

    @GetMapping("/final", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun finalPdf(response: HttpServletResponse): ByteArray {
        response.setHeader("Content-Disposition", "attachment; filename=\"${encodeFileName()}.pdf\"")
        return runBlocking { getFinalApplicationPdfUseCase.getFinalApplicationPdf() }
    }

    private fun encodeFileName(): String {
        return String(FILE_NAME.toByteArray(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
    }
}
