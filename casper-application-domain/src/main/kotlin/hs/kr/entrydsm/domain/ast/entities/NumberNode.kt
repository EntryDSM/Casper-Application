package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.exceptions.ASTException
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
        if (!value.isFinite()) {
            throw ASTException.numberNotFinite(value)
        }
    }

    override fun getVariables(): Set<String> = emptySet()

    override fun getChildren(): List<ASTNode> = emptyList()

    override fun isLiteral(): Boolean = true

    override fun getDepth(): Int = 1

    override fun getNodeCount(): Int = 1

    override fun copy(): NumberNode = NumberNode(value)

    override fun toSimpleString(): String = when {
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> value.toString()
    }

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitNumber(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is NumberNode && areNumbersEqual(this.value, other.value)
        
    /**
     * 두 double 값이 의미적으로 같은지 확인합니다.
     * 부동소수점 정밀도 문제를 해결하기 위해 엡실론 기반 비교를 사용합니다.
     * 
     * @param a 첫 번째 값
     * @param b 두 번째 값
     * @return 값이 같으면 true
     */
    private fun areNumbersEqual(a: Double, b: Double): Boolean {
        // NaN과 무한대는 정확히 같아야 함
        if (a.isNaN() && b.isNaN()) return true
        if (a.isInfinite() && b.isInfinite()) return a == b
        if (a.isNaN() || b.isNaN()) return false
        if (a.isInfinite() || b.isInfinite()) return false
        
        // 0에 가까운 값들은 절대 차이로 비교
        if (kotlin.math.abs(a) < EPSILON && kotlin.math.abs(b) < EPSILON) {
            return kotlin.math.abs(a - b) < EPSILON
        }
        
        // 일반적인 경우는 상대 오차로 비교
        val diff = kotlin.math.abs(a - b)
        val maxAbs = kotlin.math.max(kotlin.math.abs(a), kotlin.math.abs(b))
        return diff <= EPSILON * maxAbs
    }

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
        if (!isInteger()) {
            throw ASTException.notIntegerForInt(value)
        }
        return value.toInt()
    }

    /**
     * 숫자 값을 Long으로 변환합니다.
     *
     * @return Long 값
     * @throws IllegalStateException 정수가 아닌 경우
     */
    fun toLong(): Long {
        if (!isInteger()) {
            throw ASTException.notIntegerForLong(value)
        }
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
     */
    operator fun div(other: NumberNode): NumberNode {
        if (other.isZero()) {
            throw ASTException.divisionByZero()
        }
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
        return "${spaces}$NUMBER_NODE: $value"
    }

    companion object {

        const val NUMBER_NODE = "NumberNode:"
        /**
         * 부동소수점 비교를 위한 엡실론 값
         */
        private const val EPSILON = 1e-10
        
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