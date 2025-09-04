package hs.kr.entrydsm.application.global.document.pdf.generator

import com.itextpdf.html2pdf.HtmlConverter
import hs.kr.entrydsm.application.global.document.pdf.config.ConverterPropertiesCreator
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
}
