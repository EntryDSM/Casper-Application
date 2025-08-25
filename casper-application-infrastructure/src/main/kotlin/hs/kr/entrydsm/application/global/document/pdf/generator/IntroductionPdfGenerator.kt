package hs.kr.entrydsm.application.global.document.pdf.generator

import hs.kr.entrydsm.application.global.document.pdf.data.IntroductionPdfConverter
import hs.kr.entrydsm.application.global.document.pdf.data.TemplateFileName
import org.springframework.stereotype.Component

@Component
class IntroductionPdfGenerator(
    private val pdfProcessor: PdfProcessor,
    private val introductionPdfConverter: IntroductionPdfConverter,
    private val templateProcessor: TemplateProcessor
) {

    fun generate(application: Any): ByteArray {
        val data = introductionPdfConverter.execute(application)
        val html = templateProcessor.convertTemplateIntoHtmlString(
            TemplateFileName.ADMIN_INTRODUCTION,
            data.toMap()
        )
        val pdfStream = pdfProcessor.convertHtmlToPdf(html)
        return pdfStream.toByteArray()
    }
}
