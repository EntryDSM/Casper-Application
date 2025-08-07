package hs.kr.entrydsm.domain.evaluator.values

import java.time.LocalDateTime

/**
 * 표현식 평가 결과를 나타내는 값 객체입니다.
 *
 * 평가 결과 값과 함께 평가 과정에서 발생한 메타데이터를 포함합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
data class EvaluationResult private constructor(
    val value: Any?,
    val type: ResultType,
    val isSuccess: Boolean,
    val errorMessage: String?,
    val evaluationTime: Long,
    val variablesUsed: Set<String>,
    val functionsUsed: Set<String>,
    val evaluatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    /**
     * 숫자 값을 반환합니다.
     */
    fun asNumber(): Double {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            else -> throw IllegalStateException("결과가 숫자가 아닙니다: $value")
        }
    }
    
    /**
     * 불리언 값을 반환합니다.
     */
    fun asBoolean(): Boolean {
        return when (value) {
            is Boolean -> value
            is Double -> value != 0.0
            is Int -> value != 0
            else -> throw IllegalStateException("결과가 불리언으로 변환할 수 없습니다: $value")
        }
    }
    
    /**
     * 문자열 값을 반환합니다.
     */
    fun asString(): String {
        return value?.toString() ?: "null"
    }
    
    /**
     * 결과가 숫자인지 확인합니다.
     */
    fun isNumeric(): Boolean = type == ResultType.NUMBER
    
    /**
     * 결과가 불리언인지 확인합니다.
     */
    fun isBoolean(): Boolean = type == ResultType.BOOLEAN
    
    /**
     * 결과가 null인지 확인합니다.
     */
    fun isNull(): Boolean = type == ResultType.NULL
    
    /**
     * 오류 결과인지 확인합니다.
     */
    fun isError(): Boolean = !isSuccess
    
    /**
     * 평가 시간을 milliseconds 단위로 반환합니다.
     */
    fun getEvaluationTimeMs(): Long = evaluationTime
    
    /**
     * 평가 시간을 nanoseconds 단위로 반환합니다.
     * 
     * @return 평가 시간 (nanoseconds), 오버플로우 발생 시 Long.MAX_VALUE
     */
    fun getEvaluationTimeNs(): Long {
        return try {
            if (evaluationTime > Long.MAX_VALUE / 1_000_000) {
                Long.MAX_VALUE
            } else {
                evaluationTime * 1_000_000
            }
        } catch (e: ArithmeticException) {
            Long.MAX_VALUE
        }
    }
    
    /**
     * 사용된 변수 개수를 반환합니다.
     */
    fun getVariableCount(): Int = variablesUsed.size
    
    /**
     * 사용된 함수 개수를 반환합니다.
     */
    fun getFunctionCount(): Int = functionsUsed.size
    
    /**
     * 성능 정보를 반환합니다.
     */
    fun getPerformanceInfo(): PerformanceInfo {
        return PerformanceInfo(
            evaluationTime = evaluationTime,
            variableCount = variablesUsed.size,
            functionCount = functionsUsed.size,
            complexity = calculateComplexity()
        )
    }
    
    /**
     * 복잡도를 계산합니다.
     */
    private fun calculateComplexity(): ComplexityLevel {
        val totalOperations = variablesUsed.size + functionsUsed.size
        return when {
            totalOperations <= 5 -> ComplexityLevel.LOW
            totalOperations <= 15 -> ComplexityLevel.MEDIUM
            totalOperations <= 30 -> ComplexityLevel.HIGH
            else -> ComplexityLevel.VERY_HIGH
        }
    }
    
    /**
     * 결과 타입 열거형
     */
    enum class ResultType {
        NUMBER,
        BOOLEAN,
        STRING,
        NULL
    }
    
    /**
     * 복잡도 레벨 열거형
     */
    enum class ComplexityLevel {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
    
    /**
     * 성능 정보 데이터 클래스
     */
    data class PerformanceInfo(
        val evaluationTime: Long,
        val variableCount: Int,
        val functionCount: Int,
        val complexity: ComplexityLevel
    )
    
    companion object {
        /**
         * 성공 결과를 생성합니다.
         */
        fun success(
            value: Any?,
            evaluationTime: Long,
            variablesUsed: Set<String> = emptySet(),
            functionsUsed: Set<String> = emptySet()
        ): EvaluationResult {
            val type = determineType(value)
            return EvaluationResult(
                value = value,
                type = type,
                isSuccess = true,
                errorMessage = null,
                evaluationTime = evaluationTime,
                variablesUsed = variablesUsed,
                functionsUsed = functionsUsed
            )
        }
        
        /**
         * 실패 결과를 생성합니다.
         */
        fun failure(
            errorMessage: String,
            evaluationTime: Long,
            variablesUsed: Set<String> = emptySet(),
            functionsUsed: Set<String> = emptySet()
        ): EvaluationResult {
            return EvaluationResult(
                value = null,
                type = ResultType.NULL,
                isSuccess = false,
                errorMessage = errorMessage,
                evaluationTime = evaluationTime,
                variablesUsed = variablesUsed,
                functionsUsed = functionsUsed
            )
        }
        
        /**
         * 숫자 결과를 생성합니다.
         */
        fun ofNumber(
            value: Double,
            evaluationTime: Long,
            variablesUsed: Set<String> = emptySet(),
            functionsUsed: Set<String> = emptySet()
        ): EvaluationResult {
            return EvaluationResult(
                value = value,
                type = ResultType.NUMBER,
                isSuccess = true,
                errorMessage = null,
                evaluationTime = evaluationTime,
                variablesUsed = variablesUsed,
                functionsUsed = functionsUsed
            )
        }
        
        /**
         * 불리언 결과를 생성합니다.
         */
        fun ofBoolean(
            value: Boolean,
            evaluationTime: Long,
            variablesUsed: Set<String> = emptySet(),
            functionsUsed: Set<String> = emptySet()
        ): EvaluationResult {
            return EvaluationResult(
                value = value,
                type = ResultType.BOOLEAN,
                isSuccess = true,
                errorMessage = null,
                evaluationTime = evaluationTime,
                variablesUsed = variablesUsed,
                functionsUsed = functionsUsed
            )
        }
        
        /**
         * 문자열 결과를 생성합니다.
         */
        fun ofString(
            value: String,
            evaluationTime: Long,
            variablesUsed: Set<String> = emptySet(),
            functionsUsed: Set<String> = emptySet()
        ): EvaluationResult {
            return EvaluationResult(
                value = value,
                type = ResultType.STRING,
                isSuccess = true,
                errorMessage = null,
                evaluationTime = evaluationTime,
                variablesUsed = variablesUsed,
                functionsUsed = functionsUsed
            )
        }
        
        /**
         * null 결과를 생성합니다.
         */
        fun ofNull(
            evaluationTime: Long,
            variablesUsed: Set<String> = emptySet(),
            functionsUsed: Set<String> = emptySet()
        ): EvaluationResult {
            return EvaluationResult(
                value = null,
                type = ResultType.NULL,
                isSuccess = true,
                errorMessage = null,
                evaluationTime = evaluationTime,
                variablesUsed = variablesUsed,
                functionsUsed = functionsUsed
            )
        }
        
        /**
         * 값의 타입을 결정합니다.
         */
        private fun determineType(value: Any?): ResultType {
            return when (value) {
                null -> ResultType.NULL
                is Double, is Float, is Int, is Long -> ResultType.NUMBER
                is Boolean -> ResultType.BOOLEAN
                is String -> ResultType.STRING
                else -> ResultType.STRING
            }
        }
    }
}