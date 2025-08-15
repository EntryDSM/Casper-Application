package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 단항 연산(예: 음수, 논리 부정)을 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 모든 단항 연산자를 표현하며, 연산자와
 * 피연산자로 구성됩니다. 음수 연산자(-), 논리 부정 연산자(!) 등을
 * 지원하며, 불변 객체로 설계되어 안전한 연산 트리를 구성합니다.
 *
 * @property operator 연산자 문자열 (예: "-", "!")
 * @property operand 피연산자 AST 노드
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class UnaryOpNode(
    val operator: String,
    val operand: ASTNode
) : ASTNode() {
    
    init {
        if (operator.isBlank()) {
            throw ASTException.operatorEmpty()
        }

        if (!isSupportedOperator(operator)) {
            throw ASTException.unsupportedUnaryOperator(operator)
        }
    }

    override fun getVariables(): Set<String> = operand.getVariables()

    override fun getChildren(): List<ASTNode> = listOf(operand)

    override fun isOperator(): Boolean = true

    override fun getDepth(): Int = operand.getDepth() + 1

    override fun getNodeCount(): Int = operand.getNodeCount() + 1

    override fun copy(): UnaryOpNode = UnaryOpNode(operator, operand.copy())

    override fun toSimpleString(): String = "$operator$operand"

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitUnaryOp(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is UnaryOpNode && 
        this.operator == other.operator &&
        this.operand.isStructurallyEqual(other.operand)

    /**
     * 연산자가 지원되는지 확인합니다.
     *
     * @param op 확인할 연산자
     * @return 지원되면 true, 아니면 false
     */
    private fun isSupportedOperator(op: String): Boolean = op in SUPPORTED_OPERATORS

    /**
     * 연산자가 산술 단항 연산자인지 확인합니다.
     *
     * @return 산술 단항 연산자이면 true, 아니면 false
     */
    fun isArithmeticOperator(): Boolean = operator in ARITHMETIC_OPERATORS

    /**
     * 연산자가 논리 단항 연산자인지 확인합니다.
     *
     * @return 논리 단항 연산자이면 true, 아니면 false
     */
    fun isLogicalOperator(): Boolean = operator in LOGICAL_OPERATORS

    /**
     * 연산자가 음수 연산자인지 확인합니다.
     *
     * @return 음수 연산자이면 true, 아니면 false
     */
    fun isNegation(): Boolean = operator == MINUS

    /**
     * 연산자가 논리 부정 연산자인지 확인합니다.
     *
     * @return 논리 부정 연산자이면 true, 아니면 false
     */
    fun isLogicalNot(): Boolean = operator == EXCLAMATION

    /**
     * 연산자가 양수 표시 연산자인지 확인합니다.
     *
     * @return 양수 표시 연산자이면 true, 아니면 false
     */
    fun isPositive(): Boolean = operator == PLUS

    /**
     * 연산자의 우선순위를 반환합니다.
     *
     * @return 우선순위 (높을수록 먼저 계산)
     */
    fun getPrecedence(): Int = OPERATOR_PRECEDENCE[operator] ?: 0

    /**
     * 연산자의 타입을 반환합니다.
     *
     * @return 연산자 타입
     */
    fun getOperatorType(): OperatorType = when {
        isArithmeticOperator() -> OperatorType.ARITHMETIC
        isLogicalOperator() -> OperatorType.LOGICAL
        else -> OperatorType.UNKNOWN
    }

    /**
     * 피연산자를 교체한 새로운 UnaryOpNode를 반환합니다.
     *
     * @param newOperand 새로운 피연산자
     * @return 새로운 UnaryOpNode
     */
    fun withOperand(newOperand: ASTNode): UnaryOpNode = UnaryOpNode(operator, newOperand)

    /**
     * 연산자를 교체한 새로운 UnaryOpNode를 반환합니다.
     *
     * @param newOperator 새로운 연산자
     * @return 새로운 UnaryOpNode
     */
    fun withOperator(newOperator: String): UnaryOpNode = UnaryOpNode(newOperator, operand)

    /**
     * 이중 음수(-(-x))를 단순화할 수 있는지 확인합니다.
     *
     * @return 단순화 가능하면 true, 아니면 false
     */
    fun canSimplifyDoubleNegation(): Boolean = 
        isNegation() && operand is UnaryOpNode && operand.isNegation()

    /**
     * 이중 부정(!(!x))를 단순화할 수 있는지 확인합니다.
     *
     * @return 단순화 가능하면 true, 아니면 false
     */
    fun canSimplifyDoubleNegationLogical(): Boolean = 
        isLogicalNot() && operand is UnaryOpNode && operand.isLogicalNot()

    /**
     * 이중 음수를 단순화합니다.
     *
     * @return 단순화된 AST 노드
     * @throws IllegalStateException 단순화할 수 없는 경우
     */
    fun simplifyDoubleNegation(): ASTNode {
        if (!canSimplifyDoubleNegation()) {
            throw ASTException.doubleNegationNotSimplifiable()
        }
        return (operand as UnaryOpNode).operand
    }

    /**
     * 이중 논리 부정을 단순화합니다.
     *
     * @return 단순화된 AST 노드
     * @throws IllegalStateException 단순화할 수 없는 경우
     */
    fun simplifyDoubleLogicalNegation(): ASTNode {
        if (!canSimplifyDoubleNegationLogical()) {
            throw ASTException.doubleLogicalNegationNotSimplifiable()
        }
        return (operand as UnaryOpNode).operand
    }

    /**
     * UnaryOpNode를 단순화합니다.
     *
     * @return 단순화된 AST 노드
     */
    fun simplify(): ASTNode {
        return when {
            canSimplifyDoubleNegation() -> simplifyDoubleNegation()
            canSimplifyDoubleNegationLogical() -> simplifyDoubleLogicalNegation()
            else -> this
        }
    }

    /**
     * 괄호를 포함한 문자열 표현을 생성합니다.
     *
     * @return 괄호가 포함된 문자열
     */
    fun toStringWithParentheses(): String {
        val operandStr = when {
            operand is BinaryOpNode -> "(${operand.toSimpleString()})"
            operand is UnaryOpNode && operand.getPrecedence() <= getPrecedence() -> "(${operand.toSimpleString()})"
            else -> operand.toSimpleString()
        }
        return "$operator$operandStr"
    }

    override fun toString(): String = toStringWithParentheses()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return buildString {
            appendLine("${spaces}UnaryOpNode: $operator")
            appendLine("${spaces}  operand:")
            append(operand.toTreeString(indent + 2))
        }
    }

    /**
     * 연산자 타입을 나타내는 열거형입니다.
     */
    enum class OperatorType {
        ARITHMETIC, LOGICAL, UNKNOWN
    }

    companion object {

        const val MINUS = "-"
        const val PLUS = "+"
        const val EXCLAMATION = "!"

        /**
         * 지원되는 모든 단항 연산자 목록입니다.
         */
        private val SUPPORTED_OPERATORS = setOf("-", "!", "+")

        /**
         * 산술 단항 연산자 목록입니다.
         */
        private val ARITHMETIC_OPERATORS = setOf("-", "+")

        /**
         * 논리 단항 연산자 목록입니다.
         */
        private val LOGICAL_OPERATORS = setOf("!")

        /**
         * 연산자 우선순위 맵입니다.
         */
        private val OPERATOR_PRECEDENCE = mapOf(
            "!" to 8,  // 최고 우선순위
            "-" to 8,  // 단항 마이너스
            "+" to 8   // 단항 플러스
        )

        /**
         * 지원되는 연산자 목록을 반환합니다.
         *
         * @return 지원되는 연산자 집합
         */
        fun getSupportedOperators(): Set<String> = SUPPORTED_OPERATORS.toSet()

        /**
         * 특정 타입의 연산자 목록을 반환합니다.
         *
         * @param type 연산자 타입
         * @return 해당 타입의 연산자 집합
         */
        fun getOperatorsByType(type: OperatorType): Set<String> = when (type) {
            OperatorType.ARITHMETIC -> ARITHMETIC_OPERATORS
            OperatorType.LOGICAL -> LOGICAL_OPERATORS
            OperatorType.UNKNOWN -> emptySet()
        }

        /**
         * 음수 노드를 생성합니다.
         *
         * @param operand 피연산자
         * @return 음수 UnaryOpNode
         */
        fun negate(operand: ASTNode): UnaryOpNode = UnaryOpNode("-", operand)

        /**
         * 논리 부정 노드를 생성합니다.
         *
         * @param operand 피연산자
         * @return 논리 부정 UnaryOpNode
         */
        fun logicalNot(operand: ASTNode): UnaryOpNode = UnaryOpNode("!", operand)

        /**
         * 양수 표시 노드를 생성합니다.
         *
         * @param operand 피연산자
         * @return 양수 표시 UnaryOpNode
         */
        fun positive(operand: ASTNode): UnaryOpNode = UnaryOpNode("+", operand)
    }
}