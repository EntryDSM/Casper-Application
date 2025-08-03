package hs.kr.entrydsm.domain.calculator.values

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



/**
 * 계산 요청을 나타내는 값 객체입니다.
 *
 * 수식 계산에 필요한 모든 정보를 포함하며, 불변성을 보장합니다.
 * 수식 문자열과 변수 바인딩 정보를 포함하여 계산기가 수행할 작업을 정의합니다.
 *
 * @property formula 계산할 수식 문자열
 * @property variables 변수 바인딩 맵
 * @property options 계산 옵션 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
data class CalculationRequest(
    val formula: String,
    val variables: Map<String, Any> = emptyMap(),
    val options: Map<String, Any> = emptyMap()
) {
    
    init {
        require(formula.isNotBlank()) { "수식은 비어있을 수 없습니다" }
        require(formula.length <= 10000) { "수식이 너무 깁니다: ${formula.length}자 (최대 10000자)" }
        require(variables.size <= 1000) { "변수가 너무 많습니다: ${variables.size}개 (최대 1000개)" }
    }

    /**
     * 새로운 변수를 추가한 요청을 생성합니다.
     *
     * @param name 변수 이름
     * @param value 변수 값
     * @return 새로운 CalculationRequest
     */
    fun withVariable(name: String, value: Any): CalculationRequest {
        require(name.isNotBlank()) { "변수 이름은 비어있을 수 없습니다" }
        return copy(variables = variables + (name to value))
    }

    /**
     * 여러 변수를 추가한 요청을 생성합니다.
     *
     * @param newVariables 추가할 변수 맵
     * @return 새로운 CalculationRequest
     */
    fun withVariables(newVariables: Map<String, Any>): CalculationRequest {
        return copy(variables = variables + newVariables)
    }

    /**
     * 새로운 옵션을 추가한 요청을 생성합니다.
     *
     * @param key 옵션 키
     * @param value 옵션 값
     * @return 새로운 CalculationRequest
     */
    fun withOption(key: String, value: Any): CalculationRequest {
        require(key.isNotBlank()) { "옵션 키는 비어있을 수 없습니다" }
        return copy(options = options + (key to value))
    }

    /**
     * 새로운 수식으로 요청을 생성합니다.
     *
     * @param newFormula 새로운 수식
     * @return 새로운 CalculationRequest
     */
    fun withFormula(newFormula: String): CalculationRequest {
        require(newFormula.isNotBlank()) { "수식은 비어있을 수 없습니다" }
        return copy(formula = newFormula)
    }

    /**
     * 특정 변수를 제거한 요청을 생성합니다.
     *
     * @param name 제거할 변수 이름
     * @return 새로운 CalculationRequest
     */
    fun withoutVariable(name: String): CalculationRequest {
        return copy(variables = variables - name)
    }

    /**
     * 모든 변수를 제거한 요청을 생성합니다.
     *
     * @return 새로운 CalculationRequest
     */
    fun withoutVariables(): CalculationRequest {
        return copy(variables = emptyMap())
    }

    /**
     * 특정 옵션을 제거한 요청을 생성합니다.
     *
     * @param key 제거할 옵션 키
     * @return 새로운 CalculationRequest
     */
    fun withoutOption(key: String): CalculationRequest {
        return copy(options = options - key)
    }

    /**
     * 변수가 정의되어 있는지 확인합니다.
     *
     * @param name 확인할 변수 이름
     * @return 변수가 정의되어 있으면 true, 아니면 false
     */
    fun hasVariable(name: String): Boolean = name in variables

    /**
     * 옵션이 정의되어 있는지 확인합니다.
     *
     * @param key 확인할 옵션 키
     * @return 옵션이 정의되어 있으면 true, 아니면 false
     */
    fun hasOption(key: String): Boolean = key in options

    /**
     * 변수 값을 가져옵니다.
     *
     * @param name 변수 이름
     * @return 변수 값 또는 null
     */
    fun getVariable(name: String): Any? = variables[name]

    /**
     * 옵션 값을 가져옵니다.
     *
     * @param key 옵션 키
     * @return 옵션 값 또는 null
     */
    fun getOption(key: String): Any? = options[key]

    /**
     * 수식의 복잡도를 추정합니다.
     *
     * @return 복잡도 점수 (0-100)
     */
    fun estimateComplexity(): Int {
        var complexity = 0
        
        // 수식 길이에 따른 복잡도
        complexity += (formula.length / 10).coerceAtMost(30)
        
        // 연산자 개수에 따른 복잡도
        val operators = listOf("+", "-", "*", "/", "^", "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!")
        complexity += operators.sumOf { op -> formula.count { it.toString() == op } * 2 }
        
        // 괄호 개수에 따른 복잡도
        complexity += formula.count { it == '(' } * 3
        
        // 함수 호출 개수에 따른 복잡도
        complexity += formula.count { it.isLetter() } * 1
        
        // 변수 개수에 따른 복잡도
        complexity += variables.size * 2
        
        return complexity.coerceAtMost(100)
    }

    /**
     * 요청의 유효성을 검사합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean {
        return try {
            formula.isNotBlank() && 
            formula.length <= 10000 && 
            variables.size <= 1000 &&
            variables.keys.all { it.isNotBlank() }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 수식에 사용될 가능성이 있는 변수들을 추출합니다.
     *
     * @return 변수명 집합
     */
    fun extractPossibleVariables(): Set<String> {
        val regex = Regex("[a-zA-Z_][a-zA-Z0-9_]*")
        return regex.findAll(formula)
            .map { it.value }
            .filter { !ReservedKeywords.isReserved(it) }
            .toSet()
    }

    /**
     * 누락된 변수들을 확인합니다.
     *
     * @return 누락된 변수명 집합
     */
    fun findMissingVariables(): Set<String> {
        val possibleVariables = extractPossibleVariables()
        return possibleVariables - variables.keys
    }

    /**
     * 사용되지 않는 변수들을 확인합니다.
     *
     * @return 사용되지 않는 변수명 집합
     */
    fun findUnusedVariables(): Set<String> {
        val possibleVariables = extractPossibleVariables()
        return variables.keys - possibleVariables
    }

    /**
     * 요청의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "formulaLength" to formula.length,
        "variableCount" to variables.size,
        "optionCount" to options.size,
        "estimatedComplexity" to estimateComplexity(),
        "possibleVariables" to extractPossibleVariables(),
        "missingVariables" to findMissingVariables(),
        "unusedVariables" to findUnusedVariables(),
        "isValid" to isValid()
    )

    /**
     * 요청을 JSON 형태로 표현합니다.
     * kotlinx.serialization을 사용하여 안전하게 직렬화합니다.
     *
     * @return JSON 형태의 문자열
     */
    fun toJson(): String {
        @Serializable
        data class CalculationRequestJson(
            val formula: String,
            val variables: Map<String, String>,
            val options: Map<String, String>
        )
        
        val jsonData = CalculationRequestJson(
            formula = formula,
            variables = variables.mapValues { it.value.toString() },
            options = options.mapValues { it.value.toString() }
        )
        
        return Json.encodeToString(jsonData)
    }

    /**
     * 요청을 사람이 읽기 쉬운 형태로 표현합니다.
     *
     * @return 읽기 쉬운 형태의 문자열
     */
    override fun toString(): String = buildString {
        append("CalculationRequest(")
        append("formula=\"$formula\"")
        if (variables.isNotEmpty()) {
            append(", variables=$variables")
        }
        if (options.isNotEmpty()) {
            append(", options=$options")
        }
        append(")")
    }

    companion object {
        /**
         * 수식만으로 간단한 요청을 생성합니다.
         *
         * @param formula 수식
         * @return CalculationRequest
         */
        fun simple(formula: String): CalculationRequest = CalculationRequest(formula)

        /**
         * 수식과 변수로 요청을 생성합니다.
         *
         * @param formula 수식
         * @param variables 변수 맵
         * @return CalculationRequest
         */
        fun withVariables(formula: String, variables: Map<String, Any>): CalculationRequest =
            CalculationRequest(formula, variables)

        /**
         * 수식과 단일 변수로 요청을 생성합니다.
         *
         * @param formula 수식
         * @param variableName 변수 이름
         * @param variableValue 변수 값
         * @return CalculationRequest
         */
        fun withVariable(formula: String, variableName: String, variableValue: Any): CalculationRequest =
            CalculationRequest(formula, mapOf(variableName to variableValue))

        /**
         * 빈 요청을 생성합니다 (테스트용).
         *
         * @return 빈 CalculationRequest
         */
        fun empty(): CalculationRequest = CalculationRequest("0")

        /**
         * 여러 요청을 배치로 생성합니다.
         *
         * @param formulas 수식 목록
         * @return CalculationRequest 목록
         */
        fun batch(formulas: List<String>): List<CalculationRequest> =
            formulas.map { CalculationRequest(it) }

        /**
         * 변수 템플릿을 사용하여 요청을 생성합니다.
         *
         * @param template 수식 템플릿
         * @param variables 변수 맵
         * @return CalculationRequest
         */
        fun fromTemplate(template: String, variables: Map<String, Any>): CalculationRequest =
            CalculationRequest(template, variables)
    }
}