package hs.kr.entrydsm.domain.lexer.policies

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 토큰 검증 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 토큰의 유효성을 검증하는 비즈니스 규칙을 
 * 캡슐화합니다. 토큰의 구조적 무결성, 값의 유효성, 타입 일치성 등을
 * 검증하여 잘못된 토큰이 시스템에 유입되는 것을 방지합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "TokenValidation",
    description = "토큰의 구조적 무결성과 값의 유효성을 검증하는 정책",
    domain = "lexer",
    scope = Scope.ENTITY
)
class TokenValidationPolicy {

    companion object {
        private const val MAX_TOKEN_LENGTH = 1000
        private const val MAX_NUMBER_VALUE = 1e15
        private const val MIN_NUMBER_VALUE = -1e15
        private const val MAX_IDENTIFIER_LENGTH = 255
        private const val MAX_VARIABLE_NAME_LENGTH = 100
    }

    /**
     * 토큰의 전반적인 유효성을 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 검증 통과 시 true
     * @throws IllegalArgumentException 검증 실패 시
     */
    fun validate(token: Token): Boolean {
        validateBasicStructure(token)
        validateTypeConsistency(token)
        validateValueFormat(token)
        validateLength(token)
        
        return true
    }

    /**
     * 토큰 목록의 유효성을 일괄 검증합니다.
     *
     * @param tokens 검증할 토큰 목록
     * @return 모든 토큰이 유효하면 true
     */
    fun validateTokens(tokens: List<Token>): Boolean {
        require(tokens.isNotEmpty()) { "검증할 토큰 목록이 비어있습니다" }
        
        tokens.forEach { token ->
            validate(token)
        }
        
        validateTokenSequence(tokens)
        return true
    }

    /**
     * 특정 타입의 토큰이 유효한지 검증합니다.
     *
     * @param token 검증할 토큰
     * @param expectedType 기대하는 토큰 타입
     * @return 유효하면 true
     */
    fun validateTokenType(token: Token, expectedType: TokenType): Boolean {
        require(token.type == expectedType) {
            "토큰 타입이 일치하지 않습니다. 기대: $expectedType, 실제: ${token.type}"
        }
        
        validate(token)
        return true
    }

    /**
     * 숫자 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 숫자 토큰
     * @return 유효하면 true
     */
    fun validateNumberToken(token: Token): Boolean {
        require(token.type == TokenType.NUMBER) {
            "숫자 토큰이 아닙니다: ${token.type}"
        }
        
        val value = try {
            token.value.toDouble()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("유효하지 않은 숫자 형식: ${token.value}", e)
        }
        
        require(value.isFinite()) {
            "숫자 값이 유한하지 않습니다: $value"
        }
        
        require(value in MIN_NUMBER_VALUE..MAX_NUMBER_VALUE) {
            "숫자 값이 허용 범위를 벗어났습니다: $value (범위: $MIN_NUMBER_VALUE ~ $MAX_NUMBER_VALUE)"
        }
        
        return true
    }

