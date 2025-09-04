package hs.kr.entrydsm.application.global.document.pdf.generator

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

/**
 * Thymeleaf 템플릿을 HTML로 변환하는 처리기입니다.
 *
 * PDF 생성을 위해 Thymeleaf 템플릿 엔진을 사용하여
 * 템플릿 파일과 데이터를 결합해 HTML 문자열을 생성합니다.
 * 생성된 HTML은 PdfProcessor에서 PDF로 변환됩니다.
 */
@Component
class TemplateProcessor(
    private val templateEngine: TemplateEngine,
) {
    /**
     * 템플릿 파일과 데이터를 결합하여 HTML 문자열을 생성합니다.
     *
     * @param template 템플릿 파일명
     * @param data 템플릿에 바인딩할 데이터 맵
     * @return 렌더링된 HTML 문자열
     */
    fun convertTemplateIntoHtmlString(
        template: String?,
        data: MutableMap<String, Any>?,
    ): String {
        val context = Context()
        context.setVariables(data)
        return templateEngine.process(template, context)
    }
}
