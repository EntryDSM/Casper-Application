package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 조건문 (IF)을 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 삼항 조건 연산자를 표현하며, 조건식, 참 값, 거짓 값으로
 * 구성됩니다. IF(condition, trueValue, falseValue) 형태로 사용되며,
 * 조건에 따라 다른 값을 반환하는 조건부 표현식을 구현합니다.
 *
 * @property condition 조건식 AST 노드
 * @property trueValue 조건이 참일 때 평가될 AST 노드
 * @property falseValue 조건이 거짓일 때 평가될 AST 노드
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class IfNode(
    val condition: ASTNode,
    val trueValue: ASTNode,
    val falseValue: ASTNode
) : ASTNode() {

    override fun getVariables(): Set<String> = 
        condition.getVariables() + trueValue.getVariables() + falseValue.getVariables()

    override fun getChildren(): List<ASTNode> = listOf(condition, trueValue, falseValue)

    override fun isConditional(): Boolean = true

    override fun getDepth(): Int = maxOf(condition.getDepth(), trueValue.getDepth(), falseValue.getDepth()) + 1

    override fun getNodeCount(): Int = condition.getNodeCount() + trueValue.getNodeCount() + falseValue.getNodeCount() + 1

    override fun copy(): IfNode = IfNode(condition.copy(), trueValue.copy(), falseValue.copy())

    override fun toSimpleString(): String = "$IF(${condition.toSimpleString()}, ${trueValue.toSimpleString()}, ${falseValue.toSimpleString()})"

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitIf(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is IfNode &&
        this.condition.isStructurallyEqual(other.condition) &&
        this.trueValue.isStructurallyEqual(other.trueValue) &&
        this.falseValue.isStructurallyEqual(other.falseValue)

    /**
     * 조건식이 상수 값인지 확인합니다.
     *
     * @return 조건식이 상수이면 true, 아니면 false
     */
    fun hasConstantCondition(): Boolean = condition.isLiteral()

    /**
     * 조건식이 불린 리터럴인지 확인합니다.
     *
     * @return 불린 리터럴이면 true, 아니면 false
     */
    fun hasBooleanCondition(): Boolean = condition is BooleanNode

    /**
     * 조건식이 숫자 리터럴인지 확인합니다.
     *
     * @return 숫자 리터럴이면 true, 아니면 false
     */
    fun hasNumberCondition(): Boolean = condition is NumberNode

    /**
     * 참 값과 거짓 값이 같은지 확인합니다.
     *
     * @return 같으면 true, 다르면 false
     */
    fun hasSameValues(): Boolean = trueValue.isStructurallyEqual(falseValue)

    /**
     * 참 값이 상수인지 확인합니다.
     *
     * @return 참 값이 상수이면 true, 아니면 false
     */
    fun hasConstantTrueValue(): Boolean = trueValue.isLiteral()

    /**
     * 거짓 값이 상수인지 확인합니다.
     *
     * @return 거짓 값이 상수이면 true, 아니면 false
     */
    fun hasConstantFalseValue(): Boolean = falseValue.isLiteral()

    /**
     * 모든 값이 상수인지 확인합니다.
     *
     * @return 모든 값이 상수이면 true, 아니면 false
     */
    fun hasAllConstantValues(): Boolean = hasConstantCondition() && hasConstantTrueValue() && hasConstantFalseValue()

    /**
     * 중첩된 IF 문인지 확인합니다.
     *
     * @return 중첩된 IF 문이면 true, 아니면 false
     */
    fun isNestedIf(): Boolean = trueValue is IfNode || falseValue is IfNode

    /**
     * 참 값이 IF 문인지 확인합니다.
     *
     * @return 참 값이 IF 문이면 true, 아니면 false
     */
    fun hasTrueValueAsIf(): Boolean = trueValue is IfNode

    /**
     * 거짓 값이 IF 문인지 확인합니다.
     *
     * @return 거짓 값이 IF 문이면 true, 아니면 false
     */
    fun hasFalseValueAsIf(): Boolean = falseValue is IfNode

    /**
     * 조건식을 교체한 새로운 IfNode를 반환합니다.
     *
     * @param newCondition 새로운 조건식
     * @return 새로운 IfNode
     */
    fun withCondition(newCondition: ASTNode): IfNode = IfNode(newCondition, trueValue, falseValue)

    /**
     * 참 값을 교체한 새로운 IfNode를 반환합니다.
     *
     * @param newTrueValue 새로운 참 값
     * @return 새로운 IfNode
     */
    fun withTrueValue(newTrueValue: ASTNode): IfNode = IfNode(condition, newTrueValue, falseValue)

    /**
     * 거짓 값을 교체한 새로운 IfNode를 반환합니다.
     *
     * @param newFalseValue 새로운 거짓 값
     * @return 새로운 IfNode
     */
    fun withFalseValue(newFalseValue: ASTNode): IfNode = IfNode(condition, trueValue, newFalseValue)

    /**
     * 모든 구성 요소를 교체한 새로운 IfNode를 반환합니다.
     *
     * @param newCondition 새로운 조건식
     * @param newTrueValue 새로운 참 값
     * @param newFalseValue 새로운 거짓 값
     * @return 새로운 IfNode
     */
    fun withAll(newCondition: ASTNode, newTrueValue: ASTNode, newFalseValue: ASTNode): IfNode =
        IfNode(newCondition, newTrueValue, newFalseValue)

    /**
     * 단순화할 수 있는지 확인합니다.
     *
     * @return 단순화 가능하면 true, 아니면 false
     */
    fun canSimplify(): Boolean = when {
        hasSameValues() -> true
        hasBooleanCondition() -> true
        hasNumberCondition() && (condition as NumberNode).let { it.isZero() || !it.isZero() } -> true
        else -> false
    }

    /**
     * IF 노드를 단순화합니다.
     *
     * @return 단순화된 AST 노드
     * @throws IllegalStateException 단순화할 수 없는 경우
     */
    fun simplify(): ASTNode {
        if (!canSimplify()) {
            throw ASTException.ifNotSimplifiable()
        }
        return when {
            hasSameValues() -> trueValue
            hasBooleanCondition() -> {
                val boolCondition = condition as BooleanNode
                if (boolCondition.value) trueValue else falseValue
            }
            hasNumberCondition() -> {
                val numCondition = condition as NumberNode
                if (!numCondition.isZero()) trueValue else falseValue
            }
            else -> throw ASTException.simplificationUnexpectedCase()
        }
    }

    /**
     * IF 노드를 최적화합니다.
     *
     * @return 최적화된 AST 노드
     */
    fun optimize(): ASTNode {
        return when {
            canSimplify() -> simplify()
            else -> this
        }
    }

    /**
     * 조건을 반전시킨 새로운 IfNode를 반환합니다.
     *
     * @return 조건이 반전되고 참/거짓 값이 바뀐 새로운 IfNode
     */
    fun negate(): IfNode {
        val negatedCondition = when (condition) {
            is BooleanNode -> condition.not()
            is UnaryOpNode -> if (condition.isLogicalNot()) condition.operand else UnaryOpNode.logicalNot(condition)
            else -> UnaryOpNode.logicalNot(condition)
        }
        return IfNode(negatedCondition, falseValue, trueValue)
    }

    /**
     * 중첩 깊이를 계산합니다.
     *
     * @return 중첩 IF 문의 최대 깊이
     */
    override fun getNestingDepth(): Int {
        val trueDepth = if (trueValue is IfNode) trueValue.getNestingDepth() + 1 else 1
        val falseDepth = if (falseValue is IfNode) falseValue.getNestingDepth() + 1 else 1
        return maxOf(trueDepth, falseDepth)
    }

    /**
     * 삼항 연산자 형태의 문자열을 반환합니다.
     *
     * @return "condition ? trueValue : falseValue" 형태의 문자열
     */
    fun toTernaryString(): String = 
        "${condition.toSimpleString()} ? ${trueValue.toSimpleString()} : ${falseValue.toSimpleString()}"

    override fun toString(): String = toSimpleString()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return buildString {
            appendLine("${spaces}$IF_NODE")
            appendLine("${spaces}  $CONDITION")
            appendLine(condition.toTreeString(indent + 2))
            appendLine("${spaces}  $TRUE_VALUE")
            appendLine(trueValue.toTreeString(indent + 2))
            appendLine("${spaces}  $FALSE_VALUE")
            append(falseValue.toTreeString(indent + 2))
        }
    }

    companion object {

        const val IF = "IF"
        const val IF_NODE = "IfNode:"
        const val CONDITION = "condition:"
        const val TRUE_VALUE = "trueValue:"
        const val FALSE_VALUE = "falseValue:"
        const val LOGICAL_AND = "&&"

        /**
         * 불린 조건으로 IF 노드를 생성합니다.
         *
         * @param condition 불린 조건
         * @param trueValue 참 값
         * @param falseValue 거짓 값
         * @return IfNode 인스턴스
         */
        fun withBooleanCondition(condition: Boolean, trueValue: ASTNode, falseValue: ASTNode): IfNode =
            IfNode(BooleanNode.of(condition), trueValue, falseValue)

        /**
         * 숫자 조건으로 IF 노드를 생성합니다.
         *
         * @param condition 숫자 조건 (0이면 거짓, 그 외는 참)
         * @param trueValue 참 값
         * @param falseValue 거짓 값
         * @return IfNode 인스턴스
         */
        fun withNumberCondition(condition: Double, trueValue: ASTNode, falseValue: ASTNode): IfNode =
            IfNode(NumberNode.of(condition), trueValue, falseValue)

        /**
         * 단순 불린 분기를 생성합니다.
         *
         * @param condition 조건식
         * @param trueValue 참일 때 값
         * @param falseValue 거짓일 때 값
         * @return IfNode 인스턴스
         */
        fun createBranch(condition: ASTNode, trueValue: ASTNode, falseValue: ASTNode): IfNode =
            IfNode(condition, trueValue, falseValue)

        /**
         * 중첩 IF 문을 평면화합니다.
         *
         * @param ifNode 평면화할 IF 노드
         * @return 평면화된 IF 조건들의 리스트
         */
        fun flatten(ifNode: IfNode): List<Pair<ASTNode, ASTNode>> {
            val result = mutableListOf<Pair<ASTNode, ASTNode>>()
            
            fun collect(node: IfNode, currentConditions: List<ASTNode>) {
                val trueConditions = currentConditions + node.condition
                val falseConditions = currentConditions + UnaryOpNode.logicalNot(node.condition)
                
                when (node.trueValue) {
                    is IfNode -> collect(node.trueValue, trueConditions)
                    else -> result.add(createCompoundCondition(trueConditions) to node.trueValue)
                }
                
                when (node.falseValue) {
                    is IfNode -> collect(node.falseValue, falseConditions)
                    else -> result.add(createCompoundCondition(falseConditions) to node.falseValue)
                }
            }
            
            collect(ifNode, emptyList())
            return result
        }

        /**
         * 조건 리스트를 하나의 복합 조건으로 결합합니다.
         *
         * @param conditions 결합할 조건들
         * @return 결합된 조건
         */
        private fun createCompoundCondition(conditions: List<ASTNode>): ASTNode {
            return when {
                conditions.isEmpty() -> BooleanNode.TRUE
                conditions.size == 1 -> conditions.first()
                else -> conditions.reduce { acc, condition ->
                    BinaryOpNode(acc, LOGICAL_AND, condition)
                }
            }
        }
    }
}