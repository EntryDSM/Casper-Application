package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.lexer.entities.TokenType

/**
 * 연산자의 결합성을 나타내는 값 객체입니다.
 *
 * 파싱 과정에서 동일한 우선순위를 가진 연산자들이 연속으로 나타날 때
 * 어떤 방향으로 결합할지를 결정하는 규칙을 정의합니다.
 * DDD Value Object 패턴을 적용하여 불변성과 도메인 의미를 보장합니다.
 *
 * @property type 결합성 타입
 * @property operator 연산자 토큰 타입
 * @property precedence 우선순위 (높을수록 우선)
 * @property description 결합성 설명
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
data class Associativity(
    val type: AssociativityType,
    val operator: TokenType,
    val precedence: Int,
    val description: String = ""
) {
    
    init {
        require(operator.isOperator || operator.isTerminal) {
            "연산자 토큰이어야 합니다: $operator" 
        }
        require(precedence >= 0) { 
            "우선순위는 0 이상이어야 합니다: $precedence" 
        }
    }

    /**
     * 결합성 타입을 정의하는 열거형입니다.
     */
    enum class AssociativityType(val symbol: String, val description: String) {
        /** 좌결합: a op b op c = (a op b) op c */
        LEFT("L", "좌결합"),
        
        /** 우결합: a op b op c = a op (b op c) */
        RIGHT("R", "우결합"),
        
        /** 비결합: a op b op c = 오류 */
        NONE("N", "비결합"),
        
        /** 체인결합: a op b op c = a op b && b op c (비교 연산자용) */
        CHAIN("C", "체인결합");

        /**
         * 좌결합 타입인지 확인합니다.
         * 
         * @return 좌결합이면 true
         */
        fun isLeft(): Boolean = this == LEFT

        /**
         * 우결합 타입인지 확인합니다.
         * 
         * @return 우결합이면 true
         */
        fun isRight(): Boolean = this == RIGHT

        /**
         * 비결합 타입인지 확인합니다.
         * 
         * @return 비결합이면 true
         */
        fun isNone(): Boolean = this == NONE

        /**
         * 체인결합 타입인지 확인합니다.
         * 
         * @return 체인결합이면 true
         */
        fun isChain(): Boolean = this == CHAIN
        
        companion object {
            /**
             * 심볼로부터 결합성 타입을 찾습니다.
             *
             * @param symbol 결합성 심볼
             * @return 해당 결합성 타입
             * @throws IllegalArgumentException 알 수 없는 심볼인 경우
             */
            fun fromSymbol(symbol: String): AssociativityType {
                return values().find { it.symbol == symbol }
                    ?: throw IllegalArgumentException("알 수 없는 결합성 심볼: $symbol")
            }
        }
    }

    companion object {
        /**
         * 공통 Associativity 생성 로직입니다.
         *
         * @param type 결합성 타입
         * @param operator 연산자 토큰
         * @param precedence 우선순위
         * @param description 설명
         * @return 생성된 Associativity
         */
        private fun create(
            type: AssociativityType,
            operator: TokenType,
            precedence: Int,
            description: String = ""
        ): Associativity {
            return Associativity(type, operator, precedence, description)
        }

        /**
         * 좌결합 연산자를 생성합니다.
         *
         * @param operator 연산자 토큰
         * @param precedence 우선순위
         * @param description 설명
         * @return 좌결합 Associativity
         */
        fun leftAssoc(
            operator: TokenType, 
            precedence: Int, 
            description: String = ""
        ): Associativity = create(AssociativityType.LEFT, operator, precedence, description)

        /**
         * 우결합 연산자를 생성합니다.
         *
         * @param operator 연산자 토큰
         * @param precedence 우선순위
         * @param description 설명
         * @return 우결합 Associativity
         */
        fun rightAssoc(
            operator: TokenType, 
            precedence: Int, 
            description: String = ""
        ): Associativity = create(AssociativityType.RIGHT, operator, precedence, description)

        /**
         * 비결합 연산자를 생성합니다.
         *
         * @param operator 연산자 토큰
         * @param precedence 우선순위
         * @param description 설명
         * @return 비결합 Associativity
         */
        fun nonAssoc(
            operator: TokenType, 
            precedence: Int, 
            description: String = ""
        ): Associativity = create(AssociativityType.NONE, operator, precedence, description)

        /**
         * 체인결합 연산자를 생성합니다.
         *
         * @param operator 연산자 토큰
         * @param precedence 우선순위
         * @param description 설명
         * @return 체인결합 Associativity
         */
        fun chainAssoc(
            operator: TokenType, 
            precedence: Int, 
            description: String = ""
        ): Associativity = create(AssociativityType.CHAIN, operator, precedence, description)

        /**
         * 기본 연산자들의 결합성 규칙을 반환합니다.
         *
         * @return 기본 결합성 규칙들
         */
        fun getDefaultRules(): List<Associativity> = listOf(
            // 논리 연산자 (낮은 우선순위)
            leftAssoc(TokenType.OR, 1, "논리합 연산자"),
            leftAssoc(TokenType.AND, 2, "논리곱 연산자"),
            
            // 비교 연산자
            chainAssoc(TokenType.EQUAL, 3, "같음 비교"),
            chainAssoc(TokenType.NOT_EQUAL, 3, "다름 비교"),
            chainAssoc(TokenType.LESS, 4, "미만 비교"),
            chainAssoc(TokenType.LESS_EQUAL, 4, "이하 비교"),
            chainAssoc(TokenType.GREATER, 4, "초과 비교"),
            chainAssoc(TokenType.GREATER_EQUAL, 4, "이상 비교"),
            
            // 산술 연산자
            leftAssoc(TokenType.PLUS, 5, "덧셈"),
            leftAssoc(TokenType.MINUS, 5, "뺄셈"),
            leftAssoc(TokenType.MULTIPLY, 6, "곱셈"),
            leftAssoc(TokenType.DIVIDE, 6, "나눗셈"),
            leftAssoc(TokenType.MODULO, 6, "나머지"),
            
            // 지수 연산자 (높은 우선순위, 우결합)
            rightAssoc(TokenType.POWER, 7, "거듭제곱"),
            
            // 단항 연산자들 (가장 높은 우선순위)
            rightAssoc(TokenType.NOT, 8, "논리 부정"),
            rightAssoc(TokenType.MINUS, 8, "단항 마이너스"), // 문맥에 따라 이항/단항 구분 필요
            rightAssoc(TokenType.PLUS, 8, "단항 플러스")
        )

        /**
         * 연산자별 결합성 규칙 맵을 반환합니다.
         *
         * @return 연산자 -> 결합성 규칙 맵
         */
        fun getDefaultRuleMap(): Map<TokenType, Associativity> {
            return getDefaultRules().associateBy { it.operator }
        }
    }

    /**
     * 좌결합인지 확인합니다.
     *
     * @return 좌결합이면 true
     */
    fun isLeftAssociative(): Boolean = type == AssociativityType.LEFT

    /**
     * 우결합인지 확인합니다.
     *
     * @return 우결합이면 true
     */
    fun isRightAssociative(): Boolean = type == AssociativityType.RIGHT

    /**
     * 비결합인지 확인합니다.
     *
     * @return 비결합이면 true
     */
    fun isNonAssociative(): Boolean = type == AssociativityType.NONE

    /**
     * 체인결합인지 확인합니다.
     *
     * @return 체인결합이면 true
     */
    fun isChainAssociative(): Boolean = type == AssociativityType.CHAIN

    /**
     * 다른 연산자와의 우선순위를 비교합니다.
     *
     * @param other 비교할 결합성 규칙
     * @return 이 규칙이 더 높은 우선순위면 양수, 같으면 0, 낮으면 음수
     */
    fun comparePrecedence(other: Associativity): Int {
        return this.precedence.compareTo(other.precedence)
    }

    /**
     * 동일한 우선순위를 가지는지 확인합니다.
     *
     * @param other 비교할 결합성 규칙
     * @return 우선순위가 같으면 true
     */
    fun hasSamePrecedence(other: Associativity): Boolean {
        return this.precedence == other.precedence
    }

    /**
     * 더 높은 우선순위를 가지는지 확인합니다.
     *
     * @param other 비교할 결합성 규칙
     * @return 더 높은 우선순위면 true
     */
    fun hasHigherPrecedence(other: Associativity): Boolean {
        return this.precedence > other.precedence
    }

    /**
     * 더 낮은 우선순위를 가지는지 확인합니다.
     *
     * @param other 비교할 결합성 규칙
     * @return 더 낮은 우선순위면 true
     */
    fun hasLowerPrecedence(other: Associativity): Boolean {
        return this.precedence < other.precedence
    }

    /**
     * 충돌 해결 방법을 결정합니다.
     * 우선순위를 먼저 비교하고, 동일할 경우 결합성 규칙을 적용합니다.
     *
     * @param other 충돌하는 다른 결합성 규칙
     * @return 충돌 해결 방법
     */
    fun resolveConflict(other: Associativity): ConflictResolution {
        return when {
            precedence > other.precedence -> ConflictResolution.SHIFT
            precedence < other.precedence -> ConflictResolution.REDUCE
            else -> resolveSamePrecedenceConflict(other)
        }
    }

    /**
     * 동일한 우선순위에서의 충돌을 해결합니다.
     * 결합성 타입에 따라 적절한 해결 방법을 결정합니다.
     *
     * @param other 충돌하는 다른 결합성 규칙
     * @return 충돌 해결 방법
     */
    private fun resolveSamePrecedenceConflict(other: Associativity): ConflictResolution = when {
        isLeftAssociative() && other.isLeftAssociative() -> ConflictResolution.REDUCE
        isRightAssociative() && other.isRightAssociative() -> ConflictResolution.SHIFT
        isNonAssociative() || other.isNonAssociative() -> ConflictResolution.ERROR
        isChainAssociative() && other.isChainAssociative() -> ConflictResolution.SPECIAL
        else -> ConflictResolution.ERROR
    }

    /**
     * 충돌 해결 결과를 나타내는 열거형입니다.
     */
    enum class ConflictResolution(val description: String) {
        SHIFT("시프트 수행"),
        REDUCE("리듀스 수행"),
        ERROR("에러 발생"),
        SPECIAL("특수 처리 필요")
    }

    /**
     * 결합성 규칙의 유효성을 검증합니다.
     *
     * @return 유효하면 true
     */
    fun isValid(): Boolean {
        return try {
            // 연산자가 실제 연산자 토큰인지 확인
            operator.isOperator || operator.isTerminal
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 우선순위를 변경한 새로운 결합성 규칙을 반환합니다.
     *
     * @param newPrecedence 새로운 우선순위
     * @return 우선순위가 변경된 새 규칙
     */
    fun withPrecedence(newPrecedence: Int): Associativity {
        return copy(precedence = newPrecedence)
    }

    /**
     * 설명을 변경한 새로운 결합성 규칙을 반환합니다.
     *
     * @param newDescription 새로운 설명
     * @return 설명이 변경된 새 규칙
     */
    fun withDescription(newDescription: String): Associativity {
        return copy(description = newDescription)
    }

    /**
     * 결합성 규칙의 상세 정보를 반환합니다.
     *
     * @return 상세 정보 맵
     */
    fun getDetailInfo(): Map<String, Any> = mapOf(
        "operator" to operator.name,
        "associativity" to type.description,
        "precedence" to precedence,
        "description" to description,
        "isLeftAssoc" to isLeftAssociative(),
        "isRightAssoc" to isRightAssociative(),
        "isNonAssoc" to isNonAssociative(),
        "isChainAssoc" to isChainAssociative(),
        "symbol" to type.symbol,
        "isValid" to isValid()
    )

    /**
     * 결합성 규칙을 테이블 형태로 출력합니다.
     *
     * @return 테이블 문자열
     */
    fun toTableRow(): String {
        return "%-12s | %-8s | %-10d | %s".format(
            operator.name,
            type.symbol,
            precedence,
            description.take(30)
        )
    }

    /**
     * 결합성 규칙을 상세 문자열로 표현합니다.
     *
     * @return 상세 정보 문자열
     */
    fun toDetailString(): String = buildString {
        append("$operator: ")
        append("${type.description}(${type.symbol}), ")
        append("우선순위=$precedence")
        if (description.isNotEmpty()) {
            append(", $description")
        }
    }

    /**
     * 결합성 규칙의 간단한 요약을 반환합니다.
     *
     * @return 요약 문자열
     */
    override fun toString(): String {
        return "$operator(${type.symbol},$precedence)"
    }
}