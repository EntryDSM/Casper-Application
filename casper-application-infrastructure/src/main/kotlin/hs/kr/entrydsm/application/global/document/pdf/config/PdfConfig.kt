package hs.kr.entrydsm.application.global.document.pdf.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

/**
 * PDF 생성을 위한 Thymeleaf 템플릿 엔진 설정을 담당하는 Configuration 클래스입니다.
 *
 * HTML 템플릿을 사용하여 PDF 문서를 생성하기 위한 Thymeleaf 템플릿 엔진을
 * Bean으로 등록하고 설정합니다.
 */
@Configuration
class PdfConfig {
    /**
     * PDF 생성용 Thymeleaf 템플릿 엔진을 생성합니다.
     *
     * classpath의 templates 디렉토리에서 HTML 템플릿을 로드하도록 설정하며,
     * PDF로 변환될 HTML 문서 생성에 사용됩니다.
     *
     * @return 설정된 TemplateEngine 인스턴스
     */
    @Bean
    fun pdfTemplateEngine(): TemplateEngine {
        val templateResolver =
            ClassLoaderTemplateResolver().apply {
                prefix = "classpath:/templates/"
                suffix = ".html"
                templateMode = TemplateMode.HTML
            }
        return TemplateEngine().apply {
            setTemplateResolver(templateResolver)
        }
    }
}
