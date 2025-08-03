package hs.kr.entrydsm.domain.calculator.values

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 계산 결과를 나타내는 값 객체입니다.
 *
 * 수식 계산의 결과와 함께 실행 통계, 중간 과정 정보를 포함합니다.
 * 불변성을 보장하며, 계산 성공 여부와 관련된 다양한 메타데이터를 제공합니다.
 *
 * @property result 계산 결과값
 * @property executionTimeMs 실행 시간 (밀리초)
 * @property formula 원본 수식
 * @property variables 사용된 변수 맵
 * @property steps 실행 단계 목록
 * @property ast 생성된 AST 노드 (선택사항)
 * @property errors 발생한 오류 목록
 * @property warnings 경고 목록
 * @property metadata 추가 메타데이터
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
data class CalculationResult(
    val result: Any?,
    val executionTimeMs: Long,
    val formula: String,
    val variables: Map<String, Any> = emptyMap(),
    val steps: List<String> = emptyList(),
    val ast: ASTNode? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(executionTimeMs >= 0) { "실행 시간은 0 이상이어야 합니다: $executionTimeMs" }
        require(formula.isNotBlank()) { "수식은 비어있을 수 없습니다" }
    }

    /**
     * 계산이 성공했는지 확인합니다.
     *
     * @return 성공하면 true, 아니면 false
     */
    fun isSuccess(): Boolean = result != null && errors.isEmpty()

    /**
     * 계산이 실패했는지 확인합니다.
     *
     * @return 실패하면 true, 아니면 false
     */
    fun isFailure(): Boolean = !isSuccess()

    /**
     * 경고가 있는지 확인합니다.
     *
     * @return 경고가 있으면 true, 아니면 false
     */
    fun hasWarnings(): Boolean = warnings.isNotEmpty()

    /**
     * 결과가 숫자인지 확인합니다.
     *
     * @return 숫자이면 true, 아니면 false
     */
    fun isNumericResult(): Boolean = result is Number

    /**
     * 결과가 불린값인지 확인합니다.
     *
     * @return 불린값이면 true, 아니면 false
     */
    fun isBooleanResult(): Boolean = result is Boolean

    /**
     * 결과가 문자열인지 확인합니다.
     *
     * @return 문자열이면 true, 아니면 false
     */
    fun isStringResult(): Boolean = result is String

    /**
     * 결과를 Double로 변환합니다.
     *
     * @return Double 값 또는 null
     */
    fun asDouble(): Double? = when (result) {
        is Double -> result
        is Int -> result.toDouble()
        is Float -> result.toDouble()
        is Long -> result.toDouble()
        else -> null
    }

    /**
     * 결과를 Int로 변환합니다.
     *
     * @return Int 값 또는 null
     */
    fun asInt(): Int? = when (result) {
        is Int -> result
        is Double -> if (result.isFinite() && result == result.toInt().toDouble()) result.toInt() else null
        is Float -> if (result.isFinite() && result == result.toInt().toFloat()) result.toInt() else null
        is Long -> if (result in Int.MIN_VALUE..Int.MAX_VALUE) result.toInt() else null
        else -> null
    }

    /**
     * 결과를 Boolean으로 변환합니다.
     *
     * @return Boolean 값 또는 null
     */
    fun asBoolean(): Boolean? = when (result) {
        is Boolean -> result
        is Number -> result.toDouble() != 0.0
        is String -> result.isNotEmpty()
        else -> null
    }

    /**
     * 결과를 String으로 변환합니다.
     *
     * @return String 값
     */
    fun asString(): String = result?.toString() ?: "null"

    /**
     * 성능 등급을 계산합니다.
     *
     * @return 성능 등급 (A, B, C, D, F)
     */
    fun getPerformanceGrade(): String = when {
        executionTimeMs < 1 -> "A"
        executionTimeMs < 10 -> "B"
        executionTimeMs < 100 -> "C"
        executionTimeMs < 1000 -> "D"
        else -> "F"
    }

    /**
     * 복잡도를 추정합니다.
     *
     * @return 복잡도 점수 (0-100)
     */
    fun estimateComplexity(): Int {
        var complexity = 0
        
        // 수식 길이에 따른 복잡도
        complexity += (formula.length / 10).coerceAtMost(20)
        
        // 실행 시간에 따른 복잡도
        complexity += when {
            executionTimeMs < 1 -> 0
            executionTimeMs < 10 -> 5
            executionTimeMs < 100 -> 15
            executionTimeMs < 1000 -> 30
            else -> 50
        }
        
        // 변수 개수에 따른 복잡도
        complexity += variables.size * 2
        
        // 실행 단계에 따른 복잡도
        complexity += steps.size * 1
        
        // AST 존재 여부에 따른 복잡도
        if (ast != null) complexity += 10
        
        return complexity.coerceAtMost(100)
    }

    /**
     * 새로운 경고를 추가한 결과를 생성합니다.
     *
     * @param warning 추가할 경고
     * @return 새로운 CalculationResult
     */
    fun withWarning(warning: String): CalculationResult {
        return copy(warnings = warnings + warning)
    }

    /**
     * 새로운 오류를 추가한 결과를 생성합니다.
     *
     * @param error 추가할 오류
     * @return 새로운 CalculationResult
     */
    fun withError(error: String): CalculationResult {
        return copy(errors = errors + error)
    }

    /**
     * 새로운 단계를 추가한 결과를 생성합니다.
     *
     * @param step 추가할 단계
     * @return 새로운 CalculationResult
     */
    fun withStep(step: String): CalculationResult {
        return copy(steps = steps + step)
    }

    /**
     * 새로운 메타데이터를 추가한 결과를 생성합니다.
     *
     * @param key 메타데이터 키
     * @param value 메타데이터 값
     * @return 새로운 CalculationResult
     */
    fun withMetadata(key: String, value: Any): CalculationResult {
        return copy(metadata = metadata + (key to value))
    }

    /**
     * AST를 추가한 결과를 생성합니다.
     *
     * @param astNode AST 노드
     * @return 새로운 CalculationResult
     */
    fun withAST(astNode: ASTNode): CalculationResult {
        return copy(ast = astNode)
    }

    /**
     * 결과의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "isSuccess" to isSuccess(),
        "isFailure" to isFailure(),
        "hasWarnings" to hasWarnings(),
        "resultType" to (result?.javaClass?.simpleName ?: "null"),
        "executionTimeMs" to executionTimeMs,
        "performanceGrade" to getPerformanceGrade(),
        "estimatedComplexity" to estimateComplexity(),
        "formulaLength" to formula.length,
        "variableCount" to variables.size,
        "stepCount" to steps.size,
        "errorCount" to errors.size,
        "warningCount" to warnings.size,
        "hasAST" to (ast != null),
        "metadataCount" to metadata.size
    )

    /**
     * 결과를 요약합니다.
     *
     * @return 요약 정보
     */
    fun getSummary(): String = buildString {
        append("결과: ${asString()}")
        append(" (${result?.javaClass?.simpleName ?: "null"})")
        append(", 실행시간: ${executionTimeMs}ms")
        append(", 성능등급: ${getPerformanceGrade()}")
        if (hasWarnings()) {
            append(", 경고: ${warnings.size}개")
        }
        if (isFailure()) {
            append(", 오류: ${errors.size}개")
        }
    }

    /**
     * 결과를 JSON 형태로 표현합니다.
     * kotlinx.serialization을 사용하여 안전하게 직렬화합니다.
     *
     * @return JSON 형태의 문자열
     */
    fun toJson(): String {
        @Serializable
        data class CalculationResultJson(
            val result: String,
            val executionTimeMs: Long,
            val formula: String,
            val variables: Map<String, String>,
            val steps: List<String>,
            val errors: List<String>,
            val warnings: List<String>,
            val isSuccess: Boolean
        )
        
        val jsonData = CalculationResultJson(
            result = asString(),
            executionTimeMs = executionTimeMs,
            formula = formula,
            variables = variables.mapValues { it.value.toString() },
            steps = steps,
            errors = errors,
            warnings = warnings,
            isSuccess = isSuccess()
        )
        
        return Json.encodeToString(jsonData)
    }

    /**
     * 결과를 상세하게 표현합니다.
     *
     * @return 상세 정보가 포함된 문자열
     */
    fun toDetailString(): String = buildString {
        appendLine("=== 계산 결과 ===")
        appendLine("수식: $formula")
        appendLine("결과: ${asString()}")
        appendLine("타입: ${result?.javaClass?.simpleName ?: "null"}")
        appendLine("실행 시간: ${executionTimeMs}ms")
        appendLine("성능 등급: ${getPerformanceGrade()}")
        appendLine("복잡도: ${estimateComplexity()}")
        appendLine("성공 여부: ${isSuccess()}")
        
        if (variables.isNotEmpty()) {
            appendLine("변수:")
            variables.forEach { (name, value) ->
                appendLine("  $name = $value")
            }
        }
        
        if (steps.isNotEmpty()) {
            appendLine("실행 단계:")
            steps.forEachIndexed { index, step ->
                appendLine("  ${index + 1}. $step")
            }
        }
        
        if (warnings.isNotEmpty()) {
            appendLine("경고:")
            warnings.forEach { warning ->
                appendLine("  - $warning")
            }
        }
        
        if (errors.isNotEmpty()) {
            appendLine("오류:")
            errors.forEach { error ->
                appendLine("  - $error")
            }
        }
        
        if (metadata.isNotEmpty()) {
            appendLine("메타데이터:")
            metadata.forEach { (key, value) ->
                appendLine("  $key = $value")
            }
        }
    }

    /**
     * 결과를 사람이 읽기 쉬운 형태로 표현합니다.
     *
     * @return 읽기 쉬운 형태의 문자열
     */
    override fun toString(): String = getSummary()

    companion object {
        /**
         * 성공 결과를 생성합니다.
         *
         * @param result 결과값
         * @param executionTimeMs 실행 시간
         * @param formula 원본 수식
         * @return CalculationResult
         */
        fun success(result: Any, executionTimeMs: Long, formula: String): CalculationResult =
            CalculationResult(result, executionTimeMs, formula)

        /**
         * 실패 결과를 생성합니다.
         *
         * @param error 오류 메시지
         * @param executionTimeMs 실행 시간
         * @param formula 원본 수식
         * @return CalculationResult
         */
        fun failure(error: String, executionTimeMs: Long, formula: String): CalculationResult =
            CalculationResult(null, executionTimeMs, formula, errors = listOf(error))

        /**
         * 경고와 함께 성공 결과를 생성합니다.
         *
         * @param result 결과값
         * @param warning 경고 메시지
         * @param executionTimeMs 실행 시간
         * @param formula 원본 수식
         * @return CalculationResult
         */
        fun successWithWarning(result: Any, warning: String, executionTimeMs: Long, formula: String): CalculationResult =
            CalculationResult(result, executionTimeMs, formula, warnings = listOf(warning))

        /**
         * 빈 결과를 생성합니다 (테스트용).
         *
         * @return 빈 CalculationResult
         */
        fun empty(): CalculationResult = CalculationResult(null, 0, "")

        /**
         * 여러 결과를 병합합니다.
         *
         * @param results 병합할 결과들
         * @return 병합된 CalculationResult
         */
        fun merge(results: List<CalculationResult>): CalculationResult {
            require(results.isNotEmpty()) { "병합할 결과가 없습니다" }
            
            val firstResult = results.first()
            val totalExecutionTime = results.sumOf { it.executionTimeMs }
            val allVariables = results.flatMap { it.variables.entries }.associate { it.key to it.value }
            val allSteps = results.flatMap { it.steps }
            val allErrors = results.flatMap { it.errors }
            val allWarnings = results.flatMap { it.warnings }
            val allMetadata = results.flatMap { it.metadata.entries }.associate { it.key to it.value }
            
            return CalculationResult(
                result = results.lastOrNull { it.isSuccess() }?.result,
                executionTimeMs = totalExecutionTime,
                formula = results.joinToString(" -> ") { it.formula },
                variables = allVariables,
                steps = allSteps,
                errors = allErrors,
                warnings = allWarnings,
                metadata = allMetadata
            )
        }

        /**
         * 결과를 비교합니다.
         *
         * @param result1 첫 번째 결과
         * @param result2 두 번째 결과
         * @return 비교 결과 맵
         */
        fun compare(result1: CalculationResult, result2: CalculationResult): Map<String, Any> = mapOf(
            "result1" to result1.asString(),
            "result2" to result2.asString(),
            "resultsEqual" to (result1.result == result2.result),
            "executionTimeDiff" to (result2.executionTimeMs - result1.executionTimeMs),
            "performanceComparison" to "${result1.getPerformanceGrade()} vs ${result2.getPerformanceGrade()}",
            "complexityDiff" to (result2.estimateComplexity() - result1.estimateComplexity()),
            "bothSuccess" to (result1.isSuccess() && result2.isSuccess()),
            "bothFailure" to (result1.isFailure() && result2.isFailure())
        )
    }
}