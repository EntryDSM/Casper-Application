package hs.kr.entrydsm.domain.ast.policies

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.utils.ASTValidationUtils
import hs.kr.entrydsm.domain.ast.utils.FunctionValidationRules
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.policies.validation.*
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.PolicyResult
import hs.kr.entrydsm.global.annotation.policy.type.Scope
import java.util.concurrent.atomic.AtomicLong

/**
 * AST 노드 생성 정책을 구현하는 클래스입니다.
 *
 * 노드 생성 시 적용되는 비즈니스 규칙과 제약사항을 정의하며,
 * 생성 전 검증과 생성 후 최적화 규칙을 관리합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hex아키텍처</a>
 *
 * @author kange
 * @since 2025.07.16
 */
@Policy(
    name = "AST 노드 생성 정책",
    description = "AST 노드 생성 시 적용되는 비즈니스 규칙과 제약사항을 정의",
    domain = "ast",
    scope = Scope.AGGREGATE
)
class NodeCreationPolicy {
    
    // 연산자별 검증 전략들
    private val validationStrategies: Map<String, BinaryOperatorValidationStrategy> = mapOf(
        "/" to DivisionValidationStrategy(),
        "%" to ModuloValidationStrategy(),
        "^" to PowerValidationStrategy(),
        "*" to MultiplicationValidationStrategy(),
        "+" to DefaultValidationStrategy("+"),
        "-" to DefaultValidationStrategy("-")
    )

