package hs.kr.entrydsm.domain.calculator.specifications

import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.global.annotation.specification.Specification

/**
 * 계산 유효성 검증 명세를 구현하는 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 계산 요청의 유효성을 검증하는
 * 복합적인 비즈니스 규칙을 캡슐화합니다. 구문 검증, 의미 검증,
 * 보안 검증 등을 통해 계산의 실행 가능성을 판단합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "CalculationValidity",
    description = "계산 요청의 유효성과 실행 가능성을 검증하는 명세",
    domain = "calculator",
    priority = hs.kr.entrydsm.global.annotation.specification.type.Priority.HIGH
)
class CalculationValiditySpec {

    companion object {
        private const val MAX_EXPRESSION_LENGTH = 10000
        private const val MAX_VARIABLE_COUNT = 100
        private const val MAX_NESTING_DEPTH = 50
        private const val MAX_FUNCTION_ARGUMENTS = 20
        
        // 허용된 문자들
        private val ALLOWED_CHARACTERS = setOf(
            // 숫자와 소수점
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.',
            // 연산자
            '+', '-', '*', '/', '%', '^', '=', '!', '<', '>', '&', '|',
            // 괄호
            '(', ')',
            // 함수와 변수
            '_',
            // 공백과 구분자
            ' ', '\t', ',', ';'
        ) + ('a'..'z').toSet() + ('A'..'Z').toSet()
        
        // 허용된 연산자들
        private val ALLOWED_OPERATORS = setOf(
            "+", "-", "*", "/", "%", "^",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||", "!"
        )
        
        // 허용된 함수들
        private val ALLOWED_FUNCTIONS = setOf(
            "ABS", "SQRT", "ROUND", "MIN", "MAX", "SUM", "AVG", "AVERAGE",
            "IF", "POW", "LOG", "LOG10", "EXP", "SIN", "COS", "TAN",
            "ASIN", "ACOS", "ATAN", "ATAN2", "SINH", "COSH", "TANH",
            "ASINH", "ACOSH", "ATANH", "FLOOR", "CEIL", "CEILING",
            "TRUNCATE", "TRUNC", "SIGN", "RANDOM", "RAND", "RADIANS",
            "DEGREES", "PI", "E", "MOD", "GCD", "LCM", "FACTORIAL",
            "COMBINATION", "COMB", "PERMUTATION", "PERM"
        )
        
        // 금지된 패턴들
        private val FORBIDDEN_PATTERNS = listOf(
            Regex("\\beval\\b", RegexOption.IGNORE_CASE),
            Regex("\\bexec\\b", RegexOption.IGNORE_CASE),
            Regex("\\bsystem\\b", RegexOption.IGNORE_CASE),
            Regex("\\bprocess\\b", RegexOption.IGNORE_CASE),
            Regex("\\bfile\\b", RegexOption.IGNORE_CASE),
            Regex("\\bimport\\b", RegexOption.IGNORE_CASE),
            Regex("\\binclude\\b", RegexOption.IGNORE_CASE),
            Regex("__.*__"), // Python dunder methods
            Regex("\\$\\{.*\\}"), // Shell expansion
            Regex("<%.*%>") // Template injection
        )
        
        // 유효한 변수명 패턴
        private val VALID_VARIABLE_PATTERN = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
        
        // 유효한 숫자 패턴
        private val VALID_NUMBER_PATTERN = Regex("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")
    }

    /**
     * 계산 요청이 유효한지 검증합니다.
     *
     * @param request 검증할 계산 요청
     * @param session 계산 세션 (선택적)
     * @return 유효하면 true
     */
    fun isSatisfiedBy(request: CalculationRequest, session: CalculationSession? = null): Boolean {
        return try {
            validateBasicSyntax(request.formula) &&
            validateSecurity(request.formula) &&
            validateComplexity(request.formula) &&
            validateVariables(request, session) &&
            validateFunctions(request.formula) &&
            validateSemantics(request.formula)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 표현식의 기본 구문이 유효한지 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 유효하면 true
     */
    fun validateBasicSyntax(expression: String): Boolean {
        if (expression.isBlank() || expression.length > MAX_EXPRESSION_LENGTH) {
            return false
        }
        
        // 허용된 문자만 포함하는지 확인
        if (!expression.all { it in ALLOWED_CHARACTERS }) {
            return false
        }
        
        // 기본 구문 검사
        return validateParentheses(expression) &&
               validateOperators(expression) &&
               validateNumbers(expression)
    }

    /**
     * 표현식의 보안성을 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 안전하면 true
     */
    fun validateSecurity(expression: String): Boolean {
        // 금지된 패턴 검사
        if (FORBIDDEN_PATTERNS.any { it.containsMatchIn(expression) }) {
            return false
        }
        
        // 스크립트 인젝션 패턴 검사
        if (containsScriptInjection(expression)) {
            return false
        }
        
        // 과도한 재귀나 무한 루프 가능성 검사
        if (containsSuspiciousRecursion(expression)) {
            return false
        }
        
        return true
    }

    /**
     * 표현식의 복잡도를 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 적절한 복잡도면 true
     */
    fun validateComplexity(expression: String): Boolean {
        val nestingDepth = calculateNestingDepth(expression)
        if (nestingDepth > MAX_NESTING_DEPTH) {
            return false
        }
        
        val functionCount = countFunctions(expression)
        if (functionCount > 50) { // 함수 호출 개수 제한
            return false
        }
        
        val operatorCount = countOperators(expression)
        if (operatorCount > 1000) { // 연산자 개수 제한
            return false
        }
        
        return true
    }

    /**
     * 변수 사용의 유효성을 검증합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션
     * @return 유효하면 true
     */
    fun validateVariables(request: CalculationRequest, session: CalculationSession?): Boolean {
        val usedVariables = extractVariables(request.formula)
        
        // 변수 개수 제한
        if (usedVariables.size > MAX_VARIABLE_COUNT) {
            return false
        }
        
        // 변수명 유효성 검사
        if (!usedVariables.all { VALID_VARIABLE_PATTERN.matches(it) }) {
            return false
        }
        
        // 세션 컨텍스트에서 변수 존재 여부 확인
        if (session != null) {
            val providedVariables = request.variables + session.variables
            if (!usedVariables.all { it in providedVariables }) {
                return false
            }
        }
        
        return true
    }

    /**
     * 함수 사용의 유효성을 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 유효하면 true
     */
    fun validateFunctions(expression: String): Boolean {
        val usedFunctions = extractFunctions(expression)
        
        // 허용된 함수만 사용했는지 확인
        if (!usedFunctions.all { it.uppercase() in ALLOWED_FUNCTIONS }) {
            return false
        }
        
        // 함수 인수 개수 검증
        return validateFunctionArguments(expression)
    }

    /**
     * 표현식의 의미론적 유효성을 검증합니다.
     *
     * @param expression 검증할 표현식
     * @return 유효하면 true
     */
    fun validateSemantics(expression: String): Boolean {
        // 0으로 나누기 가능성 검사
        if (containsDivisionByZero(expression)) {
            return false
        }
        
        // 정의역 오류 가능성 검사
        if (containsDomainErrors(expression)) {
            return false
        }
        
        // 타입 불일치 가능성 검사
        if (containsTypeMismatches(expression)) {
            return false
        }
        
        return true
    }

    /**
     * 표현식에서 발견된 모든 오류를 반환합니다.
     *
     * @param request 검증할 계산 요청
     * @param session 계산 세션 (선택적)
     * @return 오류 목록
     */
    fun getValidationErrors(request: CalculationRequest, session: CalculationSession? = null): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        try {
            if (!validateBasicSyntax(request.formula)) {
                errors.add(ValidationError("SYNTAX_ERROR", "기본 구문 오류"))
            }
            
            if (!validateSecurity(request.formula)) {
                errors.add(ValidationError("SECURITY_ERROR", "보안 위험 감지"))
            }
            
            if (!validateComplexity(request.formula)) {
                errors.add(ValidationError("COMPLEXITY_ERROR", "표현식이 너무 복잡함"))
            }
            
            if (!validateVariables(request, session)) {
                errors.add(ValidationError("VARIABLE_ERROR", "변수 사용 오류"))
            }
            
            if (!validateFunctions(request.formula)) {
                errors.add(ValidationError("FUNCTION_ERROR", "함수 사용 오류"))
            }
            
            if (!validateSemantics(request.formula)) {
                errors.add(ValidationError("SEMANTIC_ERROR", "의미론적 오류"))
            }
        } catch (e: Exception) {
            errors.add(ValidationError("VALIDATION_ERROR", "검증 중 오류 발생: ${e.message}"))
        }
        
        return errors
    }

    /**
     * 표현식의 위험도를 계산합니다.
     *
     * @param expression 분석할 표현식
     * @return 위험도 점수 (0-100)
     */
    fun calculateRiskScore(expression: String): Int {
        var risk = 0
        
        // 길이에 따른 위험도
        risk += (expression.length / 100).coerceAtMost(10)
        
        // 복잡도에 따른 위험도
        risk += (calculateNestingDepth(expression) * 2).coerceAtMost(20)
        
        // 함수 사용에 따른 위험도
        risk += (countFunctions(expression) * 2).coerceAtMost(20)
        
        // 금지된 패턴에 따른 위험도
        val forbiddenMatches = FORBIDDEN_PATTERNS.count { it.containsMatchIn(expression) }
        risk += (forbiddenMatches * 30).coerceAtMost(50)
        
        return risk.coerceAtMost(100)
    }

    // Private helper methods

    private fun validateParentheses(expression: String): Boolean {
        var depth = 0
        for (char in expression) {
            when (char) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth < 0) return false
                }
            }
        }
        return depth == 0
    }

    private fun validateOperators(expression: String): Boolean {
        // 연속된 연산자 검사
        return !Regex("[+\\-*/^%]{2,}").containsMatchIn(expression) &&
               !expression.startsWith("*/^%") && // 시작 부분 연산자 검사
               !expression.endsWith("+-*/^%") // 끝 부분 연산자 검사
    }

    private fun validateNumbers(expression: String): Boolean {
        val numbers = Regex("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b").findAll(expression)
        return numbers.all { 
            try {
                it.value.toDouble()
                true
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private fun calculateNestingDepth(expression: String): Int {
        var depth = 0
        var maxDepth = 0
        for (char in expression) {
            when (char) {
                '(' -> {
                    depth++
                    maxDepth = maxOf(maxDepth, depth)
                }
                ')' -> depth--
            }
        }
        return maxDepth
    }

    private fun countFunctions(expression: String): Int {
        return Regex("[a-zA-Z]\\w*\\s*\\(").findAll(expression).count()
    }

    private fun countOperators(expression: String): Int {
        return expression.count { it in "+-*/%^=!<>&|" }
    }

    private fun extractVariables(expression: String): Set<String> {
        val variables = mutableSetOf<String>()
        val pattern = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
        
        pattern.findAll(expression).forEach { match ->
            val word = match.value.uppercase()
            if (word !in ALLOWED_FUNCTIONS) {
                variables.add(match.value)
            }
        }
        
        return variables
    }

    private fun extractFunctions(expression: String): Set<String> {
        val functions = mutableSetOf<String>()
        val pattern = Regex("([a-zA-Z]\\w*)\\s*\\(")
        
        pattern.findAll(expression).forEach { match ->
            functions.add(match.groupValues[1])
        }
        
        return functions
    }

    private fun validateFunctionArguments(expression: String): Boolean {
        val functionCalls = Regex("([a-zA-Z]\\w*)\\s*\\(([^)]*)\\)").findAll(expression)
        
        return functionCalls.all { match ->
            val functionName = match.groupValues[1]
            val argsString = match.groupValues[2].trim()
            val argCount = if (argsString.isEmpty()) 0 else argsString.split(',').size
            
            argCount <= MAX_FUNCTION_ARGUMENTS
        }
    }

    private fun containsScriptInjection(expression: String): Boolean {
        val injectionPatterns = listOf(
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("vbscript:", RegexOption.IGNORE_CASE),
            Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE)
        )
        
        return injectionPatterns.any { it.containsMatchIn(expression) }
    }

    private fun containsSuspiciousRecursion(expression: String): Boolean {
        // 매우 간단한 검사 - 실제로는 더 정교한 분석이 필요
        return expression.contains("while") ||
               expression.contains("for") ||
               expression.contains("loop")
    }

    private fun containsDivisionByZero(expression: String): Boolean {
        // 명시적인 0으로 나누기 검사
        return Regex("/\\s*0\\b").containsMatchIn(expression) ||
               Regex("/\\s*\\(\\s*0\\s*\\)").containsMatchIn(expression)
    }

    private fun containsDomainErrors(expression: String): Boolean {
        // 일반적인 정의역 오류 패턴들
        return Regex("sqrt\\s*\\(\\s*-").containsMatchIn(expression) ||
               Regex("log\\s*\\(\\s*0\\b").containsMatchIn(expression) ||
               Regex("log\\s*\\(\\s*-").containsMatchIn(expression)
    }

    private fun containsTypeMismatches(expression: String): Boolean {
        // 간단한 타입 불일치 검사
        return false // 실제로는 더 정교한 타입 분석이 필요
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
            WARNING, ERROR, CRITICAL
        }
    }

    /**
     * 명세의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxExpressionLength" to MAX_EXPRESSION_LENGTH,
        "maxVariableCount" to MAX_VARIABLE_COUNT,
        "maxNestingDepth" to MAX_NESTING_DEPTH,
        "maxFunctionArguments" to MAX_FUNCTION_ARGUMENTS,
        "allowedFunctions" to ALLOWED_FUNCTIONS.size,
        "allowedOperators" to ALLOWED_OPERATORS.size,
        "forbiddenPatterns" to FORBIDDEN_PATTERNS.size,
        "validationLayers" to listOf("syntax", "security", "complexity", "variables", "functions", "semantics")
    )

    /**
     * 명세의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "specificationName" to "CalculationValiditySpec",
        "validationRules" to 6,
        "securityChecks" to FORBIDDEN_PATTERNS.size,
        "supportedFunctions" to ALLOWED_FUNCTIONS.size,
        "supportedOperators" to ALLOWED_OPERATORS.size,
        "riskFactors" to listOf("length", "complexity", "functions", "forbidden_patterns")
    )
}