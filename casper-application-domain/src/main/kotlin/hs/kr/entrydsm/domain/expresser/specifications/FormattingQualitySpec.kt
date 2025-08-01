package hs.kr.entrydsm.domain.expresser.specifications

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression
import hs.kr.entrydsm.global.annotation.specification.Specification

/**
 * 형식화 품질 검증 명세를 구현하는 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 표현식 형식화의 품질을 검증하는
 * 복합적인 비즈니스 규칙을 캡슐화합니다. 가독성, 정확성, 일관성,
 * 접근성 등을 통해 형식화 결과의 품질을 판단합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "FormattingQuality",
    description = "표현식 형식화의 품질과 적절성을 검증하는 명세",
    domain = "expresser",
    priority = hs.kr.entrydsm.global.annotation.specification.type.Priority.HIGH
)
class FormattingQualitySpec {

    companion object {
        private const val MIN_READABILITY_SCORE = 70
        private const val MAX_LINE_LENGTH = 120
        private const val MIN_CONTRAST_RATIO = 4.5
        private const val MAX_NESTING_DEPTH = 10
        private const val MIN_FONT_SIZE = 10
        private const val MAX_COMPLEXITY_SCORE = 100
        
        // 가독성 개선 패턴들
        private val READABILITY_PATTERNS = mapOf(
            "proper_spacing" to Regex("\\s+[+\\-*/]\\s+"),
            "parentheses_balance" to Regex("\\([^()]*\\)"),
            "function_formatting" to Regex("[a-zA-Z]\\w*\\s*\\([^)]*\\)"),
            "subscript_superscript" to Regex("[a-zA-Z]\\w*[_^]\\{[^}]*\\}")
        )
        
        // 품질 기준들
        private val QUALITY_CRITERIA = mapOf(
            "readability" to 0.3,
            "accuracy" to 0.3,
            "consistency" to 0.2,
            "accessibility" to 0.1,
            "aesthetics" to 0.1
        )
        
        // 형식별 품질 기준
        private val FORMAT_QUALITY_STANDARDS = mapOf(
            "latex" to mapOf("math_symbols" to true, "subscript_superscript" to true),
            "mathml" to mapOf("semantic_markup" to true, "accessibility" to true),
            "html" to mapOf("semantic_tags" to true, "css_styling" to true),
            "text" to mapOf("unicode_symbols" to true, "ascii_fallback" to true)
        )
    }

    /**
     * 형식화된 표현식이 품질 기준을 만족하는지 검증합니다.
     *
     * @param formatted 검증할 형식화된 표현식
     * @param options 사용된 형식화 옵션
     * @return 품질 기준을 만족하면 true
     */
    fun isSatisfiedBy(formatted: FormattedExpression, options: FormattingOptions): Boolean {
        return try {
            val qualityScore = calculateQualityScore(formatted, options)
            qualityScore >= MIN_READABILITY_SCORE
        } catch (e: Exception) {
            false
        }
    }

    /**
     * AST와 형식화 옵션으로부터 예상 품질을 검증합니다.
     *
     * @param ast 원본 AST
     * @param options 형식화 옵션
     * @return 예상 품질이 기준을 만족하면 true
     */
    fun isSatisfiedBy(ast: ASTNode, options: FormattingOptions): Boolean {
        return try {
            validateStructuralComplexity(ast) &&
            validateFormattingOptions(options) &&
            predictFormattingQuality(ast, options) >= MIN_READABILITY_SCORE
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 가독성 점수를 계산합니다.
     *
     * @param formatted 형식화된 표현식
     * @return 가독성 점수 (0-100)
     */
    fun calculateReadabilityScore(formatted: FormattedExpression): Int {
        var score = 0
        
        // 적절한 공백 사용
        score += if (hasProperSpacing(formatted.expression)) 20 else 0
        
        // 줄 길이 적절성
        score += if (hasAppropriateLineLength(formatted.expression)) 15 else 0
        
        // 괄호 균형
        score += if (hasBalancedParentheses(formatted.expression)) 15 else 0
        
        // 일관된 형식화
        score += if (hasConsistentFormatting(formatted.expression)) 20 else 0
        
        // 복잡도 관리
        score += if (hasManagedComplexity(formatted.expression)) 15 else 0
        
        // 특수 문자 사용 적절성
        score += if (hasAppropriateSymbolUsage(formatted.expression)) 15 else 0
        
        return score.coerceIn(0, 100)
    }

    /**
     * 정확성 점수를 계산합니다.
     *
     * @param original 원본 AST
     * @param formatted 형식화된 표현식
     * @return 정확성 점수 (0-100)
     */
    fun calculateAccuracyScore(original: ASTNode, formatted: FormattedExpression): Int {
        var score = 0
        
        // 의미 보존
        score += if (preservesMeaning(original, formatted)) 40 else 0
        
        // 구조 보존
        score += if (preservesStructure(original, formatted)) 30 else 0
        
        // 연산자 우선순위 보존
        score += if (preservesOperatorPrecedence(original, formatted)) 20 else 0
        
        // 변수명 보존
        score += if (preservesVariableNames(original, formatted)) 10 else 0
        
        return score.coerceIn(0, 100)
    }

    /**
     * 일관성 점수를 계산합니다.
     *
     * @param formatted 형식화된 표현식
     * @param options 형식화 옵션
     * @return 일관성 점수 (0-100)
     */
    fun calculateConsistencyScore(formatted: FormattedExpression, options: FormattingOptions): Int {
        var score = 0
        
        // 스타일 일관성
        score += if (hasConsistentStyle(formatted, options)) 30 else 0
        
        // 들여쓰기 일관성
        score += if (hasConsistentIndentation(formatted.expression)) 25 else 0
        
        // 색상 사용 일관성
        score += if (hasConsistentColors(formatted)) 20 else 0
        
        // 폰트 사용 일관성
        score += if (hasConsistentFonts(formatted)) 15 else 0
        
        // 기호 사용 일관성
        score += if (hasConsistentSymbols(formatted.expression)) 10 else 0
        
        return score.coerceIn(0, 100)
    }

    /**
     * 접근성 점수를 계산합니다.
     *
     * @param formatted 형식화된 표현식
     * @return 접근성 점수 (0-100)
     */
    fun calculateAccessibilityScore(formatted: FormattedExpression): Int {
        var score = 0
        
        // 색상 대비
        score += if (hasAdequateColorContrast(formatted)) 30 else 0
        
        // 폰트 크기 적절성
        score += if (hasAppropriateFont(formatted)) 25 else 0
        
        // 스크린 리더 지원
        score += if (hasScreenReaderSupport(formatted)) 20 else 0
        
        // 대체 텍스트
        score += if (hasAlternativeText(formatted)) 15 else 0
        
        // 키보드 접근성
        score += if (hasKeyboardAccessibility(formatted)) 10 else 0
        
        return score.coerceIn(0, 100)
    }

    /**
     * 미적 품질 점수를 계산합니다.
     *
     * @param formatted 형식화된 표현식
     * @return 미적 품질 점수 (0-100)
     */
    fun calculateAestheticScore(formatted: FormattedExpression): Int {
        var score = 0
        
        // 시각적 균형
        score += if (hasVisualBalance(formatted.expression)) 25 else 0
        
        // 적절한 여백
        score += if (hasAppropriateWhitespace(formatted.expression)) 20 else 0
        
        // 색상 조화
        score += if (hasHarmoniousColors(formatted)) 20 else 0
        
        // 타이포그래피 품질
        score += if (hasGoodTypography(formatted)) 20 else 0
        
        // 전체적인 정돈성
        score += if (hasOverallNeatness(formatted.expression)) 15 else 0
        
        return score.coerceIn(0, 100)
    }

    /**
     * 전체 품질 점수를 계산합니다.
     *
     * @param formatted 형식화된 표현식
     * @param options 형식화 옵션
     * @param original 원본 AST (선택적)
     * @return 전체 품질 점수 (0-100)
     */
    fun calculateQualityScore(
        formatted: FormattedExpression, 
        options: FormattingOptions, 
        original: ASTNode? = null
    ): Int {
        val readabilityScore = calculateReadabilityScore(formatted)
        val consistencyScore = calculateConsistencyScore(formatted, options)
        val accessibilityScore = calculateAccessibilityScore(formatted)
        val aestheticScore = calculateAestheticScore(formatted)
        
        val accuracyScore = if (original != null) {
            calculateAccuracyScore(original, formatted)
        } else {
            80 // 기본 점수
        }
        
        return (readabilityScore * QUALITY_CRITERIA["readability"]!! +
                accuracyScore * QUALITY_CRITERIA["accuracy"]!! +
                consistencyScore * QUALITY_CRITERIA["consistency"]!! +
                accessibilityScore * QUALITY_CRITERIA["accessibility"]!! +
                aestheticScore * QUALITY_CRITERIA["aesthetics"]!!).toInt()
    }

    /**
     * 복잡도 관리 품질을 검증합니다.
     *
     * @param ast AST 노드
     * @return 복잡도가 적절히 관리되면 true
     */
    fun validateComplexityManagement(ast: ASTNode): Boolean {
        val complexity = calculateComplexityScore(ast)
        return complexity <= MAX_COMPLEXITY_SCORE
    }

    /**
     * 형식화 옵션의 적절성을 검증합니다.
     *
     * @param options 형식화 옵션
     * @return 적절하면 true
     */
    fun validateFormattingOptions(options: FormattingOptions): Boolean {
        return isValidFontSize(options) &&
               isValidColorScheme(options) &&
               isValidLayoutSettings(options)
    }

    /**
     * 품질 문제들을 식별합니다.
     *
     * @param formatted 형식화된 표현식
     * @param options 형식화 옵션
     * @return 발견된 품질 문제들
     */
    fun identifyQualityIssues(formatted: FormattedExpression, options: FormattingOptions): List<QualityIssue> {
        val issues = mutableListOf<QualityIssue>()
        
        // 가독성 문제
        if (!hasProperSpacing(formatted.expression)) {
            issues.add(QualityIssue("SPACING", "부적절한 공백 사용", QualityIssue.Severity.MEDIUM))
        }
        
        if (!hasAppropriateLineLength(formatted.expression)) {
            issues.add(QualityIssue("LINE_LENGTH", "줄 길이 초과", QualityIssue.Severity.LOW))
        }
        
        // 접근성 문제
        if (!hasAdequateColorContrast(formatted)) {
            issues.add(QualityIssue("COLOR_CONTRAST", "색상 대비 부족", QualityIssue.Severity.HIGH))
        }
        
        if (!hasAppropriateFont(formatted)) {
            issues.add(QualityIssue("FONT_SIZE", "부적절한 폰트 크기", QualityIssue.Severity.MEDIUM))
        }
        
        // 일관성 문제
        if (!hasConsistentStyle(formatted, options)) {
            issues.add(QualityIssue("STYLE_CONSISTENCY", "스타일 불일치", QualityIssue.Severity.MEDIUM))
        }
        
        return issues
    }

    // Private helper methods

    private fun validateStructuralComplexity(ast: ASTNode): Boolean {
        val depth = calculateDepth(ast)
        val nodeCount = countNodes(ast)
        return depth <= MAX_NESTING_DEPTH && nodeCount <= 1000
    }

    private fun predictFormattingQuality(ast: ASTNode, options: FormattingOptions): Int {
        // 예상 품질 계산 (간단한 휴리스틱)
        var predictedScore = 70 // 기본 점수
        
        if (calculateDepth(ast) <= 5) predictedScore += 10
        if (countNodes(ast) <= 50) predictedScore += 10
        if (isValidFontSize(options)) predictedScore += 5
        if (isValidColorScheme(options)) predictedScore += 5
        
        return predictedScore.coerceIn(0, 100)
    }

    private fun hasProperSpacing(content: String): Boolean {
        return READABILITY_PATTERNS["proper_spacing"]?.containsMatchIn(content) ?: false
    }

    private fun hasAppropriateLineLength(content: String): Boolean {
        return content.lines().all { it.length <= MAX_LINE_LENGTH }
    }

    private fun hasBalancedParentheses(content: String): Boolean {
        var balance = 0
        for (char in content) {
            when (char) {
                '(' -> balance++
                ')' -> {
                    balance--
                    if (balance < 0) return false
                }
            }
        }
        return balance == 0
    }

    private fun hasConsistentFormatting(content: String): Boolean {
        // 일관된 들여쓰기와 공백 사용 검사
        val lines = content.lines()
        if (lines.size <= 1) return true
        
        val indentationPattern = lines.firstOrNull { it.isNotBlank() }?.takeWhile { it.isWhitespace() }?.length ?: 0
        return lines.filter { it.isNotBlank() }.all { line ->
            line.takeWhile { it.isWhitespace() }.length % (indentationPattern.coerceAtLeast(1)) == 0
        }
    }

    private fun hasManagedComplexity(content: String): Boolean {
        val complexity = content.length + content.count { it in "(){}[]" } * 2
        return complexity <= MAX_COMPLEXITY_SCORE
    }

    private fun hasAppropriateSymbolUsage(content: String): Boolean {
        // 유니코드 수학 기호의 적절한 사용 검사
        val mathSymbols = setOf('∑', '∏', '∫', '√', '∞', 'π', 'α', 'β', 'γ', 'θ', 'λ', 'μ', 'σ', 'Δ')
        val symbolCount = content.count { it in mathSymbols }
        return symbolCount <= content.length * 0.1 // 전체 길이의 10% 이하
    }

    private fun preservesMeaning(original: ASTNode, formatted: FormattedExpression): Boolean {
        // 의미 보존 검사 (간단한 구현)
        return true // 실제로는 더 정교한 의미 분석 필요
    }

    private fun preservesStructure(original: ASTNode, formatted: FormattedExpression): Boolean {
        // 구조 보존 검사 (간단한 구현)
        return true // 실제로는 AST 구조 비교 필요
    }

    private fun preservesOperatorPrecedence(original: ASTNode, formatted: FormattedExpression): Boolean {
        // 연산자 우선순위 보존 검사
        return !formatted.expression.contains(Regex("\\d\\s*[+\\-]\\s*\\d\\s*[*/]\\s*\\d"))
    }

    private fun preservesVariableNames(original: ASTNode, formatted: FormattedExpression): Boolean {
        // 변수명 보존 검사
        val originalVars = extractVariables(original.toString())
        val formattedVars = extractVariables(formatted.expression)
        return originalVars == formattedVars
    }

    private fun hasConsistentStyle(formatted: FormattedExpression, options: FormattingOptions): Boolean {
        // 스타일 일관성 검사 (간단한 구현)
        return true
    }

    private fun hasConsistentIndentation(content: String): Boolean {
        return hasConsistentFormatting(content)
    }

    private fun hasConsistentColors(formatted: FormattedExpression): Boolean {
        // 색상 일관성 검사 (간단한 구현)
        return formatted.style.name != "HTML" || formatted.expression.count { it == '#' } <= 5
    }

    private fun hasConsistentFonts(formatted: FormattedExpression): Boolean {
        // 폰트 일관성 검사 (간단한 구현)
        return formatted.style.name != "HTML" || !formatted.expression.contains("font-family")
    }

    private fun hasConsistentSymbols(content: String): Boolean {
        // 기호 사용 일관성 검사
        return true // 간단한 구현
    }

    private fun hasAdequateColorContrast(formatted: FormattedExpression): Boolean {
        // 색상 대비 검사 (간단한 구현)
        return formatted.style.name != "HTML" || !formatted.expression.contains("color: gray")
    }

    private fun hasAppropriateFont(formatted: FormattedExpression): Boolean {
        // 폰트 크기 적절성 검사 (간단한 구현)
        return !formatted.expression.contains("font-size: [1-9]px") // 10px 미만 제외
    }

    private fun hasScreenReaderSupport(formatted: FormattedExpression): Boolean {
        return formatted.expression.contains("aria-label") || 
               formatted.expression.contains("alt=")
    }

    private fun hasAlternativeText(formatted: FormattedExpression): Boolean {
        return formatted.expression.contains("alt=") || formatted.expression.contains("title=")
    }

    private fun hasKeyboardAccessibility(formatted: FormattedExpression): Boolean {
        return !formatted.expression.contains("onmouse") && 
               !formatted.expression.contains("onclick")
    }

    private fun hasVisualBalance(content: String): Boolean {
        // 시각적 균형 검사 (간단한 구현)
        return content.length <= 200 || content.contains("\n")
    }

    private fun hasAppropriateWhitespace(content: String): Boolean {
        return !content.contains(Regex("\\S{50,}")) // 50자 이상 연속 문자 없음
    }

    private fun hasHarmoniousColors(formatted: FormattedExpression): Boolean {
        // 색상 조화 검사 (간단한 구현)
        return true
    }

    private fun hasGoodTypography(formatted: FormattedExpression): Boolean {
        // 타이포그래피 품질 검사 (간단한 구현)
        return true
    }

    private fun hasOverallNeatness(content: String): Boolean {
        return !content.contains(Regex("\\s{5,}")) && // 5개 이상 연속 공백 없음
               !content.contains("\t\t\t") // 과도한 탭 없음
    }

    private fun calculateComplexityScore(ast: ASTNode): Int {
        return calculateDepth(ast) * 5 + countNodes(ast)
    }

    private fun calculateDepth(ast: ASTNode): Int {
        return if (ast.getChildren().isEmpty()) {
            1
        } else {
            1 + (ast.getChildren().maxOfOrNull { calculateDepth(it) } ?: 0)
        }
    }

    private fun countNodes(ast: ASTNode): Int {
        return 1 + ast.getChildren().sumOf { countNodes(it) }
    }

    private fun isValidFontSize(options: FormattingOptions): Boolean {
        return true // 간단한 구현
    }

    private fun isValidColorScheme(options: FormattingOptions): Boolean {
        return true // 간단한 구현
    }

    private fun isValidLayoutSettings(options: FormattingOptions): Boolean {
        return true // 간단한 구현
    }

    private fun extractVariables(expression: String): Set<String> {
        val pattern = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
        return pattern.findAll(expression).map { it.value }.toSet()
    }

    /**
     * 품질 문제를 나타내는 데이터 클래스입니다.
     */
    data class QualityIssue(
        val code: String,
        val message: String,
        val severity: Severity
    ) {
        enum class Severity {
            LOW, MEDIUM, HIGH, CRITICAL
        }
    }

    /**
     * 명세의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "minReadabilityScore" to MIN_READABILITY_SCORE,
        "maxLineLength" to MAX_LINE_LENGTH,
        "minContrastRatio" to MIN_CONTRAST_RATIO,
        "maxNestingDepth" to MAX_NESTING_DEPTH,
        "minFontSize" to MIN_FONT_SIZE,
        "maxComplexityScore" to MAX_COMPLEXITY_SCORE,
        "qualityCriteria" to QUALITY_CRITERIA,
        "readabilityPatterns" to READABILITY_PATTERNS.size
    )

    /**
     * 명세의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "specificationName" to "FormattingQualitySpec",
        "qualityDimensions" to QUALITY_CRITERIA.size,
        "readabilityPatterns" to READABILITY_PATTERNS.size,
        "formatQualityStandards" to FORMAT_QUALITY_STANDARDS.size,
        "qualityChecks" to listOf("readability", "accuracy", "consistency", "accessibility", "aesthetics")
    )
}