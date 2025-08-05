package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity
import kotlin.math.pow

/**
 * 이항 연산(예: 덧셈, 뺄셈, 비교)을 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 모든 이항 연산자를 표현하며, 좌측 피연산자,
 * 연산자, 우측 피연산자로 구성됩니다. 산술 연산, 비교 연산, 논리 연산 등을
 * 모두 지원하며, 불변 객체로 설계되어 안전한 연산 트리를 구성합니다.
 *
 * @property left 좌측 피연산자 AST 노드
 * @property operator 연산자 문자열 (예: "+", "-", "==")
 * @property right 우측 피연산자 AST 노드
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class BinaryOpNode(
    val left: ASTNode,
    val operator: String,
    val right: ASTNode
) : ASTNode() {
    
    init {
        require(operator.isNotBlank()) { "연산자는 비어있을 수 없습니다" }
        require(isSupportedOperator(operator)) { "지원하지 않는 연산자입니다: $operator" }
    }

    override fun getVariables(): Set<String> = left.getVariables() + right.getVariables()

    override fun getChildren(): List<ASTNode> = listOf(left, right)

    override fun isOperator(): Boolean = true

    override fun getDepth(): Int = maxOf(left.getDepth(), right.getDepth()) + 1

    override fun getNodeCount(): Int = left.getNodeCount() + right.getNodeCount() + 1

    override fun copy(): BinaryOpNode = BinaryOpNode(left.copy(), operator, right.copy())

    override fun toSimpleString(): String = "($left $operator $right)"

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitBinaryOp(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is BinaryOpNode && 
        this.operator == other.operator &&
        this.left.isStructurallyEqual(other.left) &&
        this.right.isStructurallyEqual(other.right)

    /**
     * 연산자가 지원되는지 확인합니다.
     *
     * @param op 확인할 연산자
     * @return 지원되면 true, 아니면 false
     */
    private fun isSupportedOperator(op: String): Boolean = op in SUPPORTED_OPERATORS

    /**
     * 연산자가 산술 연산자인지 확인합니다.
     *
     * @return 산술 연산자이면 true, 아니면 false
     */
    fun isArithmeticOperator(): Boolean = operator in ARITHMETIC_OPERATORS

    /**
     * 연산자가 비교 연산자인지 확인합니다.
     *
     * @return 비교 연산자이면 true, 아니면 false
     */
    fun isComparisonOperator(): Boolean = operator in COMPARISON_OPERATORS

    /**
     * 연산자가 논리 연산자인지 확인합니다.
     *
     * @return 논리 연산자이면 true, 아니면 false
     */
    fun isLogicalOperator(): Boolean = operator in LOGICAL_OPERATORS

    /**
     * 연산자가 교환법칙이 성립하는지 확인합니다.
     *
     * @return 교환법칙이 성립하면 true, 아니면 false
     */
    fun isCommutative(): Boolean = operator in COMMUTATIVE_OPERATORS

    /**
     * 연산자가 결합법칙이 성립하는지 확인합니다.
     *
     * @return 결합법칙이 성립하면 true, 아니면 false
     */
    fun isAssociative(): Boolean = operator in ASSOCIATIVE_OPERATORS

    /**
     * 연산자의 우선순위를 반환합니다.
     *
     * @return 우선순위 (높을수록 먼저 계산)
     */
    fun getPrecedence(): Int = OPERATOR_PRECEDENCE[operator] ?: 0

    /**
     * 연산자의 결합성을 반환합니다.
     *
     * @return 결합성 (LEFT 또는 RIGHT)
     */
    fun getAssociativity(): Associativity = OPERATOR_ASSOCIATIVITY[operator] ?: Associativity.LEFT

    /**
     * 연산자가 좌결합인지 확인합니다.
     *
     * @return 좌결합이면 true, 아니면 false
     */
    fun isLeftAssociative(): Boolean = getAssociativity() == Associativity.LEFT

    /**
     * 연산자가 우결합인지 확인합니다.
     *
     * @return 우결합이면 true, 아니면 false
     */
    fun isRightAssociative(): Boolean = getAssociativity() == Associativity.RIGHT

    /**
     * 연산자의 타입을 반환합니다.
     *
     * @return 연산자 타입
     */
    fun getOperatorType(): OperatorType = when {
        isArithmeticOperator() -> OperatorType.ARITHMETIC
        isComparisonOperator() -> OperatorType.COMPARISON
        isLogicalOperator() -> OperatorType.LOGICAL
        else -> OperatorType.UNKNOWN
    }

    /**
     * 좌측 피연산자를 교체한 새로운 BinaryOpNode를 반환합니다.
     *
     * @param newLeft 새로운 좌측 피연산자
     * @return 새로운 BinaryOpNode
     */
    fun withLeft(newLeft: ASTNode): BinaryOpNode = BinaryOpNode(newLeft, operator, right)

    /**
     * 우측 피연산자를 교체한 새로운 BinaryOpNode를 반환합니다.
     *
     * @param newRight 새로운 우측 피연산자
     * @return 새로운 BinaryOpNode
     */
    fun withRight(newRight: ASTNode): BinaryOpNode = BinaryOpNode(left, operator, newRight)

    /**
     * 연산자를 교체한 새로운 BinaryOpNode를 반환합니다.
     *
     * @param newOperator 새로운 연산자
     * @return 새로운 BinaryOpNode
     */
    fun withOperator(newOperator: String): BinaryOpNode = BinaryOpNode(left, newOperator, right)

    /**
     * 교환법칙을 적용한 새로운 BinaryOpNode를 반환합니다.
     *
     * @return 피연산자가 교환된 새로운 BinaryOpNode
     * @throws IllegalStateException 교환법칙이 성립하지 않는 연산자인 경우
     */
    fun commute(): BinaryOpNode {
        check(isCommutative()) { "교환법칙이 성립하지 않는 연산자입니다: $operator" }
        return BinaryOpNode(right, operator, left)
    }


    /**
     * BinaryOpNode를 단순화합니다.
     *
     * 다음과 같은 최적화를 수행합니다:
     * - 산술 연산: 0과의 덧셈/뺄셈, 1과의 곱셈/나눗셈, 0과의 곱셈
     * - 논리 연산: true/false와의 논리 연산
     * - 상수 계산: 두 상수의 연산 결과를 미리 계산
     * - 동일한 피연산자의 연산 처리
     *
     * @return 단순화된 AST 노드
     */
    fun simplify(): ASTNode {
        // 양쪽 피연산자가 NumberNode인 경우 상수 계산
        if (left is NumberNode && right is NumberNode) {
            return when (operator) {
                "+" -> NumberNode(left.value + right.value)
                "-" -> NumberNode(left.value - right.value)
                "*" -> NumberNode(left.value * right.value)
                "/" -> {
                    if (right.isZero()) return this // 0으로 나누기는 단순화하지 않음
                    NumberNode(left.value / right.value)
                }
                "%" -> {
                    if (right.isZero()) return this
                    NumberNode(left.value % right.value)
                }
                "^" -> NumberNode(left.value.pow(right.value))
                "==" -> NumberNode(if (left.value == right.value) 1.0 else 0.0)
                "!=" -> NumberNode(if (left.value != right.value) 1.0 else 0.0)
                "<" -> NumberNode(if (left.value < right.value) 1.0 else 0.0)
                "<=" -> NumberNode(if (left.value <= right.value) 1.0 else 0.0)
                ">" -> NumberNode(if (left.value > right.value) 1.0 else 0.0)
                ">=" -> NumberNode(if (left.value >= right.value) 1.0 else 0.0)
                "&&" -> NumberNode(if (!left.isZero() && !right.isZero()) 1.0 else 0.0)
                "||" -> NumberNode(if (!left.isZero() || !right.isZero()) 1.0 else 0.0)
                else -> this
            }
        }

        // 산술 연산 최적화
        if (left is NumberNode || right is NumberNode) {
            when (operator) {
                "+" -> {
                    if (left is NumberNode && left.isZero()) return right
                    if (right is NumberNode && right.isZero()) return left
                }
                "-" -> {
                    if (right is NumberNode && right.isZero()) return left
                    if (left is NumberNode && left.isZero()) return UnaryOpNode("-", right)
                }
                "*" -> {
                    if ((left is NumberNode && left.isZero()) || (right is NumberNode && right.isZero())) {
                        return NumberNode.ZERO
                    }
                    if (left is NumberNode && left.value == 1.0) return right
                    if (right is NumberNode && right.value == 1.0) return left
                    if (left is NumberNode && left.value == -1.0) return UnaryOpNode("-", right)
                    if (right is NumberNode && right.value == -1.0) return UnaryOpNode("-", left)
                }
                "/" -> {
                    if (left is NumberNode && left.isZero()) return NumberNode.ZERO
                    if (right is NumberNode && right.value == 1.0) return left
                    if (right is NumberNode && right.value == -1.0) return UnaryOpNode("-", left)
                }
                "^" -> {
                    if (right is NumberNode && right.isZero()) return NumberNode.ONE
                    if (right is NumberNode && right.value == 1.0) return left
                    if (left is NumberNode && left.value == 1.0) return NumberNode.ONE
                    if (left is NumberNode && left.isZero()) return NumberNode.ZERO
                }
            }
        }

        // 논리 연산 최적화
        when (operator) {
            "&&" -> {
                if (left is NumberNode && left.isZero()) return NumberNode.ZERO
                if (right is NumberNode && right.isZero()) return NumberNode.ZERO
                if (left is NumberNode && !left.isZero()) return right
                if (right is NumberNode && !right.isZero()) return left
            }
            "||" -> {
                if (left is NumberNode && !left.isZero()) return NumberNode.ONE
                if (right is NumberNode && !right.isZero()) return NumberNode.ONE
                if (left is NumberNode && left.isZero()) return right
                if (right is NumberNode && right.isZero()) return left
            }
        }

        // 동일한 피연산자 처리
        if (left.isStructurallyEqual(right)) {
            when (operator) {
                "-" -> return NumberNode.ZERO
                "/" -> return NumberNode.ONE
                "%" -> return NumberNode.ZERO
                "==" -> return NumberNode.ONE
                "!=" -> return NumberNode.ZERO
                "<=" -> return NumberNode.ONE
                ">=" -> return NumberNode.ONE
                "<" -> return NumberNode.ZERO
                ">" -> return NumberNode.ZERO
            }
        }

        return this
    }

    /**
     * 괄호 없이 연산자 우선순위에 따라 문자열을 생성합니다.
     *
     * @return 우선순위를 고려한 문자열
     */
    fun toStringWithPrecedence(): String {
        val leftStr = if (left is BinaryOpNode && left.getPrecedence() < getPrecedence()) {
            "(${left.toStringWithPrecedence()})"
        } else {
            left.toSimpleString()
        }
        
        val rightStr = if (right is BinaryOpNode && 
            (right.getPrecedence() < getPrecedence() || 
             (right.getPrecedence() == getPrecedence() && getAssociativity() == Associativity.LEFT))) {
            "(${right.toStringWithPrecedence()})"
        } else {
            right.toSimpleString()
        }
        
        return "$leftStr $operator $rightStr"
    }

    override fun toString(): String = toStringWithPrecedence()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return buildString {
            appendLine("${spaces}BinaryOpNode: $operator")
            appendLine("${spaces}  left:")
            appendLine(left.toTreeString(indent + 2))
            appendLine("${spaces}  right:")
            append(right.toTreeString(indent + 2))
        }
    }

    /**
     * 연산자 결합성을 나타내는 열거형입니다.
     */
    enum class Associativity {
        LEFT, RIGHT
    }

    /**
     * 연산자 타입을 나타내는 열거형입니다.
     */
    enum class OperatorType {
        ARITHMETIC, COMPARISON, LOGICAL, UNKNOWN
    }

    companion object {
        /**
         * 지원되는 모든 연산자 목록입니다.
         */
        private val SUPPORTED_OPERATORS = setOf(
            "+", "-", "*", "/", "^", "%",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||"
        )

        /**
         * 산술 연산자 목록입니다.
         */
        private val ARITHMETIC_OPERATORS = setOf("+", "-", "*", "/", "^", "%")

        /**
         * 비교 연산자 목록입니다.
         */
        private val COMPARISON_OPERATORS = setOf("==", "!=", "<", "<=", ">", ">=")

        /**
         * 논리 연산자 목록입니다.
         */
        private val LOGICAL_OPERATORS = setOf("&&", "||")

        /**
         * 교환법칙이 성립하는 연산자 목록입니다.
         */
        private val COMMUTATIVE_OPERATORS = setOf("+", "*", "==", "!=", "&&", "||")

        /**
         * 결합법칙이 성립하는 연산자 목록입니다.
         */
        private val ASSOCIATIVE_OPERATORS = setOf("+", "*", "&&", "||")

        /**
         * 연산자 우선순위 맵입니다.
         */
        private val OPERATOR_PRECEDENCE = mapOf(
            "||" to 1,
            "&&" to 2,
            "==" to 3, "!=" to 3,
            "<" to 4, "<=" to 4, ">" to 4, ">=" to 4,
            "+" to 5, "-" to 5,
            "*" to 6, "/" to 6, "%" to 6,
            "^" to 7
        )

        /**
         * 연산자 결합성 맵입니다.
         */
        private val OPERATOR_ASSOCIATIVITY = mapOf(
            "||" to Associativity.LEFT,
            "&&" to Associativity.LEFT,
            "==" to Associativity.LEFT, "!=" to Associativity.LEFT,
            "<" to Associativity.LEFT, "<=" to Associativity.LEFT, ">" to Associativity.LEFT, ">=" to Associativity.LEFT,
            "+" to Associativity.LEFT, "-" to Associativity.LEFT,
            "*" to Associativity.LEFT, "/" to Associativity.LEFT, "%" to Associativity.LEFT,
            "^" to Associativity.RIGHT
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
            OperatorType.COMPARISON -> COMPARISON_OPERATORS
            OperatorType.LOGICAL -> LOGICAL_OPERATORS
            OperatorType.UNKNOWN -> emptySet()
        }
    }
}