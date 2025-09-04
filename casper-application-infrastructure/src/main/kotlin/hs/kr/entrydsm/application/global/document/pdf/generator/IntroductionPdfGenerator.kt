package hs.kr.entrydsm.application.global.document.pdf.generator

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import hs.kr.entrydsm.application.global.document.pdf.data.IntroductionPdfConverter
import hs.kr.entrydsm.application.global.document.pdf.data.TemplateFileName
import hs.kr.entrydsm.application.global.document.pdf.facade.PdfDocumentFacade
import hs.kr.entrydsm.domain.application.aggregates.Application
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import kotlin.collections.toMap

/**
 * 소개서 PDF를 생성하는 Generator입니다.
 *
 * 1차 합격자들의 소개서만을 모아서 하나의 PDF로 생성합니다.
 * 관리자가 면접 등에서 활용할 수 있도록 지원자별 소개서를
 * 순차적으로 배치하여 제공합니다.
 */
@Component
class IntroductionPdfGenerator(
    private val pdfProcessor: PdfProcessor,
    private val introductionPdfConverter: IntroductionPdfConverter,
    private val templateProcessor: TemplateProcessor,
    private val pdfDocumentFacade: PdfDocumentFacade,
) {
    /**
     * 소개서 PDF를 생성합니다.
     *
     * @param applicationList 지원서 목록 (1차 합격자)
     * @return 생성된 소개서 PDF 바이트 배열
     */
    fun generate(applicationList: List<Application>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val mergedDocument = PdfDocument(PdfWriter(outputStream))
        val pdfMerger = PdfMerger(mergedDocument)

        applicationList.forEach { application ->
            val pdfStream = generateIntroductionPdf(application)
            val pdfDoc = pdfDocumentFacade.getPdfDocument(pdfStream)
            mergeDocument(pdfMerger, pdfDoc)
        }

        mergedDocument.close()
        return outputStream.toByteArray()
    }

    private fun generateIntroductionPdf(application: Application): ByteArrayOutputStream {
        val data = introductionPdfConverter.execute(application)
        val template = TemplateFileName.ADMIN_INTRODUCTION

        val html = templateProcessor.convertTemplateIntoHtmlString(template, data.toMap())
        val pdfStream = pdfProcessor.convertHtmlToPdf(html)
        return pdfStream
    }

    private fun mergeDocument(
        merger: PdfMerger,
        document: PdfDocument?,
    ) {
        if (document != null) {
            merger.merge(document, 1, document.numberOfPages)
            document.close()
        }
    }
}
