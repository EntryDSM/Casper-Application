package hs.kr.entrydsm.application.global.document.pdf.facade

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Component
class PdfDocumentFacade {
    fun getPdfDocument(pdfStream: ByteArrayOutputStream): PdfDocument? {
        return try {
            val inputStream = ByteArrayInputStream(pdfStream.toByteArray())
            PdfDocument(PdfReader(inputStream))
        } catch (e: Exception) {
            null
        }
    }
}
