package hs.kr.entrydsm.domain.lexer.policies

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.lexer.exceptions.LexerException
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
        if (tokens.isEmpty()) {
            throw LexerException.tokensEmpty()
        }

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
        if (token.type != expectedType) {
            throw LexerException.tokenTypeMismatch(expected = expectedType.name, actual = token.type.name)
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
        if (token.type != TokenType.NUMBER) {
            throw LexerException.notNumberToken(token.type.name) // (LEX008 재사용)
        }
        
        val value = try {
            token.value.toDouble()
        } catch (e: NumberFormatException) {
            throw LexerException.invalidNumberFormat(token.value)
        }

        val parsed = token.value.toDouble()
        if (!parsed.isFinite()) {
            throw LexerException.numberNotFinite(parsed)
        }

        if (parsed < MIN_NUMBER_VALUE || parsed > MAX_NUMBER_VALUE) {
            throw LexerException.numberOutOfRange(parsed, MIN_NUMBER_VALUE, MAX_NUMBER_VALUE)
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
        if (token.type != TokenType.IDENTIFIER) {
            throw LexerException.notIdentifierToken(token.type.name)
        }

        if (token.value.isEmpty()) {
            throw LexerException.identifierEmpty()
        }

        if (token.value.length > MAX_IDENTIFIER_LENGTH) {
            throw LexerException.identifierTooLong(token.value.length, MAX_IDENTIFIER_LENGTH)
        }

        if (!token.value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))) {
            throw LexerException.identifierInvalidFormat(token.value)
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
        if (token.type != TokenType.VARIABLE) {
            throw LexerException.notVariableToken(token.type.name)
        }

        if (token.value.isEmpty()) {
            throw LexerException.variableNameEmpty(token.value) // (LEX006 재사용)
        }

        if (token.value.length > MAX_VARIABLE_NAME_LENGTH) {
            throw LexerException.variableNameTooLong(token.value.length, MAX_VARIABLE_NAME_LENGTH)
        }

        if (!token.value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))) {
            throw LexerException.variableNameInvalidFormat(token.value)
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
        if (!token.type.isOperator) {
            throw LexerException.notOperatorType(token.type.name)
        }

        if (token.value.isEmpty()) {
            throw LexerException.operatorValueEmpty()
        }
        
        val validOperators = setOf(
            "+", "-", "*", "/", "^", "%",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||", "!"
        )

        if (token.value !in validOperators) {
            throw LexerException.unsupportedOperator(token.value) // (LEX013 재사용)
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
        if (!token.type.isKeyword) {
            throw LexerException.notKeywordToken(token.type.name)
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

        if (!token.value.equals(expectedValue, ignoreCase = true)) {
            throw LexerException.keywordValueMismatch(expectedValue, token.value)
        }

        return true
    }

    /**
     * 토큰의 기본 구조를 검증합니다.
     */
    private fun validateBasicStructure(token: Token) {
        if (token.value.length > MAX_TOKEN_LENGTH) {
            throw LexerException.tokenTooLong(token.value.length, MAX_TOKEN_LENGTH)
        }
    }

    /**
     * 토큰 타입과 값의 일치성을 검증합니다.
     */
    private fun validateTypeConsistency(token: Token) {
        when (token.type) {
            TokenType.NUMBER -> {
                if (token.value.toDoubleOrNull() == null) {
                    throw LexerException.numberTokenNotNumeric(token.value)
                }
            }

            TokenType.TRUE, TokenType.FALSE -> {
                val v = token.value.lowercase()
                if (v != "true" && v != "false") {
                    throw LexerException.booleanTokenInvalid(token.value) // (LEX020 재사용)
                }
            }

            TokenType.DOLLAR -> {
                if (token.value != "$") {
                    throw LexerException.dollarTokenInvalidValue(token.value)
                }
            }
            else -> { /* 다른 타입들은 추가 검증 없음 */ }
        }
    }

    /**
     * 토큰 값의 형식을 검증합니다.
     */
    private fun validateValueFormat(token: Token) {
        when (token.type) {
            TokenType.IDENTIFIER, TokenType.VARIABLE -> {
                if (!token.value.matches(Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$"""))) {
                    throw LexerException.invalidIdentifierFormat(token.value)
                }
            }
            TokenType.NUMBER -> {
                if (!token.value.matches(Regex("""^-?\d+(\.\d+)?$"""))) {
                    throw LexerException.invalidNumberFormat(token.value)
                }
            }
            else -> { /* 다른 타입들은 형식 검증 없음 */ }
        }
    }

    /**
     * 토큰 길이를 검증합니다.
     */
    private fun validateLength(token: Token) {
        when (token.type) {
            TokenType.IDENTIFIER -> {
                if (token.value.length > MAX_IDENTIFIER_LENGTH) {
                    throw LexerException.identifierTooLong(token.value.length, MAX_IDENTIFIER_LENGTH)
                }
            }

            TokenType.VARIABLE -> {
                if (token.value.length > MAX_VARIABLE_NAME_LENGTH) {
                    throw LexerException.variableNameTooLong(token.value.length, MAX_VARIABLE_NAME_LENGTH)
                }
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
                    throw LexerException.invalidOperatorSequence(current.value, next.value)
                }
            }
        }
        
        // EOF 토큰은 마지막에만 위치해야 함
        val eofTokens = tokens.filter { it.type == TokenType.DOLLAR }
        if (eofTokens.isNotEmpty()) {
            if (eofTokens.size != 1) {
                throw LexerException.multipleEofTokens(eofTokens.size)
            }

            if (tokens.last().type != TokenType.DOLLAR) {
                throw LexerException.eofNotAtEnd()
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