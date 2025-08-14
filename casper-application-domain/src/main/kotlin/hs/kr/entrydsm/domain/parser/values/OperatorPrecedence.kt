package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.exceptions.ParserException

/**
 * 연산자의 우선순위와 결합성을 나타내는 값 객체입니다.
 *
 * 파서의 Shift/Reduce 충돌 해결에 사용되며, 수학 표현식의
 * 정확한 파싱을 위해 필수적인 정보를 제공합니다.
 *
 * @property precedence 우선순위 (높을수록 먼저 계산)
 * @property associativity 결합성
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
data class OperatorPrecedence(
    val precedence: Int,
    val associativity: Associativity.AssociativityType
) {
    
    init {
        if (precedence < 0) {
            throw ParserException.precedenceNegative(precedence)
        }
    }

    /**
     * 현재 연산자가 다른 연산자보다 높은 우선순위를 가지는지 확인합니다.
     *
     * @param other 비교할 다른 연산자 우선순위
     * @return 현재 연산자의 우선순위가 더 높으면 true
     */
    fun hasHigherPrecedenceThan(other: OperatorPrecedence): Boolean = 
        precedence > other.precedence

    /**
     * 현재 연산자가 다른 연산자와 같은 우선순위를 가지는지 확인합니다.
     *
     * @param other 비교할 다른 연산자 우선순위
     * @return 우선순위가 같으면 true
     */
    fun hasSamePrecedenceAs(other: OperatorPrecedence): Boolean = 
        precedence == other.precedence

    /**
     * 좌결합 연산자인지 확인합니다.
     *
     * @return 좌결합이면 true
     */
    fun isLeftAssociative(): Boolean = associativity.isLeft()

    /**
     * 우결합 연산자인지 확인합니다.
     *
     * @return 우결합이면 true
     */
    fun isRightAssociative(): Boolean = associativity.isRight()

    /**
     * 비결합 연산자인지 확인합니다.
     *
     * @return 비결합이면 true
     */
    fun isNonAssociative(): Boolean = associativity.isNone()

    /**
     * 체인결합 연산자인지 확인합니다.
     *
     * @return 체인결합이면 true
     */
    fun isChainAssociative(): Boolean = associativity.isChain()

    companion object {
        /** 가장 낮은 우선순위 */
        val LOWEST = OperatorPrecedence(0, Associativity.AssociativityType.LEFT)
        
        /** 가장 높은 우선순위 */
        val HIGHEST = OperatorPrecedence(Int.MAX_VALUE, Associativity.AssociativityType.LEFT)

        /** 논리 OR 연산자 우선순위 */
        val LOGICAL_OR = OperatorPrecedence(1, Associativity.AssociativityType.LEFT)
        
        /** 논리 AND 연산자 우선순위 */
        val LOGICAL_AND = OperatorPrecedence(2, Associativity.AssociativityType.LEFT)
        
        /** 동등 비교 연산자 우선순위 */
        val EQUALITY = OperatorPrecedence(3, Associativity.AssociativityType.LEFT)
        
        /** 관계 비교 연산자 우선순위 */
        val RELATIONAL = OperatorPrecedence(4, Associativity.AssociativityType.LEFT)
        
        /** 덧셈/뺄셈 연산자 우선순위 */
        val ADDITIVE = OperatorPrecedence(5, Associativity.AssociativityType.LEFT)
        
        /** 곱셈/나눗셈/나머지 연산자 우선순위 */
        val MULTIPLICATIVE = OperatorPrecedence(6, Associativity.AssociativityType.LEFT)
        
        /** 단항 연산자 우선순위 */
        val UNARY = OperatorPrecedence(7, Associativity.AssociativityType.RIGHT)
        
        /** 거듭제곱 연산자 우선순위 */
        val POWER = OperatorPrecedence(8, Associativity.AssociativityType.RIGHT)

        /**
         * 연산자별 우선순위 테이블
         */
        private val precedenceTable = mapOf(
            // 논리 연산자
            TokenType.OR to LOGICAL_OR,
            TokenType.AND to LOGICAL_AND,
            TokenType.NOT to UNARY,
            
            // 비교 연산자
            TokenType.EQUAL to EQUALITY,
            TokenType.NOT_EQUAL to EQUALITY,
            TokenType.LESS to RELATIONAL,
            TokenType.LESS_EQUAL to RELATIONAL,
            TokenType.GREATER to RELATIONAL,
            TokenType.GREATER_EQUAL to RELATIONAL,
            
            // 산술 연산자
            TokenType.PLUS to ADDITIVE,
            TokenType.MINUS to ADDITIVE,
            TokenType.MULTIPLY to MULTIPLICATIVE,
            TokenType.DIVIDE to MULTIPLICATIVE,
            TokenType.MODULO to MULTIPLICATIVE,
            TokenType.POWER to POWER
        )

        /**
         * 지정된 토큰의 연산자 우선순위를 반환합니다.
         *
         * @param token 조회할 토큰 타입
         * @return 연산자 우선순위 또는 null (연산자가 아닌 경우)
         */
        fun getPrecedence(token: TokenType): OperatorPrecedence? = 
            precedenceTable[token]

        /**
         * 지정된 토큰이 연산자인지 확인합니다.
         *
         * @param token 확인할 토큰 타입
         * @return 연산자이면 true
         */
        fun isOperator(token: TokenType): Boolean = 
            token in precedenceTable

        /**
         * 모든 연산자 토큰을 반환합니다.
         *
         * @return 연산자 토큰 집합
         */
        fun getAllOperators(): Set<TokenType> = 
            precedenceTable.keys

        /**
         * 우선순위별로 그룹화된 연산자를 반환합니다.
         *
         * @return 우선순위 -> 연산자 목록 맵
         */
        fun getOperatorsByPrecedence(): Map<Int, List<TokenType>> = 
            precedenceTable.entries
                .groupBy { it.value.precedence }
                .mapValues { (_, entries) -> entries.map { it.key } }

        /**
         * 사용자 정의 연산자 우선순위를 생성합니다.
         *
         * @param precedence 우선순위
         * @param associativity 결합성
         * @return 연산자 우선순위 객체
         */
        fun custom(precedence: Int, associativity: Associativity.AssociativityType): OperatorPrecedence =
            OperatorPrecedence(precedence, associativity)
    }

    override fun toString(): String = "Precedence($precedence, $associativity)"
}