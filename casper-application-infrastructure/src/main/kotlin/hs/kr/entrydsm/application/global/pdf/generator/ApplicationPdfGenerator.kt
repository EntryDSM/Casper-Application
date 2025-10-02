package hs.kr.entrydsm.application.global.pdf.generator

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import hs.kr.entrydsm.application.global.pdf.data.PdfDataConverter
import hs.kr.entrydsm.application.global.pdf.data.TemplateFileName
import hs.kr.entrydsm.application.global.pdf.facade.PdfDocumentFacade
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationPdfGeneratorContract
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.LinkedList

/**
 * 지원서 PDF를 생성하는 Generator입니다.
 *
 * HTML 템플릿을 기반으로 지원서, 개인정보동의서, 소개서, 금연서약서 등
 * 여러 문서를 하나의 PDF로 병합하여 완성된 지원서 PDF를 생성합니다.
 * 지원유형에 따라 추천서 등 추가 문서가 포함될 수 있습니다.
 */
@Component
class ApplicationPdfGenerator(
    private val pdfProcessor: PdfProcessor,
    private val pdfDataConverter: PdfDataConverter,
    private val templateProcessor: TemplateProcessor,
    private val pdfDocumentFacade: PdfDocumentFacade,
) : ApplicationPdfGeneratorContract {
    /**
     * 지원서 PDF를 생성합니다.
     *
     * @param application 지원서 정보
     * @param scoreDetails 계산된 점수 상세 정보 (deprecated - Application 객체에서 직접 계산)
     * @return 생성된 PDF 바이트 배열
     */
    override fun generate(
        application: Application,
        scoreDetails: Map<String, Any>,
    ): ByteArray {
        return generateApplicationPdf(application)
    }

    private fun generateApplicationPdf(application: Application): ByteArray {
        val calculatedScoreDetails = application.getScoreDetails()
            .mapValues { it.value as Any }
        
        val data = pdfDataConverter.applicationToInfo(application, calculatedScoreDetails)
        val templates = getTemplateFileNames(application)

        val outStream =
            templates.stream()
                .map { template ->
                    templateProcessor.convertTemplateIntoHtmlString(template, data.toMap())
                }
                .map { html ->
                    pdfProcessor.convertHtmlToPdf(html)
                }
                .toArray { size -> arrayOfNulls<ByteArrayOutputStream>(size) }

        val outputStream = ByteArrayOutputStream()
        val mergedDocument = PdfDocument(PdfWriter(outputStream))
        val pdfMerger = PdfMerger(mergedDocument)
        val document = Document(mergedDocument)

        for (pdfStream in outStream) {
            val pdfDoc = pdfDocumentFacade.getPdfDocument(pdfStream!!)
            mergeDocument(pdfMerger, pdfDoc)
        }

        document.close()

        return outputStream.toByteArray()
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

    private fun getTemplateFileNames(application: Application): MutableList<String> {
        val result =
            LinkedList(
                listOf(
                    TemplateFileName.APPLICATION_FOR_ADMISSION,
                    TemplateFileName.PRIVACY_AGREEMENT,
                    TemplateFileName.INTRODUCTION,
                    TemplateFileName.NON_SMOKING,
                    TemplateFileName.SMOKING_EXAMINE,
                    TemplateFileName.ENROLLMENT_AGREEMENT,
                ),
            )

        if (application.applicationType.name != "COMMON") {
            result.add(2, TemplateFileName.RECOMMENDATION)
        }

        return result
    }
}
