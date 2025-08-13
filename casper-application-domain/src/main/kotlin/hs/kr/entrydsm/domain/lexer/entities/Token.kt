package hs.kr.entrydsm.domain.lexer.entities

import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.domain.lexer.exceptions.LexerException
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.values.Position

/**
 * 토큰의 정보를 담는 값 객체입니다.
 *
 * 렉서가 입력 텍스트를 분석하여 생성하는 토큰의 모든 정보를 포함합니다.
 * 토큰 타입, 원본 문자열 값, 위치 정보를 가지며, 파서에서 구문 분석에 사용됩니다.
 * 불변 객체로 설계되어 안전한 토큰 전달을 보장합니다.
 *
 * @property type 토큰의 타입
 * @property value 토큰의 원본 문자열 값
 * @property position 토큰의 위치 정보
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(context = "lexer", aggregateRoot = LexerAggregate::class)
data class Token(
    val type: TokenType,
    val value: String,
    val position: Position
) {

    init {
        if (value.isEmpty() && type != TokenType.DOLLAR) {
            throw LexerException.tokenValueEmptyExceptEof(type.name)
        }
        // Position은 length 속성이 없으므로 검증 제거
    }

    companion object {
        /**
         * 기본 위치 정보로 토큰을 생성합니다.
         *
         * @param type 토큰 타입
         * @param value 토큰 값
         * @param startIndex 시작 인덱스 (기본값: 0)
         * @return Token 인스턴스
         */
        fun of(type: TokenType, value: String, startIndex: Int = 0): Token {
            val position = Position.of(startIndex)
            return Token(type, value, position)
        }

        /**
         * Position을 이용하여 토큰을 생성합니다.
         *
         * @param type 토큰 타입
         * @param value 토큰 값
         * @param start 시작 위치
         * @return Token 인스턴스
         */
        fun at(type: TokenType, value: String, start: Position): Token {
            val position = start
            return Token(type, value, position)
        }

        /**
         * 범위를 지정하여 토큰을 생성합니다.
         *
         * @param type 토큰 타입
         * @param value 토큰 값
         * @param startIndex 시작 인덱스
         * @param endIndex 끝 인덱스
         * @return Token 인스턴스
         */
        fun between(type: TokenType, value: String, startIndex: Int, endIndex: Int): Token {
            val position = Position.of(startIndex)
            return Token(type, value, position)
        }

        /**
         * EOF 토큰을 생성합니다.
         *
         * @param position EOF 위치 (기본값: Position.START)
         * @return EOF Token 인스턴스
         */
        fun eof(position: Position = Position.START): Token {
            return Token(TokenType.DOLLAR, "$", position)
        }

        /**
         * 숫자 토큰을 생성합니다.
         *
         * @param value 숫자 문자열
         * @param startIndex 시작 인덱스
         * @return 숫자 Token 인스턴스
         */
        fun number(value: String, startIndex: Int): Token {
            if (!value.matches(Regex("""\d+(\.\d+)?"""))) {
                throw LexerException.invalidNumberFormat(value)
            }
            return of(TokenType.NUMBER, value, startIndex)
        }

        /**
         * 식별자 토큰을 생성합니다.
         *
         * @param value 식별자 문자열
         * @param startIndex 시작 인덱스
         * @return 식별자 Token 인스턴스
         */
        fun identifier(value: String, startIndex: Int): Token {
            if (!value.matches(Regex("""[a-zA-Z_][a-zA-Z0-9_]*"""))) {
                throw LexerException.invalidIdentifierFormat(value)
            }

            // 키워드 검사
            val keywordType = TokenType.findKeyword(value)
            val type = keywordType ?: TokenType.IDENTIFIER
            return of(type, value, startIndex)
        }

        /**
         * 변수 토큰을 생성합니다.
         *
         * @param value 변수명 (중괄호 제외)
         * @param startIndex 시작 인덱스 (중괄호 포함)
         * @return 변수 Token 인스턴스
         */
        fun variable(value: String, startIndex: Int): Token {
            if (value.isEmpty()) {
                throw LexerException.variableNameEmpty(value)
            }

            val position = Position.of(startIndex) // {변수명} 포함
            return Token(TokenType.VARIABLE, value, position)
        }

        /**
         * 연산자 토큰을 생성합니다.
         *
         * @param type 연산자 타입
         * @param value 연산자 문자열
         * @param startIndex 시작 인덱스
         * @return 연산자 Token 인스턴스
         */
        fun operator(type: TokenType, value: String, startIndex: Int): Token {
            if (!type.isOperator) {
                throw LexerException.notOperatorType(type.name)
            }

            return of(type, value, startIndex)
        }
    }

    /**
     * 토큰이 터미널 심볼인지 확인합니다.
     *
     * @return 터미널 심볼이면 true, 아니면 false
     */
    fun isTerminal(): Boolean = type.isTerminal

    /**
     * 토큰이 논터미널 심볼인지 확인합니다.
     *
     * @return 논터미널 심볼이면 true, 아니면 false
     */
    fun isNonTerminal(): Boolean = type.isNonTerminal()

    /**
     * 토큰이 연산자인지 확인합니다.
     *
     * @return 연산자이면 true, 아니면 false
     */
    fun isOperator(): Boolean = type.isOperator

    /**
     * 토큰이 키워드인지 확인합니다.
     *
     * @return 키워드이면 true, 아니면 false
     */
    fun isKeyword(): Boolean = type.isKeyword

    /**
     * 토큰이 리터럴인지 확인합니다.
     *
     * @return 리터럴이면 true, 아니면 false
     */
    fun isLiteral(): Boolean = type.isLiteral

    /**
     * 토큰이 숫자인지 확인합니다.
     *
     * @return 숫자이면 true, 아니면 false
     */
    fun isNumber(): Boolean = type == TokenType.NUMBER

    /**
     * 토큰이 식별자인지 확인합니다.
     *
     * @return 식별자이면 true, 아니면 false
     */
    fun isIdentifier(): Boolean = type == TokenType.IDENTIFIER

    /**
     * 토큰이 변수인지 확인합니다.
     *
     * @return 변수이면 true, 아니면 false
     */
    fun isVariable(): Boolean = type == TokenType.VARIABLE

    /**
     * 토큰이 EOF인지 확인합니다.
     *
     * @return EOF이면 true, 아니면 false
     */
    fun isEOF(): Boolean = type == TokenType.DOLLAR

    /**
     * 토큰이 불린 값인지 확인합니다.
     *
     * @return 불린 값이면 true, 아니면 false
     */
    fun isBoolean(): Boolean = type.isBooleanLiteral()

    /**
     * 토큰 값을 숫자로 변환합니다.
     *
     * @return 변환된 Double 값
     * @throws IllegalStateException 숫자 토큰이 아닌 경우
     * @throws NumberFormatException 숫자 변환 실패시
     */
    fun toNumber(): Double {
        if (!isNumber()) {
            throw LexerException.notNumberToken(type.name)
        }

        return value.toDouble()
    }

    /**
     * 토큰 값을 불린으로 변환합니다.
     *
     * @return 변환된 Boolean 값
     * @throws IllegalStateException 불린 토큰이 아닌 경우
     */
    fun toBoolean(): Boolean {
        if (!isBoolean()) {
            throw LexerException.notBooleanToken(type.name)
        }

        return when (type) {
            TokenType.TRUE -> true
            TokenType.FALSE -> false
            else -> throw LexerException.unexpectedBooleanTokenType(type.name)
        }
    }

    /**
     * 토큰의 시작 위치를 반환합니다.
     *
     * @return 시작 Position
     */
    fun getStartPosition(): Position = position

    /**
     * 토큰의 끝 위치를 반환합니다.
     *
     * @return 끝 Position
     */
    fun getEndPosition(): Position = position.advance(value.length)

    /**
     * 토큰의 길이를 반환합니다.
     *
     * @return 토큰 길이
     */
    fun getLength(): Int = value.length

    /**
     * 토큰이 특정 위치를 포함하는지 확인합니다.
     *
     * @param checkPosition 확인할 위치
     * @return 포함하면 true, 아니면 false
     */
    fun contains(checkPosition: Position): Boolean = checkPosition.index >= position.index && checkPosition.index < position.index + value.length

    /**
     * 토큰의 카테고리를 반환합니다.
     *
     * @return 토큰 카테고리 문자열
     */
    fun getCategory(): String = type.getCategory()

    /**
     * 토큰을 문자열로 표현합니다.
     * 값이 있으면 "TYPE(value)", 없으면 "TYPE" 형태로 반환합니다.
     *
     * @return 토큰 문자열 표현
     */
    override fun toString(): String = if (value.isNotEmpty() && type != TokenType.DOLLAR) {
        "$type($value)"
    } else {
        type.toString()
    }

    /**
     * 디버깅용 상세 문자열을 반환합니다.
     *
     * @return "TYPE(value) at position" 형태의 문자열
     */
    fun toDetailString(): String = "$this at ${position.toShortString()}"
}