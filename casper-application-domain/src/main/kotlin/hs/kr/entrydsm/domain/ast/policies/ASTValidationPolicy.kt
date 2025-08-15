package hs.kr.entrydsm.domain.ast.policies

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.utils.ASTValidationUtils
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.PolicyResult
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * AST 노드 유효성 검증 정책을 구현하는 클래스입니다.
 *
 * AST 노드의 생성과 조작에 대한 비즈니스 규칙을 정의하고 검증하며,
 * 도메인 무결성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Policy(
    name = "AST 노드 유효성 검증 정책",
    description = "AST 노드의 생성과 조작에 대한 비즈니스 규칙을 정의하고 검증",
    domain = "ast",
    scope = Scope.DOMAIN
)
class ASTValidationPolicy {

    object ErrorMessages {
        // 숫자 노드 관련 메시지
        const val NUMBER_NOT_FINITE = "숫자 값은 유한해야 합니다"
        const val NUMBER_IS_NAN = "숫자 값은 NaN이 될 수 없습니다"
        const val NUMBER_EXCEEDS_MAX = "숫자 값이 최대값을 초과합니다"
        const val NUMBER_BELOW_MIN = "숫자 값이 최소값을 미만입니다"

        // 변수 노드 관련 메시지
        const val VARIABLE_NAME_BLANK = "변수명은 비어있을 수 없습니다"
        const val VARIABLE_NAME_TOO_LONG = "변수명이 최대 길이를 초과합니다"
        const val VARIABLE_NAME_INVALID = "유효하지 않은 변수명입니다"
        const val VARIABLE_NAME_RESERVED = "예약어는 변수명으로 사용할 수 없습니다"

        // 연산자 관련 메시지
        const val OPERATOR_BLANK = "연산자는 비어있을 수 없습니다"
        const val BINARY_OPERATOR_UNSUPPORTED = "지원되지 않는 이항 연산자입니다"
        const val UNARY_OPERATOR_UNSUPPORTED = "지원되지 않는 단항 연산자입니다"
        const val DIVISION_BY_ZERO = "0으로 나눌 수 없습니다"
        const val MODULO_BY_ZERO = "0으로 나눈 나머지를 구할 수 없습니다"

        // 피연산자 관련 메시지
        const val LEFT_OPERAND_INVALID = "좌측 피연산자가 유효하지 않습니다"
        const val RIGHT_OPERAND_INVALID = "우측 피연산자가 유효하지 않습니다"
        const val OPERAND_INVALID = "피연산자가 유효하지 않습니다"

        // 함수 관련 메시지
        const val FUNCTION_NAME_BLANK = "함수명은 비어있을 수 없습니다"
        const val FUNCTION_NAME_TOO_LONG = "함수명이 최대 길이를 초과합니다"
        const val FUNCTION_NAME_INVALID = "유효하지 않은 함수명입니다"
        const val FUNCTION_ARGS_EXCEED_MAX = "함수 인수 개수가 최대값을 초과합니다"
        const val FUNCTION_ARG_INVALID = "인수가 유효하지 않습니다"

        // 조건문 관련 메시지
        const val CONDITION_INVALID = "조건식이 유효하지 않습니다"
        const val TRUE_VALUE_INVALID = "참 값이 유효하지 않습니다"
        const val FALSE_VALUE_INVALID = "거짓 값이 유효하지 않습니다"
        const val NESTING_DEPTH_EXCEEDED = "중첩 깊이가 최대값을 초과합니다"

        // 인수 관련 메시지
        const val ARGUMENTS_COUNT_EXCEEDED = "인수 개수가 최대값을 초과합니다"
        const val ARGUMENT_INVALID = "인수가 유효하지 않습니다"

        // 노드 일반 검증 메시지
        const val NODE_SIZE_EXCEEDED = "노드 크기가 최대값을 초과합니다"
        const val NODE_DEPTH_EXCEEDED = "노드 깊이가 최대값을 초과합니다"
        const val VARIABLES_PER_NODE_EXCEEDED = "노드당 변수 개수가 최대값을 초과합니다"
    }

