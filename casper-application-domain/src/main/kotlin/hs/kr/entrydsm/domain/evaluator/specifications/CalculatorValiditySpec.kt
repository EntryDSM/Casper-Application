package hs.kr.entrydsm.domain.evaluator.specifications

import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.MultiStepCalculationRequest
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority
import hs.kr.entrydsm.global.constants.ErrorCodes

/**
 * POC 코드의 FormulaValidator 기능을 DDD Specification 패턴으로 구현한 클래스입니다.
 *
 * 계산 요청의 유효성을 검증하는 복합적인 비즈니스 규칙을 캡슐화합니다.
 * POC 코드의 validateCalculationRequest, validateVariables 등의 기능을
 * 체계적이고 확장 가능한 명세 패턴으로 재구성했습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
@Specification(
    name = "CalculatorValidity",
    description = "POC 코드 기반의 계산기 요청 유효성 검증 명세",
    domain = "evaluator",
    priority = Priority.HIGH
)
class CalculatorValiditySpec {

    companion object {
        // POC 코드의 CalculatorProperties 기본값들
        private const val DEFAULT_MAX_FORMULA_LENGTH = 5000
        private const val DEFAULT_MAX_STEPS = 50
        private const val DEFAULT_MAX_VARIABLES = 100
        
        // POC 코드에서 사용된 정규식 패턴들
        private val VALID_VARIABLE_NAME_PATTERN = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
        private val SUSPICIOUS_PATTERN = Regex("(eval|exec|system|runtime|process)")
        
        // POC 코드의 허용된 토큰들
        private val ALLOWED_OPERATORS = setOf(
            "+", "-", "*", "/", "%", "^",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||", "!", "(", ")"
        )
        
        private val ALLOWED_FUNCTIONS = setOf(
            "ABS", "SQRT", "ROUND", "MIN", "MAX", "SUM", "AVG", "AVERAGE",
            "IF", "POW", "LOG", "LOG10", "EXP", "SIN", "COS", "TAN",
            "ASIN", "ACOS", "ATAN", "ATAN2", "SINH", "COSH", "TANH",
            "ASINH", "ACOSH", "ATANH", "FLOOR", "CEIL", "CEILING",
            "TRUNCATE", "TRUNC", "SIGN", "RANDOM", "RAND", "RADIANS",
            "DEGREES", "PI", "E", "MOD", "GCD", "LCM", "FACTORIAL",
            "COMBINATION", "COMB", "PERMUTATION", "PERM"
        )
        
        // POC 코드의 예약어들
        private val RESERVED_WORDS = setOf(
            "null", "undefined", "true", "false", "NaN", "Infinity",
            "eval", "function", "var", "let", "const", "class", "interface"
        )
    }

    /**
     * POC 코드의 validateCalculationRequest 구현
     */
    fun isSatisfiedBy(
        request: CalculationRequest,
        maxFormulaLength: Int = DEFAULT_MAX_FORMULA_LENGTH,
        maxVariables: Int = DEFAULT_MAX_VARIABLES
    ): Boolean {
        return try {
            validateFormula(request.formula, maxFormulaLength) &&
            validateVariables(request.variables, maxVariables) &&
            validateSecurity(request.formula) &&
            validateSyntax(request.formula)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * POC 코드의 다단계 계산 요청 검증
     */
    fun isSatisfiedBy(
        request: MultiStepCalculationRequest,
        maxSteps: Int = DEFAULT_MAX_STEPS,
        maxVariables: Int = DEFAULT_MAX_VARIABLES
    ): Boolean {
        return try {
            validateStepCount(request.steps.size, maxSteps) &&
            validateInitialVariables(request.variables, maxVariables) &&
            validateStepSequence(request.steps) &&
            validateStepSecurity(request.steps)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * POC 코드의 수식 길이 및 복잡도 검증
     */
    private fun validateFormula(formula: String, maxLength: Int): Boolean {
        return formula.isNotBlank() &&
               formula.length <= maxLength &&
               !containsControlCharacters(formula) &&
               hasBalancedParentheses(formula)
    }

    /**
     * POC 코드의 변수 유효성 검증
     */
    private fun validateVariables(variables: Map<String, Any?>, maxVariables: Int): Boolean {
        if (variables.size > maxVariables) {
            return false
        }
        
        return variables.keys.all { variableName ->
            isValidVariableName(variableName) &&
            !isReservedWord(variableName)
        } && variables.values.all { value ->
            isValidVariableValue(value)
        }
    }

    /**
     * POC 코드의 보안 검증 (의심스러운 패턴 감지)
     */
    private fun validateSecurity(formula: String): Boolean {
        val lowerFormula = formula.lowercase()
        return !SUSPICIOUS_PATTERN.containsMatchIn(lowerFormula) &&
               !containsDangerousSequences(lowerFormula) &&
               !hasExcessiveRecursion(formula)
    }

    /**
     * POC 코드의 구문 검증 (허용된 토큰만 사용)
     */
    private fun validateSyntax(formula: String): Boolean {
        return try {
            val tokens = tokenizeFormula(formula)
            tokens.all { token ->
                isAllowedToken(token)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 다단계 계산의 단계 수 검증
     */
    private fun validateStepCount(stepCount: Int, maxSteps: Int): Boolean {
        return stepCount > 0 && stepCount <= maxSteps
    }

    /**
     * 다단계 계산의 초기 변수 검증
     */
    private fun validateInitialVariables(variables: Map<String, Any?>, maxVariables: Int): Boolean {
        return validateVariables(variables, maxVariables)
    }

    /**
     * 다단계 계산의 단계 순서 검증
     */
    private fun validateStepSequence(steps: List<Any>): Boolean {
        if (steps.isEmpty()) return false
        
        // 단계 간 의존성 검증
        val definedVariables = mutableSetOf<String>()
        
        steps.forEachIndexed { index, step ->
            // 각 단계의 수식 검증
            val formula = extractFormulaFromStep(step)
            if (!validateFormula(formula, DEFAULT_MAX_FORMULA_LENGTH)) {
                return false
            }
            
            // 변수 의존성 검증
            val usedVariables = extractVariablesFromFormula(formula)
            val undefinedVariables = usedVariables - definedVariables
            
            if (undefinedVariables.isNotEmpty() && index > 0) {
                // 첫 번째 단계가 아닌데 정의되지 않은 변수가 있으면 실패
                return false
            }
            
            // 이 단계에서 정의되는 변수 추가
            val assignedVariable = extractAssignedVariable(step)
            if (assignedVariable != null) {
                definedVariables.add(assignedVariable)
            }
        }
        
        return true
    }

    /**
     * 다단계 계산의 보안 검증
     */
    private fun validateStepSecurity(steps: List<Any>): Boolean {
        return steps.all { step ->
            val formula = extractFormulaFromStep(step)
            validateSecurity(formula)
        }
    }

    // Helper methods

    private fun containsControlCharacters(formula: String): Boolean {
        return formula.any { char ->
            char.isISOControl() && char != '\t' && char != '\n' && char != '\r'
        }
    }

    private fun hasBalancedParentheses(formula: String): Boolean {
        var balance = 0
        for (char in formula) {
            when (char) {
                '(' -> balance++
                ')' -> balance--
            }
            if (balance < 0) return false
        }
        return balance == 0
    }

    private fun isValidVariableName(name: String): Boolean {
        return name.isNotBlank() &&
               name.length <= 50 &&
               VALID_VARIABLE_NAME_PATTERN.matches(name)
    }

    private fun isReservedWord(name: String): Boolean {
        return RESERVED_WORDS.contains(name.lowercase())
    }

    private fun isValidVariableValue(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Number -> {
                when (value) {
                    is Double -> value.isFinite()
                    is Float -> value.isFinite()
                    else -> true
                }
            }
            is Boolean -> true
            is String -> value.length <= 1000
            else -> false
        }
    }

    private fun containsDangerousSequences(formula: String): Boolean {
        val dangerousSequences = listOf(
            "javascript:", "data:", "vbscript:",
            "../", "..\\", "./", ".\\",
            "<script", "</script>", "<iframe", "</iframe>",
            "onload=", "onerror=", "onclick="
        )
        
        return dangerousSequences.any { sequence ->
            formula.contains(sequence, ignoreCase = true)
        }
    }

    private fun hasExcessiveRecursion(formula: String): Boolean {
        // 간단한 재귀 패턴 감지
        val functionCalls = Regex("[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(").findAll(formula).count()
        return functionCalls > 20
    }

    private fun tokenizeFormula(formula: String): List<String> {
        // 간단한 토큰화 (실제로는 Lexer를 사용해야 함)
        val tokens = mutableListOf<String>()
        var i = 0
        
        while (i < formula.length) {
            val char = formula[i]
            when {
                char.isWhitespace() -> i++
                char.isDigit() -> {
                    val start = i
                    while (i < formula.length && (formula[i].isDigit() || formula[i] == '.')) {
                        i++
                    }
                    tokens.add(formula.substring(start, i))
                }
                char.isLetter() -> {
                    val start = i
                    while (i < formula.length && (formula[i].isLetterOrDigit() || formula[i] == '_')) {
                        i++
                    }
                    tokens.add(formula.substring(start, i))
                }
                else -> {
                    tokens.add(char.toString())
                    i++
                }
            }
        }
        
        return tokens
    }

    private fun isAllowedToken(token: String): Boolean {
        return when {
            token.matches(Regex("\\d+(\\.\\d+)?")) -> true // 숫자
            VALID_VARIABLE_NAME_PATTERN.matches(token) -> {
                // 변수명 또는 함수명
                token.uppercase() in ALLOWED_FUNCTIONS || isValidVariableName(token)
            }
            token in ALLOWED_OPERATORS -> true
            else -> false
        }
    }

    private fun extractFormulaFromStep(step: Any): String {
        // 실제 구현에서는 Step 객체의 구조에 따라 구현
        return step.toString()
    }

    private fun extractVariablesFromFormula(formula: String): Set<String> {
        val tokens = tokenizeFormula(formula)
        return tokens.filter { token ->
            VALID_VARIABLE_NAME_PATTERN.matches(token) &&
            token.uppercase() !in ALLOWED_FUNCTIONS
        }.toSet()
    }

    private fun extractAssignedVariable(step: Any): String? {
        // 실제 구현에서는 Step 객체에서 할당 변수를 추출
        // 예: "x = 2 + 3" -> "x"
        return null
    }

    /**
     * 검증 오류를 상세히 반환합니다.
     */
    fun getValidationErrors(
        request: CalculationRequest,
        maxFormulaLength: Int = DEFAULT_MAX_FORMULA_LENGTH,
        maxVariables: Int = DEFAULT_MAX_VARIABLES
    ): List<Map<String, Any>> {
        val errors = mutableListOf<Map<String, Any>>()
        
        try {
            // 수식 검증 오류
            if (!validateFormula(request.formula, maxFormulaLength)) {
                errors.add(mapOf(
                    "errorCode" to ErrorCodes.Calculator.INVALID_FORMULA.code,
                    "message" to "수식이 유효하지 않습니다: ${request.formula}",
                    "severity" to "ERROR"
                ))
            }
            
            // 변수 검증 오류
            if (!validateVariables(request.variables, maxVariables)) {
                errors.add(mapOf(
                    "errorCode" to ErrorCodes.Calculator.TOO_MANY_VARIABLES.code,
                    "message" to "변수 개수 또는 유효성 오류: ${request.variables.size}개 변수",
                    "severity" to "ERROR"
                ))
            }
            
            // 보안 검증 오류
            if (!validateSecurity(request.formula)) {
                errors.add(mapOf(
                    "errorCode" to ErrorCodes.Evaluator.SECURITY_VIOLATION.code,
                    "message" to "보안 위반: 의심스러운 패턴이 감지되었습니다",
                    "severity" to "CRITICAL"
                ))
            }
            
            // 구문 검증 오류
            if (!validateSyntax(request.formula)) {
                errors.add(mapOf(
                    "errorCode" to ErrorCodes.Parser.SYNTAX_ERROR.code,
                    "message" to "구문 오류: 허용되지 않는 토큰이 포함되어 있습니다",
                    "severity" to "ERROR"
                ))
            }
            
        } catch (e: Exception) {
            errors.add(mapOf(
                "errorCode" to ErrorCodes.Common.UNKNOWN_ERROR.code,
                "message" to "검증 중 예상치 못한 오류 발생: ${e.message}",
                "severity" to "CRITICAL"
            ))
        }
        
        return errors
    }

    /**
     * 검증 오류를 나타내는 데이터 클래스입니다.
     */
    data class ValidationError(
        val code: String,
        val message: String,
        val severity: Severity = Severity.ERROR
    ) {
        enum class Severity {
            INFO, WARNING, ERROR, CRITICAL
        }
    }

    /**
     * 명세의 설정 정보를 반환합니다.
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "name" to "CalculatorValiditySpec",
        "based_on" to "POC_FormulaValidator",
        "maxFormulaLength" to DEFAULT_MAX_FORMULA_LENGTH,
        "maxSteps" to DEFAULT_MAX_STEPS,
        "maxVariables" to DEFAULT_MAX_VARIABLES,
        "allowedOperators" to ALLOWED_OPERATORS.size,
        "allowedFunctions" to ALLOWED_FUNCTIONS.size,
        "reservedWords" to RESERVED_WORDS.size,
        "securityValidation" to true,
        "syntaxValidation" to true,
        "variableValidation" to true,
        "multiStepSupport" to true
    )

    /**
     * 명세의 통계 정보를 반환합니다.
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "specificationName" to "CalculatorValiditySpec",
        "implementedFeatures" to listOf(
            "formula_validation", "variable_validation", "security_validation",
            "syntax_validation", "multi_step_validation", "step_sequence_validation"
        ),
        "pocCompatibility" to true,
        "validationLayers" to 4,
        "securityChecks" to 3,
        "priority" to Priority.HIGH.name
    )
}