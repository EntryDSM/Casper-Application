package hs.kr.entrydsm.domain.ast.factories

import hs.kr.entrydsm.domain.ast.entities.*
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.specifications.ASTValiditySpec
import hs.kr.entrydsm.domain.ast.specifications.NodeStructureSpec
import hs.kr.entrydsm.domain.ast.policies.ASTValidationPolicy
import hs.kr.entrydsm.domain.ast.policies.NodeCreationPolicy
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import java.util.concurrent.atomic.AtomicLong

/**
 * AST 노드 객체들을 생성하는 팩토리입니다.
 *
 * 다양한 타입의 AST 노드를 생성하며, 도메인 규칙과 정책을 
 * 적용하여 일관된 객체 생성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Factory(context = "ast", complexity = Complexity.HIGH, cache = true)
class ASTNodeFactory {

    private val validitySpec = ASTValiditySpec()
    private val structureSpec = NodeStructureSpec()
    private val validationPolicy = ASTValidationPolicy()
    private val creationPolicy = NodeCreationPolicy()

    companion object {
        /**
         * 지원되는 수학 함수 목록입니다.
         */
        private val SUPPORTED_MATH_FUNCTIONS = setOf(
            "SIN", "COS", "TAN", "SQRT", "ABS", "LOG", "EXP"
        )

        private val createdNodeCount = AtomicLong(0)
        private val createdNumberCount = AtomicLong(0)
        private val createdBooleanCount = AtomicLong(0)
        private val createdVariableCount = AtomicLong(0)
        private val createdBinaryOpCount = AtomicLong(0)
        private val createdUnaryOpCount = AtomicLong(0)
        private val createdFunctionCallCount = AtomicLong(0)
        private val createdIfCount = AtomicLong(0)
        private val createdArgumentsCount = AtomicLong(0)

        private val instance = ASTNodeFactory()

        /**
         * 싱글톤 팩토리 인스턴스를 반환합니다.
         */
        @JvmStatic
        fun getInstance(): ASTNodeFactory = instance

        /**
         * 기본 설정으로 노드를 생성하는 편의 메서드입니다.
         */
        @JvmStatic
        fun quickCreateNumber(value: Double): NumberNode {
            return getInstance().createNumber(value)
        }

        @JvmStatic
        fun quickCreateBoolean(value: Boolean): BooleanNode {
            return getInstance().createBoolean(value)
        }

        @JvmStatic
        fun quickCreateVariable(name: String): VariableNode {
            return getInstance().createVariable(name)
        }
    }

    /**
     * 숫자 노드를 생성합니다.
     *
     * @param value 숫자 값
     * @return NumberNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    fun createNumber(value: Double): NumberNode {
        // 생성 전 정책 검증
        creationPolicy.validateNumberCreation(value)
        
        val node = NumberNode(value)
        
        // 생성 후 유효성 검증
        if (!validitySpec.isSatisfiedBy(node)) {
            throw ASTException.nodeValidationFailed(
                reason = validitySpec.getWhyNotSatisfied(node)
            )
        }
        
        createdNumberCount.incrementAndGet()
        
        return node
    }

    /**
     * 불리언 노드를 생성합니다.
     *
     * @param value 불리언 값
     * @return BooleanNode 인스턴스
     */
    fun createBoolean(value: Boolean): BooleanNode {
        // 생성 전 정책 검증
        creationPolicy.validateBooleanCreation(value)
        
        val node = BooleanNode(value)
        
        // 생성 후 유효성 검증
        // 숫자/불리언/변수/연산/함수 호출/조건문/인수 목록 노드 생성 후 유효성
        if (!validitySpec.isSatisfiedBy(node)) {
            throw ASTException.nodeValidationFailed(
                reason = validitySpec.getWhyNotSatisfied(node)
            )
        }
        
        createdBooleanCount.incrementAndGet()
        
        return node
    }

    /**
     * 변수 노드를 생성합니다.
     *
     * @param name 변수명
     * @return VariableNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 변수명인 경우
     */
    fun createVariable(name: String): VariableNode {
        // 생성 전 정책 검증
        creationPolicy.validateVariableCreation(name)
        
        val node = VariableNode(name)
        
        // 생성 후 유효성 검증
        if (!validitySpec.isSatisfiedBy(node)) {
            throw ASTException.nodeValidationFailed(
                reason = validitySpec.getWhyNotSatisfied(node)
            )
        }

        createdVariableCount.incrementAndGet()
        
        return node
    }

    /**
     * 이항 연산 노드를 생성합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 연산자
     * @param right 우측 피연산자
     * @return BinaryOpNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 연산자이거나 피연산자인 경우
     */
    fun createBinaryOp(left: ASTNode, operator: String, right: ASTNode): BinaryOpNode {
        // 생성 전 정책 검증
        creationPolicy.validateBinaryOpCreation(left, operator, right)
        
        val node = BinaryOpNode(left, operator, right)
        validateNodeAfterBuild(node)

        createdBinaryOpCount.incrementAndGet()
        
        return node
    }

    /**
     * 단항 연산 노드를 생성합니다.
     *
     * @param operator 연산자
     * @param operand 피연산자
     * @return UnaryOpNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 연산자이거나 피연산자인 경우
     */
    fun createUnaryOp(operator: String, operand: ASTNode): UnaryOpNode {
        // 생성 전 정책 검증
        creationPolicy.validateUnaryOpCreation(operator, operand)
        
        val node = UnaryOpNode(operator, operand)
        validateNodeAfterBuild(node)

        createdUnaryOpCount.incrementAndGet()
        
        return node
    }

    /**
     * 함수 호출 노드를 생성합니다.
     *
     * @param name 함수명
     * @param args 인수 목록
     * @return FunctionCallNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 함수명이거나 인수인 경우
     */
    fun createFunctionCall(name: String, args: List<ASTNode>): FunctionCallNode {
        // 생성 전 정책 검증
        creationPolicy.validateFunctionCallCreation(name, args)
        
        val node = FunctionCallNode(name, args)
        validateNodeAfterBuild(node)

        createdFunctionCallCount.incrementAndGet()
        
        return node
    }

    /**
     * 조건문 노드를 생성합니다.
     *
     * @param condition 조건식
     * @param trueValue 참 값
     * @param falseValue 거짓 값
     * @return IfNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 조건이거나 값인 경우
     */
    fun createIf(condition: ASTNode, trueValue: ASTNode, falseValue: ASTNode): IfNode {
        // 생성 전 정책 검증
        creationPolicy.validateIfCreation(condition, trueValue, falseValue)
        
        val node = IfNode(condition, trueValue, falseValue)
        validateNodeAfterBuild(node)
        
        createdIfCount.incrementAndGet()
        
        return node
    }

    /**
     * 인수 목록 노드를 생성합니다.
     *
     * @param arguments 인수 목록
     * @return ArgumentsNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 인수인 경우
     */
    fun createArguments(arguments: List<ASTNode>): ArgumentsNode {
        // 생성 전 정책 검증
        creationPolicy.validateArgumentsCreation(arguments)
        
        val node = ArgumentsNode(arguments)
        
        // 생성 후 유효성 검증
        if (!validitySpec.isSatisfiedBy(node)) {
            throw ASTException.nodeValidationFailed(
                reason = validitySpec.getWhyNotSatisfied(node)
            )
        }
        
        createdArgumentsCount.incrementAndGet()
        
        return node
    }

    /**
     * 정수 값으로 숫자 노드를 생성합니다.
     *
     * @param value 정수 값
     * @return NumberNode 인스턴스
     */
    fun createNumber(value: Int): NumberNode = createNumber(value.toDouble())

    /**
     * Long 값으로 숫자 노드를 생성합니다.
     *
     * @param value Long 값
     * @return NumberNode 인스턴스
     */
    fun createNumber(value: Long): NumberNode = createNumber(value.toDouble())

    /**
     * Float 값으로 숫자 노드를 생성합니다.
     *
     * @param value Float 값
     * @return NumberNode 인스턴스
     */
    fun createNumber(value: Float): NumberNode = createNumber(value.toDouble())

    /**
     * 문자열에서 숫자 노드를 생성합니다.
     *
     * @param value 숫자 문자열
     * @return NumberNode 인스턴스
     * @throws NumberFormatException 유효하지 않은 숫자 형식인 경우
     */
    fun createNumberFromString(value: String): NumberNode {
        val doubleValue = value.toDoubleOrNull()
            ?: throw ASTException.invalidNumberLiteral(value)
        return createNumber(doubleValue)
    }

    /**
     * 문자열에서 불리언 노드를 생성합니다.
     *
     * @param value 불리언 문자열
     * @return BooleanNode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 불리언 형식인 경우
     */
    fun createBooleanFromString(value: String): BooleanNode {
        val booleanValue = when (value.lowercase()) {
            "true", "1", "yes", "y", "on" -> true
            "false", "0", "no", "n", "off" -> false
            else -> throw ASTException.invalidBooleanValue(value)
        }
        return createBoolean(booleanValue)
    }

    /**
     * 산술 연산 노드를 생성합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 산술 연산자
     * @param right 우측 피연산자
     * @return BinaryOpNode 인스턴스
     */
    fun createArithmeticOp(left: ASTNode, operator: String, right: ASTNode): BinaryOpNode {
        if (operator !in setOf("+","-","*","/","^","%")) {
            throw ASTException.notArithmeticOperator(operator)
        }
        return createBinaryOp(left, operator, right)
    }

    /**
     * 비교 연산 노드를 생성합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 비교 연산자
     * @param right 우측 피연산자
     * @return BinaryOpNode 인스턴스
     */
    fun createComparisonOp(left: ASTNode, operator: String, right: ASTNode): BinaryOpNode {
        if (operator !in setOf("==","!=", "<","<=" ,">",">=")) {
            throw ASTException.notComparisonOperator(operator)
        }
        return createBinaryOp(left, operator, right)
    }

    /**
     * 논리 연산 노드를 생성합니다.
     *
     * @param left 좌측 피연산자
     * @param operator 논리 연산자
     * @param right 우측 피연산자
     * @return BinaryOpNode 인스턴스
     */
    fun createLogicalOp(left: ASTNode, operator: String, right: ASTNode): BinaryOpNode {
        if (operator !in setOf("&&","||")) {
            throw ASTException.notLogicalOperator(operator)
        }
        return createBinaryOp(left, operator, right)
    }

    /**
     * 단항 마이너스 노드를 생성합니다.
     *
     * @param operand 피연산자
     * @return UnaryOpNode 인스턴스
     */
    fun createUnaryMinus(operand: ASTNode): UnaryOpNode = createUnaryOp("-", operand)

    /**
     * 단항 플러스 노드를 생성합니다.
     *
     * @param operand 피연산자
     * @return UnaryOpNode 인스턴스
     */
    fun createUnaryPlus(operand: ASTNode): UnaryOpNode = createUnaryOp("+", operand)

    /**
     * 논리 부정 노드를 생성합니다.
     *
     * @param operand 피연산자
     * @return UnaryOpNode 인스턴스
     */
    fun createLogicalNot(operand: ASTNode): UnaryOpNode = createUnaryOp("!", operand)

    /**
     * 수학 함수 호출 노드를 생성합니다.
     *
     * @param name 수학 함수명
     * @param args 인수 목록
     * @return FunctionCallNode 인스턴스
     */
    fun createMathFunction(name: String, args: List<ASTNode>): FunctionCallNode {
        if (!SUPPORTED_MATH_FUNCTIONS.contains(name.uppercase())) {
            throw ASTException.unsupportedMathFunction(name)
        }
        return createFunctionCall(name.uppercase(), args)
    }

    /**
     * 삼항 연산자 노드를 생성합니다.
     *
     * @param condition 조건식
     * @param trueValue 참 값
     * @param falseValue 거짓 값
     * @return IfNode 인스턴스
     */
    fun createTernary(condition: ASTNode, trueValue: ASTNode, falseValue: ASTNode): IfNode {
        return createIf(condition, trueValue, falseValue)
    }

    /**
     * 최적화된 노드를 생성합니다.
     *
     * @param node 최적화할 노드
     * @return 최적화된 노드
     */
    fun createOptimized(node: ASTNode): ASTNode {
        return when (node) {
            is IfNode -> node.optimize()
            is UnaryOpNode -> node.simplify()
            else -> node
        }
    }

    /**
     * 팩토리 통계를 반환합니다.
     *
     * @return 팩토리 사용 통계
     */
    fun getFactoryStatistics(): Map<String, Any> {
        return mapOf(
            "totalNodesCreated" to createdNodeCount.get(),
            "numberNodesCreated" to createdNumberCount.get(),
            "booleanNodesCreated" to createdBooleanCount.get(),
            "variableNodesCreated" to createdVariableCount.get(),
            "binaryOpNodesCreated" to createdBinaryOpCount.get(),
            "unaryOpNodesCreated" to createdUnaryOpCount.get(),
            "functionCallNodesCreated" to createdFunctionCallCount.get(),
            "ifNodesCreated" to createdIfCount.get(),
            "argumentsNodesCreated" to createdArgumentsCount.get(),
            "factoryComplexity" to Complexity.HIGH.name,
            "cacheEnabled" to true
        )
    }


    init {
        createdNodeCount.incrementAndGet()
    }

    private fun validateNodeAfterBuild(node: ASTNode) {
        // 생성 후 유효성 검증
        if (!validitySpec.isSatisfiedBy(node)) {
            throw ASTException.nodeValidationFailed(
                reason = validitySpec.getWhyNotSatisfied(node)
            )
        }

        // 구조 검증
        if (!structureSpec.isSatisfiedBy(node)) {
            throw ASTException.nodeStructureFailed(
                reason = structureSpec.getWhyNotSatisfied(node)
            )
        }
    }
}