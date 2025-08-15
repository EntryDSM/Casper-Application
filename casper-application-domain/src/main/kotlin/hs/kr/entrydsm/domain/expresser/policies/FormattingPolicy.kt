package hs.kr.entrydsm.domain.expresser.policies

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 표현식 형식화 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 표현식 형식화 과정에서 적용되는
 * 비즈니스 규칙과 정책을 캡슐화합니다. 가독성, 보안, 성능과
 * 관련된 형식화 정책을 중앙 집중식으로 관리하여 일관성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "Formatting",
    description = "표현식 형식화 과정의 비즈니스 규칙과 정책을 관리",
    domain = "expresser",
    scope = Scope.DOMAIN
)
class FormattingPolicy {

    companion object {
        private const val MAX_OUTPUT_LENGTH = 100000
        private const val MAX_DEPTH_FOR_PRETTY_PRINT = 50
        private const val MAX_LINE_LENGTH = 120
        private const val MAX_NESTING_LEVELS = 20
        private const val MIN_FONT_SIZE = 8
        private const val MAX_FONT_SIZE = 72
        
        // 허용된 출력 형식들
        private val ALLOWED_FORMATS = setOf(
            "infix", "prefix", "postfix", "latex", "mathml", "html", 
            "json", "xml", "text", "unicode", "ascii"
        )
        
        // 허용된 색상 스키마들
        private val ALLOWED_COLOR_SCHEMES = setOf(
            "default", "dark", "light", "high-contrast", "colorblind",
            "solarized", "monokai", "github", "idea"
        )
        
        // 허용된 테마들
        private val ALLOWED_THEMES = setOf(
            "classic", "modern", "minimal", "academic", "presentation",
            "print", "web", "mobile"
        )
        
        // 안전한 HTML 태그들
        private val SAFE_HTML_TAGS = setOf(
            "span", "div", "sub", "sup", "em", "strong", "i", "b",
            "math", "mi", "mn", "mo", "mrow", "mfrac", "msqrt", "mroot"
        )
        
        // 금지된 문자들 (보안)
        private val FORBIDDEN_CHARACTERS = setOf(
            '<', '>', '"', '\'', '&', '\u0000', '\u001F'
        ).filter { it.code <= 31 || it.code == 127 }.toSet()
    }

    private val formatMetrics = mutableMapOf<String, FormatMetrics>()

