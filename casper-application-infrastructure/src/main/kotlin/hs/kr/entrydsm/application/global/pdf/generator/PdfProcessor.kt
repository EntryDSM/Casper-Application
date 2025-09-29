package hs.kr.entrydsm.application.global.pdf.generator

import com.itextpdf.html2pdf.HtmlConverter
import hs.kr.entrydsm.application.global.pdf.config.ConverterPropertiesCreator
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

/**
 * HTML을 PDF로 변환하는 핵심 처리기입니다.
 *
 * iText PDF 라이브러리를 사용하여 HTML 문자열을 PDF 바이트 스트림으로 변환합니다.
 * 한글 폰트 설정 및 기타 PDF 생성 옵션들이 ConverterPropertiesCreator를 통해 적용됩니다.
 */
@Component
class PdfProcessor(
    private val converterPropertiesCreator: ConverterPropertiesCreator,
) {
    /**
     * HTML 문자열을 PDF로 변환합니다.
     *
     * iText HTML2PDF를 사용하여 HTML을 PDF로 변환하며,
     * 한글 폰트 설정과 기타 변환 옵션이 적용됩니다.
     *
     * @param html 변환할 HTML 문자열
     * @return PDF 데이터가 포함된 ByteArrayOutputStream
     */
    fun convertHtmlToPdf(html: String): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        HtmlConverter.convertToPdf(html, outputStream, converterPropertiesCreator.createConverterProperties())
        return outputStream
    }
}
