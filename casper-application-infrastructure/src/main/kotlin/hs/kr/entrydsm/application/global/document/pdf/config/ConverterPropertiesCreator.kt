package hs.kr.entrydsm.application.global.document.pdf.config

import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider
import com.itextpdf.io.font.FontProgramFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ConverterPropertiesCreator {
    private var fontPath: String = "/fonts/"

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
