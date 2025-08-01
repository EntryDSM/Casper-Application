package hs.kr.entrydsm.domain.lexer.factories

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.values.Position

/**
 * 토큰 생성을 담당하는 팩토리 클래스입니다.
 *
 * DDD Factory 패턴을 적용하여 복잡한 토큰 생성 로직을 캡슐화하고,
 * 일관된 토큰 객체 생성을 보장합니다. 다양한 타입의 토큰을 생성하며,
 * 위치 정보와 검증 로직을 포함한 완전한 토큰을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Factory(context = "lexer", complexity = Complexity.NORMAL, cache = true)
class TokenFactory {

    companion object {
        private val KEYWORD_MAP = mapOf(
            "if" to TokenType.IF,
            "true" to TokenType.TRUE,
            "false" to TokenType.FALSE,
            "and" to TokenType.AND,
            "or" to TokenType.OR,
            "not" to TokenType.NOT,
            "mod" to TokenType.MODULO
        )

        private val OPERATOR_MAP = mapOf(
            "+" to TokenType.PLUS,
            "-" to TokenType.MINUS,
            "*" to TokenType.MULTIPLY,
            "/" to TokenType.DIVIDE,
            "^" to TokenType.POWER,
            "%" to TokenType.MODULO,
            "==" to TokenType.EQUAL,
            "!=" to TokenType.NOT_EQUAL,
            "<" to TokenType.LESS,
            "<=" to TokenType.LESS_EQUAL,
            ">" to TokenType.GREATER,
            ">=" to TokenType.GREATER_EQUAL,
            "&&" to TokenType.AND,
            "||" to TokenType.OR,
            "!" to TokenType.NOT
        )

        private val DELIMITER_MAP = mapOf(
            "(" to TokenType.LEFT_PAREN,
            ")" to TokenType.RIGHT_PAREN,
            "," to TokenType.COMMA
        )
    }

    /**
     * 문자열 값과 위치 정보로 토큰을 생성합니다.
     *
     * @param value 토큰 값
     * @param position 토큰 위치
     * @return 생성된 Token
     */
    fun createToken(value: String, position: Position): Token {
        val type = determineTokenType(value)
        return Token(type, value, position)
    }

    /**
     * 토큰 타입과 값, 위치로 토큰을 생성합니다.
     *
     * @param type 토큰 타입
     * @param value 토큰 값
     * @param position 토큰 위치
     * @return 생성된 Token
     */
    fun createToken(type: TokenType, value: String, position: Position): Token {
        validateTokenData(type, value)
        return Token(type, value, position)
    }

    /**
     * 숫자 토큰을 생성합니다.
     *
     * @param value 숫자 문자열
     * @param startPosition 시작 위치
     * @return 숫자 Token
     * @throws IllegalArgumentException 유효하지 않은 숫자 형식인 경우
     */
    fun createNumberToken(value: String, startPosition: Position): Token {
        require(isValidNumber(value)) { "유효하지 않은 숫자 형식입니다: $value" }
        
        val position = startPosition
        return Token(TokenType.NUMBER, value, position)
    }

    /**
     * 식별자 토큰을 생성합니다.
     *
     * @param value 식별자 문자열
     * @param startPosition 시작 위치
     * @return 식별자 Token (키워드인 경우 해당 키워드 토큰)
     */
    fun createIdentifierToken(value: String, startPosition: Position): Token {
        require(isValidIdentifier(value)) { "유효하지 않은 식별자입니다: $value" }
        
        val position = startPosition
        val type = KEYWORD_MAP[value.lowercase()] ?: TokenType.IDENTIFIER
        
        return Token(type, value, position)
    }

    /**
     * 변수 토큰을 생성합니다.
     *
     * @param variableName 변수명 (중괄호 제외)
     * @param startPosition 시작 위치 (중괄호 포함)
     * @return 변수 Token
     */
    fun createVariableToken(variableName: String, startPosition: Position): Token {
        require(variableName.isNotEmpty()) { "변수명은 비어있을 수 없습니다" }
        require(isValidIdentifier(variableName)) { "유효하지 않은 변수명입니다: $variableName" }
        
        val position = startPosition // {변수명} 포함
        return Token(TokenType.VARIABLE, variableName, position)
    }

    /**
     * 연산자 토큰을 생성합니다.
     *
     * @param operator 연산자 문자열
     * @param startPosition 시작 위치
     * @return 연산자 Token
     * @throws IllegalArgumentException 지원하지 않는 연산자인 경우
     */
    fun createOperatorToken(operator: String, startPosition: Position): Token {
        val type = OPERATOR_MAP[operator] 
            ?: throw IllegalArgumentException("지원하지 않는 연산자입니다: $operator")
        
        val position = startPosition
        return Token(type, operator, position)
    }

    /**
     * 구분자 토큰을 생성합니다.
     *
     * @param delimiter 구분자 문자열
     * @param startPosition 시작 위치
     * @return 구분자 Token
     * @throws IllegalArgumentException 지원하지 않는 구분자인 경우
     */
    fun createDelimiterToken(delimiter: String, startPosition: Position): Token {
        val type = DELIMITER_MAP[delimiter]
            ?: throw IllegalArgumentException("지원하지 않는 구분자입니다: $delimiter")
        
        val position = startPosition
        return Token(type, delimiter, position)
    }

    /**
     * EOF(End of File) 토큰을 생성합니다.
     *
     * @param position EOF 위치
     * @return EOF Token
     */
    fun createEOFToken(position: Position): Token {
        val tokenPosition = position
        return Token(TokenType.DOLLAR, "$", tokenPosition)
    }

    /**
     * 불린 리터럴 토큰을 생성합니다.
     *
     * @param value "true" 또는 "false"
     * @param startPosition 시작 위치
     * @return 불린 Token
     * @throws IllegalArgumentException 유효하지 않은 불린 값인 경우
     */
    fun createBooleanToken(value: String, startPosition: Position): Token {
        val type = when (value.lowercase()) {
            "true" -> TokenType.TRUE
            "false" -> TokenType.FALSE
            else -> throw IllegalArgumentException("유효하지 않은 불린 값입니다: $value")
        }
        
        val position = startPosition
        return Token(type, value, position)
    }

    /**
     * 문자열로부터 토큰 타입을 결정합니다.
     *
     * @param value 토큰 값
     * @return 결정된 TokenType
     */
    private fun determineTokenType(value: String): TokenType = when {
        value.isEmpty() -> throw IllegalArgumentException("토큰 값은 비어있을 수 없습니다")
        isValidNumber(value) -> TokenType.NUMBER
        KEYWORD_MAP.containsKey(value.lowercase()) -> KEYWORD_MAP[value.lowercase()]!!
        OPERATOR_MAP.containsKey(value) -> OPERATOR_MAP[value]!!
        DELIMITER_MAP.containsKey(value) -> DELIMITER_MAP[value]!!
        value == "$" -> TokenType.DOLLAR
        isValidIdentifier(value) -> TokenType.IDENTIFIER
        else -> throw IllegalArgumentException("인식할 수 없는 토큰 값입니다: $value")
    }

    /**
     * 유효한 숫자 형식인지 검증합니다.
     *
     * @param value 검증할 문자열
     * @return 유효한 숫자이면 true
     */
    private fun isValidNumber(value: String): Boolean {
        return try {
            value.toDouble()
            value.matches(Regex("""^-?\d+(\.\d+)?$"""))
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * 유효한 식별자 형식인지 검증합니다.
     *
     * @param value 검증할 문자열
     * @return 유효한 식별자이면 true
     */
    private fun isValidIdentifier(value: String): Boolean {
        return value.isNotEmpty() && 
               value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))
    }

    /**
     * 토큰 데이터의 유효성을 검증합니다.
     *
     * @param type 토큰 타입
     * @param value 토큰 값
     * @throws IllegalArgumentException 유효하지 않은 데이터인 경우
     */
    private fun validateTokenData(type: TokenType, value: String) {
        require(value.isNotEmpty() || type == TokenType.DOLLAR) {
            "토큰 값은 비어있을 수 없습니다 (EOF 토큰 제외): type=$type"
        }

        when (type) {
            TokenType.NUMBER -> require(isValidNumber(value)) {
                "NUMBER 타입 토큰은 유효한 숫자여야 합니다: $value"
            }
            TokenType.IDENTIFIER -> require(isValidIdentifier(value)) {
                "IDENTIFIER 타입 토큰은 유효한 식별자여야 합니다: $value"
            }
            TokenType.VARIABLE -> require(isValidIdentifier(value)) {
                "VARIABLE 타입 토큰은 유효한 변수명이어야 합니다: $value"
            }
            in listOf(TokenType.TRUE, TokenType.FALSE) -> require(
                value.lowercase() in listOf("true", "false")
            ) {
                "불린 타입 토큰은 'true' 또는 'false'여야 합니다: $value"
            }
            else -> { /* 다른 타입들은 추가 검증 없음 */ }
        }
    }

    /**
     * 팩토리에서 지원하는 토큰 타입 목록을 반환합니다.
     *
     * @return 지원되는 TokenType 집합
     */
    fun getSupportedTokenTypes(): Set<TokenType> = setOf(
        TokenType.NUMBER,
        TokenType.IDENTIFIER,
        TokenType.VARIABLE,
        TokenType.DOLLAR,
        *KEYWORD_MAP.values.toTypedArray(),
        *OPERATOR_MAP.values.toTypedArray(),
        *DELIMITER_MAP.values.toTypedArray()
    )

    /**
     * 특정 문자열이 키워드인지 확인합니다.
     *
     * @param value 확인할 문자열
     * @return 키워드이면 true
     */
    fun isKeyword(value: String): Boolean = KEYWORD_MAP.containsKey(value.lowercase())

    /**
     * 특정 문자열이 연산자인지 확인합니다.
     *
     * @param value 확인할 문자열
     * @return 연산자이면 true
     */
    fun isOperator(value: String): Boolean = OPERATOR_MAP.containsKey(value)

    /**
     * 특정 문자열이 구분자인지 확인합니다.
     *
     * @param value 확인할 문자열
     * @return 구분자이면 true
     */
    fun isDelimiter(value: String): Boolean = DELIMITER_MAP.containsKey(value)
}