package hs.kr.entrydsm.application.global.pdf.config

import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider
import com.itextpdf.io.font.FontProgramFactory
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * PDF 변환을 위한 변환 속성을 생성하는 클래스입니다.
 *
 * iText PDF 라이브러리에서 HTML을 PDF로 변환할 때 필요한 설정을 관리하며,
 * 특히 한글 폰트 설정을 담당하여 PDF에서 한글이 정상적으로 표시되도록 합니다.
 *
 * @property fontPath 폰트 파일이 위치한 경로
 */
@Component
class ConverterPropertiesCreator {
    private var fontPath: String = "/fonts/"

    /**
     * PDF 변환을 위한 ConverterProperties를 생성합니다.
     *
     * DefaultFontProvider를 설정하고 한글 폰트들을 등록하여
     * PDF 생성 시 한글 텍스트가 올바르게 렌더링되도록 합니다.
     *
     * @return 폰트 설정이 완료된 ConverterProperties 객체
     * @throws IllegalStateException 폰트 파일을 찾을 수 없는 경우
     */
    fun createConverterProperties(): ConverterProperties {
        val properties = ConverterProperties()
        val fontProvider = DefaultFontProvider(false, false, false)

        Font.fonts.forEach { font ->
            try {
                val fontProgram = FontProgramFactory.createFont("$fontPath$font")
                fontProvider.addFont(fontProgram)
            } catch (e: IOException) {
                throw IllegalStateException("폰트 파일을 찾을 수 없습니다: $font", e)
            }
        }

        properties.fontProvider = fontProvider
        return properties
    }
}
