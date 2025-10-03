package hs.kr.entrydsm.application.domain.pdf.presentation

import hs.kr.entrydsm.application.domain.pdf.presentation.dto.request.PreviewPdfRequest
import hs.kr.entrydsm.application.domain.pdf.usecase.GetFinalApplicationPdfUseCase
import hs.kr.entrydsm.application.domain.pdf.usecase.GetPreviewApplicationPdfUseCase
import hs.kr.entrydsm.application.global.document.pdf.PdfApiDocument
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/v1/application/pdf")
class WebApplicationPdfAdapter(
    private val getFinalApplicationPdfUseCase: GetFinalApplicationPdfUseCase,
    private val getPreviewApplicationPdfUseCase: GetPreviewApplicationPdfUseCase
) : PdfApiDocument {

    /**
     * 프론트에서 전달받은 임시저장 데이터로 미리보기 PDF 생성
     */
    @PostMapping("/preview")
    override fun previewPdf(@RequestBody request: PreviewPdfRequest): ResponseEntity<ByteArray> {
        val pdfBytes = getPreviewApplicationPdfUseCase.execute(request)
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .body(pdfBytes)
    }

    @GetMapping("/final")
    override fun finalPdf(response: HttpServletResponse): ResponseEntity<ByteArray> {
        val pdfBytes = getFinalApplicationPdfUseCase.execute()
        
        response.setHeader("Content-Disposition", "attachment; filename=final_application.pdf")
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .body(pdfBytes)
    }
}