    /**
     * 숫자 노드 생성 정책을 검증합니다.
     *
     * @param value 숫자 값
     * @return 정책 검증 결과
     */
    fun validateNumberCreation(value: Double): PolicyResult {
        val violations = mutableListOf<String>()

        if (!value.isFinite()) {
            violations.add("${ErrorMessages.NUMBER_NOT_FINITE}: $value")
        }

        if (value.isNaN()) {
            violations.add(ErrorMessages.NUMBER_IS_NAN)
        }

        // 너무 큰 값 검증
        if (value > MAX_NUMBER_VALUE) {
            violations.add("${ErrorMessages.NUMBER_EXCEEDS_MAX}: $value > $MAX_NUMBER_VALUE")
        }

        if (value < MIN_NUMBER_VALUE) {
            violations.add("${ErrorMessages.NUMBER_BELOW_MIN}: $value < $MIN_NUMBER_VALUE")
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to NUMBER_NODE_POLICY)
        )
    }

    /**
     * 불리언 노드 생성 정책을 검증합니다.
     *
     * @param value 불리언 값
     * @return 정책 검증 결과
     */
    fun validateBooleanCreation(value: Boolean): PolicyResult {
        // 불리언 값은 항상 유효
        return PolicyResult(
            success = true,
            message = "",
            data = mapOf("policyName" to BOOLEAN_NODE_POLICY)
        )
    }

    /**
     * 변수 노드 생성 정책을 검증합니다.
     *
     * @param name 변수명
     * @return 정책 검증 결과
     */
    fun validateVariableCreation(name: String): PolicyResult {
        val violations = mutableListOf<String>()

        if (name.isBlank()) {
            violations.add(ErrorMessages.VARIABLE_NAME_BLANK)
        }

        if (name.length > MAX_VARIABLE_NAME_LENGTH) {
            violations.add("${ErrorMessages.VARIABLE_NAME_TOO_LONG}: ${name.length} > $MAX_VARIABLE_NAME_LENGTH")
        }

        if (!isValidVariableName(name)) {
            violations.add("${ErrorMessages.VARIABLE_NAME_INVALID}: $name")
        }

        if (isReservedWord(name)) {
            violations.add("${ErrorMessages.VARIABLE_NAME_RESERVED}: $name")
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to VARIABLE_NODE_POLICY)
        )
    }

    /**
     * 이항 연산 노드 생성 정책을 검증합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 연산자
     * @param right 우측 피연산자
     * @return 정책 검증 결과
     */
    fun validateBinaryOpCreation(left: ASTNode, operator: String, right: ASTNode): PolicyResult {
        val violations = mutableListOf<String>()

        if (operator.isBlank()) {
            violations.add(ErrorMessages.OPERATOR_BLANK)
        }

        if (!isSupportedBinaryOperator(operator)) {
            violations.add("${ErrorMessages.BINARY_OPERATOR_UNSUPPORTED}: $operator")
        }

        // 피연산자 검증
        val leftValidation = validateNode(left)
        if (!leftValidation.success) {
            violations.add("${ErrorMessages.LEFT_OPERAND_INVALID}: ${leftValidation.message}")
        }

        val rightValidation = validateNode(right)
        if (!rightValidation.success) {
            violations.add("${ErrorMessages.RIGHT_OPERAND_INVALID}: ${rightValidation.message}")
        }

        // 연산자별 특별 검증
        when (operator) {
            "/" -> {
                if (isZeroConstant(right)) {
                    violations.add(ErrorMessages.DIVISION_BY_ZERO)
                }
            }
            "%" -> {
                if (isZeroConstant(right)) {
                    violations.add(ErrorMessages.MODULO_BY_ZERO)
                }
            }
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to BINARY_OPERATION_NODE_POLICY)
        )
    }

    /**
     * 단항 연산 노드 생성 정책을 검증합니다.
     *
     * @param operator 연산자
     * @param operand 피연산자
     * @return 정책 검증 결과
     */
    fun validateUnaryOpCreation(operator: String, operand: ASTNode): PolicyResult {
        val violations = mutableListOf<String>()

        if (operator.isBlank()) {
            violations.add(ErrorMessages.OPERATOR_BLANK)
        }

        if (!isSupportedUnaryOperator(operator)) {
            violations.add("${ErrorMessages.UNARY_OPERATOR_UNSUPPORTED}: $operator")
        }

        // 피연산자 검증
        val operandValidation = validateNode(operand)
        if (!operandValidation.success) {
            violations.add("${ErrorMessages.OPERAND_INVALID}: ${operandValidation.message}")
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to UNARY_OPERATION_NODE_POLICY)
        )
    }

    /**
     * 함수 호출 노드 생성 정책을 검증합니다.
     *
     * @param name 함수명
     * @param args 인수 목록
     * @return 정책 검증 결과
     */
    fun validateFunctionCallCreation(name: String, args: List<ASTNode>): PolicyResult {
        val violations = mutableListOf<String>()

        if (name.isBlank()) {
            violations.add(ErrorMessages.FUNCTION_NAME_BLANK)
        }

        if (name.length > MAX_FUNCTION_NAME_LENGTH) {
            violations.add("${ErrorMessages.FUNCTION_NAME_TOO_LONG}: ${name.length} > $MAX_FUNCTION_NAME_LENGTH")
        }

        if (!isValidFunctionName(name)) {
            violations.add("${ErrorMessages.FUNCTION_NAME_INVALID}: $name")
        }

        if (args.size > MAX_FUNCTION_ARGS) {
            violations.add("${ErrorMessages.FUNCTION_ARGS_EXCEED_MAX}: ${args.size} > $MAX_FUNCTION_ARGS")
        }

        // 각 인수 검증
        args.forEachIndexed { index, arg ->
            val argValidation = validateNode(arg)
            if (!argValidation.success) {
                violations.add("${ErrorMessages.FUNCTION_ARG_INVALID} $index: ${argValidation.message}")
            }
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to FUNCTION_CALL_NODE_POLICY)
        )
    }

    /**
     * 조건문 노드 생성 정책을 검증합니다.
     *
     * @param condition 조건식
     * @param trueValue 참 값
     * @param falseValue 거짓 값
     * @return 정책 검증 결과
     */
    fun validateIfCreation(condition: ASTNode, trueValue: ASTNode, falseValue: ASTNode): PolicyResult {
        val violations = mutableListOf<String>()

        // 조건식 검증
        val conditionValidation = validateNode(condition)
        if (!conditionValidation.success) {
            violations.add("${ErrorMessages.CONDITION_INVALID}: ${conditionValidation.message}")
        }

        // 참 값 검증
        val trueValidation = validateNode(trueValue)
        if (!trueValidation.success) {
            violations.add("${ErrorMessages.TRUE_VALUE_INVALID}: ${trueValidation.message}")
        }

        // 거짓 값 검증
        val falseValidation = validateNode(falseValue)
        if (!falseValidation.success) {
            violations.add("${ErrorMessages.FALSE_VALUE_INVALID}: ${falseValidation.message}")
        }

        // 중첩 깊이 검증
        val nestingDepth = calculateIfNodeNestingDepth(condition) +
                calculateIfNodeNestingDepth(trueValue) +
                calculateIfNodeNestingDepth(falseValue)
        if (nestingDepth > MAX_NESTING_DEPTH) {
            violations.add("${ErrorMessages.NESTING_DEPTH_EXCEEDED}: $nestingDepth > $MAX_NESTING_DEPTH")
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to IF_NODE_POLICY)
        )
    }

    /**
     * 인수 목록 노드 생성 정책을 검증합니다.
     *
     * @param arguments 인수 목록
     * @return 정책 검증 결과
     */
    fun validateArgumentsCreation(arguments: List<ASTNode>): PolicyResult {
        val violations = mutableListOf<String>()

        if (arguments.size > MAX_ARGUMENTS_COUNT) {
            violations.add("${ErrorMessages.ARGUMENTS_COUNT_EXCEEDED}: ${arguments.size} > $MAX_ARGUMENTS_COUNT")
        }

        // 각 인수 검증
        arguments.forEachIndexed { index, arg ->
            val argValidation = validateNode(arg)
            if (!argValidation.success) {
                violations.add("${ErrorMessages.ARGUMENT_INVALID} $index: ${argValidation.message}")
            }
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to ARGUMENT_LIST_POLICY)
        )
    }

    /**
     * 노드 일반 검증을 수행합니다.
     *
     * @param node 검증할 노드
     * @return 정책 검증 결과
     */
    fun validateNode(node: ASTNode): PolicyResult {
        val violations = mutableListOf<String>()

        // 노드 크기 검증
        if (node.getSize() > MAX_NODE_SIZE) {
            violations.add("${ErrorMessages.NODE_SIZE_EXCEEDED}: ${node.getSize()} > $MAX_NODE_SIZE")
        }

        // 노드 깊이 검증
        if (node.getDepth() > MAX_NODE_DEPTH) {
            violations.add("${ErrorMessages.NODE_DEPTH_EXCEEDED}: ${node.getDepth()} > $MAX_NODE_DEPTH")
        }

        // 변수 개수 검증
        if (node.getVariables().size > MAX_VARIABLES_PER_NODE) {
            violations.add("${ErrorMessages.VARIABLES_PER_NODE_EXCEEDED}: ${node.getVariables().size} > $MAX_VARIABLES_PER_NODE")
        }

        return PolicyResult(
            success = violations.isEmpty(),
            message = violations.joinToString("; "),
            data = mapOf("policyName" to NODE_GENERAL_VERIFICATION_POLICY)
        )
    }

    // 중복 메서드들을 ASTValidationUtils로 대체
    private fun isValidVariableName(name: String): Boolean = ASTValidationUtils.isValidVariableName(name)
    private fun isValidFunctionName(name: String): Boolean = ASTValidationUtils.isValidFunctionName(name)
    private fun isReservedWord(name: String): Boolean = ASTValidationUtils.isReservedWord(name)
    private fun isSupportedBinaryOperator(operator: String): Boolean = ASTValidationUtils.isSupportedBinaryOperator(operator)
    private fun isSupportedUnaryOperator(operator: String): Boolean = ASTValidationUtils.isSupportedUnaryOperator(operator)
    private fun isZeroConstant(node: ASTNode): Boolean = ASTValidationUtils.isZeroConstant(node)

    /**
     * IfNode의 중첩 깊이를 계산합니다.
     * 다른 노드 타입의 경우 0을 반환합니다.
     */
    private fun calculateIfNodeNestingDepth(node: ASTNode): Int {
        return when (node) {
            is hs.kr.entrydsm.domain.ast.entities.IfNode -> 1 + maxOf(
                calculateIfNodeNestingDepth(node.condition),
                calculateIfNodeNestingDepth(node.trueValue),
                calculateIfNodeNestingDepth(node.falseValue)
            )
            else -> 0
        }
    }

    companion object {
        private const val MAX_NUMBER_VALUE = 1e15
        private const val MIN_NUMBER_VALUE = -1e15
        private const val MAX_VARIABLE_NAME_LENGTH = 50
        private const val MAX_FUNCTION_NAME_LENGTH = 50
        private const val MAX_FUNCTION_ARGS = 10
        private const val MAX_ARGUMENTS_COUNT = 100
        private const val MAX_NODE_SIZE = 1000
        private const val MAX_NODE_DEPTH = 50
        private const val MAX_VARIABLES_PER_NODE = 100
        private const val MAX_NESTING_DEPTH = 20

        private const val NUMBER_NODE_POLICY = "숫자 노드 생성 정책"
        private const val BOOLEAN_NODE_POLICY = "불리언 노드 생성 정책"
        private const val VARIABLE_NODE_POLICY = "변수 노드 생성 정책"
        private const val BINARY_OPERATION_NODE_POLICY = "이항 연산 노드 생성 정책"
        private const val UNARY_OPERATION_NODE_POLICY = "단항 연산 노드 생성 정책"
        private const val FUNCTION_CALL_NODE_POLICY = "함수 호출 노드 생성 정책"
        private const val IF_NODE_POLICY = "조건문 노드 생성 정책"
        private const val ARGUMENT_LIST_POLICY = "인수 목록 노드 생성 정책"
        private const val NODE_GENERAL_VERIFICATION_POLICY = "노드 일반 검증 정책"

        // 중복 상수들을 ASTValidationUtils로 대체
        // RESERVED_WORDS, BINARY_OPERATORS, UNARY_OPERATORS는 ASTValidationUtils에서 관리
    }
}