    /**
     * 식별자 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 식별자 토큰
     * @return 유효하면 true
     */
    fun validateIdentifierToken(token: Token): Boolean {
        require(token.type == TokenType.IDENTIFIER) {
            "식별자 토큰이 아닙니다: ${token.type}"
        }
        
        require(token.value.isNotEmpty()) {
            "식별자 값이 비어있습니다"
        }
        
        require(token.value.length <= MAX_IDENTIFIER_LENGTH) {
            "식별자 길이가 제한을 초과했습니다: ${token.value.length} > $MAX_IDENTIFIER_LENGTH"
        }
        
        require(token.value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))) {
            "유효하지 않은 식별자 형식: ${token.value}"
        }
        
        return true
    }

    /**
     * 변수 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 변수 토큰
     * @return 유효하면 true
     */
    fun validateVariableToken(token: Token): Boolean {
        require(token.type == TokenType.VARIABLE) {
            "변수 토큰이 아닙니다: ${token.type}"
        }
        
        require(token.value.isNotEmpty()) {
            "변수명이 비어있습니다"
        }
        
        require(token.value.length <= MAX_VARIABLE_NAME_LENGTH) {
            "변수명 길이가 제한을 초과했습니다: ${token.value.length} > $MAX_VARIABLE_NAME_LENGTH"
        }
        
        require(token.value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))) {
            "유효하지 않은 변수명 형식: ${token.value}"
        }
        
        return true
    }

    /**
     * 연산자 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 연산자 토큰
     * @return 유효하면 true
     */
    fun validateOperatorToken(token: Token): Boolean {
        require(token.type.isOperator) {
            "연산자 토큰이 아닙니다: ${token.type}"
        }
        
        require(token.value.isNotEmpty()) {
            "연산자 값이 비어있습니다"
        }
        
        val validOperators = setOf(
            "+", "-", "*", "/", "^", "%",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||", "!"
        )
        
        require(token.value in validOperators) {
            "지원하지 않는 연산자입니다: ${token.value}"
        }
        
        return true
    }

    /**
     * 키워드 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 키워드 토큰
     * @return 유효하면 true
     */
    fun validateKeywordToken(token: Token): Boolean {
        require(token.type.isKeyword) {
            "키워드 토큰이 아닙니다: ${token.type}"
        }
        
        val validKeywords = mapOf(
            TokenType.IF to "if",
            TokenType.TRUE to "true",
            TokenType.FALSE to "false",
            TokenType.AND to "and",
            TokenType.OR to "or",
            TokenType.NOT to "not"
        )
        
        val expectedValue = validKeywords[token.type]
        require(token.value.equals(expectedValue, ignoreCase = true)) {
            "키워드 값이 일치하지 않습니다. 기대: $expectedValue, 실제: ${token.value}"
        }
        
        return true
    }

    /**
     * 토큰의 기본 구조를 검증합니다.
     */
    private fun validateBasicStructure(token: Token) {
        require(token.value.length <= MAX_TOKEN_LENGTH) {
            "토큰 길이가 제한을 초과했습니다: ${token.value.length} > $MAX_TOKEN_LENGTH"
        }
    }

    /**
     * 토큰 타입과 값의 일치성을 검증합니다.
     */
    private fun validateTypeConsistency(token: Token) {
        when (token.type) {
            TokenType.NUMBER -> require(token.value.toDoubleOrNull() != null) {
                "NUMBER 타입이지만 숫자가 아닙니다: ${token.value}"
            }
            TokenType.TRUE, TokenType.FALSE -> require(
                token.value.lowercase() in listOf("true", "false")
            ) {
                "불린 타입이지만 불린 값이 아닙니다: ${token.value}"
            }
            TokenType.DOLLAR -> require(token.value == "$") {
                "EOF 타입이지만 '$' 값이 아닙니다: ${token.value}"
            }
            else -> { /* 다른 타입들은 추가 검증 없음 */ }
        }
    }

    /**
     * 토큰 값의 형식을 검증합니다.
     */
    private fun validateValueFormat(token: Token) {
        when (token.type) {
            TokenType.IDENTIFIER, TokenType.VARIABLE -> require(
                token.value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))
            ) {
                "유효하지 않은 식별자/변수 형식: ${token.value}"
            }
            TokenType.NUMBER -> require(
                token.value.matches(Regex("""^-?\d+(\.\d+)?$"""))
            ) {
                "유효하지 않은 숫자 형식: ${token.value}"
            }
            else -> { /* 다른 타입들은 형식 검증 없음 */ }
        }
    }

    /**
     * 토큰 길이를 검증합니다.
     */
    private fun validateLength(token: Token) {
        when (token.type) {
            TokenType.IDENTIFIER -> require(token.value.length <= MAX_IDENTIFIER_LENGTH) {
                "식별자 길이 초과: ${token.value.length} > $MAX_IDENTIFIER_LENGTH"
            }
            TokenType.VARIABLE -> require(token.value.length <= MAX_VARIABLE_NAME_LENGTH) {
                "변수명 길이 초과: ${token.value.length} > $MAX_VARIABLE_NAME_LENGTH"
            }
            else -> { /* 다른 타입들은 길이 제한 없음 */ }
        }
    }

    /**
     * 토큰 시퀀스의 유효성을 검증합니다.
     */
    private fun validateTokenSequence(tokens: List<Token>) {
        // 연속된 연산자 검증
        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            
            if (current.type.isOperator && next.type.isOperator) {
                // 일부 연산자 조합은 허용 (예: !, ++)
                if (!isValidOperatorSequence(current, next)) {
                    throw IllegalArgumentException(
                        "유효하지 않은 연산자 시퀀스: ${current.value} ${next.value}"
                    )
                }
            }
        }
        
        // EOF 토큰은 마지막에만 위치해야 함
        val eofTokens = tokens.filter { it.type == TokenType.DOLLAR }
        if (eofTokens.isNotEmpty()) {
            require(eofTokens.size == 1) {
                "EOF 토큰이 여러 개 존재합니다: ${eofTokens.size}개"
            }
            require(tokens.last().type == TokenType.DOLLAR) {
                "EOF 토큰이 마지막 위치에 있지 않습니다"
            }
        }
    }

    /**
     * 연산자 시퀀스가 유효한지 확인합니다.
     */
    private fun isValidOperatorSequence(first: Token, second: Token): Boolean {
        // 단항 연산자 뒤에는 다른 연산자가 올 수 있음
        if (first.type in listOf(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
            return true
        }
        
        // 기타 경우는 무효
        return false
    }

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxTokenLength" to MAX_TOKEN_LENGTH,
        "maxNumberValue" to MAX_NUMBER_VALUE,
        "minNumberValue" to MIN_NUMBER_VALUE,
        "maxIdentifierLength" to MAX_IDENTIFIER_LENGTH,
        "maxVariableNameLength" to MAX_VARIABLE_NAME_LENGTH
    )
}