package hs.kr.entrydsm.domain.calculator.values



/**
 * 다단계 계산의 개별 단계를 나타내는 값 객체입니다.
 *
 * 각 계산 단계는 실행할 수식과 선택적으로 결과를 저장할 변수명을 포함합니다.
 * POC 코드의 CalculationStep DTO를 DDD 값 객체로 구현하였습니다.
 *
 * @property stepName 단계의 이름 (선택사항)
 * @property formula 해당 단계에서 계산할 수식 문자열
 * @property resultVariable 이 단계의 계산 결과를 저장할 변수 이름 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.21
 */
data class CalculationStep(
    val stepName: String? = null,
    val formula: String,
    val resultVariable: String? = null
) {
    
    init {
        require(formula.isNotBlank()) { "수식은 비어있을 수 없습니다" }
        require(formula.length <= 10000) { "수식이 너무 깁니다: ${formula.length}자 (최대 10000자)" }
        
        stepName?.let { name ->
            require(name.isNotBlank()) { "단계 이름은 비어있을 수 없습니다" }
            require(name.length <= 100) { "단계 이름이 너무 깁니다: ${name.length}자 (최대 100자)" }
        }
        
        resultVariable?.let { varName ->
            require(varName.isNotBlank()) { "결과 변수명은 비어있을 수 없습니다" }
            require(isValidVariableName(varName)) { "결과 변수명이 유효하지 않습니다: $varName" }
        }
    }

    /**
     * 새로운 단계 이름을 가진 단계를 생성합니다.
     *
     * @param newStepName 새로운 단계 이름
     * @return 새로운 CalculationStep
     */
    fun withStepName(newStepName: String?): CalculationStep {
        return copy(stepName = newStepName)
    }

    /**
     * 새로운 수식을 가진 단계를 생성합니다.
     *
     * @param newFormula 새로운 수식
     * @return 새로운 CalculationStep
     */
    fun withFormula(newFormula: String): CalculationStep {
        require(newFormula.isNotBlank()) { "수식은 비어있을 수 없습니다" }
        return copy(formula = newFormula)
    }

    /**
     * 새로운 결과 변수명을 가진 단계를 생성합니다.
     *
     * @param newResultVariable 새로운 결과 변수명
     * @return 새로운 CalculationStep
     */
    fun withResultVariable(newResultVariable: String?): CalculationStep {
        newResultVariable?.let { varName ->
            require(isValidVariableName(varName)) { "결과 변수명이 유효하지 않습니다: $varName" }
        }
        return copy(resultVariable = newResultVariable)
    }

    /**
     * 결과 변수를 제거한 단계를 생성합니다.
     *
     * @return 새로운 CalculationStep
     */
    fun withoutResultVariable(): CalculationStep {
        return copy(resultVariable = null)
    }

    /**
     * 이 단계가 결과를 변수에 저장하는지 확인합니다.
     *
     * @return 결과 변수가 있으면 true, 아니면 false
     */
    fun hasResultVariable(): Boolean = resultVariable != null

    /**
     * 이 단계가 이름을 가지는지 확인합니다.
     *
     * @return 단계 이름이 있으면 true, 아니면 false
     */
    fun hasStepName(): Boolean = stepName != null

    /**
     * 수식에서 사용되는 변수들을 추출합니다.
     *
     * @return 사용되는 변수명 집합
     */
    fun extractVariables(): Set<String> {
        // 중괄호로 둘러싸인 변수 패턴과 일반 식별자 패턴
        val variablePatterns = listOf(
            Regex("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}"), // {변수명}
            Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b")   // 일반 식별자
        )
        
        val variables = mutableSetOf<String>()
        
        variablePatterns.forEach { pattern ->
            pattern.findAll(formula).forEach { match ->
                val variable = if (match.groups.size > 1) match.groups[1]?.value else match.value
                if (variable != null && !ReservedKeywords.isReserved(variable)) {
                    variables.add(variable)
                }
            }
        }
        
        return variables
    }

    /**
     * 수식의 복잡도를 추정합니다.
     *
     * @return 복잡도 점수
     */
    fun estimateComplexity(): Int {
        var complexity = 0
        
        // 수식 길이에 따른 복잡도
        complexity += (formula.length / 10).coerceAtMost(50)
        
        // 연산자 개수에 따른 복잡도
        val operators = listOf("+", "-", "*", "/", "^", "%", "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!")
        operators.forEach { op ->
            complexity += formula.split(op).size - 1
        }
        
        // 괄호 개수에 따른 복잡도 (중첩 고려)
        var maxDepth = 0
        var currentDepth = 0
        for (char in formula) {
            when (char) {
                '(' -> {
                    currentDepth++
                    maxDepth = maxOf(maxDepth, currentDepth)
                }
                ')' -> currentDepth--
            }
        }
        complexity += maxDepth * 3
        
        // 함수 호출 개수에 따른 복잡도
        val functionPattern = Regex("[a-zA-Z]+\\(")
        complexity += functionPattern.findAll(formula).count() * 5
        
        // 변수 개수에 따른 복잡도
        complexity += extractVariables().size * 2
        
        return complexity
    }

    /**
     * 단계의 표시 이름을 반환합니다.
     *
     * @return 단계 이름이 있으면 단계 이름, 없으면 기본 이름
     */
    fun getDisplayName(): String {
        return stepName ?: "계산 단계"
    }

    /**
     * 단계의 요약 정보를 반환합니다.
     *
     * @return 요약 정보 문자열
     */
    fun getSummary(): String = buildString {
        append(getDisplayName())
        append(": ")
        append(if (formula.length > 50) "${formula.take(47)}..." else formula)
        resultVariable?.let { append(" → $it") }
    }

    /**
     * 단계의 유효성을 검사합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean {
        return try {
            formula.isNotBlank() &&
            formula.length <= 10000 &&
            (stepName?.isNotBlank() != false) &&
            (stepName?.length ?: 0) <= 100 &&
            (resultVariable?.let { isValidVariableName(it) } != false)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 변수명이 유효한지 확인합니다.
     *
     * @param variableName 확인할 변수명
     * @return 유효한 변수명이면 true, 아니면 false
     */
    private fun isValidVariableName(variableName: String): Boolean {
        if (variableName.isBlank()) return false
        
        // 변수명은 알파벳, 숫자, 언더스코어만 허용하고 숫자로 시작할 수 없음
        val validPattern = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
        return variableName.matches(validPattern) && variableName.length <= 50
    }

    /**
     * 단계의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "stepName" to (stepName ?: ""),
        "formulaLength" to formula.length,
        "hasResultVariable" to hasResultVariable(),
        "resultVariable" to (resultVariable ?: ""),
        "variableCount" to extractVariables().size,
        "variables" to extractVariables(),
        "complexity" to estimateComplexity(),
        "isValid" to isValid()
    )

    /**
     * 단계의 디버그 정보를 반환합니다.
     *
     * @return 디버그 정보 문자열
     */
    fun getDebugInfo(): String = buildString {
        appendLine("=== 계산 단계 디버그 정보 ===")
        appendLine("단계 이름: ${stepName ?: "없음"}")
        appendLine("수식: $formula")
        appendLine("결과 변수: ${resultVariable ?: "없음"}")
        appendLine("수식 길이: ${formula.length}")
        appendLine("복잡도: ${estimateComplexity()}")
        appendLine("사용 변수: ${extractVariables()}")
        appendLine("유효성: ${isValid()}")
    }

    /**
     * 사람이 읽기 쉬운 형태로 표현합니다.
     *
     * @return 읽기 쉬운 형태의 문자열
     */
    override fun toString(): String = buildString {
        append("CalculationStep(")
        stepName?.let { append("stepName=\"$it\", ") }
        append("formula=\"$formula\"")
        resultVariable?.let { append(", resultVariable=\"$it\"") }
        append(")")
    }

    companion object {
        /**
         * 수식만으로 간단한 단계를 생성합니다.
         *
         * @param formula 수식
         * @return CalculationStep
         */
        fun simple(formula: String): CalculationStep {
            return CalculationStep(formula = formula)
        }

        /**
         * 수식과 결과 변수로 단계를 생성합니다.
         *
         * @param formula 수식
         * @param resultVariable 결과 변수명
         * @return CalculationStep
         */
        fun withResult(formula: String, resultVariable: String): CalculationStep {
            return CalculationStep(formula = formula, resultVariable = resultVariable)
        }

        /**
         * 이름과 수식으로 단계를 생성합니다.
         *
         * @param stepName 단계 이름
         * @param formula 수식
         * @return CalculationStep
         */
        fun named(stepName: String, formula: String): CalculationStep {
            return CalculationStep(stepName = stepName, formula = formula)
        }

        /**
         * 완전한 정보로 단계를 생성합니다.
         *
         * @param stepName 단계 이름
         * @param formula 수식
         * @param resultVariable 결과 변수명
         * @return CalculationStep
         */
        fun complete(stepName: String, formula: String, resultVariable: String): CalculationStep {
            return CalculationStep(stepName = stepName, formula = formula, resultVariable = resultVariable)
        }

        /**
         * 단계 목록을 일괄 생성합니다.
         *
         * @param formulas 수식 목록
         * @param namePrefix 단계 이름 접두사
         * @param resultPrefix 결과 변수명 접두사
         * @return CalculationStep 목록
         */
        fun batch(
            formulas: List<String>, 
            namePrefix: String = "단계",
            resultPrefix: String? = null
        ): List<CalculationStep> {
            return formulas.mapIndexed { index, formula ->
                val stepName = "$namePrefix ${index + 1}"
                val resultVariable = resultPrefix?.let { "${it}_${index + 1}" }
                CalculationStep(stepName = stepName, formula = formula, resultVariable = resultVariable)
            }
        }

        /**
         * 빌더 패턴으로 단계를 생성합니다.
         *
         * @return CalculationStepBuilder
         */
        fun builder(): CalculationStepBuilder {
            return CalculationStepBuilder()
        }
    }

    /**
     * 계산 단계를 구성하기 위한 빌더 클래스입니다.
     */
    class CalculationStepBuilder {
        private var stepName: String? = null
        private var formula: String = ""
        private var resultVariable: String? = null

        fun stepName(name: String): CalculationStepBuilder {
            this.stepName = name
            return this
        }

        fun formula(formula: String): CalculationStepBuilder {
            this.formula = formula
            return this
        }

        fun resultVariable(variable: String): CalculationStepBuilder {
            this.resultVariable = variable
            return this
        }

        fun build(): CalculationStep {
            return CalculationStep(stepName, formula, resultVariable)
        }
    }
}