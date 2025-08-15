package hs.kr.entrydsm.domain.evaluator.specifications

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.evaluator.entities.EvaluationContext
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.exception.ValidationException

/**
 * 표현식 유효성 검증 명세를 구현하는 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 표현식의 유효성을 검증하는
 * 복합적인 비즈니스 규칙을 캡슐화합니다. 구문 검증, 의미 검증,
 * 타입 검증 등을 통해 표현식의 평가 가능성을 판단합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "ExpressionValidity",
    description = "표현식의 구문적, 의미적 유효성을 검증하는 명세",
    domain = "evaluator",
    priority = hs.kr.entrydsm.global.annotation.specification.type.Priority.HIGH
)
class ExpressionValiditySpec {

    companion object {
        private const val MAX_EXPRESSION_DEPTH = 100
        private const val MAX_VARIABLE_NAME_LENGTH = 100
        private const val MAX_FUNCTION_ARGUMENTS = 50
        private const val MAX_STRING_LENGTH = 10000
        
        // 예약된 함수 이름들
        private val RESERVED_FUNCTION_NAMES = setOf(
            "eval", "exec", "system", "runtime", "process", "file", "io"
        )
        
        // 의심스러운 함수 패턴들
        private val SUSPICIOUS_FUNCTION_PATTERNS = setOf(
            "eval", "exec", "system", "runtime", "process", "file", "io",
            "script", "command", "shell", "import", "require", "load"
        )
        
        // 의심스러운 변수 패턴들
        private val SUSPICIOUS_VARIABLE_PATTERNS = setOf(
            "system", "runtime", "process", "file", "path", "command",
            "exec", "eval", "shell", "script"
        )
        
        // 유효한 함수 이름 패턴
        private val VALID_FUNCTION_NAME_PATTERN = Regex("^[a-zA-Z][a-zA-Z0-9_]*$")
        
        // 유효한 변수 이름 패턴
        private val VALID_VARIABLE_NAME_PATTERN = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
    }

    /**
     * 표현식이 유효한지 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트
     * @return 유효하면 true
     */
    fun isSatisfiedBy(node: ASTNode, context: EvaluationContext): Boolean {
        return try {
            validateSyntax(node) &&
            validateSemantics(node, context) &&
            validateStructure(node) &&
            validateSecurity(node)
        } catch (e: Exception) {
            throw hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException.evaluationFailed(
                RuntimeException("표현식 유효성 검증 실패: ${e.message}", e)
            )
        }
    }

    /**
     * 표현식이 유효한지 검증합니다 (컨텍스트 없이).
     *
     * @param node 검증할 AST 노드
     * @return 유효하면 true
     */
    fun isSatisfiedBy(node: ASTNode): Boolean {
        return try {
            validateSyntax(node) &&
            validateStructure(node) &&
            validateSecurity(node)
        } catch (e: Exception) {
            throw hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException.evaluationFailed(
                RuntimeException("표현식 유효성 검증 실패: ${e.message}", e)
            )
        }
    }

    /**
     * 구문적 유효성을 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @return 유효하면 true
     */
    fun validateSyntax(node: ASTNode): Boolean {
        return when (node) {
            is NumberNode -> validateNumberNode(node)
            is BooleanNode -> validateBooleanNode(node)
            is VariableNode -> validateVariableNode(node)
            is BinaryOpNode -> validateBinaryOpNode(node)
            is UnaryOpNode -> validateUnaryOpNode(node)
            is FunctionCallNode -> validateFunctionCallNode(node)
            is IfNode -> validateIfNode(node)
            else -> {
                // 알 수 없는 노드 타입
                false
            }
        }
    }

    /**
     * 의미적 유효성을 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트
     * @return 유효하면 true
     */
    fun validateSemantics(node: ASTNode, context: EvaluationContext): Boolean {
        return validateVariableBindings(node, context) &&
               validateFunctionAvailability(node) &&
               validateTypeConsistency(node, context)
    }

    /**
     * 구조적 유효성을 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @return 유효하면 true
     */
    fun validateStructure(node: ASTNode): Boolean {
        return validateDepth(node, 0) &&
               validateComplexity(node) &&
               validateCircularReferences(node)
    }

    /**
     * 보안 검증을 수행합니다.
     *
     * @param node 검증할 AST 노드
     * @return 안전하면 true
     */
    fun validateSecurity(node: ASTNode): Boolean {
        return !containsReservedFunctions(node) &&
               !containsSuspiciousPatterns(node) &&
               validateFunctionSafety(node)
    }

    /**
     * 표현식에서 발견된 모든 오류를 반환합니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트 (선택적)
     * @return 오류 목록
     */
    fun getValidationErrors(node: ASTNode, context: EvaluationContext? = null): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        try {
            collectSyntaxErrors(node, errors)
            if (context != null) {
                collectSemanticErrors(node, context, errors)
            }
            collectStructuralErrors(node, errors)
            collectSecurityErrors(node, errors)
        } catch (e: Exception) {
            errors.add(ValidationError("UNKNOWN_ERROR", "검증 중 예상치 못한 오류 발생: ${e.message}"))
        }
        
        return errors
    }

    /**
     * 표현식의 복잡도 점수를 계산합니다.
     *
     * @param node 분석할 AST 노드
     * @return 복잡도 점수
     */
    fun calculateComplexityScore(node: ASTNode): Int {
        return when (node) {
            is NumberNode, is BooleanNode, is VariableNode -> 1
            is UnaryOpNode -> 2 + calculateComplexityScore(node.operand)
            is BinaryOpNode -> 3 + calculateComplexityScore(node.left) + calculateComplexityScore(node.right)
            is FunctionCallNode -> 5 + node.args.sumOf { calculateComplexityScore(it) }
            is IfNode -> 7 + calculateComplexityScore(node.condition) + 
                            calculateComplexityScore(node.trueValue) + 
                            calculateComplexityScore(node.falseValue)
            else -> 10 // 알 수 없는 노드는 높은 복잡도
        }
    }

    // Private validation methods

    private fun validateNumberNode(node: NumberNode): Boolean {
        return node.value.isFinite() && !node.value.isNaN()
    }

    private fun validateBooleanNode(node: BooleanNode): Boolean {
        return true // Boolean 노드는 항상 유효
    }

    private fun validateVariableNode(node: VariableNode): Boolean {
        return node.name.isNotBlank() &&
               node.name.length <= MAX_VARIABLE_NAME_LENGTH &&
               VALID_VARIABLE_NAME_PATTERN.matches(node.name)
    }

    private fun validateBinaryOpNode(node: BinaryOpNode): Boolean {
        return validateSyntax(node.left) &&
               validateSyntax(node.right) &&
               isValidBinaryOperator(node.operator)
    }

    private fun validateUnaryOpNode(node: UnaryOpNode): Boolean {
        return validateSyntax(node.operand) &&
               isValidUnaryOperator(node.operator)
    }

    private fun validateFunctionCallNode(node: FunctionCallNode): Boolean {
        return node.name.isNotBlank() &&
               VALID_FUNCTION_NAME_PATTERN.matches(node.name) &&
               node.args.size <= MAX_FUNCTION_ARGUMENTS &&
               node.args.all { validateSyntax(it) } &&
               !RESERVED_FUNCTION_NAMES.contains(node.name.lowercase())
    }

    private fun validateIfNode(node: IfNode): Boolean {
        return validateSyntax(node.condition) &&
               validateSyntax(node.trueValue) &&
               validateSyntax(node.falseValue)
    }

    private fun validateVariableBindings(node: ASTNode, context: EvaluationContext): Boolean {
        val requiredVariables = node.getVariables()
        return requiredVariables.all { context.hasVariable(it) }
    }

    private fun validateFunctionAvailability(node: ASTNode): Boolean {
        val usedFunctions = extractFunctionNames(node)
        return usedFunctions.all { isKnownFunction(it) }
    }

    private fun validateTypeConsistency(node: ASTNode, context: EvaluationContext): Boolean {
        // 간단한 타입 일관성 검사
        return true // 실제 구현에서는 더 정교한 타입 검사 필요
    }

    private fun validateDepth(node: ASTNode, currentDepth: Int): Boolean {
        if (currentDepth > MAX_EXPRESSION_DEPTH) {
            return false
        }
        return node.getChildren().all { validateDepth(it, currentDepth + 1) }
    }

    private fun validateComplexity(node: ASTNode): Boolean {
        val complexity = calculateComplexityScore(node)
        return complexity < 1000 // 복잡도 제한
    }

    private fun validateCircularReferences(node: ASTNode): Boolean {
        val visited = mutableSetOf<ASTNode>()
        
        fun checkCircular(current: ASTNode): Boolean {
            if (current in visited) {
                return false // 순환 참조 발견
            }
            visited.add(current)
            val result = current.getChildren().all { checkCircular(it) }
            visited.remove(current)
            return result
        }
        
        return checkCircular(node)
    }

    private fun containsReservedFunctions(node: ASTNode): Boolean {
        val usedFunctions = extractFunctionNames(node)
        return usedFunctions.any { RESERVED_FUNCTION_NAMES.contains(it.lowercase()) }
    }

    private fun containsSuspiciousPatterns(node: ASTNode): Boolean {
        return when (node) {
            is FunctionCallNode -> {
                // Check function name directly against suspicious patterns
                val functionName = node.name.lowercase()
                val isSuspiciousFunction = SUSPICIOUS_FUNCTION_PATTERNS.any { pattern ->
                    functionName == pattern || functionName.contains(pattern)
                }
                
                // Recursively check function arguments
                isSuspiciousFunction || node.args.any { containsSuspiciousPatterns(it) }
            }
            is VariableNode -> {
                // Check variable name directly against suspicious patterns
                val variableName = node.name.lowercase()
                SUSPICIOUS_VARIABLE_PATTERNS.any { pattern ->
                    variableName == pattern || variableName.contains(pattern)
                }
            }
            is BinaryOpNode -> {
                // Check both operands
                containsSuspiciousPatterns(node.left) || containsSuspiciousPatterns(node.right)
            }
            is UnaryOpNode -> {
                // Check the operand
                containsSuspiciousPatterns(node.operand)
            }
            is IfNode -> {
                // Check all branches of conditional expression
                containsSuspiciousPatterns(node.condition) ||
                containsSuspiciousPatterns(node.trueValue) ||
                containsSuspiciousPatterns(node.falseValue)
            }
            is NumberNode, is BooleanNode -> {
                // Primitive values are safe
                false
            }
            else -> {
                // For other node types, recursively check all children
                node.getChildren().any { containsSuspiciousPatterns(it) }
            }
        }
    }

    private fun validateFunctionSafety(node: ASTNode): Boolean {
        // 함수 안전성 검증 (예: 무한 재귀 가능성 등)
        return true // 간단한 구현
    }

    private fun isValidBinaryOperator(operator: String): Boolean {
        return operator in setOf(
            "+", "-", "*", "/", "%", "^",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||"
        )
    }

    private fun isValidUnaryOperator(operator: String): Boolean {
        return operator in setOf("+", "-", "!")
    }

    private fun isKnownFunction(functionName: String): Boolean {
        // 실제로는 등록된 함수 목록과 비교
        return functionName.uppercase() in setOf(
            "ABS", "SQRT", "ROUND", "MIN", "MAX", "SUM", "AVG", "AVERAGE",
            "IF", "POW", "LOG", "LOG10", "EXP", "SIN", "COS", "TAN",
            "ASIN", "ACOS", "ATAN", "ATAN2", "SINH", "COSH", "TANH",
            "ASINH", "ACOSH", "ATANH", "FLOOR", "CEIL", "CEILING",
            "TRUNCATE", "TRUNC", "SIGN", "RANDOM", "RAND", "RADIANS",
            "DEGREES", "PI", "E", "MOD", "GCD", "LCM", "FACTORIAL",
            "COMBINATION", "COMB", "PERMUTATION", "PERM"
        )
    }

    private fun extractFunctionNames(node: ASTNode): Set<String> {
        return when (node) {
            is FunctionCallNode -> {
                setOf(node.name) + node.args.flatMap { extractFunctionNames(it) }.toSet()
            }
            else -> {
                node.getChildren().flatMap { extractFunctionNames(it) }.toSet()
            }
        }
    }

    // Error collection methods

    private fun collectSyntaxErrors(node: ASTNode, errors: MutableList<ValidationError>) {
        if (!validateSyntax(node)) {
            errors.add(ValidationError("SYNTAX_ERROR", "구문 오류: $node"))
        }
        node.getChildren().forEach { collectSyntaxErrors(it, errors) }
    }

    private fun collectSemanticErrors(node: ASTNode, context: EvaluationContext, errors: MutableList<ValidationError>) {
        if (!validateSemantics(node, context)) {
            errors.add(ValidationError("SEMANTIC_ERROR", "의미 오류: $node"))
        }
        node.getChildren().forEach { collectSemanticErrors(it, context, errors) }
    }

    private fun collectStructuralErrors(node: ASTNode, errors: MutableList<ValidationError>) {
        if (!validateStructure(node)) {
            errors.add(ValidationError("STRUCTURAL_ERROR", "구조 오류: $node"))
        }
        node.getChildren().forEach { collectStructuralErrors(it, errors) }
    }

    private fun collectSecurityErrors(node: ASTNode, errors: MutableList<ValidationError>) {
        if (!validateSecurity(node)) {
            errors.add(ValidationError("SECURITY_ERROR", "보안 오류: $node"))
        }
        node.getChildren().forEach { collectSecurityErrors(it, errors) }
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
        "maxExpressionDepth" to MAX_EXPRESSION_DEPTH,
        "maxVariableNameLength" to MAX_VARIABLE_NAME_LENGTH,
        "maxFunctionArguments" to MAX_FUNCTION_ARGUMENTS,
        "maxStringLength" to MAX_STRING_LENGTH,
        "reservedFunctionNames" to RESERVED_FUNCTION_NAMES.size,
        "validationRules" to listOf("syntax", "semantics", "structure", "security")
    )

    /**
     * 명세의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "specificationName" to "ExpressionValiditySpec",
        "supportedNodeTypes" to 7,
        "validationLayers" to 4,
        "securityChecks" to 3,
        "complexityThreshold" to 1000
    )
}