    /**
     * 숫자 노드 생성 정책을 검증합니다.
     *
     * @param value 숫자 값
     */
    fun validateNumberCreation(value: Double) {
        if (!value.isFinite()) {
            throw ASTException.numberNotFinite(value)
        }
        if (value.isNaN()) {
            throw ASTException.numberIsNaN(value)
        }
        if (value < MIN_NUMBER_VALUE) {
            throw ASTException.numberTooSmall(value, MIN_NUMBER_VALUE)
        }
        if (value > MAX_NUMBER_VALUE) {
            throw ASTException.numberTooLarge(value, MAX_NUMBER_VALUE)
        }
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
     */
    fun validateVariableCreation(name: String) {
        if (name.isBlank()) {
            throw ASTException.variableNameEmpty()
        }
        if (name.length > MAX_VARIABLE_NAME_LENGTH) {
            throw ASTException.variableNameTooLong(name.length, MAX_VARIABLE_NAME_LENGTH)
        }
        if (!isValidVariableName(name)) {
            throw ASTException.invalidVariableName(name)
        }
        if (isReservedWord(name)) {
            throw ASTException.variableReservedWord(name)
        }

        // 변수명 패턴 검증 (옵션)
        if (ENFORCE_NAMING_CONVENTION && !isValidNamingConvention(name)) {
            throw ASTException.nodeValidationFailed(
                reason = "네이밍 규칙 위반: $name"
            )
        }
    }

    /**
     * 이항 연산 노드 생성 정책을 검증합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 연산자
     * @param right 우측 피연산자
     */
    fun validateBinaryOpCreation(left: ASTNode, operator: String, right: ASTNode) {
        if (operator.isBlank()) {
            throw ASTException.operatorEmpty()
        }
        if (!isSupportedBinaryOperator(operator)) {
            throw ASTException.unsupportedBinaryOperator(operator)
        }

        // 피연산자 검증
        validateNodeForOperation(left,  "좌측 피연산자")
        validateNodeForOperation(right, "우측 피연산자")
        
        // 연산자별 특별 검증 - Strategy 패턴 적용
        val strategy = validationStrategies[operator]
        if (strategy != null) {
            strategy.validate(left, right, zeroConstantOptimizationCount)
        }
        
        // 추가 고급 최적화 로직 (논리 연산자, 비교 연산자 등)
        when (operator) {
            "&&" -> {
                if (isTrueConstant(left)  || isFalseConstant(left) ||
                    isTrueConstant(right) || isFalseConstant(right) ||
                    left.isStructurallyEqual(right)
                ) {
                    constantConditionOptimizationCount.incrementAndGet()
                }
            }
            "||" -> {
                if (isTrueConstant(left)  || isFalseConstant(left) ||
                    isTrueConstant(right) || isFalseConstant(right) ||
                    left.isStructurallyEqual(right)
                ) {
                    constantConditionOptimizationCount.incrementAndGet()
                }
            }
            "==", "!=" -> {
                if (left.isStructurallyEqual(right)) {
                    constantConditionOptimizationCount.incrementAndGet()
                }
            }
            "<", "<=", ">", ">=" -> {
                if (left.isStructurallyEqual(right)) {
                    constantConditionOptimizationCount.incrementAndGet()
                }
            }
        }

        // 순환 참조 검증(옵션)
        if (PREVENT_CIRCULAR_REFERENCES && hasCircularReference(left, right)) {
            circularReferenceDetectionCount.incrementAndGet()
            throw ASTException.nodeValidationFailed(
                reason = "순환 참조가 감지되었습니다"
            )
        }
    }

    /**
     * 단항 연산 노드 생성 정책을 검증합니다.
     *
     * @param operator 연산자
     * @param operand 피연산자
     */
    fun validateUnaryOpCreation(operator: String, operand: ASTNode) {
        if (operator.isBlank()) {
            throw ASTException.operatorEmpty()
        }
        if (!isSupportedUnaryOperator(operator)) {
            throw ASTException.unsupportedUnaryOperator(operator)
        }

        // 피연산자 검증
        validateNodeForOperation(operand, "피연산자")

        // 연산자별 특별 검증 및 최적화 힌트
        when (operator) {
            "!" -> {
                if (STRICT_LOGICAL_OPERATIONS && !isLogicalCompatible(operand)) {
                    throw ASTException.logicalIncompatibleOperand()
                }
                if (isTrueConstant(operand) || isFalseConstant(operand) ||
                    (operand is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode && operand.isLogicalNot())
                ) {
                    constantConditionOptimizationCount.incrementAndGet()
                }
            }
            "-" -> {
                if (isZeroConstant(operand)) {
                    zeroConstantOptimizationCount.incrementAndGet() // -0 = 0
                } else if (operand is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode && operand.isNegation()) {
                    zeroConstantOptimizationCount.incrementAndGet() // -(-x) = x
                } else if (operand is hs.kr.entrydsm.domain.ast.entities.NumberNode && operand.value < 0) {
                    zeroConstantOptimizationCount.incrementAndGet() // -(음수) = 양수
                }
            }
            "+" -> {
                // +x = x
                zeroConstantOptimizationCount.incrementAndGet()
            }
        }
    }

    /**
     * 함수 호출 노드 생성 정책을 검증합니다.
     *
     * @param name 함수명
     * @param args 인수 목록
     */
    fun validateFunctionCallCreation(name: String, args: List<ASTNode>) {
        if (name.isBlank()) {
            throw ASTException.functionNameEmpty()
        }
        if (name.length > MAX_FUNCTION_NAME_LENGTH) {
            throw ASTException.functionNameTooLong(name.length, MAX_FUNCTION_NAME_LENGTH)
        }
        if (!isValidFunctionName(name)) {
            throw ASTException.invalidFunctionName(name)
        }
        if (args.size > MAX_FUNCTION_ARGS) {
            throw ASTException.functionArgumentsExceeded(args.size, MAX_FUNCTION_ARGS)
        }

        // 각 인수 검증
        args.forEachIndexed { index, arg -> validateNodeForOperation(arg, "인수 $index") }

        // 함수별 규칙
        validateFunctionSpecificRules(name, args)
    }

    /**
     * 조건문 노드 생성 정책을 검증합니다.
     *
     * @param condition 조건식
     * @param trueValue 참 값
     * @param falseValue 거짓 값
     */
    fun validateIfCreation(condition: ASTNode, trueValue: ASTNode, falseValue: ASTNode) {
        // 각 노드 검증
        validateNodeForOperation(condition,  "조건식")
        validateNodeForOperation(trueValue,  "참 값")
        validateNodeForOperation(falseValue, "거짓 값")

        // 중첩 깊이 검증
        val totalDepth = condition.getDepth() + trueValue.getDepth() + falseValue.getDepth()
        if (totalDepth > MAX_TOTAL_DEPTH) {
            throw ASTException.ifTotalDepthExceeded(totalDepth, MAX_TOTAL_DEPTH)
        }

        // 조건문 특별 검증(상수 조건 최적화 감지)
        if (OPTIMIZE_CONSTANT_CONDITIONS && condition.isLiteral()) {
            when (condition) {
                is hs.kr.entrydsm.domain.ast.entities.BooleanNode -> {
                    // 항상 참/거짓
                    constantConditionOptimizationCount.incrementAndGet()
                }
                is hs.kr.entrydsm.domain.ast.entities.NumberNode -> {
                    // 0/비0
                    constantConditionOptimizationCount.incrementAndGet()
                }
                else -> { /* no-op */ }
            }
        }
    }

    /**
     * 인수 목록 노드 생성 정책을 검증합니다.
     *
     * @param arguments 인수 목록
     */
    fun validateArgumentsCreation(arguments: List<ASTNode>) {
        if (arguments.size > MAX_ARGUMENTS_COUNT) {
            throw ASTException.argumentsExceeded(arguments.size, MAX_ARGUMENTS_COUNT)
        }

        // 각 인수 검증
        arguments.forEachIndexed { index, arg ->
            validateNodeForOperation(arg, "인수 $index")
        }

        // 인수 중복 검증(옵션)
        if (PREVENT_DUPLICATE_ARGUMENTS) {
            val duplicates = findDuplicateArguments(arguments)
            if (duplicates.isNotEmpty()) {
                throw ASTException.argumentsDuplicated(duplicates)
            }
        }
    }

    /**
     * 연산에 사용될 노드를 검증합니다.
     *
     * @param node 검증할 노드
     * @param context 컨텍스트 정보
     */
    private fun validateNodeForOperation(node: ASTNode, context: String) {
        val size = node.getSize()
        val depth = node.getDepth()
        val vars  = node.getVariables().size

        if (size  > MAX_NODE_SIZE) {
            throw ASTException.nodeSizeExceeded(size,  MAX_NODE_SIZE,  context)
        }
        if (depth > MAX_NODE_DEPTH) {
            throw ASTException.nodeDepthExceeded(depth, MAX_NODE_DEPTH, context)
        }
        if (vars  > MAX_VARIABLES_PER_NODE) {
            throw ASTException.nodeVariablesExceeded(vars, MAX_VARIABLES_PER_NODE, context)
        }
    }

    // === ASTValidationUtils 위임 ===
    private fun isValidVariableName(name: String): Boolean = ASTValidationUtils.isValidVariableName(name)
    private fun isValidFunctionName(name: String): Boolean = ASTValidationUtils.isValidFunctionName(name)
    private fun isReservedWord(name: String): Boolean = ASTValidationUtils.isReservedWord(name)
    private fun isSupportedBinaryOperator(operator: String): Boolean = ASTValidationUtils.isSupportedBinaryOperator(operator)
    private fun isSupportedUnaryOperator(operator: String): Boolean = ASTValidationUtils.isSupportedUnaryOperator(operator)
    private fun isZeroConstant(node: ASTNode): Boolean = ASTValidationUtils.isZeroConstant(node)

    /**
     * 네이밍 규칙을 준수하는지 확인합니다.
     */
    private fun isValidNamingConvention(name: String): Boolean {
        // 카멜 케이스 또는 스네이크 케이스 허용
        return name.matches(Regex("^[a-z_][a-zA-Z0-9_]*$"))
    }

    /**
     * 노드가 1 상수인지 확인합니다.
     */
    private fun isOneConstant(node: ASTNode): Boolean {
        return node is hs.kr.entrydsm.domain.ast.entities.NumberNode && node.value == 1.0
    }

    /**
     * 노드가 true 상수인지 확인합니다.
     */
    private fun isTrueConstant(node: ASTNode): Boolean {
        return node is hs.kr.entrydsm.domain.ast.entities.BooleanNode && node.value
    }

    /**
     * 노드가 false 상수인지 확인합니다.
     */
    private fun isFalseConstant(node: ASTNode): Boolean {
        return node is hs.kr.entrydsm.domain.ast.entities.BooleanNode && !node.value
    }

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
        // 직접적인 동일성 검사
        if (left === right) {
            return true
        }
        
        // 좌측 노드가 우측 노드를 참조하는지 확인
        if (containsNode(left, right)) {
            return true
        }
        
        // 우측 노드가 좌측 노드를 참조하는지 확인
        if (containsNode(right, left)) {
            return true
        }
        
        return false
    }

