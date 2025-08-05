package hs.kr.entrydsm.domain.ast.policies

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.utils.ASTValidationUtils
import hs.kr.entrydsm.domain.ast.utils.FunctionValidationRules
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.PolicyResult
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * AST 노드 생성 정책을 구현하는 클래스입니다.
 *
 * 노드 생성 시 적용되는 비즈니스 규칙과 제약사항을 정의하며,
 * 생성 전 검증과 생성 후 최적화 규칙을 관리합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Policy(
    name = "AST 노드 생성 정책",
    description = "AST 노드 생성 시 적용되는 비즈니스 규칙과 제약사항을 정의",
    domain = "ast",
    scope = Scope.AGGREGATE
)
class NodeCreationPolicy {

    /**
     * 숫자 노드 생성 정책을 검증합니다.
     *
     * @param value 숫자 값
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateNumberCreation(value: Double) {
        require(value.isFinite()) { "숫자 값은 유한해야 합니다: $value" }
        require(!value.isNaN()) { "숫자 값은 NaN이 될 수 없습니다" }
        require(value >= MIN_NUMBER_VALUE) { "숫자 값이 최소값을 미만입니다: $value < $MIN_NUMBER_VALUE" }
        require(value <= MAX_NUMBER_VALUE) { "숫자 값이 최대값을 초과합니다: $value > $MAX_NUMBER_VALUE" }
    }

    /**
     * 불리언 노드 생성 정책을 검증합니다.
     *
     * @param value 불리언 값
     */
    fun validateBooleanCreation(value: Boolean) {
        // 불리언 값은 항상 유효
    }

