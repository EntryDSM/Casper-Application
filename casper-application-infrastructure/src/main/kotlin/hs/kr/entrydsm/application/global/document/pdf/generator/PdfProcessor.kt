package hs.kr.entrydsm.application.global.document.pdf.generator

import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import hs.kr.entrydsm.application.global.document.pdf.config.ConverterPropertiesCreator
import hs.kr.entrydsm.application.global.document.pdf.facade.PdfDocumentFacade
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class PdfProcessor(
    private val converterPropertiesCreator: ConverterPropertiesCreator,
) {

    fun convertHtmlToPdf(html: String): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        HtmlConverter.convertToPdf(html, outputStream, converterPropertiesCreator.createConverterProperties())
        return outputStream
    }


    private fun mergeDocument(merger: PdfMerger, document: PdfDocument?) {
        document?.let {
            merger.merge(it, 1, it.numberOfPages)
            it.close()
        }
    }
}