    /**
     * 주어진 노드가 다른 노드를 포함하는지 DFS로 확인합니다.
     */
    private fun containsNode(
        container: ASTNode,
        target: ASTNode,
        visited: MutableSet<ASTNode> = mutableSetOf()
    ): Boolean {
        // 무한 루프 방지
        if (container in visited) {
            return false
        }
        
        // 직접 일치
        if (container === target) {
            return true
        }
        
        // 방문 표시
        visited.add(container)
        
        // 자식 노드들을 재귀적으로 검사
        val hasTarget = container.getChildren().any { child ->
            containsNode(child, target, visited)
        }
        
        // 방문 표시 해제 (백트래킹)
        visited.remove(container)
        return hasTarget
    }

    /**
     * 노드 트리에서 순환 참조를 감지합니다.
     */
    private fun detectCircularReferenceInTree(root: ASTNode): Boolean {
        val visiting = mutableSetOf<ASTNode>()  // 현재 방문 중인 노드들
        val visited = mutableSetOf<ASTNode>()   // 완전히 처리된 노드들
        
        fun dfs(node: ASTNode): Boolean {
            // 현재 방문 중인 노드를 다시 방문하면 순환 참조
            if (node in visiting) {
                return true
            }
            
            // 이미 처리된 노드는 건너뛰기
            if (node in visited) {
                return false
            }
            
            // 방문 시작
            visiting.add(node)
            for (child in node.getChildren()) {
                if (dfs(child)) {
                    return true
                }
            }

            // 방문 완료
            visiting.remove(node)
            visited.add(node)
            return false
        }
        return dfs(root)
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
        if (!FunctionValidationRules.isValidFunctionCall(name, args)) {
            val description = FunctionValidationRules.getArgumentCountDescription(name)
            throw ASTException.functionArgumentCountMismatch(
                name = name,
                expectedDesc = description,
                actual = args.size
            )
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

        // 최적화 통계 카운터
        private val constantConditionOptimizationCount = AtomicLong(0)
        private val zeroConstantOptimizationCount = AtomicLong(0)
        private val circularReferenceDetectionCount = AtomicLong(0)

        /**
         * 정책 통계를 반환합니다.
         */
        fun getPolicyStatistics(): Map<String, Any> {
            return mapOf(
                "constantConditionOptimizations" to constantConditionOptimizationCount.get(),
                "zeroConstantOptimizations" to zeroConstantOptimizationCount.get(),
                "circularReferenceDetections" to circularReferenceDetectionCount.get(),
                "optimizationFlags" to mapOf(
                    "enforceNamingConvention" to ENFORCE_NAMING_CONVENTION,
                    "strictLogicalOperations" to STRICT_LOGICAL_OPERATIONS,
                    "preventCircularReferences" to PREVENT_CIRCULAR_REFERENCES,
                    "optimizeConstantConditions" to OPTIMIZE_CONSTANT_CONDITIONS,
                    "preventDuplicateArguments" to PREVENT_DUPLICATE_ARGUMENTS
                )
            )
        }

        /**
         * 통계 카운터를 초기화합니다.
         */
        fun resetStatistics() {
            constantConditionOptimizationCount.set(0)
            zeroConstantOptimizationCount.set(0)
            circularReferenceDetectionCount.set(0)
        }
    }
}