    /**
     * 변수 노드 생성 정책을 검증합니다.
     *
     * @param name 변수명
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateVariableCreation(name: String) {
        require(name.isNotBlank()) { "변수명은 비어있을 수 없습니다" }
        require(name.length <= MAX_VARIABLE_NAME_LENGTH) { 
            "변수명이 최대 길이를 초과합니다: ${name.length} > $MAX_VARIABLE_NAME_LENGTH" 
        }
        require(isValidVariableName(name)) { "유효하지 않은 변수명입니다: $name" }
        require(!isReservedWord(name)) { "예약어는 변수명으로 사용할 수 없습니다: $name" }
        
        // 변수명 패턴 검증
        if (ENFORCE_NAMING_CONVENTION) {
            require(isValidNamingConvention(name)) { 
                "변수명이 네이밍 규칙을 위반합니다: $name" 
            }
        }
    }

    /**
     * 이항 연산 노드 생성 정책을 검증합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 연산자
     * @param right 우측 피연산자
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateBinaryOpCreation(left: ASTNode, operator: String, right: ASTNode) {
        require(operator.isNotBlank()) { "연산자는 비어있을 수 없습니다" }
        require(isSupportedBinaryOperator(operator)) { "지원되지 않는 이항 연산자입니다: $operator" }
        
        // 피연산자 검증
        validateNodeForOperation(left, "좌측 피연산자")
        validateNodeForOperation(right, "우측 피연산자")
        
        // 연산자별 특별 검증
        when (operator) {
            "/" -> {
                require(!isZeroConstant(right)) { "0으로 나눌 수 없습니다" }
            }
            "%" -> {
                require(!isZeroConstant(right)) { "0으로 나눈 나머지를 구할 수 없습니다" }
            }
            "^" -> {
                if (isZeroConstant(left) && isZeroConstant(right)) {
                    throw IllegalArgumentException("0^0은 정의되지 않습니다")
                }
            }
        }
        
        // 순환 참조 검증
        if (PREVENT_CIRCULAR_REFERENCES) {
            require(!hasCircularReference(left, right)) { 
                "순환 참조가 감지되었습니다" 
            }
        }
    }

    /**
     * 단항 연산 노드 생성 정책을 검증합니다.
     *
     * @param operator 연산자
     * @param operand 피연산자
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateUnaryOpCreation(operator: String, operand: ASTNode) {
        require(operator.isNotBlank()) { "연산자는 비어있을 수 없습니다" }
        require(isSupportedUnaryOperator(operator)) { "지원되지 않는 단항 연산자입니다: $operator" }
        
        // 피연산자 검증
        validateNodeForOperation(operand, "피연산자")
        
        // 연산자별 특별 검증
        when (operator) {
            "!" -> {
                if (STRICT_LOGICAL_OPERATIONS) {
                    require(isLogicalCompatible(operand)) { 
                        "논리 연산자는 논리적으로 호환되는 피연산자만 허용합니다" 
                    }
                }
            }
        }
    }

    /**
     * 함수 호출 노드 생성 정책을 검증합니다.
     *
     * @param name 함수명
     * @param args 인수 목록
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateFunctionCallCreation(name: String, args: List<ASTNode>) {
        require(name.isNotBlank()) { "함수명은 비어있을 수 없습니다" }
        require(name.length <= MAX_FUNCTION_NAME_LENGTH) { 
            "함수명이 최대 길이를 초과합니다: ${name.length} > $MAX_FUNCTION_NAME_LENGTH" 
        }
        require(isValidFunctionName(name)) { "유효하지 않은 함수명입니다: $name" }
        require(args.size <= MAX_FUNCTION_ARGS) { 
            "함수 인수 개수가 최대값을 초과합니다: ${args.size} > $MAX_FUNCTION_ARGS" 
        }
        
        // 각 인수 검증
        args.forEachIndexed { index, arg ->
            validateNodeForOperation(arg, "인수 $index")
        }
        
        // 함수별 특별 검증
        validateFunctionSpecificRules(name, args)
    }

    /**
     * 조건문 노드 생성 정책을 검증합니다.
     *
     * @param condition 조건식
     * @param trueValue 참 값
     * @param falseValue 거짓 값
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateIfCreation(condition: ASTNode, trueValue: ASTNode, falseValue: ASTNode) {
        // 각 노드 검증
        validateNodeForOperation(condition, "조건식")
        validateNodeForOperation(trueValue, "참 값")
        validateNodeForOperation(falseValue, "거짓 값")
        
        // 중첩 깊이 검증
        val totalDepth = condition.getDepth() + trueValue.getDepth() + falseValue.getDepth()
        require(totalDepth <= MAX_TOTAL_DEPTH) { 
            "조건문의 총 깊이가 최대값을 초과합니다: $totalDepth > $MAX_TOTAL_DEPTH" 
        }
        
        // 조건문 특별 검증
        if (OPTIMIZE_CONSTANT_CONDITIONS) {
            when (condition) {
                is hs.kr.entrydsm.domain.ast.entities.BooleanNode -> {
                    // 상수 조건에 대한 경고 (정책 위반은 아님)
                }
                is hs.kr.entrydsm.domain.ast.entities.NumberNode -> {
                    // 숫자 조건에 대한 경고
                }
                is hs.kr.entrydsm.domain.ast.entities.ArgumentsNode -> {
                    // ArgumentsNode 처리
                is hs.kr.entrydsm.domain.ast.entities.BinaryOpNode -> {
                    // BinaryOpNode 처리
                }
                is hs.kr.entrydsm.domain.ast.entities.FunctionCallNode -> {
                    // FunctionCallNode 처리
                }
                is hs.kr.entrydsm.domain.ast.entities.IfNode -> {
                    // IfNode 처리
                }
                is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode -> {
                    // UnaryOpNode 처리
                }
                is hs.kr.entrydsm.domain.ast.entities.VariableNode -> {
                    // VariableNode 처리
                }
            }
        }
    }

    /**
     * 인수 목록 노드 생성 정책을 검증합니다.
     *
     * @param arguments 인수 목록
     * @throws IllegalArgumentException 정책 위반 시
     */
    fun validateArgumentsCreation(arguments: List<ASTNode>) {
        require(arguments.size <= MAX_ARGUMENTS_COUNT) { 
            "인수 개수가 최대값을 초과합니다: ${arguments.size} > $MAX_ARGUMENTS_COUNT" 
        }
        
        // 각 인수 검증
        arguments.forEachIndexed { index, arg ->
            validateNodeForOperation(arg, "인수 $index")
        }
        
        // 인수 중복 검증
        if (PREVENT_DUPLICATE_ARGUMENTS) {
            val duplicates = findDuplicateArguments(arguments)
            require(duplicates.isEmpty()) { 
                "중복된 인수가 발견되었습니다: $duplicates" 
            }
        }
    }

