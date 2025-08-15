package hs.kr.entrydsm.domain.lexer.entities

import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * POC 코드와 완전히 일치하는 토큰 타입을 정의하는 열거형입니다.
 *
 * POC 코드의 완전한 LR(1) 파서에서 사용되는 터미널 심볼과 논터미널 심볼을
 * 정확히 복제하여 기능적 누락을 방지합니다. POC의 34개 생성 규칙과
 * 완전히 호환됩니다.
 *
 * @see POC Grammar object의 TokenType 정의
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
@Entity(context = "lexer", aggregateRoot = LexerAggregate::class)
enum class TokenType(
    val isTerminal: Boolean = true,
    val isOperator: Boolean = false,
    val isKeyword: Boolean = false,
    val isLiteral: Boolean = false,
    val symbol: String? = null
) {
    // === POC 코드의 터미널 심볼들 (정확한 복제) ===
    NUMBER(isLiteral = true), // 숫자 리터럴 (123, 3.14)
    IDENTIFIER(isLiteral = true), // 식별자 (변수명, 함수명)
    VARIABLE(isLiteral = true), // 변수 토큰

    // POC 코드의 산술 연산자들
    PLUS(isOperator = true, symbol = "+"), // +
    MINUS(isOperator = true, symbol = "-"), // -
    MULTIPLY(isOperator = true, symbol = "*"), // *
    DIVIDE(isOperator = true, symbol = "/"), // /
    POWER(isOperator = true, symbol = "^"), // ^
    MODULO(isOperator = true, symbol = "%"), // %

    // POC 코드의 비교 연산자들
    EQUAL(isOperator = true, symbol = "=="), // ==
    NOT_EQUAL(isOperator = true, symbol = "!="), // !=
    LESS(isOperator = true, symbol = "<"), // <
    LESS_EQUAL(isOperator = true, symbol = "<="), // <=
    GREATER(isOperator = true, symbol = ">"), // >
    GREATER_EQUAL(isOperator = true, symbol = ">="), // >=

    // 터미널 심볼들 - 논리 연산자
    AND(isOperator = true, symbol = "&&"), // 논리 AND (&&)
    OR(isOperator = true, symbol = "||"), // 논리 OR (||)
    NOT(isOperator = true, symbol = "!"), // 논리 NOT (!)

    // 터미널 심볼들 - 구분자
    LEFT_PAREN(symbol = "("), // 왼쪽 괄호 (()
    RIGHT_PAREN(symbol = ")"), // 오른쪽 괄호 ())
    COMMA(symbol = ","), // 쉼표 (,)

    // 터미널 심볼들 - 키워드
    IF(isKeyword = true, symbol = "if"), // IF 키워드
    TRUE(isKeyword = true, isLiteral = true, symbol = "true"), // TRUE 키워드
    FALSE(isKeyword = true, isLiteral = true, symbol = "false"), // FALSE 키워드
    BOOLEAN(isKeyword = true, isLiteral = true, symbol = "boolean"), // BOOLEAN 타입
    FUNCTION(isKeyword = true, symbol = "function"), // FUNCTION 키워드

    // 터미널 심볼들 - 추가 구분자
    QUESTION(symbol = "?"), // 물음표 (?)
    COLON(symbol = ":"), // 콜론 (:)
    WHITESPACE, // 공백

    // 추가 연산자 별칭
    LESS_THAN(isOperator = true, symbol = "<"), // < (LESS의 별칭)
    GREATER_THAN(isOperator = true, symbol = ">"), // > (GREATER의 별칭)

    // 특수 심볼
    DOLLAR(symbol = "$"), // EOF (End Of File) 심볼
    EPSILON, // 엡실론 (빈 문자열) 심볼

    // 논터미널 심볼들 (파싱 과정에서 생성되는 중간 심볼)
    START(isTerminal = false), // 문법의 시작 심볼 (확장된 문법용)
    EXPR(isTerminal = false), // 표현식
    AND_EXPR(isTerminal = false), // AND 표현식
    COMP_EXPR(isTerminal = false), // 비교 표현식
    ARITH_EXPR(isTerminal = false), // 산술 표현식
    TERM(isTerminal = false), // 항
    FACTOR(isTerminal = false), // 인자
    PRIMARY(isTerminal = false), // 기본 요소
    ARGS(isTerminal = false), // 함수 인수 목록

    // 추가 논터미널 심볼들
    EQUALITY_EXPR(isTerminal = false), // 동등성 표현식
    RELATIONAL_EXPR(isTerminal = false), // 관계 표현식
    ADDITIVE_EXPR(isTerminal = false), // 덧셈/뺄셈 표현식
    MULTIPLICATIVE_EXPR(isTerminal = false), // 곱셈/나눗셈 표현식
    UNARY_EXPR(isTerminal = false), // 단항 표현식
    POWER_EXPR(isTerminal = false), // 거듭제곱 표현식
    PRIMARY_EXPR(isTerminal = false), // 기본 표현식
    ATOM(isTerminal = false), // 원자 표현식
    FUNCTION_CALL(isTerminal = false), // 함수 호출
    ARGUMENTS(isTerminal = false), // 인수들
    ARGUMENT_LIST(isTerminal = false), // 인수 목록
    CONDITIONAL_EXPR(isTerminal = false); // 조건부 표현식

    /**
     * 토큰 타입이 논터미널 심볼인지 확인합니다.
     */
    fun isNonTerminal(): Boolean = !isTerminal

    /**
     * 토큰 타입이 단항 연산자인지 확인합니다.
     */
    fun isUnaryOperator(): Boolean = this in unaryOperators

    /**
     * 토큰 타입이 이항 연산자인지 확인합니다.
     */
    fun isBinaryOperator(): Boolean = isOperator && !isUnaryOperator()

    /**
     * 토큰 타입이 비교 연산자인지 확인합니다.
     */
    fun isComparisonOperator(): Boolean = this in comparisonOperators

    /**
     * 토큰 타입이 산술 연산자인지 확인합니다.
     */
    fun isArithmeticOperator(): Boolean = this in arithmeticOperators

    /**
     * 토큰 타입이 논리 연산자인지 확인합니다.
     */
    fun isLogicalOperator(): Boolean = this in logicalOperators

    /**
     * 토큰 타입이 불린 리터럴인지 확인합니다.
     */
    fun isBooleanLiteral(): Boolean = this in booleanLiterals

    /**
     * 토큰 타입이 괄호인지 확인합니다.
     */
    fun isParenthesis(): Boolean = this in parentheses

    /**
     * 토큰 타입이 여는 괄호인지 확인합니다.
     */
    fun isOpeningParenthesis(): Boolean = this == LEFT_PAREN

    /**
     * 토큰 타입이 닫는 괄호인지 확인합니다.
     */
    fun isClosingParenthesis(): Boolean = this == RIGHT_PAREN

    /**
     * 토큰 타입의 카테고리를 반환합니다.
     */
    fun getCategory(): String = when {
        isKeyword -> CategoryNames.KEYWORD
        isLiteral && !isKeyword -> CategoryNames.LITERAL
        isOperator -> CategoryNames.OPERATOR
        isParenthesis() -> CategoryNames.PARENTHESIS
        this == COMMA -> CategoryNames.SEPARATOR
        this == DOLLAR -> CategoryNames.EOF
        isNonTerminal() -> CategoryNames.NON_TERMINAL
        else -> CategoryNames.UNKNOWN
    }

    companion object {
        // 연산자 그룹들 - 하드코딩 최소화를 위한 집합 정의
        private val unaryOperators by lazy {
            values().filter { it.isOperator && it.symbol in setOf("-", "!") }.toSet()
        }

        private val comparisonOperators by lazy {
            values().filter { it.isOperator && it.symbol?.let { s ->
                s in setOf("==", "!=", "<", "<=", ">", ">=")
            } == true }.toSet()
        }

        private val arithmeticOperators by lazy {
            values().filter { it.isOperator && it.symbol?.let { s ->
                s in setOf("+", "-", "*", "/", "^", "%")
            } == true }.toSet()
        }

        private val logicalOperators by lazy {
            values().filter { it.isOperator && it.symbol?.let { s ->
                s in setOf("&&", "||", "!")
            } == true }.toSet()
        }

        private val booleanLiterals by lazy {
            values().filter { it.symbol in setOf("true", "false") }.toSet()
        }

        private val parentheses by lazy {
            values().filter { it.symbol in setOf("(", ")") }.toSet()
        }

        // 키워드 맵 - 동적 생성
        private val keywordMap by lazy {
            values().filter { it.isKeyword && it.symbol != null }
                .associateBy { it.symbol!! }
        }

        /**
         * 모든 터미널 심볼을 반환합니다.
         */
        fun getTerminals(): List<TokenType> = values().filter { it.isTerminal }

        /**
         * 모든 논터미널 심볼을 반환합니다.
         */
        fun getNonTerminals(): List<TokenType> = values().filter { it.isNonTerminal() }

        /**
         * 모든 연산자를 반환합니다.
         */
        fun getOperators(): List<TokenType> = values().filter { it.isOperator }

        /**
         * 모든 키워드를 반환합니다.
         */
        fun getKeywords(): List<TokenType> = values().filter { it.isKeyword }

        /**
         * 문자열로부터 키워드 토큰 타입을 찾습니다.
         */
        fun findKeyword(text: String): TokenType? = keywordMap[text.lowercase()]

        /**
         * 심볼로부터 토큰 타입을 찾습니다.
         */
        fun findBySymbol(symbol: String): TokenType? =
            values().find { it.symbol == symbol }
    }

    /**
     * 카테고리 이름들을 상수로 관리
     */
    private object CategoryNames {
        const val KEYWORD = "KEYWORD"
        const val LITERAL = "LITERAL"
        const val OPERATOR = "OPERATOR"
        const val PARENTHESIS = "PARENTHESIS"
        const val SEPARATOR = "SEPARATOR"
        const val EOF = "EOF"
        const val NON_TERMINAL = "NON_TERMINAL"
        const val UNKNOWN = "UNKNOWN"
    }
}