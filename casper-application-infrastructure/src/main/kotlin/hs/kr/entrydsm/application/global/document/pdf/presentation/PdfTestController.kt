package hs.kr.entrydsm.application.global.document.pdf.presentation

import hs.kr.entrydsm.application.global.document.pdf.generator.ApplicationPdfGenerator
import hs.kr.entrydsm.application.global.document.pdf.generator.IntroductionPdfGenerator
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pdf")
class PdfTestController(
    private val applicationPdfGenerator: ApplicationPdfGenerator,
    private val introductionPdfGenerator: IntroductionPdfGenerator,
) {
    @GetMapping("/test")
    fun testPdf(): ResponseEntity<ByteArray> {
        val dummyApp = Any()
        val dummyScore = Any()

        val pdfBytes = applicationPdfGenerator.generate(dummyApp, dummyScore)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=test.pdf")
            .body(pdfBytes)
    }

    @GetMapping("/test-introduction")
    fun testIntroductionPdf(): ResponseEntity<ByteArray> {
        val dummyApp = Any()

        val pdfBytes = introductionPdfGenerator.generate(dummyApp)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=test-introduction.pdf")
            .body(pdfBytes)
    }
}