    /**
     * 형식화 옵션이 허용되는지 검증합니다.
     *
     * @param options 검증할 형식화 옵션
     * @return 허용되면 true
     */
    fun isFormattingAllowed(options: FormattingOptions): Boolean {
        return try {
            validateFormat(options.style.toString()) &&
            validateColorScheme(options.toString()) &&
            validateTheme(options.toString()) &&
            validateSafety(options) &&
            validatePerformance(options)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 출력 길이가 허용 범위 내인지 확인합니다.
     *
     * @param content 확인할 내용
     * @return 허용 범위 내면 true
     */
    fun isOutputLengthAcceptable(content: String): Boolean {
        return content.length <= MAX_OUTPUT_LENGTH
    }

    /**
     * 형식화 복잡도가 허용 범위 내인지 확인합니다.
     *
     * @param ast 확인할 AST 노드
     * @return 허용 범위 내면 true
     */
    fun isComplexityAcceptable(ast: ASTNode): Boolean {
        val depth = calculateASTDepth(ast)
        val nodeCount = countASTNodes(ast)
        
        return depth <= MAX_DEPTH_FOR_PRETTY_PRINT &&
               nodeCount <= 10000 // 노드 개수 제한
    }

    /**
     * 출력 내용이 안전한지 확인합니다.
     *
     * @param content 확인할 내용
     * @param format 출력 형식
     * @return 안전하면 true
     */
    fun isContentSafe(content: String, format: String): Boolean {
        return when (format.lowercase()) {
            "html", "mathml", "xml" -> isHTMLSafe(content)
            "latex" -> isLatexSafe(content)
            "json" -> isJSONSafe(content)
            else -> isGenerallySafe(content)
        }
    }

    /**
     * 폰트 설정이 유효한지 확인합니다.
     *
     * @param fontFamily 폰트 패밀리
     * @param fontSize 폰트 크기
     * @return 유효하면 true
     */
    fun isFontSettingValid(fontFamily: String, fontSize: Int): Boolean {
        return fontFamily.isNotBlank() &&
               fontFamily.length <= 100 &&
               fontSize in MIN_FONT_SIZE..MAX_FONT_SIZE &&
               !containsDangerousContent(fontFamily)
    }

    /**
     * 색상 값이 유효한지 확인합니다.
     *
     * @param color 색상 값 (예: "#FF0000", "red", "rgb(255,0,0)")
     * @return 유효하면 true
     */
    fun isColorValid(color: String): Boolean {
        return when {
            color.startsWith("#") -> isHexColorValid(color)
            color.startsWith("rgb") -> isRGBColorValid(color)
            color.startsWith("hsl") -> isHSLColorValid(color)
            else -> isNamedColorValid(color)
        }
    }

    /**
     * 줄 길이 제한을 적용합니다.
     *
     * @param content 내용
     * @param maxLength 최대 줄 길이
     * @return 제한이 적용된 내용
     */
    fun applyLineLengthLimit(content: String, maxLength: Int = MAX_LINE_LENGTH): String {
        val effectiveMaxLength = maxLength.coerceAtMost(MAX_LINE_LENGTH)
        
        return content.lines().joinToString("\n") { line ->
            if (line.length <= effectiveMaxLength) {
                line
            } else {
                // 적절한 위치에서 줄 바꿈
                breakLongLine(line, effectiveMaxLength)
            }
        }
    }

    /**
     * 중첩 깊이 제한을 적용합니다.
     *
     * @param ast AST 노드
     * @return 제한 적용 여부
     */
    fun applyNestingLimit(ast: ASTNode): Boolean {
        val depth = calculateASTDepth(ast)
        return depth <= MAX_NESTING_LEVELS
    }

    /**
     * 보안 필터를 적용합니다.
     *
     * @param content 필터링할 내용
     * @param format 출력 형식
     * @return 필터링된 내용
     */
    fun applySecurityFilter(content: String, format: String): String {
        var filtered = content
        
        // 금지된 문자 제거
        FORBIDDEN_CHARACTERS.forEach { char ->
            filtered = filtered.replace(char.toString(), "")
        }
        
        // 형식별 특수 필터링
        filtered = when (format.lowercase()) {
            "html", "mathml", "xml" -> escapeHTML(filtered)
            "latex" -> escapeLatex(filtered)
            "json" -> escapeJSON(filtered)
            else -> filtered
        }
        
        return filtered
    }

    /**
     * 성능 최적화를 적용합니다.
     *
     * @param content 최적화할 내용
     * @param options 형식화 옵션
     * @return 최적화된 내용
     */
    fun applyPerformanceOptimization(content: String, options: FormattingOptions): String {
        var optimized = content
        
        // 불필요한 공백 제거 (설정에 따라)
        if (options.toString().contains("minify")) {
            optimized = optimized.replace(Regex("\\s+"), " ").trim()
        }
        
        // 중복 제거
        optimized = removeDuplicateSpaces(optimized)
        
        // 길이 제한 적용
        if (optimized.length > MAX_OUTPUT_LENGTH) {
            optimized = optimized.take(MAX_OUTPUT_LENGTH - 3) + "..."
        }
        
        return optimized
    }

    /**
     * 접근성 가이드라인을 적용합니다.
     *
     * @param content 내용
     * @param options 형식화 옵션
     * @return 접근성이 개선된 내용
     */
    fun applyAccessibilityGuidelines(content: String, options: FormattingOptions): String {
        var accessible = content
        
        // 색상 대비 개선
        if (options.toString().contains("high-contrast")) {
            accessible = applyHighContrastColors(accessible)
        }
        
        // 스크린 리더 지원 개선
        accessible = addAriaLabels(accessible)
        
        // 대체 텍스트 추가
        accessible = addAltText(accessible)
        
        return accessible
    }

    /**
     * 형식화 메트릭을 업데이트합니다.
     *
     * @param format 형식
     * @param processingTime 처리 시간
     * @param outputSize 출력 크기
     */
    fun updateMetrics(format: String, processingTime: Long, outputSize: Int) {
        val metrics = formatMetrics.getOrPut(format) { FormatMetrics() }
        metrics.update(processingTime, outputSize)
    }

    // Private helper methods

    private fun validateFormat(format: String): Boolean {
        return format.lowercase() in ALLOWED_FORMATS
    }

    private fun validateColorScheme(scheme: String): Boolean {
        return scheme.lowercase() in ALLOWED_COLOR_SCHEMES
    }

    private fun validateTheme(theme: String): Boolean {
        return theme.lowercase() in ALLOWED_THEMES
    }

    private fun validateSafety(options: FormattingOptions): Boolean {
        val optionsStr = options.toString()
        return !containsDangerousContent(optionsStr)
    }

    private fun validatePerformance(options: FormattingOptions): Boolean {
        // 성능에 영향을 줄 수 있는 옵션들 검사
        return true // 간단한 구현
    }

    private fun calculateASTDepth(ast: ASTNode): Int {
        return if (ast.getChildren().isEmpty()) {
            1
        } else {
            1 + (ast.getChildren().maxOfOrNull { calculateASTDepth(it) } ?: 0)
        }
    }

    private fun countASTNodes(ast: ASTNode): Int {
        return 1 + ast.getChildren().sumOf { countASTNodes(it) }
    }

    private fun isHTMLSafe(content: String): Boolean {
        // HTML 태그 안전성 검사
        val tagPattern = Regex("<(/?)([a-zA-Z][a-zA-Z0-9]*)")
        val matches = tagPattern.findAll(content)
        
        return matches.all { match ->
            val tagName = match.groupValues[2].lowercase()
            tagName in SAFE_HTML_TAGS
        }
    }

    private fun isLatexSafe(content: String): Boolean {
        // LaTeX 명령어 안전성 검사
        val dangerousCommands = setOf("\\input", "\\include", "\\write", "\\openout", "\\read")
        return dangerousCommands.none { content.contains(it) }
    }

    private fun isJSONSafe(content: String): Boolean {
        // JSON 안전성 검사
        try {
            // 기본적인 JSON 형식 검사
            return !content.contains("</script>") && !content.contains("javascript:")
        } catch (e: Exception) {
            return false
        }
    }

    private fun isGenerallySafe(content: String): Boolean {
        return !containsDangerousContent(content)
    }

    private fun containsDangerousContent(content: String): Boolean {
        val dangerousPatterns = listOf(
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("vbscript:", RegexOption.IGNORE_CASE),
            Regex("data:", RegexOption.IGNORE_CASE),
            Regex("\\\\def", RegexOption.IGNORE_CASE),
            Regex("\\\\let", RegexOption.IGNORE_CASE)
        )
        
        return dangerousPatterns.any { it.containsMatchIn(content) }
    }

    private fun isHexColorValid(color: String): Boolean {
        return Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$").matches(color)
    }

    private fun isRGBColorValid(color: String): Boolean {
        return Regex("^rgb\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*\\)$").matches(color)
    }

    private fun isHSLColorValid(color: String): Boolean {
        return Regex("^hsl\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}%\\s*,\\s*\\d{1,3}%\\s*\\)$").matches(color)
    }

    private fun isNamedColorValid(color: String): Boolean {
        val namedColors = setOf(
            "red", "green", "blue", "black", "white", "yellow", "orange",
            "purple", "pink", "brown", "gray", "grey", "cyan", "magenta"
        )
        return color.lowercase() in namedColors
    }

    private fun breakLongLine(line: String, maxLength: Int): String {
        if (line.length <= maxLength) return line
        
        // 연산자나 공백에서 줄 바꿈
        val breakPoints = listOf(" + ", " - ", " * ", " / ", " = ", " < ", " > ", ", ")
        
        for (breakPoint in breakPoints) {
            val index = line.lastIndexOf(breakPoint, maxLength - breakPoint.length)
            if (index > 0) {
                return line.substring(0, index + breakPoint.length) + "\n  " +
                       breakLongLine(line.substring(index + breakPoint.length), maxLength)
            }
        }
        
        // 적절한 구분점이 없으면 강제로 자름
        return line.substring(0, maxLength - 3) + "...\n  " +
               breakLongLine(line.substring(maxLength - 3), maxLength)
    }

    private fun removeDuplicateSpaces(content: String): String {
        return content.replace(Regex("\\s+"), " ")
    }

    private fun escapeHTML(content: String): String {
        return content.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#x27;")
    }

    private fun escapeLatex(content: String): String {
        return content.replace("\\", "\\textbackslash{}")
                     .replace("{", "\\{")
                     .replace("}", "\\}")
                     .replace("$", "\\$")
                     .replace("&", "\\&")
                     .replace("%", "\\%")
                     .replace("#", "\\#")
                     .replace("^", "\\textasciicircum{}")
                     .replace("_", "\\_")
                     .replace("~", "\\textasciitilde{}")
    }

    private fun escapeJSON(content: String): String {
        return content.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("/", "\\/")
                     .replace("\b", "\\b")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t")
    }

    private fun applyHighContrastColors(content: String): String {
        // 고대비 색상 적용 (간단한 구현)
        return content.replace("color: gray", "color: black")
                     .replace("color: #888", "color: #000")
    }

    private fun addAriaLabels(content: String): String {
        // ARIA 라벨 추가 (간단한 구현)
        return content.replace("<math", "<math role=\"math\" aria-label=\"mathematical expression\"")
    }

    private fun addAltText(content: String): String {
        // 대체 텍스트 추가 (간단한 구현)
        return content // 실제로는 수식에 대한 텍스트 설명 추가
    }

    /**
     * 형식별 메트릭을 관리하는 클래스입니다.
     */
    private class FormatMetrics {
        private var totalRequests = 0L
        private var totalProcessingTime = 0L
        private var totalOutputSize = 0L

        fun update(processingTime: Long, outputSize: Int) {
            totalRequests++
            totalProcessingTime += processingTime
            totalOutputSize += outputSize
        }

        fun getAverageProcessingTime(): Double = 
            if (totalRequests > 0) totalProcessingTime.toDouble() / totalRequests else 0.0

        fun getAverageOutputSize(): Double = 
            if (totalRequests > 0) totalOutputSize.toDouble() / totalRequests else 0.0
    }

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxOutputLength" to MAX_OUTPUT_LENGTH,
        "maxDepthForPrettyPrint" to MAX_DEPTH_FOR_PRETTY_PRINT,
        "maxLineLength" to MAX_LINE_LENGTH,
        "maxNestingLevels" to MAX_NESTING_LEVELS,
        "minFontSize" to MIN_FONT_SIZE,
        "maxFontSize" to MAX_FONT_SIZE,
        "allowedFormats" to ALLOWED_FORMATS.size,
        "allowedColorSchemes" to ALLOWED_COLOR_SCHEMES.size,
        "allowedThemes" to ALLOWED_THEMES.size,
        "safeHtmlTags" to SAFE_HTML_TAGS.size
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "FormattingPolicy",
        "activeFormats" to formatMetrics.size,
        "securityRules" to listOf("html_safety", "latex_safety", "json_safety", "content_filtering"),
        "performanceRules" to listOf("length_limits", "nesting_limits", "line_breaks"),
        "accessibilityFeatures" to listOf("high_contrast", "aria_labels", "alt_text")
    )
}