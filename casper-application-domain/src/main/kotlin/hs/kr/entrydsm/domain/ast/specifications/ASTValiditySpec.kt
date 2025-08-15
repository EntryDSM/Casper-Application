package hs.kr.entrydsm.domain.ast.specifications

import hs.kr.entrydsm.domain.ast.entities.*
import hs.kr.entrydsm.domain.ast.utils.FunctionValidationRules
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
            else -> Msg.unsupportedNodeType(node::class.simpleName ?: UNKNOWN)
        }
    }

    fun getValidationResult(node: ASTNode): SpecificationResult<ASTNode> {
        val isValid = isSatisfiedBy(node)
        val message = if (isValid) Msg.VALIDATION_SUCCESS else getWhyNotSatisfied(node)
        return SpecificationResult(
            success = isValid,
            message = message,
            specification = this
        )
    }

    // ---- validators ----

    private fun isValidNumberNode(node: NumberNode): Boolean {
        return node.value.isFinite() &&
                !node.value.isNaN() &&
                node.value >= MIN_NUMBER_VALUE &&
                node.value <= MAX_NUMBER_VALUE
    }

    private fun isValidBooleanNode(@Suppress("UNUSED_PARAMETER") node: BooleanNode): Boolean = true

    private fun isValidVariableNode(node: VariableNode): Boolean {
        return node.name.isNotBlank() &&
                node.name.length <= MAX_VARIABLE_NAME_LENGTH &&
                isValidVariableName(node.name) &&
                !isReservedWord(node.name)
    }

    private fun isValidBinaryOpNode(node: BinaryOpNode): Boolean {
        return node.operator.isNotBlank() &&
                isSupportedBinaryOperator(node.operator) &&
                isSatisfiedBy(node.left) &&
                isSatisfiedBy(node.right) &&
                isValidBinaryOperation(node.left, node.operator, node.right)
    }

    private fun isValidUnaryOpNode(node: UnaryOpNode): Boolean {
        return node.operator.isNotBlank() &&
                isSupportedUnaryOperator(node.operator) &&
                isSatisfiedBy(node.operand) &&
                isValidUnaryOperation(node.operator, node.operand)
    }

    private fun isValidFunctionCallNode(node: FunctionCallNode): Boolean {
        return node.name.isNotBlank() &&
                node.name.length <= MAX_FUNCTION_NAME_LENGTH &&
                isValidFunctionName(node.name) &&
                node.args.size <= MAX_FUNCTION_ARGS &&
                node.args.all { isSatisfiedBy(it) } &&
                isValidFunctionCall(node.name, node.args)
    }

    private fun isValidIfNode(node: IfNode): Boolean {
        return isSatisfiedBy(node.condition) &&
                isSatisfiedBy(node.trueValue) &&
                isSatisfiedBy(node.falseValue) &&
                node.getDepth() <= MAX_NODE_DEPTH &&
                node.getSize() <= MAX_NODE_SIZE
    }

    private fun isValidArgumentsNode(node: ArgumentsNode): Boolean {
        return node.arguments.size <= MAX_ARGUMENTS_COUNT &&
                node.arguments.all { isSatisfiedBy(it) }
    }

    // ---- violation builders ----

    private fun getNumberNodeViolations(node: NumberNode): String {
        val violations = mutableListOf<String>()
        if (!node.value.isFinite()) {
            violations.add(Msg.numberNotFinite(node.value))
        }
        if (node.value.isNaN()) {
            violations.add(Msg.numberIsNaN())
        }
        if (node.value < MIN_NUMBER_VALUE) {
            violations.add(Msg.numberBelowMin(node.value, MIN_NUMBER_VALUE))
        }
        if (node.value > MAX_NUMBER_VALUE) {
            violations.add(Msg.numberAboveMax(node.value, MAX_NUMBER_VALUE))
        }
        return violations.joinToString("; ")
    }

    private fun getBooleanNodeViolations(@Suppress("UNUSED_PARAMETER") node: BooleanNode): String {
        return Msg.BOOLEAN_ALWAYS_VALID
    }

    private fun getVariableNodeViolations(node: VariableNode): String {
        val violations = mutableListOf<String>()
        if (node.name.isBlank()) {
            violations.add(Msg.variableNameBlank())
        }
        if (node.name.length > MAX_VARIABLE_NAME_LENGTH) {
            violations.add(Msg.variableNameTooLong(node.name.length, MAX_VARIABLE_NAME_LENGTH))
        }
        if (!isValidVariableName(node.name)) {
            violations.add(Msg.variableNameInvalid(node.name))
        }
        if (isReservedWord(node.name)) {
            violations.add(Msg.variableNameReserved(node.name))
        }
        return violations.joinToString("; ")
    }

    private fun getBinaryOpNodeViolations(node: BinaryOpNode): String {
        val violations = mutableListOf<String>()
        if (node.operator.isBlank()) {
            violations.add(Msg.operatorBlank())
        }
        if (!isSupportedBinaryOperator(node.operator)) {
            violations.add(Msg.binaryOperatorUnsupported(node.operator))
        }
        if (!isSatisfiedBy(node.left))  {
            violations.add(Msg.leftOperandInvalid(getWhyNotSatisfied(node.left)))
        }
        if (!isSatisfiedBy(node.right)) {
            violations.add(Msg.rightOperandInvalid(getWhyNotSatisfied(node.right)))
        }
        if (!isValidBinaryOperation(node.left, node.operator, node.right)) {
            violations.add(Msg.binaryOperationInvalid(node.left, node.operator, node.right))
        }
        return violations.joinToString("; ")
    }

    private fun getUnaryOpNodeViolations(node: UnaryOpNode): String {
        val violations = mutableListOf<String>()
        if (node.operator.isBlank()) {
            violations.add(Msg.operatorBlank())
        }
        if (!isSupportedUnaryOperator(node.operator)) {
            violations.add(Msg.unaryOperatorUnsupported(node.operator))
        }
        if (!isSatisfiedBy(node.operand)) {
            violations.add(Msg.operandInvalid(getWhyNotSatisfied(node.operand)))
        }
        if (!isValidUnaryOperation(node.operator, node.operand)) {
            violations.add(Msg.unaryOperationInvalid(node.operator, node.operand))
        }
        return violations.joinToString("; ")
    }

    private fun getFunctionCallNodeViolations(node: FunctionCallNode): String {
        val violations = mutableListOf<String>()
        if (node.name.isBlank()) {
            violations.add(Msg.functionNameBlank())
        }
        if (node.name.length > MAX_FUNCTION_NAME_LENGTH) {
            violations.add(Msg.functionNameTooLong(node.name.length, MAX_FUNCTION_NAME_LENGTH))
        }
        if (!isValidFunctionName(node.name)) {
            violations.add(Msg.functionNameInvalid(node.name))
        }
        if (node.args.size > MAX_FUNCTION_ARGS) {
            violations.add(Msg.functionArgsTooMany(node.args.size, MAX_FUNCTION_ARGS))
        }
        node.args.forEachIndexed { index, arg ->
            if (!isSatisfiedBy(arg)) {
                violations.add(Msg.functionArgInvalid(index, getWhyNotSatisfied(arg)))
            }
        }
        if (!isValidFunctionCall(node.name, node.args)) {
            violations.add(Msg.functionCallInvalid(node.name, node.args))
        }
        return violations.joinToString("; ")
    }

    private fun getIfNodeViolations(node: IfNode): String {
        val violations = mutableListOf<String>()
        if (!isSatisfiedBy(node.condition)) {
            violations.add(Msg.ifConditionInvalid(getWhyNotSatisfied(node.condition)))
        }
        if (!isSatisfiedBy(node.trueValue)) {
            violations.add(Msg.ifTrueInvalid(getWhyNotSatisfied(node.trueValue)))
        }
        if (!isSatisfiedBy(node.falseValue)) {
            violations.add(Msg.ifFalseInvalid(getWhyNotSatisfied(node.falseValue)))
        }
        val depth = node.getDepth()
        if (depth > MAX_NODE_DEPTH) {
            violations.add(Msg.nodeDepthExceeded(depth, MAX_NODE_DEPTH))
        }
        val size = node.getSize()
        if (size > MAX_NODE_SIZE) {
            violations.add(Msg.nodeSizeExceeded(size, MAX_NODE_SIZE))
        }
        return violations.joinToString("; ")
    }

    private fun getArgumentsNodeViolations(node: ArgumentsNode): String {
        val violations = mutableListOf<String>()
        if (node.arguments.size > MAX_ARGUMENTS_COUNT) {
            violations.add(Msg.argumentsTooMany(node.arguments.size, MAX_ARGUMENTS_COUNT))
        }
        node.arguments.forEachIndexed { index, arg ->
            if (!isSatisfiedBy(arg)) {
                violations.add(Msg.argumentInvalid(index, getWhyNotSatisfied(arg)))
            }
        }
        return violations.joinToString("; ")
    }

    // ---- helpers ----

    private fun isValidVariableName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter() && name[0] != '_') return false
        return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    private fun isValidFunctionName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter()) return false
        return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    private fun isReservedWord(name: String): Boolean = RESERVED_WORDS.contains(name.lowercase())

    private fun isSupportedBinaryOperator(operator: String): Boolean = BINARY_OPERATORS.contains(operator)

    private fun isSupportedUnaryOperator(operator: String): Boolean = UNARY_OPERATORS.contains(operator)

    private fun isValidBinaryOperation(left: ASTNode, operator: String, right: ASTNode): Boolean {
        return when (operator) {
            "/" -> !isZeroConstant(right)
            "%" -> !isZeroConstant(right)
            "^" -> !(isZeroConstant(left) && isZeroConstant(right))
            else -> true
        }
    }

    private fun isValidUnaryOperation(operator: String, operand: ASTNode): Boolean {
        return when (operator) {
            "!" -> true
            "-", "+" -> true
            else -> false
        }
    }

    private fun isValidFunctionCall(name: String, args: List<ASTNode>): Boolean {
        return FunctionValidationRules.isValidFunctionCall(name, args)
    }

    private fun isZeroConstant(node: ASTNode): Boolean {
        return node is NumberNode && node.value == 0.0
    }

    companion object {
        // --- constraints ---
        private const val MAX_NUMBER_VALUE = 1e15
        private const val MIN_NUMBER_VALUE = -1e15
        private const val MAX_VARIABLE_NAME_LENGTH = 50
        private const val MAX_FUNCTION_NAME_LENGTH = 50
        private const val MAX_FUNCTION_ARGS = 10
        private const val MAX_ARGUMENTS_COUNT = 100
        private const val MAX_NODE_SIZE = 1000
        private const val MAX_NODE_DEPTH = 50

        private val RESERVED_WORDS = setOf(
            "if", "else", "while", "for", "do", "break", "continue",
            "function", "return", "var", "let", "const", "true", "false",
            "null", "undefined", "this", "new", "typeof", "instanceof",
            "try", "catch", "finally", "throw", "switch", "case", "default"
        )

        private val BINARY_OPERATORS = setOf(
            "+", "-", "*", "/", "%", "^",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||"
        )

        private val UNARY_OPERATORS = setOf("-", "+", "!")

        private const val UNKNOWN = "Unknown"

        // --- messages / builders ---
        object Msg {
            // common
            const val VALIDATION_SUCCESS = "검증 성공"
            fun unsupportedNodeType(type: String) = "지원되지 않는 노드 타입입니다: $type"

            // number
            fun numberNotFinite(value: Double) = "숫자 값이 유한하지 않습니다: $value"
            fun numberIsNaN() = "숫자 값이 NaN입니다"
            fun numberBelowMin(value: Double, min: Double) =
                "숫자 값이 최소값 미만입니다: $value < $min"
            fun numberAboveMax(value: Double, max: Double) =
                "숫자 값이 최대값 초과입니다: $value > $max"

            // boolean
            const val BOOLEAN_ALWAYS_VALID = ""

            // variable
            fun variableNameBlank() = "변수명이 비어있습니다"
            fun variableNameTooLong(actual: Int, max: Int) =
                "변수명이 최대 길이를 초과합니다: $actual > $max"
            fun variableNameInvalid(name: String) =
                "유효하지 않은 변수명 형식입니다: $name"
            fun variableNameReserved(name: String) =
                "예약어는 변수명으로 사용할 수 없습니다: $name"

            // operators (common)
            fun operatorBlank() = "연산자가 비어있습니다"

            // binary
            fun binaryOperatorUnsupported(op: String) =
                "지원되지 않는 이항 연산자입니다: $op"
            fun leftOperandInvalid(reason: String) =
                "좌측 피연산자가 유효하지 않습니다: $reason"
            fun rightOperandInvalid(reason: String) =
                "우측 피연산자가 유효하지 않습니다: $reason"
            fun binaryOperationInvalid(left: ASTNode, op: String, right: ASTNode) =
                "유효하지 않은 이항 연산입니다: $left $op $right"

            // unary
            fun unaryOperatorUnsupported(op: String) =
                "지원되지 않는 단항 연산자입니다: $op"
            fun operandInvalid(reason: String) =
                "피연산자가 유효하지 않습니다: $reason"
            fun unaryOperationInvalid(op: String, operand: ASTNode) =
                "유효하지 않은 단항 연산입니다: $op$operand"

            // function call
            fun functionNameBlank() = "함수명이 비어있습니다"
            fun functionNameTooLong(actual: Int, max: Int) =
                "함수명이 최대 길이를 초과합니다: $actual > $max"
            fun functionNameInvalid(name: String) =
                "유효하지 않은 함수명 형식입니다: $name"
            fun functionArgsTooMany(actual: Int, max: Int) =
                "함수 인수 개수가 최대값을 초과합니다: $actual > $max"
            fun functionArgInvalid(index: Int, reason: String) =
                "인수 $index 가 유효하지 않습니다: $reason"
            fun functionCallInvalid(name: String, args: List<ASTNode>) =
                "유효하지 않은 함수 호출입니다: $name(${args.joinToString(", ")})"

            // if
            fun ifConditionInvalid(reason: String) =
                "조건식이 유효하지 않습니다: $reason"
            fun ifTrueInvalid(reason: String) =
                "참 값이 유효하지 않습니다: $reason"
            fun ifFalseInvalid(reason: String) =
                "거짓 값이 유효하지 않습니다: $reason"
            fun nodeDepthExceeded(actual: Int, max: Int) =
                "노드 깊이가 최대값을 초과합니다: $actual > $max"
            fun nodeSizeExceeded(actual: Int, max: Int) =
                "노드 크기가 최대값을 초과합니다: $actual > $max"

            // arguments
            fun argumentsTooMany(actual: Int, max: Int) =
                "인수 개수가 최대값을 초과합니다: $actual > $max"
            fun argumentInvalid(index: Int, reason: String) =
                "인수 $index 가 유효하지 않습니다: $reason"
        }
    }

    // SpecificationContract 구현
    override fun getName(): String = "AST 노드 유효성 사양"
    override fun getDescription(): String = "AST 노드가 도메인 규칙을 만족하는지 검증하는 사양"
    override fun getDomain(): String = "ast"
    override fun getPriority(): Priority = Priority.HIGH
}
