package hs.kr.entrydsm.domain.ast.specifications

import hs.kr.entrydsm.domain.ast.entities.*
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.SpecificationResult
import hs.kr.entrydsm.global.annotation.specification.SpecificationContract
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * AST 노드 유효성 사양을 정의하는 클래스입니다.
 *
 * AST 노드가 도메인 규칙을 만족하는지 검증하며,
 * 복합 사양을 통해 복잡한 검증 로직을 구성할 수 있습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Specification(
    name = "AST 노드 유효성 사양",
    description = "AST 노드가 도메인 규칙을 만족하는지 검증하는 사양",
    domain = "ast",
    priority = Priority.HIGH
)
class ASTValiditySpec : SpecificationContract<ASTNode> {

    /**
     * AST 노드가 사양을 만족하는지 확인합니다.
     *
     * @param node 검증할 AST 노드
     * @return 사양 만족 여부
     */
    override fun isSatisfiedBy(node: ASTNode): Boolean {
        return when (node) {
            is NumberNode -> isValidNumberNode(node)
            is BooleanNode -> isValidBooleanNode(node)
            is VariableNode -> isValidVariableNode(node)
            is BinaryOpNode -> isValidBinaryOpNode(node)
            is UnaryOpNode -> isValidUnaryOpNode(node)
            is FunctionCallNode -> isValidFunctionCallNode(node)
            is IfNode -> isValidIfNode(node)
            is ArgumentsNode -> isValidArgumentsNode(node)
            else -> false
        }
    }

    /**
     * 사양을 만족하지 않는 이유를 반환합니다.
     *
     * @param node 검증할 AST 노드
     * @return 사양 불만족 이유
     */
    fun getWhyNotSatisfied(node: ASTNode): String {
        return when (node) {
            is NumberNode -> getNumberNodeViolations(node)
            is BooleanNode -> getBooleanNodeViolations(node)
            is VariableNode -> getVariableNodeViolations(node)
            is BinaryOpNode -> getBinaryOpNodeViolations(node)
            is UnaryOpNode -> getUnaryOpNodeViolations(node)
            is FunctionCallNode -> getFunctionCallNodeViolations(node)
            is IfNode -> getIfNodeViolations(node)
            is ArgumentsNode -> getArgumentsNodeViolations(node)
            else -> "지원되지 않는 노드 타입입니다: ${node::class.simpleName}"
        }
    }

    /**
     * 상세한 검증 결과를 반환합니다.
     *
     * @param node 검증할 AST 노드
     * @return 검증 결과
     */
    fun getValidationResult(node: ASTNode): SpecificationResult<ASTNode> {
        val isValid = isSatisfiedBy(node)
        val message = if (isValid) "검증 성공" else getWhyNotSatisfied(node)
        
        return SpecificationResult(
            success = isValid,
            message = message,
            specification = this
        )
    }

    /**
     * 숫자 노드의 유효성을 검증합니다.
     */
    private fun isValidNumberNode(node: NumberNode): Boolean {
        return node.value.isFinite() && 
               !node.value.isNaN() &&
               node.value >= MIN_NUMBER_VALUE &&
               node.value <= MAX_NUMBER_VALUE
    }

    /**
     * 불리언 노드의 유효성을 검증합니다.
     */
    private fun isValidBooleanNode(node: BooleanNode): Boolean {
        // 불리언 노드는 항상 유효
        return true
    }

    /**
     * 변수 노드의 유효성을 검증합니다.
     */
    private fun isValidVariableNode(node: VariableNode): Boolean {
        return node.name.isNotBlank() &&
               node.name.length <= MAX_VARIABLE_NAME_LENGTH &&
               isValidVariableName(node.name) &&
               !isReservedWord(node.name)
    }

    /**
     * 이항 연산 노드의 유효성을 검증합니다.
     */
    private fun isValidBinaryOpNode(node: BinaryOpNode): Boolean {
        return node.operator.isNotBlank() &&
               isSupportedBinaryOperator(node.operator) &&
               isSatisfiedBy(node.left) &&
               isSatisfiedBy(node.right) &&
               isValidBinaryOperation(node.left, node.operator, node.right)
    }

