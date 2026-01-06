package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.usecase.GetFinalApplicationPdfUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetPreviewApplicationPdfUseCase
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import jakarta.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/pdf")
class WebApplicationPdfAdapter(
    private val getPreviewApplicationPdfUseCase: GetPreviewApplicationPdfUseCase,
    private val getFinalApplicationPdfUseCase: GetFinalApplicationPdfUseCase
) {

    companion object {
        const val FILE_NAME = "entry"
    }

    @GetMapping("/preview", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun previewPdf(): ByteArray = getPreviewApplicationPdfUseCase.execute()

    @GetMapping("/final", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun finalPdf(response: HttpServletResponse): ByteArray {
        response.setHeader("Content-Disposition", "attachment; filename=\"${encodeFileName()}.pdf\"")
        return getFinalApplicationPdfUseCase.getFinalApplicationPdf()
    }

    private fun encodeFileName(): String {
        return String(FILE_NAME.toByteArray(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
    }
}