    /**
     * 연산에 사용될 노드를 검증합니다.
     *
     * @param node 검증할 노드
     * @param context 컨텍스트 정보
     * @throws IllegalArgumentException 정책 위반 시
     */
    private fun validateNodeForOperation(node: ASTNode, context: String) {
        require(node.getSize() <= MAX_NODE_SIZE) { 
            "$context 의 크기가 최대값을 초과합니다: ${node.getSize()} > $MAX_NODE_SIZE" 
        }
        require(node.getDepth() <= MAX_NODE_DEPTH) { 
            "$context 의 깊이가 최대값을 초과합니다: ${node.getDepth()} > $MAX_NODE_DEPTH" 
        }
        require(node.getVariables().size <= MAX_VARIABLES_PER_NODE) { 
            "$context 의 변수 개수가 최대값을 초과합니다: ${node.getVariables().size} > $MAX_VARIABLES_PER_NODE" 
        }
    }

    // 중복 메서드들을 ASTValidationUtils로 대체
    private fun isValidVariableName(name: String): Boolean = ASTValidationUtils.isValidVariableName(name)
    private fun isValidFunctionName(name: String): Boolean = ASTValidationUtils.isValidFunctionName(name)
    private fun isReservedWord(name: String): Boolean = ASTValidationUtils.isReservedWord(name)

    /**
     * 네이밍 규칙을 준수하는지 확인합니다.
     */
    private fun isValidNamingConvention(name: String): Boolean {
        // 카멜 케이스 또는 스네이크 케이스 허용
        return name.matches(Regex("^[a-z_][a-zA-Z0-9_]*$"))
    }

    // 추가 중복 메서드들을 ASTValidationUtils로 대체
    private fun isSupportedBinaryOperator(operator: String): Boolean = ASTValidationUtils.isSupportedBinaryOperator(operator)
    private fun isSupportedUnaryOperator(operator: String): Boolean = ASTValidationUtils.isSupportedUnaryOperator(operator)
    private fun isZeroConstant(node: ASTNode): Boolean = ASTValidationUtils.isZeroConstant(node)

    /**
     * 논리 연산에 호환되는 노드인지 확인합니다.
     */
    private fun isLogicalCompatible(node: ASTNode): Boolean {
        return when (node) {
            is hs.kr.entrydsm.domain.ast.entities.BooleanNode -> true
            is hs.kr.entrydsm.domain.ast.entities.NumberNode -> true
            is hs.kr.entrydsm.domain.ast.entities.BinaryOpNode -> 
                node.isComparisonOperator() || node.isLogicalOperator()
            is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode -> 
                node.isLogicalOperator()
            else -> false
        }
    }

    /**
     * 순환 참조를 확인합니다.
     */
    private fun hasCircularReference(left: ASTNode, right: ASTNode): Boolean {
        // 간단한 순환 참조 검증 (실제로는 더 복잡한 로직 필요)
        return left == right
    }

    /**
     * 중복 인수를 찾습니다.
     */
    private fun findDuplicateArguments(arguments: List<ASTNode>): List<String> {
        val seen = mutableSetOf<String>()
        val duplicates = mutableListOf<String>()
        
        arguments.forEach { arg ->
            val argString = arg.toString()
            if (argString in seen) {
                duplicates.add(argString)
            } else {
                seen.add(argString)
            }
        }
        
        return duplicates
    }

    /**
     * 함수별 특별 규칙을 검증합니다.
     */
    private fun validateFunctionSpecificRules(name: String, args: List<ASTNode>) {
        require(FunctionValidationRules.isValidFunctionCall(name, args)) {
            val expectedCount = FunctionValidationRules.getExpectedArgumentCount(name)
            val description = FunctionValidationRules.getArgumentCountDescription(name)
            "$name 함수는 $description 의 인수가 필요합니다 (현재: ${args.size}개)"
        }
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
        private const val MAX_VARIABLES_PER_NODE = 100
        private const val MAX_TOTAL_DEPTH = 100

        // 정책 플래그
        private const val ENFORCE_NAMING_CONVENTION = true
        private const val STRICT_LOGICAL_OPERATIONS = true
        private const val PREVENT_CIRCULAR_REFERENCES = true
        private const val OPTIMIZE_CONSTANT_CONDITIONS = true
        private const val PREVENT_DUPLICATE_ARGUMENTS = false

        // 중복 상수들을 ASTValidationUtils로 대체
        // RESERVED_WORDS, BINARY_OPERATORS, UNARY_OPERATORS는 ASTValidationUtils에서 관리
    }
}