    /**
     * 단항 연산 노드의 유효성을 검증합니다.
     */
    private fun isValidUnaryOpNode(node: UnaryOpNode): Boolean {
        return node.operator.isNotBlank() &&
               isSupportedUnaryOperator(node.operator) &&
               isSatisfiedBy(node.operand) &&
               isValidUnaryOperation(node.operator, node.operand)
    }

    /**
     * 함수 호출 노드의 유효성을 검증합니다.
     */
    private fun isValidFunctionCallNode(node: FunctionCallNode): Boolean {
        return node.name.isNotBlank() &&
               node.name.length <= MAX_FUNCTION_NAME_LENGTH &&
               isValidFunctionName(node.name) &&
               node.args.size <= MAX_FUNCTION_ARGS &&
               node.args.all { isSatisfiedBy(it) } &&
               isValidFunctionCall(node.name, node.args)
    }

    /**
     * 조건문 노드의 유효성을 검증합니다.
     */
    private fun isValidIfNode(node: IfNode): Boolean {
        return isSatisfiedBy(node.condition) &&
               isSatisfiedBy(node.trueValue) &&
               isSatisfiedBy(node.falseValue) &&
               node.getDepth() <= MAX_NODE_DEPTH &&
               node.getSize() <= MAX_NODE_SIZE
    }

    /**
     * 인수 목록 노드의 유효성을 검증합니다.
     */
    private fun isValidArgumentsNode(node: ArgumentsNode): Boolean {
        return node.arguments.size <= MAX_ARGUMENTS_COUNT &&
               node.arguments.all { isSatisfiedBy(it) }
    }

