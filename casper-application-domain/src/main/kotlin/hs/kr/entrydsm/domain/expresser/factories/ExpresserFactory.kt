package hs.kr.entrydsm.domain.expresser.factories

import hs.kr.entrydsm.domain.expresser.aggregates.ExpressionFormatter
import hs.kr.entrydsm.domain.expresser.aggregates.ExpressionReporter
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.entities.FormattingStyle
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import java.time.Instant

/**
 * Expresser 도메인 객체들을 생성하는 팩토리입니다.
 *
 * DDD Factory 패턴을 적용하여 표현식 형식화 관련 객체들의 생성과 구성을 
 * 체계적으로 관리합니다. 다양한 형식화 옵션과 스타일을 지원하며
 * 적절한 설정과 정책을 적용하여 일관된 객체 생성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Factory(
    context = "expresser",
    complexity = Complexity.NORMAL,
    cache = true
)
class ExpresserFactory {

    companion object {
        private var createdFormatterCount = 0L
        private var createdReporterCount = 0L
        private var createdOptionsCount = 0L
        
        // 기본 설정들
        private const val DEFAULT_FONT_SIZE = 12
        private const val DEFAULT_LINE_HEIGHT = 1.4
        private const val DEFAULT_MAX_LINE_LENGTH = 80
        
        // 싱글톤 지원
        @Volatile
        private var instance: ExpresserFactory? = null
        
        /**
         * 싱글톤 인스턴스를 반환합니다.
         */
        fun getInstance(): ExpresserFactory {
            return instance ?: synchronized(this) {
                instance ?: ExpresserFactory().also { instance = it }
            }
        }
    }

    /**
     * 기본 표현식 형식화기를 생성합니다.
     *
     * @return 기본 설정의 표현식 형식화기
     */
    fun createBasicFormatter(): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter(FormattingOptions.default())
    }

    /**
     * LaTeX 형식화기를 생성합니다.
     *
     * @return LaTeX 전용 형식화기
     */
    fun createLaTeXFormatter(): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter.createLatex()
    }

    /**
     * MathML 형식화기를 생성합니다.
     *
     * @return MathML 전용 형식화기
     */
    fun createMathMLFormatter(): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter(FormattingOptions.mathematical())
    }

    /**
     * HTML 형식화기를 생성합니다.
     *
     * @return HTML 전용 형식화기
     */
    fun createHTMLFormatter(): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter(FormattingOptions.mathematical())
    }

    /**
     * 유니코드 수학 기호 형식화기를 생성합니다.
     *
     * @return 유니코드 수학 기호 전용 형식화기
     */
    fun createUnicodeFormatter(): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter(FormattingOptions.mathematical())
    }

    /**
     * ASCII 전용 형식화기를 생성합니다.
     *
     * @return ASCII 전용 형식화기
     */
    fun createASCIIFormatter(): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter(FormattingOptions.programming())
    }

    /**
     * 사용자 정의 형식화기를 생성합니다.
     *
     * @param options 형식화 옵션
     * @return 사용자 정의 형식화기
     */
    fun createCustomFormatter(options: FormattingOptions): ExpressionFormatter {
        createdFormatterCount++
        return ExpressionFormatter(options)
    }

    /**
     * 고품질 형식화기를 생성합니다.
     *
     * @return 고품질 설정의 형식화기
     */
    fun createHighQualityFormatter(): ExpressionFormatter {
        createdFormatterCount++
        val options = createHighQualityOptions()
        return ExpressionFormatter(options)
    }

    /**
     * 접근성 강화 형식화기를 생성합니다.
     *
     * @return 접근성이 강화된 형식화기
     */
    fun createAccessibleFormatter(): ExpressionFormatter {
        createdFormatterCount++
        val options = createAccessibleOptions()
        return ExpressionFormatter(options)
    }

    /**
     * 기본 표현식 리포터를 생성합니다.
     *
     * @return 기본 설정의 표현식 리포터
     */
    fun createBasicReporter(): ExpressionReporter {
        createdReporterCount++
        return ExpressionReporter.createDefault()
    }

    /**
     * 상세 분석 리포터를 생성합니다.
     *
     * @return 상세 분석 기능이 포함된 리포터
     */
    fun createDetailedReporter(): ExpressionReporter {
        createdReporterCount++
        return ExpressionReporter.createDetailed()
    }

    /**
     * 성능 분석 리포터를 생성합니다.
     *
     * @return 성능 분석 전용 리포터
     */
    fun createPerformanceReporter(): ExpressionReporter {
        createdReporterCount++
        return ExpressionReporter.createSimple()
    }

    /**
     * 사용자 정의 리포터를 생성합니다.
     *
     * @param options 형식화 옵션
     * @return 사용자 정의 리포터
     */
    fun createCustomReporter(options: FormattingOptions): ExpressionReporter {
        createdReporterCount++
        return ExpressionReporter(ExpressionFormatter(options))
    }

    /**
     * 기본 형식화 옵션을 생성합니다.
     *
     * @return 기본 형식화 옵션
     */
    fun createDefaultOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.default()
    }

    /**
     * LaTeX 전용 형식화 옵션을 생성합니다.
     *
     * @return LaTeX 전용 옵션
     */
    fun createLaTeXOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.latex()
    }

    /**
     * 웹 표시용 형식화 옵션을 생성합니다.
     *
     * @return 웹 표시용 옵션
     */
    fun createWebOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.mathematical()
    }

    /**
     * 인쇄용 형식화 옵션을 생성합니다.
     *
     * @return 인쇄용 옵션
     */
    fun createPrintOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.compact()
    }

    /**
     * 모바일용 형식화 옵션을 생성합니다.
     *
     * @return 모바일용 옵션
     */
    fun createMobileOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.mathematical()
    }

    /**
     * 고품질 형식화 옵션을 생성합니다.
     *
     * @return 고품질 옵션
     */
    fun createHighQualityOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.highPrecision()
    }

    /**
     * 접근성 강화 형식화 옵션을 생성합니다.
     *
     * @return 접근성 강화 옵션
     */
    fun createAccessibleOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.verbose()
    }

    /**
     * 개발자용 형식화 옵션을 생성합니다.
     *
     * @return 개발자용 옵션
     */
    fun createDeveloperOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.programming()
    }

    /**
     * 프레젠테이션용 형식화 옵션을 생성합니다.
     *
     * @return 프레젠테이션용 옵션
     */
    fun createPresentationOptions(): FormattingOptions {
        createdOptionsCount++
        return FormattingOptions.mathematical()
    }

    /**
     * 형식화된 표현식을 생성합니다.
     *
     * @param content 내용
     * @param format 형식
     * @param metadata 메타데이터
     * @return 형식화된 표현식
     */
    fun createFormattedExpression(
        content: String,
        format: String,
        metadata: Map<String, Any> = emptyMap()
    ): FormattedExpression {
        return FormattedExpression(
            expression = content,
            style = FormattingStyle.MATHEMATICAL,
            options = FormattingOptions.default()
        )
    }

    /**
     * 성공 결과를 생성합니다.
     *
     * @param content 내용
     * @param format 형식
     * @param processingTime 처리 시간
     * @return 성공 결과
     */
    fun createSuccessResult(
        content: String,
        format: String,
        processingTime: Long = 0
    ): FormattedExpression {
        return FormattedExpression(
            expression = content,
            style = FormattingStyle.MATHEMATICAL,
            options = FormattingOptions.default()
        )
    }

    /**
     * 실패 결과를 생성합니다.
     *
     * @param error 오류 메시지
     * @param format 시도한 형식
     * @return 실패 결과
     */
    fun createFailureResult(
        error: String,
        format: String
    ): FormattedExpression {
        return FormattedExpression(
            expression = "Error: $error",
            style = FormattingStyle.MATHEMATICAL,
            options = FormattingOptions.default()
        )
    }

    // Style creation methods

    private fun createDefaultStyle(): FormattingStyle {
        return FormattingStyle.MATHEMATICAL
    }

    private fun createLaTeXStyle(): FormattingStyle {
        return FormattingStyle.LATEX
    }

    private fun createWebStyle(): FormattingStyle {
        return FormattingStyle.MATHEMATICAL
    }

    private fun createPrintStyle(): FormattingStyle {
        return FormattingStyle.COMPACT
    }

    private fun createMobileStyle(): FormattingStyle {
        return FormattingStyle.MATHEMATICAL
    }

    private fun createHighQualityStyle(): FormattingStyle {
        return FormattingStyle.MATHEMATICAL
    }

    private fun createAccessibleStyle(): FormattingStyle {
        return FormattingStyle.VERBOSE
    }

    private fun createDeveloperStyle(): FormattingStyle {
        return FormattingStyle.PROGRAMMING
    }

    private fun createPresentationStyle(): FormattingStyle {
        return FormattingStyle.MATHEMATICAL
    }

    /**
     * 환경별 최적화된 형식화기를 생성합니다.
     *
     * @param environment 환경 ("web", "mobile", "print", "presentation")
     * @return 환경에 최적화된 형식화기
     */
    fun createFormatterForEnvironment(environment: String): ExpressionFormatter {
        val options = when (environment.lowercase()) {
            "web" -> createWebOptions()
            "mobile" -> createMobileOptions()
            "print" -> createPrintOptions()
            "presentation" -> createPresentationOptions()
            else -> createDefaultOptions()
        }
        return createCustomFormatter(options)
    }

    /**
     * 사용자 요구사항에 맞는 형식화기를 생성합니다.
     *
     * @param requirements 요구사항 맵
     * @return 요구사항에 맞는 형식화기
     */
    fun createFormatterForRequirements(requirements: Map<String, Any>): ExpressionFormatter {
        val accessibility = requirements["accessibility"] as? Boolean ?: false
        
        val options = if (accessibility) {
            createAccessibleOptions()
        } else {
            createDefaultOptions()
        }
        
        return createCustomFormatter(options)
    }

    /**
     * 팩토리의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "factoryName" to "ExpresserFactory",
        "createdFormatters" to createdFormatterCount,
        "createdReporters" to createdReporterCount,
        "createdOptions" to createdOptionsCount,
        "supportedFormats" to listOf("latex", "mathml", "html", "unicode", "ascii", "text"),
        "supportedEnvironments" to listOf("web", "mobile", "print", "presentation", "developer"),
        "supportedStyles" to listOf("default", "latex", "web", "print", "mobile", "accessible"),
        "cacheEnabled" to true,
        "complexityLevel" to Complexity.NORMAL.name
    )

    /**
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "defaultFontSize" to DEFAULT_FONT_SIZE,
        "defaultLineHeight" to DEFAULT_LINE_HEIGHT,
        "defaultMaxLineLength" to DEFAULT_MAX_LINE_LENGTH,
        "supportedFormats" to listOf("infix", "prefix", "postfix", "latex", "mathml", "html", "unicode", "ascii"),
        "supportedThemes" to listOf("default", "academic", "modern", "print", "mobile", "accessible", "developer", "presentation"),
        "supportedColorSchemes" to listOf("light", "dark", "high-contrast", "monochrome", "auto", "enhanced")
    )

}