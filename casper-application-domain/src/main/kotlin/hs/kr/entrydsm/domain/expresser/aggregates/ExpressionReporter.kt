package hs.kr.entrydsm.domain.expresser.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.entities.FormattingStyle
import hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import kotlin.math.roundToInt

/**
 * 계산 결과와 수식을 종합적으로 보고하는 집합 루트입니다.
 *
 * 계산 과정, 결과, 통계 정보를 포함한 포괄적인 보고서를 생성합니다.
 * 다양한 포맷(텍스트, HTML, JSON, XML)으로 결과를 출력할 수 있으며,
 * 교육용, 디버깅용, 문서화용 등 다양한 목적에 맞는 보고서를 제공합니다.
 *
 * @property formatter 수식 포맷터
 * @property includeSteps 단계별 설명 포함 여부
 * @property includeStatistics 통계 정보 포함 여부
 * @property includeAST AST 정보 포함 여부
 * @property maxDepth 최대 보고 깊이
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "expresser")
class ExpressionReporter(
    private val formatter: ExpressionFormatter = ExpressionFormatter.createDefault(),
    private val includeSteps: Boolean = true,
    private val includeStatistics: Boolean = true,
    private val includeAST: Boolean = false,
    private val maxDepth: Int = 10
) {

    /**
     * 단일 계산 결과에 대한 보고서를 생성합니다.
     *
     * @param result 계산 결과
     * @return 보고서 문자열
     * @throws ExpresserException 보고서 생성 중 오류 발생 시
     */
    fun generateReport(result: CalculationResult): String {
        return try {
            buildString {
                appendLine("=== 계산 결과 보고서 ===")
                appendLine()
                
                // 기본 정보
                appendLine("📊 기본 정보")
                appendLine("수식: ${result.formula}")
                appendLine("결과: ${formatter.formatResult(result.result)}")
                appendLine("실행 시간: ${result.executionTimeMs}ms")
                appendLine()
                
                // 변수 정보
                if (result.variables.isNotEmpty()) {
                    appendLine("🔢 변수 정보")
                    result.variables.forEach { (name, value) ->
                        appendLine("  $name = ${formatter.formatResult(value)}")
                    }
                    appendLine()
                }
                
                // 단계별 설명
                if (includeSteps && result.steps.isNotEmpty()) {
                    appendLine("📋 실행 단계")
                    result.steps.forEachIndexed { index, step ->
                        appendLine("  ${index + 1}. $step")
                    }
                    appendLine()
                }
                
                // AST 정보
                if (includeAST && result.ast != null) {
                    appendLine("🌳 AST 구조")
                    val formattedAST = formatter.format(result.ast)
                    appendLine("  포맷팅된 수식: ${formattedAST.expression}")
                    appendLine("  복잡도: ${formattedAST.calculateComplexity()}")
                    appendLine("  가독성: ${formattedAST.calculateReadability()}")
                    appendLine()
                }
                
                // 통계 정보
                if (includeStatistics) {
                    appendLine("📈 통계 정보")
                    appendLine("  수식 길이: ${result.formula.length}자")
                    appendLine("  변수 개수: ${result.variables.size}개")
                    appendLine("  실행 단계: ${result.steps.size}단계")
                    
                    // 결과 타입 분석
                    val resultType = when (result.result) {
                        is Double -> "실수"
                        is Int -> "정수"
                        is Boolean -> "논리값"
                        is String -> "문자열"
                        else -> "기타"
                    }
                    appendLine("  결과 타입: $resultType")
                    
                    // 성능 분석
                    val performance = when {
                        result.executionTimeMs < 1 -> "매우 빠름"
                        result.executionTimeMs < 10 -> "빠름"
                        result.executionTimeMs < 100 -> "보통"
                        result.executionTimeMs < 1000 -> "느림"
                        else -> "매우 느림"
                    }
                    appendLine("  성능: $performance")
                    appendLine()
                }
                
                appendLine("보고서 생성 시간: ${java.time.LocalDateTime.now()}")
            }
        } catch (e: Exception) {
            throw ExpresserException.reportGenerationError(e)
        }
    }

    /**
     * 다중 계산 결과에 대한 보고서를 생성합니다.
     *
     * @param results 계산 결과들
     * @return 보고서 문자열
     * @throws ExpresserException 보고서 생성 중 오류 발생 시
     */
    fun generateMultiStepReport(results: List<CalculationResult>): String {
        return try {
            buildString {
                appendLine("=== 다단계 계산 보고서 ===")
                appendLine()
                
                // 요약 정보
                appendLine("📊 요약 정보")
                appendLine("총 단계 수: ${results.size}")
                appendLine("총 실행 시간: ${results.sumOf { it.executionTimeMs }}ms")
                appendLine("평균 실행 시간: ${results.map { it.executionTimeMs }.average().roundToInt()}ms")
                appendLine()
                
                // 각 단계별 결과
                results.forEachIndexed { index, result ->
                    appendLine("=== 단계 ${index + 1} ===")
                    appendLine("수식: ${result.formula}")
                    appendLine("결과: ${formatter.formatResult(result.result)}")
                    appendLine("실행 시간: ${result.executionTimeMs}ms")
                    
                    if (result.variables.isNotEmpty()) {
                        appendLine("변수:")
                        result.variables.forEach { (name, value) ->
                            appendLine("  $name = ${formatter.formatResult(value)}")
                        }
                    }
                    appendLine()
                }
                
                // 전체 통계
                if (includeStatistics) {
                    appendLine("📈 전체 통계")
                    val totalFormulaLength = results.sumOf { it.formula.length }
                    val totalVariables = results.sumOf { it.variables.size }
                    val successRate = results.count { it.result != null } * 100.0 / results.size
                    
                    appendLine("  총 수식 길이: ${totalFormulaLength}자")
                    appendLine("  총 변수 개수: ${totalVariables}개")
                    appendLine("  성공률: ${String.format("%.1f", successRate)}%")
                    
                    // 성능 분석
                    val minTime = results.minOfOrNull { it.executionTimeMs } ?: 0
                    val maxTime = results.maxOfOrNull { it.executionTimeMs } ?: 0
                    appendLine("  최소 실행 시간: ${minTime}ms")
                    appendLine("  최대 실행 시간: ${maxTime}ms")
                    appendLine()
                }
                
                appendLine("보고서 생성 시간: ${java.time.LocalDateTime.now()}")
            }
        } catch (e: Exception) {
            throw ExpresserException.reportGenerationError(e)
        }
    }

    /**
     * HTML 형태의 보고서를 생성합니다.
     *
     * @param result 계산 결과
     * @return HTML 보고서 문자열
     * @throws ExpresserException 보고서 생성 중 오류 발생 시
     */
    fun generateHtmlReport(result: CalculationResult): String {
        return try {
            buildString {
                appendLine("<!DOCTYPE html>")
                appendLine("<html lang=\"ko\">")
                appendLine("<head>")
                appendLine("    <meta charset=\"UTF-8\">")
                appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                appendLine("    <title>계산 결과 보고서</title>")
                appendLine("    <style>")
                appendLine("        body { font-family: Arial, sans-serif; margin: 20px; }")
                appendLine("        .header { background-color: #f0f0f0; padding: 10px; border-radius: 5px; }")
                appendLine("        .section { margin: 20px 0; }")
                appendLine("        .result { font-size: 1.2em; font-weight: bold; color: #2c3e50; }")
                appendLine("        .variable { background-color: #ecf0f1; padding: 5px; margin: 2px 0; }")
                appendLine("        .step { padding: 5px; margin: 2px 0; border-left: 3px solid #3498db; }")
                appendLine("        .statistics { background-color: #f9f9f9; padding: 10px; border-radius: 5px; }")
                appendLine("    </style>")
                appendLine("</head>")
                appendLine("<body>")
                appendLine("    <div class=\"header\">")
                appendLine("        <h1>📊 계산 결과 보고서</h1>")
                appendLine("    </div>")
                
                appendLine("    <div class=\"section\">")
                appendLine("        <h2>기본 정보</h2>")
                appendLine("        <p><strong>수식:</strong> ${escapeHtml(result.formula)}</p>")
                appendLine("        <p class=\"result\"><strong>결과:</strong> ${escapeHtml(formatter.formatResult(result.result))}</p>")
                appendLine("        <p><strong>실행 시간:</strong> ${result.executionTimeMs}ms</p>")
                appendLine("    </div>")
                
                if (result.variables.isNotEmpty()) {
                    appendLine("    <div class=\"section\">")
                    appendLine("        <h2>🔢 변수 정보</h2>")
                    result.variables.forEach { (name, value) ->
                        appendLine("        <div class=\"variable\">")
                        appendLine("            <strong>$name</strong> = ${escapeHtml(formatter.formatResult(value))}")
                        appendLine("        </div>")
                    }
                    appendLine("    </div>")
                }
                
                if (includeSteps && result.steps.isNotEmpty()) {
                    appendLine("    <div class=\"section\">")
                    appendLine("        <h2>📋 실행 단계</h2>")
                    result.steps.forEachIndexed { index, step ->
                        appendLine("        <div class=\"step\">")
                        appendLine("            <strong>${index + 1}.</strong> ${escapeHtml(step)}")
                        appendLine("        </div>")
                    }
                    appendLine("    </div>")
                }
                
                if (includeStatistics) {
                    appendLine("    <div class=\"section\">")
                    appendLine("        <h2>📈 통계 정보</h2>")
                    appendLine("        <div class=\"statistics\">")
                    appendLine("            <p><strong>수식 길이:</strong> ${result.formula.length}자</p>")
                    appendLine("            <p><strong>변수 개수:</strong> ${result.variables.size}개</p>")
                    appendLine("            <p><strong>실행 단계:</strong> ${result.steps.size}단계</p>")
                    appendLine("        </div>")
                    appendLine("    </div>")
                }
                
                appendLine("    <div class=\"section\">")
                appendLine("        <p><em>보고서 생성 시간: ${java.time.LocalDateTime.now()}</em></p>")
                appendLine("    </div>")
                appendLine("</body>")
                appendLine("</html>")
            }
        } catch (e: Exception) {
            throw ExpresserException.reportGenerationError(e)
        }
    }

    /**
     * JSON 형태의 보고서를 생성합니다.
     *
     * @param result 계산 결과
     * @return JSON 보고서 문자열
     * @throws ExpresserException 보고서 생성 중 오류 발생 시
     */
    fun generateJsonReport(result: CalculationResult): String {
        return try {
            buildString {
                appendLine("{")
                appendLine("  \"report\": {")
                appendLine("    \"title\": \"계산 결과 보고서\",")
                appendLine("    \"generatedAt\": \"${java.time.LocalDateTime.now()}\",")
                appendLine("    \"basicInfo\": {")
                appendLine("      \"formula\": \"${escapeJson(result.formula)}\",")
                appendLine("      \"result\": \"${escapeJson(formatter.formatResult(result.result))}\",")
                appendLine("      \"executionTimeMs\": ${result.executionTimeMs}")
                appendLine("    },")
                
                if (result.variables.isNotEmpty()) {
                    appendLine("    \"variables\": {")
                    result.variables.entries.forEachIndexed { index, (name, value) ->
                        val comma = if (index < result.variables.size - 1) "," else ""
                        appendLine("      \"$name\": \"${escapeJson(formatter.formatResult(value))}\"$comma")
                    }
                    appendLine("    },")
                }
                
                if (includeSteps && result.steps.isNotEmpty()) {
                    appendLine("    \"steps\": [")
                    result.steps.forEachIndexed { index, step ->
                        val comma = if (index < result.steps.size - 1) "," else ""
                        appendLine("      \"${escapeJson(step)}\"$comma")
                    }
                    appendLine("    ],")
                }
                
                if (includeStatistics) {
                    appendLine("    \"statistics\": {")
                    appendLine("      \"formulaLength\": ${result.formula.length},")
                    appendLine("      \"variableCount\": ${result.variables.size},")
                    appendLine("      \"stepCount\": ${result.steps.size}")
                    appendLine("    }")
                } else {
                    // 마지막 쉼표 제거
                    val content = this.toString()
                    this.clear()
                    append(content.trimEnd().removeSuffix(","))
                }
                
                appendLine("  }")
                appendLine("}")
            }
        } catch (e: Exception) {
            throw ExpresserException.reportGenerationError(e)
        }
    }

    /**
     * XML 형태의 보고서를 생성합니다.
     *
     * @param result 계산 결과
     * @return XML 보고서 문자열
     * @throws ExpresserException 보고서 생성 중 오류 발생 시
     */
    fun generateXmlReport(result: CalculationResult): String {
        return try {
            buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                appendLine("<calculation-report>")
                appendLine("  <title>계산 결과 보고서</title>")
                appendLine("  <generated-at>${java.time.LocalDateTime.now()}</generated-at>")
                
                appendLine("  <basic-info>")
                appendLine("    <formula><![CDATA[${result.formula}]]></formula>")
                appendLine("    <result><![CDATA[${formatter.formatResult(result.result)}]]></result>")
                appendLine("    <execution-time-ms>${result.executionTimeMs}</execution-time-ms>")
                appendLine("  </basic-info>")
                
                if (result.variables.isNotEmpty()) {
                    appendLine("  <variables>")
                    result.variables.forEach { (name, value) ->
                        appendLine("    <variable>")
                        appendLine("      <name>$name</name>")
                        appendLine("      <value><![CDATA[${formatter.formatResult(value)}]]></value>")
                        appendLine("    </variable>")
                    }
                    appendLine("  </variables>")
                }
                
                if (includeSteps && result.steps.isNotEmpty()) {
                    appendLine("  <steps>")
                    result.steps.forEachIndexed { index, step ->
                        appendLine("    <step number=\"${index + 1}\">")
                        appendLine("      <description><![CDATA[$step]]></description>")
                        appendLine("    </step>")
                    }
                    appendLine("  </steps>")
                }
                
                if (includeStatistics) {
                    appendLine("  <statistics>")
                    appendLine("    <formula-length>${result.formula.length}</formula-length>")
                    appendLine("    <variable-count>${result.variables.size}</variable-count>")
                    appendLine("    <step-count>${result.steps.size}</step-count>")
                    appendLine("  </statistics>")
                }
                
                appendLine("</calculation-report>")
            }
        } catch (e: Exception) {
            throw ExpresserException.reportGenerationError(e)
        }
    }

    /**
     * 비교 보고서를 생성합니다.
     *
     * @param results 비교할 결과들
     * @return 비교 보고서 문자열
     * @throws ExpresserException 보고서 생성 중 오류 발생 시
     */
    fun generateComparisonReport(results: List<CalculationResult>): String {
        return try {
            buildString {
                appendLine("=== 계산 결과 비교 보고서 ===")
                appendLine()
                
                appendLine("📊 비교 개요")
                appendLine("비교 대상: ${results.size}개")
                appendLine("생성 시간: ${java.time.LocalDateTime.now()}")
                appendLine()
                
                // 결과 비교 테이블
                appendLine("📋 결과 비교")
                appendLine("┌─────┬──────────────────────────────────────┬──────────────────────────────┬─────────────┐")
                appendLine("│ 순번 │                 수식                 │             결과             │ 실행시간(ms) │")
                appendLine("├─────┼──────────────────────────────────────┼──────────────────────────────┼─────────────┤")
                
                results.forEachIndexed { index, result ->
                    val formula = result.formula.take(36).padEnd(36)
                    val resultStr = formatter.formatResult(result.result).take(28).padEnd(28)
                    val timeStr = result.executionTimeMs.toString().padStart(11)
                    appendLine("│ ${(index + 1).toString().padStart(3)} │ $formula │ $resultStr │ $timeStr │")
                }
                
                appendLine("└─────┴──────────────────────────────────────┴──────────────────────────────┴─────────────┘")
                appendLine()
                
                // 통계 분석
                if (includeStatistics) {
                    appendLine("📈 통계 분석")
                    val executionTimes = results.map { it.executionTimeMs }
                    val formulaLengths = results.map { it.formula.length }
                    
                    appendLine("실행 시간 통계:")
                    appendLine("  평균: ${executionTimes.average().roundToInt()}ms")
                    appendLine("  최소: ${executionTimes.minOrNull() ?: 0}ms")
                    appendLine("  최대: ${executionTimes.maxOrNull() ?: 0}ms")
                    appendLine()
                    
                    appendLine("수식 길이 통계:")
                    appendLine("  평균: ${formulaLengths.average().roundToInt()}자")
                    appendLine("  최소: ${formulaLengths.minOrNull() ?: 0}자")
                    appendLine("  최대: ${formulaLengths.maxOrNull() ?: 0}자")
                    appendLine()
                }
                
                appendLine("보고서 생성 완료")
            }
        } catch (e: Exception) {
            throw ExpresserException.reportGenerationError(e)
        }
    }

    /**
     * HTML 문자열을 이스케이프합니다.
     */
    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    /**
     * JSON 문자열을 이스케이프합니다.
     */
    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * 새로운 옵션으로 리포터를 생성합니다.
     */
    fun withOptions(
        newFormatter: ExpressionFormatter = formatter,
        newIncludeSteps: Boolean = includeSteps,
        newIncludeStatistics: Boolean = includeStatistics,
        newIncludeAST: Boolean = includeAST,
        newMaxDepth: Int = maxDepth
    ): ExpressionReporter {
        return ExpressionReporter(newFormatter, newIncludeSteps, newIncludeStatistics, newIncludeAST, newMaxDepth)
    }

    /**
     * 현재 설정 정보를 반환합니다.
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "formatterStyle" to formatter.getOptions().style.name,
        "includeSteps" to includeSteps,
        "includeStatistics" to includeStatistics,
        "includeAST" to includeAST,
        "maxDepth" to maxDepth
    )

    /**
     * 리포터 통계를 반환합니다.
     */
    fun getReporterStatistics(): Map<String, Any> = mapOf(
        "supportedFormats" to listOf("TEXT", "HTML", "JSON", "XML"),
        "configuration" to getConfiguration(),
        "formatterStatistics" to formatter.getFormatterStatistics()
    )

    companion object {
        /**
         * 기본 리포터를 생성합니다.
         */
        fun createDefault(): ExpressionReporter = ExpressionReporter()

        /**
         * 간단한 리포터를 생성합니다.
         */
        fun createSimple(): ExpressionReporter = ExpressionReporter(
            includeSteps = false,
            includeStatistics = false,
            includeAST = false
        )

        /**
         * 상세한 리포터를 생성합니다.
         */
        fun createDetailed(): ExpressionReporter = ExpressionReporter(
            includeSteps = true,
            includeStatistics = true,
            includeAST = true
        )

        /**
         * 특정 스타일의 리포터를 생성합니다.
         */
        fun createWithStyle(style: FormattingStyle): ExpressionReporter = ExpressionReporter(
            formatter = ExpressionFormatter(FormattingOptions.forStyle(style))
        )
    }
}