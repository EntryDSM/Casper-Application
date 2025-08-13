package hs.kr.entrydsm.domain.expresser.services

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.expresser.aggregates.ExpressionFormatter
import hs.kr.entrydsm.domain.expresser.aggregates.ExpressionReporter
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.entities.FormattingStyle
import hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException
import hs.kr.entrydsm.domain.expresser.factories.ExpresserFactory
import hs.kr.entrydsm.domain.expresser.interfaces.ExpresserContract
import hs.kr.entrydsm.domain.expresser.policies.FormattingPolicy
import hs.kr.entrydsm.domain.expresser.specifications.FormattingQualitySpec
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression
import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.domain.parser.aggregates.ParsingContextAggregate
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.configuration.ExpresserConfiguration
import hs.kr.entrydsm.global.configuration.interfaces.ConfigurationProvider
import java.time.Instant

/**
 * 표현식 형식화의 핵심 비즈니스 로직을 처리하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 표현식 형식화와 출력 생성의
 * 전체 파이프라인을 조율합니다. 정책과 명세를 적용하여 안전하고
 * 품질 높은 형식화를 보장하며, 다양한 형식과 스타일을 지원합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "ExpresserService",
    type = hs.kr.entrydsm.global.annotation.service.type.ServiceType.DOMAIN_SERVICE
)
class ExpresserService(
    private val lexer: LexerAggregate,
    private val parser: ParsingContextAggregate,
    private val factory: ExpresserFactory,
    private val policy: FormattingPolicy,
    private val qualitySpec: FormattingQualitySpec,
    private val configurationProvider: ConfigurationProvider
) : ExpresserContract {

    companion object {
        /**
         * 포맷별 팩토리 메서드 매핑
         */
        private val FORMAT_FACTORY_MAPPINGS = mapOf(
            "mathematical" to { factory: ExpresserFactory -> factory.createBasicFormatter() },
            "latex" to { factory: ExpresserFactory -> factory.createLaTeXFormatter() },
            "mathml" to { factory: ExpresserFactory -> factory.createMathMLFormatter() },
            "html" to { factory: ExpresserFactory -> factory.createHTMLFormatter() },
            "unicode" to { factory: ExpresserFactory -> factory.createUnicodeFormatter() },
            "ascii" to { factory: ExpresserFactory -> factory.createASCIIFormatter() }
        )
        
        /**
         * 출력 크기 추정 배율
         */
        private val OUTPUT_SIZE_MULTIPLIERS = mapOf(
            "latex" to 1.5,
            "mathml" to 2.0,
            "html" to 1.8,
            "xml" to 2.2,
            "json" to 1.3
        )
        
        /**
         * 지원되는 출력 형식들
         */
        private val SUPPORTED_FORMATS = setOf(
            "mathematical", "latex", "mathml", "html", "json", "xml", "unicode", "ascii", "text"
        )
        
        /**
         * 지원되는 색상 스키마들
         */
        private val SUPPORTED_COLOR_SCHEMES = setOf(
            "default", "dark", "light", "high-contrast", "colorblind"
        )
        
        /**
         * 캐시 관련 상수들
         */
        private const val CACHE_VALIDITY_MS = 3600000L // 1시간
        private const val MAX_CACHE_SIZE = 1000
        
        /**
         * 단계별 분해 템플릿
         */
        private val BREAKDOWN_STEPS = listOf(
            "Step 1: Parse",
            "Step 2: Format"
        )
        
        /**
         * 구문 강조 CSS 클래스 매핑
         */
        private val SYNTAX_HIGHLIGHT_CLASSES = mapOf(
            "dark" to mapOf("number" to "number-dark"),
            "light" to mapOf("number" to "number-light")
        )
    }

    private val config: ExpresserConfiguration
        get() = configurationProvider.getExpresserConfiguration()

    private val formattingCache = mutableMapOf<String, CachedFormatting>()
    private val performanceMetrics = PerformanceMetrics()

    /**
     * AST를 형식화된 표현식으로 변환합니다.
     */
    override fun format(ast: ASTNode): FormattedExpression {
        return format(ast, FormattingOptions.default())
    }

    /**
     * AST를 특정 옵션으로 형식화합니다.
     */
    override fun format(ast: ASTNode, options: FormattingOptions): FormattedExpression {
        val startTime = System.currentTimeMillis()
        
        try {
            performanceMetrics.incrementTotalRequests()
            
            // 단계별 처리
            validateFormattingRequest(ast, options)
            val cacheResult = tryGetCachedResult(ast, options)
            if (cacheResult != null) return cacheResult
            
            val rawFormatted = executeFormatting(ast, options)
            val validatedFormatted = validateFormattingQuality(rawFormatted, options)
            val secureFormatted = applySecurityFiltering(validatedFormatted)
            val finalResult = cacheFinalResult(ast, options, secureFormatted)
            
            updateMetricsAndFinalize(startTime, finalResult)
            return finalResult
            
        } catch (e: ExpresserException) {
            performanceMetrics.incrementFailures()
            throw e
        } catch (e: Exception) {
            performanceMetrics.incrementFailures()
            throw ExpresserException.formattingError("formatting_error", e.message ?: "Unknown error", e)
        }
    }

    /**
     * 표현식 문자열을 재형식화합니다.
     */
    override fun reformat(expression: String): FormattedExpression {
        return reformat(expression, FormattingOptions.default())
    }

    /**
     * 표현식 문자열을 특정 옵션으로 재형식화합니다.
     */
    override fun reformat(expression: String, options: FormattingOptions): FormattedExpression {
        try {
            // 1. 렉싱
            val lexingResult = lexer.tokenize(expression)
            if (!lexingResult.isSuccess) {
                throw ExpresserException.formattingError("lexing_failed", lexingResult.error?.message ?: "렉싱 실패: 토큰화 오류")
            }
            
            // 2. 파싱
            val parsingResult = parser.parse(lexingResult.tokens)
            if (!parsingResult.isSuccess) {
                throw ExpresserException.formattingError("parsing_failed", parsingResult.error?.message ?: "파싱 실패")
            }
            val ast = parsingResult.ast!!
            
            // 3. 형식화
            return format(ast, options)
            
        } catch (e: ExpresserException) {
            throw e
        } catch (e: Exception) {
            throw ExpresserException.formattingError("reformat_error", e.message ?: "Unknown error", e)
        }
    }

    /**
     * AST를 특정 형식으로 출력합니다.
     */
    override fun express(ast: ASTNode, format: String): FormattedExpression {
        val factoryMethod = FORMAT_FACTORY_MAPPINGS[format.lowercase()]
            ?: throw ExpresserException.unsupportedFormat(format)
        
        val formatter = factoryMethod(factory)
        return formatter.format(ast)
    }

    /**
     * 표현식을 특정 형식으로 변환합니다.
     */
    override fun convert(expression: String, sourceFormat: String, targetFormat: String): FormattedExpression {
        // 간단한 구현 - 실제로는 더 정교한 변환 로직 필요
        val reformatted = reformat(expression)
        val lexingResult = lexer.tokenize(expression)
        if (!lexingResult.isSuccess) {
            throw ExpresserException.formattingError("lexing_failed", lexingResult.error?.message ?: "렉싱 실패")
        }
        val parsingResult = parser.parse(lexingResult.tokens)
        if (!parsingResult.isSuccess) {
            throw ExpresserException.formattingError("parsing_failed", parsingResult.error?.message ?: "파싱 실패")
        }
        return express(parsingResult.ast!!, targetFormat)
    }

    /**
     * 표현식을 수학 표기법으로 변환합니다.
     */
    override fun toMathematicalNotation(ast: ASTNode): FormattedExpression {
        return express(ast, "mathematical")
    }

    /**
     * 표현식을 LaTeX 형식으로 변환합니다.
     */
    override fun toLaTeX(ast: ASTNode): FormattedExpression {
        return express(ast, "latex")
    }

    /**
     * 표현식을 MathML 형식으로 변환합니다.
     */
    override fun toMathML(ast: ASTNode): FormattedExpression {
        return express(ast, "mathml")
    }

    /**
     * 표현식을 HTML 형식으로 변환합니다.
     */
    override fun toHTML(ast: ASTNode): FormattedExpression {
        return express(ast, "html")
    }

    /**
     * 표현식을 JSON 형식으로 변환합니다.
     */
    override fun toJSON(ast: ASTNode): FormattedExpression {
        val formatted = format(ast)
        return factory.createFormattedExpression(
            content = formatted.toJson(),
            format = "json",
            metadata = mapOf("originalFormat" to formatted.style.name)
        )
    }

    /**
     * 표현식을 XML 형식으로 변환합니다.
     */
    override fun toXML(ast: ASTNode): FormattedExpression {
        val formatted = format(ast)
        return factory.createFormattedExpression(
            content = formatted.toXml(),
            format = "xml",
            metadata = mapOf("originalFormat" to formatted.style.name)
        )
    }

    /**
     * 표현식의 가독성을 향상시킵니다.
     */
    override fun beautify(expression: String): FormattedExpression {
        val options = FormattingOptions.forStyle(FormattingStyle.MATHEMATICAL)
            .withSpaces(true)
            .withOperatorSpacing(true)
        return reformat(expression, options)
    }

    /**
     * 표현식을 압축합니다.
     */
    override fun minify(expression: String): FormattedExpression {
        val options = FormattingOptions.compact()
        val formatted = reformat(expression, options)
        val optimized = policy.applyPerformanceOptimization(formatted.expression, options)
        return formatted.copy(expression = optimized)
    }

    /**
     * 표현식에 구문 강조를 적용합니다.
     */
    override fun highlight(expression: String, scheme: String): FormattedExpression {
        val formatted = reformat(expression)
        val highlighted = when (scheme) {
            "dark" -> applyDarkSyntaxHighlight(formatted.expression)
            "light" -> applyLightSyntaxHighlight(formatted.expression)
            else -> formatted.expression
        }
        return formatted.copy(expression = highlighted)
    }

    /**
     * 표현식의 복잡한 부분을 시각적으로 강조합니다.
     */
    override fun visualizeComplexity(ast: ASTNode): FormattedExpression {
        val formatted = format(ast)
        val complexity = formatted.calculateComplexity()
        return factory.createFormattedExpression(
            content = formatted.expression,
            format = "complexity-visualized",
            metadata = mapOf(
                "complexity" to complexity,
                "visualization" to "enabled"
            )
        )
    }

    /**
     * 표현식의 실행 순서를 시각적으로 표시합니다.
     */
    override fun visualizeEvaluationOrder(ast: ASTNode): FormattedExpression {
        val formatted = format(ast)
        // 간단한 실행 순서 표시 시뮬레이션
        val withOrder = addEvaluationOrder(formatted.expression)
        return formatted.copy(expression = withOrder)
    }

    /**
     * 표현식을 단계별로 분해하여 표시합니다.
     */
    override fun breakdownSteps(ast: ASTNode): List<FormattedExpression> {
        val mainFormatted = format(ast)
        return BREAKDOWN_STEPS.map { step ->
            factory.createFormattedExpression(step, "step")
        } + mainFormatted
    }

    /**
     * 지원되는 출력 형식 목록을 반환합니다.
     */
    override fun getSupportedFormats(): Set<String> {
        return SUPPORTED_FORMATS
    }

    /**
     * 지원되는 색상 스키마 목록을 반환합니다.
     */
    override fun getSupportedColorSchemes(): Set<String> {
        return SUPPORTED_COLOR_SCHEMES
    }

    /**
     * 형식화 옵션을 검증합니다.
     */
    override fun validateOptions(options: FormattingOptions): Boolean {
        return policy.isFormattingAllowed(options) && options.isValid()
    }

    /**
     * 특정 형식이 지원되는지 확인합니다.
     */
    override fun supportsFormat(format: String): Boolean {
        return getSupportedFormats().contains(format.lowercase())
    }

    /**
     * 표현식의 예상 출력 크기를 추정합니다.
     */
    override fun estimateOutputSize(ast: ASTNode, format: String): Int {
        val baseSize = ast.toString().length
        val multiplier = OUTPUT_SIZE_MULTIPLIERS[format.lowercase()] ?: 1.0
        return (baseSize * multiplier).toInt()
    }

    /**
     * 서비스의 설정 정보를 반환합니다.
     */
    override fun getConfiguration(): Map<String, Any> {
        return mapOf(
            "serviceName" to "ExpresserService",
            "defaultTimeoutMs" to config.defaultTimeoutMs,
            "maxRetries" to config.maxRetries,
            "cacheEnabled" to config.cachingEnabled,
            "maxCacheSize" to config.maxCacheSize,
            "enableQualityCheck" to config.enableQualityCheck,
            "enableSecurityFilter" to config.enableSecurityFilter,
            "supportedFormats" to config.supportedFormats,
            "supportedColorSchemes" to getSupportedColorSchemes()
        )
    }

    /**
     * 서비스의 통계 정보를 반환합니다.
     */
    override fun getStatistics(): Map<String, Any> {
        val metrics = performanceMetrics.getMetrics()
        return metrics + mapOf(
            "currentCacheSize" to formattingCache.size,
            "policyStatistics" to policy.getStatistics(),
            "qualitySpecStatistics" to qualitySpec.getStatistics()
        )
    }

    /**
     * 서비스를 초기화합니다.
     */
    override fun reset() {
        formattingCache.clear()
        performanceMetrics.reset()
    }

    /**
     * 서비스가 활성 상태인지 확인합니다.
     */
    override fun isActive(): Boolean {
        return true // 간단한 구현
    }

    /**
     * 캐시를 관리합니다.
     */
    override fun setCachingEnabled(enable: Boolean) {
        if (!enable) {
            formattingCache.clear()
        }
    }

    /**
     * 출력 품질 수준을 설정합니다.
     */
    override fun setQualityLevel(level: String) {
        // 품질 수준에 따른 설정 조정
    }

    /**
     * 성능 최적화 모드를 설정합니다.
     */
    override fun setOptimizationEnabled(enabled: Boolean) {
        // 최적화 모드 설정
    }

    /**
     * 다국어 지원을 위한 로케일을 설정합니다.
     */
    override fun setLocale(locale: String) {
        // 로케일 설정
    }

    /**
     * 수식 렌더링을 위한 폰트를 설정합니다.
     */
    override fun setFont(fontFamily: String, fontSize: Int) {
        // 폰트 설정
    }

    /**
     * 출력 스타일 테마를 설정합니다.
     */
    override fun setTheme(theme: String) {
        // 테마 설정
    }

    /**
     * 형식화 요청의 유효성을 검증합니다.
     */
    private fun validateFormattingRequest(ast: ASTNode, options: FormattingOptions) {
        // 1. 옵션 유효성 검증
        if (!policy.isFormattingAllowed(options)) {
            throw ExpresserException.invalidFormatOption(options.toString())
        }
        
        // 2. 복잡도 검증
        if (!policy.isComplexityAcceptable(ast)) {
            throw ExpresserException.formattingError("complexity_check_failed", "복잡도 초과")
        }
    }
    
    /**
     * 캐시된 결과를 시도합니다.
     */
    private fun tryGetCachedResult(ast: ASTNode, options: FormattingOptions): FormattedExpression? {
        val cacheKey = generateCacheKey(ast, options)
        val cachedResult = getCachedFormatting(cacheKey)
        if (cachedResult != null) {
            performanceMetrics.incrementCacheHits()
            return cachedResult.toFormattedExpression()
        }
        return null
    }
    
    /**
     * 실제 형식화를 실행합니다.
     */
    private fun executeFormatting(ast: ASTNode, options: FormattingOptions): FormattedExpression {
        val formatter = factory.createCustomFormatter(options)
        return formatter.format(ast)
    }
    
    /**
     * 형식화 품질을 검증합니다.
     */
    private fun validateFormattingQuality(formatted: FormattedExpression, options: FormattingOptions): FormattedExpression {
        if (!qualitySpec.isSatisfiedBy(formatted, options)) {
            val issues = qualitySpec.identifyQualityIssues(formatted, options)
            val criticalIssues = issues.filter { it.severity == FormattingQualitySpec.QualityIssue.Severity.HIGH }
            if (criticalIssues.isNotEmpty()) {
                throw ExpresserException.formattingError(
                    "quality_check_failed",
                    "품질 기준 미달: ${criticalIssues.joinToString { it.message }}"
                )
            }
        }
        return formatted
    }
    
    /**
     * 보안 필터링을 적용합니다.
     */
    private fun applySecurityFiltering(formatted: FormattedExpression): FormattedExpression {
        val safeContent = policy.applySecurityFilter(formatted.expression, "text")
        return formatted.copy(expression = safeContent)
    }
    
    /**
     * 최종 결과를 캐싱합니다.
     */
    private fun cacheFinalResult(ast: ASTNode, options: FormattingOptions, formatted: FormattedExpression): FormattedExpression {
        val cacheKey = generateCacheKey(ast, options)
        cacheFormatting(cacheKey, formatted)
        return formatted
    }
    
    /**
     * 메트릭을 업데이트하고 마무리합니다.
     */
    private fun updateMetricsAndFinalize(startTime: Long, formatted: FormattedExpression) {
        val executionTime = System.currentTimeMillis() - startTime
        policy.updateMetrics("format", executionTime, formatted.expression.length)
        performanceMetrics.updateExecutionTime(executionTime)
    }


    private fun generateCacheKey(ast: ASTNode, options: FormattingOptions): String {
        return "${ast.toString().hashCode()}_${options.hashCode()}"
    }

    private fun getCachedFormatting(key: String): CachedFormatting? {
        return formattingCache[key]?.takeIf { 
            System.currentTimeMillis() - it.timestamp < CACHE_VALIDITY_MS
        }
    }

    private fun cacheFormatting(key: String, formatted: FormattedExpression) {
        if (formattingCache.size < MAX_CACHE_SIZE) {
            formattingCache[key] = CachedFormatting(
                formatted = formatted,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun applyDarkSyntaxHighlight(content: String): String {
        val numberClass = SYNTAX_HIGHLIGHT_CLASSES["dark"]?.get("number") ?: "number-dark"
        return content.replace(Regex("\\d+")) { "<span class=\"$numberClass\">${it.value}</span>" }
    }

    private fun applyLightSyntaxHighlight(content: String): String {
        val numberClass = SYNTAX_HIGHLIGHT_CLASSES["light"]?.get("number") ?: "number-light"
        return content.replace(Regex("\\d+")) { "<span class=\"$numberClass\">${it.value}</span>" }
    }

    private fun addEvaluationOrder(content: String): String {
        // 간단한 실행 순서 표시
        return "1→($content)"
    }

    /**
     * 캐시된 형식화 결과를 나타내는 데이터 클래스입니다.
     */
    private data class CachedFormatting(
        val formatted: FormattedExpression,
        val timestamp: Long
    ) {
        fun toFormattedExpression(): FormattedExpression {
            return formatted.copy(createdAt = System.currentTimeMillis())
        }
    }

    /**
     * 성능 메트릭을 관리하는 클래스입니다.
     */
    private class PerformanceMetrics {
        private var totalRequests = 0L
        private var totalFailures = 0L
        private var totalCacheHits = 0L
        private var totalExecutionTime = 0L
        private var requestCount = 0L

        fun incrementTotalRequests() = synchronized(this) { totalRequests++ }
        fun incrementFailures() = synchronized(this) { totalFailures++ }
        fun incrementCacheHits() = synchronized(this) { totalCacheHits++ }
        
        fun updateExecutionTime(time: Long) = synchronized(this) {
            totalExecutionTime += time
            requestCount++
        }

        fun reset() = synchronized(this) {
            totalRequests = 0
            totalFailures = 0
            totalCacheHits = 0
            totalExecutionTime = 0
            requestCount = 0
        }

        fun getMetrics(): Map<String, Any> = synchronized(this) {
            mapOf(
                "totalRequests" to totalRequests,
                "totalFailures" to totalFailures,
                "totalCacheHits" to totalCacheHits,
                "averageExecutionTime" to if (requestCount > 0) totalExecutionTime.toDouble() / requestCount else 0.0,
                "successRate" to if (totalRequests > 0) ((totalRequests - totalFailures).toDouble() / totalRequests) else 0.0,
                "cacheHitRate" to if (totalRequests > 0) (totalCacheHits.toDouble() / totalRequests) else 0.0
            )
        }
    }
}