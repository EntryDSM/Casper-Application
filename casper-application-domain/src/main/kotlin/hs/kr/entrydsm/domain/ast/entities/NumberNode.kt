package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 숫자 리터럴을 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 모든 숫자 값(정수, 실수)을 표현하며,
 * Double 타입으로 값을 저장하여 정밀한 계산을 지원합니다.
 * 불변 객체로 설계되어 안전한 값 전달을 보장합니다.
 *
 * @property value 노드의 숫자 값
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class NumberNode(val value: Double) : ASTNode() {
    
    init {
        require(value.isFinite()) { "숫자 값은 유한해야 합니다: $value" }
    }

    override fun getVariables(): Set<String> = emptySet()

    override fun getChildren(): List<ASTNode> = emptyList()

    override fun isLiteral(): Boolean = true

    override fun getDepth(): Int = 1

    override fun getNodeCount(): Int = 1

    override fun copy(): NumberNode = this.copy()

    override fun toSimpleString(): String = when {
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> value.toString()
    }

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitNumber(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is NumberNode && this.value == other.value

    /**
     * 숫자 값이 정수인지 확인합니다.
     *
     * @return 정수이면 true, 아니면 false
     */
    fun isInteger(): Boolean = value == value.toLong().toDouble()

    /**
     * 숫자 값이 양수인지 확인합니다.
     *
     * @return 양수이면 true, 아니면 false
     */
    fun isPositive(): Boolean = value > 0.0

    /**
     * 숫자 값이 음수인지 확인합니다.
     *
     * @return 음수이면 true, 아니면 false
     */
    fun isNegative(): Boolean = value < 0.0

    /**
     * 숫자 값이 0인지 확인합니다.
     *
     * @return 0이면 true, 아니면 false
     */
    fun isZero(): Boolean = value == 0.0

    /**
     * 숫자 값을 정수로 변환합니다.
     *
     * @return 정수 값
     * @throws IllegalStateException 정수가 아닌 경우
     */
    fun toInt(): Int {
        check(isInteger()) { "정수가 아닌 값을 정수로 변환할 수 없습니다: $value" }
        return value.toInt()
    }

    /**
     * 숫자 값을 Long으로 변환합니다.
     *
     * @return Long 값
     * @throws IllegalStateException 정수가 아닌 경우
     */
    fun toLong(): Long {
        check(isInteger()) { "정수가 아닌 값을 Long으로 변환할 수 없습니다: $value" }
        return value.toLong()
    }

    /**
     * 숫자의 절댓값을 반환합니다.
     *
     * @return 절댓값을 가진 새로운 NumberNode
     */
    fun abs(): NumberNode = NumberNode(kotlin.math.abs(value))

    /**
     * 숫자의 부호를 반전한 값을 반환합니다.
     *
     * @return 부호가 반전된 새로운 NumberNode
     */
    fun negate(): NumberNode = NumberNode(-value)

    /**
     * 다른 NumberNode와 더합니다.
     *
     * @param other 더할 NumberNode
     * @return 합을 가진 새로운 NumberNode
     */
    operator fun plus(other: NumberNode): NumberNode = NumberNode(value + other.value)

    /**
     * 다른 NumberNode와 뺍니다.
     *
     * @param other 뺄 NumberNode
     * @return 차를 가진 새로운 NumberNode
     */
    operator fun minus(other: NumberNode): NumberNode = NumberNode(value - other.value)

    /**
     * 다른 NumberNode와 곱합니다.
     *
     * @param other 곱할 NumberNode
     * @return 곱을 가진 새로운 NumberNode
     */
    operator fun times(other: NumberNode): NumberNode = NumberNode(value * other.value)

    /**
     * 다른 NumberNode로 나눕니다.
     *
     * @param other 나눌 NumberNode
     * @return 몫을 가진 새로운 NumberNode
     * @throws IllegalArgumentException 0으로 나누는 경우
     */
    operator fun div(other: NumberNode): NumberNode {
        require(!other.isZero()) { "0으로 나눌 수 없습니다" }
        return NumberNode(value / other.value)
    }

    /**
     * 다른 NumberNode와 크기를 비교합니다.
     *
     * @param other 비교할 NumberNode
     * @return 비교 결과 (-1, 0, 1)
     */
    operator fun compareTo(other: NumberNode): Int = value.compareTo(other.value)

    override fun toString(): String = toSimpleString()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return "${spaces}NumberNode: $value"
    }

    companion object {
        /**
         * 0을 나타내는 NumberNode를 반환합니다.
         */
        val ZERO = NumberNode(0.0)

        /**
         * 1을 나타내는 NumberNode를 반환합니다.
         */
        val ONE = NumberNode(1.0)

        /**
         * -1을 나타내는 NumberNode를 반환합니다.
         */
        val MINUS_ONE = NumberNode(-1.0)

        /**
         * 정수 값으로 NumberNode를 생성합니다.
         *
         * @param value 정수 값
         * @return NumberNode 인스턴스
         */
        fun of(value: Int): NumberNode = NumberNode(value.toDouble())

        /**
         * Long 값으로 NumberNode를 생성합니다.
         *
         * @param value Long 값
         * @return NumberNode 인스턴스
         */
        fun of(value: Long): NumberNode = NumberNode(value.toDouble())

        /**
         * Double 값으로 NumberNode를 생성합니다.
         *
         * @param value Double 값
         * @return NumberNode 인스턴스
         */
        fun of(value: Double): NumberNode = NumberNode(value)

        /**
         * 문자열로부터 NumberNode를 생성합니다.
         *
         * @param value 숫자 문자열
         * @return NumberNode 인스턴스
         * @throws NumberFormatException 잘못된 숫자 형식인 경우
         */
        fun parse(value: String): NumberNode = NumberNode(value.toDouble())
    }
}