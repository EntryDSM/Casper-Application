package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 불린 리터럴을 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 불린 값(true, false)을 표현하며,
 * 논리 연산과 조건문에서 사용됩니다. 불변 객체로 설계되어
 * 안전한 값 전달을 보장합니다.
 *
 * @property value 노드의 불린 값 (true 또는 false)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class BooleanNode(val value: Boolean) : ASTNode() {

    override fun getVariables(): Set<String> = emptySet()

    override fun getChildren(): List<ASTNode> = emptyList()

    override fun isLiteral(): Boolean = true

    override fun getDepth(): Int = 1

    override fun getNodeCount(): Int = 1

    override fun copy(): BooleanNode = this

    override fun toSimpleString(): String = value.toString()

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitBoolean(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is BooleanNode && this.value == other.value

    /**
     * 불린 값이 참인지 확인합니다.
     *
     * @return 참이면 true, 아니면 false
     */
    fun isTrue(): Boolean = value

    /**
     * 불린 값이 거짓인지 확인합니다.
     *
     * @return 거짓이면 true, 아니면 false
     */
    fun isFalse(): Boolean = !value

    /**
     * 불린 값을 반전시킵니다.
     *
     * @return 반전된 값을 가진 새로운 BooleanNode
     */
    fun not(): BooleanNode = BooleanNode(!value)

    /**
     * 다른 BooleanNode와 논리곱(AND) 연산을 수행합니다.
     *
     * @param other AND 연산할 BooleanNode
     * @return AND 연산 결과를 가진 새로운 BooleanNode
     */
    infix fun and(other: BooleanNode): BooleanNode = BooleanNode(value && other.value)

    /**
     * 다른 BooleanNode와 논리합(OR) 연산을 수행합니다.
     *
     * @param other OR 연산할 BooleanNode
     * @return OR 연산 결과를 가진 새로운 BooleanNode
     */
    infix fun or(other: BooleanNode): BooleanNode = BooleanNode(value || other.value)

    /**
     * 다른 BooleanNode와 배타적 논리합(XOR) 연산을 수행합니다.
     *
     * @param other XOR 연산할 BooleanNode
     * @return XOR 연산 결과를 가진 새로운 BooleanNode
     */
    infix fun xor(other: BooleanNode): BooleanNode = BooleanNode(value xor other.value)

    /**
     * 다른 BooleanNode와 동치성을 확인합니다.
     *
     * @param other 비교할 BooleanNode
     * @return 같으면 true, 다르면 false
     */
    fun isEqualTo(other: BooleanNode): Boolean = value == other.value

    /**
     * 다른 BooleanNode와 비동치성을 확인합니다.
     *
     * @param other 비교할 BooleanNode
     * @return 다르면 true, 같으면 false
     */
    fun isNotEqualTo(other: BooleanNode): Boolean = value != other.value

    /**
     * 불린 값을 정수로 변환합니다.
     *
     * @return true면 1, false면 0
     */
    fun toInt(): Int = if (value) 1 else 0

    /**
     * 불린 값을 Double로 변환합니다.
     *
     * @return true면 1.0, false면 0.0
     */
    fun toDouble(): Double = if (value) 1.0 else 0.0

    /**
     * 불린 값을 숫자 노드로 변환합니다.
     *
     * @return true면 NumberNode(1.0), false면 NumberNode(0.0)
     */
    fun toNumberNode(): NumberNode = NumberNode(toDouble())

    override fun toString(): String = value.toString()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return "${spaces}BooleanNode: $value"
    }

    companion object {
        /**
         * TRUE를 나타내는 BooleanNode입니다.
         */
        val TRUE = BooleanNode(true)

        /**
         * FALSE를 나타내는 BooleanNode입니다.
         */
        val FALSE = BooleanNode(false)

        /**
         * Boolean 값으로 BooleanNode를 생성합니다.
         *
         * @param value Boolean 값
         * @return BooleanNode 인스턴스
         */
        fun of(value: Boolean): BooleanNode = if (value) TRUE else FALSE

        /**
         * 문자열로부터 BooleanNode를 생성합니다.
         *
         * @param value 불린 문자열 ("true", "false", 대소문자 무관)
         * @return BooleanNode 인스턴스
         * @throws IllegalArgumentException 잘못된 불린 문자열인 경우
         */
        fun parse(value: String): BooleanNode = when (value.lowercase()) {
            "true" -> TRUE
            "false" -> FALSE
            else -> throw ASTException.invalidBooleanValue(value)
        }

        /**
         * 숫자로부터 BooleanNode를 생성합니다.
         *
         * @param value 숫자 값 (0이면 false, 그 외는 true)
         * @return BooleanNode 인스턴스
         */
        fun fromNumber(value: Double): BooleanNode = of(value != 0.0)

        /**
         * NumberNode로부터 BooleanNode를 생성합니다.
         *
         * @param numberNode NumberNode (0이면 false, 그 외는 true)
         * @return BooleanNode 인스턴스
         */
        fun fromNumberNode(numberNode: NumberNode): BooleanNode = of(!numberNode.isZero())
    }
}