    /**
     * 숫자 노드 위반 사항을 반환합니다.
     */
    private fun getNumberNodeViolations(node: NumberNode): String {
        val violations = mutableListOf<String>()
        
        if (!node.value.isFinite()) {
            violations.add("숫자 값이 유한하지 않습니다: ${node.value}")
        }
        if (node.value.isNaN()) {
            violations.add("숫자 값이 NaN입니다")
        }
        if (node.value < MIN_NUMBER_VALUE) {
            violations.add("숫자 값이 최소값 미만입니다: ${node.value} < $MIN_NUMBER_VALUE")
        }
        if (node.value > MAX_NUMBER_VALUE) {
            violations.add("숫자 값이 최대값 초과입니다: ${node.value} > $MAX_NUMBER_VALUE")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 불리언 노드 위반 사항을 반환합니다.
     */
    private fun getBooleanNodeViolations(node: BooleanNode): String {
        return "" // 불리언 노드는 항상 유효
    }

    /**
     * 변수 노드 위반 사항을 반환합니다.
     */
    private fun getVariableNodeViolations(node: VariableNode): String {
        val violations = mutableListOf<String>()
        
        if (node.name.isBlank()) {
            violations.add("변수명이 비어있습니다")
        }
        if (node.name.length > MAX_VARIABLE_NAME_LENGTH) {
            violations.add("변수명이 최대 길이를 초과합니다: ${node.name.length} > $MAX_VARIABLE_NAME_LENGTH")
        }
        if (!isValidVariableName(node.name)) {
            violations.add("유효하지 않은 변수명 형식입니다: ${node.name}")
        }
        if (isReservedWord(node.name)) {
            violations.add("예약어는 변수명으로 사용할 수 없습니다: ${node.name}")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 이항 연산 노드 위반 사항을 반환합니다.
     */
    private fun getBinaryOpNodeViolations(node: BinaryOpNode): String {
        val violations = mutableListOf<String>()
        
        if (node.operator.isBlank()) {
            violations.add("연산자가 비어있습니다")
        }
        if (!isSupportedBinaryOperator(node.operator)) {
            violations.add("지원되지 않는 이항 연산자입니다: ${node.operator}")
        }
        if (!isSatisfiedBy(node.left)) {
            violations.add("좌측 피연산자가 유효하지 않습니다: ${getWhyNotSatisfied(node.left)}")
        }
        if (!isSatisfiedBy(node.right)) {
            violations.add("우측 피연산자가 유효하지 않습니다: ${getWhyNotSatisfied(node.right)}")
        }
        if (!isValidBinaryOperation(node.left, node.operator, node.right)) {
            violations.add("유효하지 않은 이항 연산입니다: ${node.left} ${node.operator} ${node.right}")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 단항 연산 노드 위반 사항을 반환합니다.
     */
    private fun getUnaryOpNodeViolations(node: UnaryOpNode): String {
        val violations = mutableListOf<String>()
        
        if (node.operator.isBlank()) {
            violations.add("연산자가 비어있습니다")
        }
        if (!isSupportedUnaryOperator(node.operator)) {
            violations.add("지원되지 않는 단항 연산자입니다: ${node.operator}")
        }
        if (!isSatisfiedBy(node.operand)) {
            violations.add("피연산자가 유효하지 않습니다: ${getWhyNotSatisfied(node.operand)}")
        }
        if (!isValidUnaryOperation(node.operator, node.operand)) {
            violations.add("유효하지 않은 단항 연산입니다: ${node.operator}${node.operand}")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 함수 호출 노드 위반 사항을 반환합니다.
     */
    private fun getFunctionCallNodeViolations(node: FunctionCallNode): String {
        val violations = mutableListOf<String>()
        
        if (node.name.isBlank()) {
            violations.add("함수명이 비어있습니다")
        }
        if (node.name.length > MAX_FUNCTION_NAME_LENGTH) {
            violations.add("함수명이 최대 길이를 초과합니다: ${node.name.length} > $MAX_FUNCTION_NAME_LENGTH")
        }
        if (!isValidFunctionName(node.name)) {
            violations.add("유효하지 않은 함수명 형식입니다: ${node.name}")
        }
        if (node.args.size > MAX_FUNCTION_ARGS) {
            violations.add("함수 인수 개수가 최대값을 초과합니다: ${node.args.size} > $MAX_FUNCTION_ARGS")
        }
        
        node.args.forEachIndexed { index, arg ->
            if (!isSatisfiedBy(arg)) {
                violations.add("인수 $index 가 유효하지 않습니다: ${getWhyNotSatisfied(arg)}")
            }
        }
        
        if (!isValidFunctionCall(node.name, node.args)) {
            violations.add("유효하지 않은 함수 호출입니다: ${node.name}(${node.args.joinToString(", ")})")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 조건문 노드 위반 사항을 반환합니다.
     */
    private fun getIfNodeViolations(node: IfNode): String {
        val violations = mutableListOf<String>()
        
        if (!isSatisfiedBy(node.condition)) {
            violations.add("조건식이 유효하지 않습니다: ${getWhyNotSatisfied(node.condition)}")
        }
        if (!isSatisfiedBy(node.trueValue)) {
            violations.add("참 값이 유효하지 않습니다: ${getWhyNotSatisfied(node.trueValue)}")
        }
        if (!isSatisfiedBy(node.falseValue)) {
            violations.add("거짓 값이 유효하지 않습니다: ${getWhyNotSatisfied(node.falseValue)}")
        }
        if (node.getDepth() > MAX_NODE_DEPTH) {
            violations.add("노드 깊이가 최대값을 초과합니다: ${node.getDepth()} > $MAX_NODE_DEPTH")
        }
        if (node.getSize() > MAX_NODE_SIZE) {
            violations.add("노드 크기가 최대값을 초과합니다: ${node.getSize()} > $MAX_NODE_SIZE")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 인수 목록 노드 위반 사항을 반환합니다.
     */
    private fun getArgumentsNodeViolations(node: ArgumentsNode): String {
        val violations = mutableListOf<String>()
        
        if (node.arguments.size > MAX_ARGUMENTS_COUNT) {
            violations.add("인수 개수가 최대값을 초과합니다: ${node.arguments.size} > $MAX_ARGUMENTS_COUNT")
        }
        
        node.arguments.forEachIndexed { index, arg ->
            if (!isSatisfiedBy(arg)) {
                violations.add("인수 $index 가 유효하지 않습니다: ${getWhyNotSatisfied(arg)}")
            }
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 변수명이 유효한지 확인합니다.
     */
    private fun isValidVariableName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter() && name[0] != '_') return false
        return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * 함수명이 유효한지 확인합니다.
     */
    private fun isValidFunctionName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter()) return false
        return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * 예약어인지 확인합니다.
     */
    private fun isReservedWord(name: String): Boolean {
        return RESERVED_WORDS.contains(name.lowercase())
    }

    /**
     * 지원되는 이항 연산자인지 확인합니다.
     */
    private fun isSupportedBinaryOperator(operator: String): Boolean {
        return BINARY_OPERATORS.contains(operator)
    }

    /**
     * 지원되는 단항 연산자인지 확인합니다.
     */
    private fun isSupportedUnaryOperator(operator: String): Boolean {
        return UNARY_OPERATORS.contains(operator)
    }

    /**
     * 유효한 이항 연산인지 확인합니다.
     */
    private fun isValidBinaryOperation(left: ASTNode, operator: String, right: ASTNode): Boolean {
        return when (operator) {
            "/" -> !isZeroConstant(right)
            "%" -> !isZeroConstant(right)
            "^" -> !(isZeroConstant(left) && isZeroConstant(right))
            else -> true
        }
    }

    /**
     * 유효한 단항 연산인지 확인합니다.
     */
    private fun isValidUnaryOperation(operator: String, operand: ASTNode): Boolean {
        return when (operator) {
            "!" -> true // 모든 타입에 대해 논리 부정 허용
            "-", "+" -> true // 모든 타입에 대해 부호 연산 허용
            else -> false
        }
    }

    /**
     * 유효한 함수 호출인지 확인합니다.
     */
    private fun isValidFunctionCall(name: String, args: List<ASTNode>): Boolean {
        return when (name.uppercase()) {
            "SQRT" -> args.size == 1
            "POW" -> args.size == 2
            "SIN", "COS", "TAN", "ABS", "LOG", "EXP" -> args.size == 1
            "MAX", "MIN" -> args.isNotEmpty()
            "IF" -> args.size == 3
            else -> true // 기본적으로 허용
        }
    }

    /**
     * 노드가 0 상수인지 확인합니다.
     */
    private fun isZeroConstant(node: ASTNode): Boolean {
        return node is NumberNode && node.value == 0.0
    }

    companion object {
        // 제약 상수
        private const val MAX_NUMBER_VALUE = 1e15
        private const val MIN_NUMBER_VALUE = -1e15
        private const val MAX_VARIABLE_NAME_LENGTH = 50
        private const val MAX_FUNCTION_NAME_LENGTH = 50
        private const val MAX_FUNCTION_ARGS = 10
        private const val MAX_ARGUMENTS_COUNT = 100
        private const val MAX_NODE_SIZE = 1000
        private const val MAX_NODE_DEPTH = 50

        // 예약어
        private val RESERVED_WORDS = setOf(
            "if", "else", "while", "for", "do", "break", "continue",
            "function", "return", "var", "let", "const", "true", "false",
            "null", "undefined", "this", "new", "typeof", "instanceof",
            "try", "catch", "finally", "throw", "switch", "case", "default"
        )

        // 지원되는 연산자
        private val BINARY_OPERATORS = setOf(
            "+", "-", "*", "/", "%", "^",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||"
        )

        private val UNARY_OPERATORS = setOf("-", "+", "!")
    }

    // SpecificationContract 구현
    override fun getName(): String = "AST 노드 유효성 사양"
    
    override fun getDescription(): String = "AST 노드가 도메인 규칙을 만족하는지 검증하는 사양"
    
    override fun getDomain(): String = "ast"
    
    override fun getPriority(): Priority = Priority.HIGH
}