package hs.kr.entrydsm.domain.lexer.specifications

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 토큰 검증을 위한 비즈니스 규칙을 정의하는 Specification 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 토큰의 유효성을 검증하는 복잡한
 * 비즈니스 로직을 캡슐화하고 조합 가능한 형태로 구성합니다.
 * 다양한 토큰 검증 규칙을 독립적으로 정의하고 조합할 수 있습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "TokenValidation",
    description = "토큰의 구조적 무결성과 비즈니스 규칙 준수를 검증하는 명세",
    domain = "lexer",
    priority = Priority.HIGH
)
class TokenValidationSpec {

    companion object {
        // 스펙 관련 상수들
        private const val SPEC_NAME = "TokenValidationSpec"
        private const val MAX_IDENTIFIER_LENGTH_VALUE = 255
        private const val MAX_VARIABLE_LENGTH_VALUE = 100
        private const val MAX_NUMBER_LENGTH_VALUE = 50

        // 스펙 키 상수들
        private const val SPEC_KEY_NAME = "name"
        private const val SPEC_KEY_SUPPORTED_TOKEN_TYPES = "supportedTokenTypes"
        private const val SPEC_KEY_VALIDATION_RULES = "validationRules"
        private const val SPEC_KEY_MAX_IDENTIFIER_LENGTH = "maxIdentifierLength"
        private const val SPEC_KEY_MAX_VARIABLE_LENGTH = "maxVariableLength"
        private const val SPEC_KEY_MAX_NUMBER_LENGTH = "maxNumberLength"

        // 검증 규칙 상수들
        private const val RULE_HAS_VALID_STRUCTURE = "hasValidStructure"
        private const val RULE_HAS_CONSISTENT_TYPE_AND_VALUE = "hasConsistentTypeAndValue"
        private const val RULE_HAS_VALID_LENGTH = "hasValidLength"
        private const val RULE_FOLLOWS_NAMING_CONVENTIONS = "followsNamingConventions"

        private val ALL_VALIDATION_RULES = listOf(
            RULE_HAS_VALID_STRUCTURE,
            RULE_HAS_CONSISTENT_TYPE_AND_VALUE,
            RULE_HAS_VALID_LENGTH,
            RULE_FOLLOWS_NAMING_CONVENTIONS
        )
    }

    /**
     * 토큰이 유효한지 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효하면 true
     */
    fun isSatisfiedBy(token: Token): Boolean {
        return hasValidStructure(token) &&
               hasConsistentTypeAndValue(token) &&
               hasValidLength(token) &&
               followsNamingConventions(token)
    }

    /**
     * 토큰이 특정 타입의 유효한 토큰인지 검증합니다.
     *
     * @param token 검증할 토큰
     * @param expectedType 기대하는 토큰 타입
     * @return 유효하면 true
     */
    fun isValidTokenOfType(token: Token, expectedType: TokenType): Boolean {
        return token.type == expectedType && isSatisfiedBy(token)
    }

    /**
     * 토큰이 유효한 리터럴인지 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효한 리터럴이면 true
     */
    fun isValidLiteral(token: Token): Boolean {
        if (!token.type.isLiteral) return false
        
        return when (token.type) {
            TokenType.NUMBER -> isValidNumberLiteral(token)
            TokenType.TRUE, TokenType.FALSE -> isValidBooleanLiteral(token)
            TokenType.IDENTIFIER -> isValidIdentifierLiteral(token)
            TokenType.VARIABLE -> isValidVariableLiteral(token)
            else -> false
        }
    }

    /**
     * 토큰이 유효한 연산자인지 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효한 연산자이면 true
     */
    fun isValidOperator(token: Token): Boolean {
        if (!token.type.isOperator) return false
        
        return when (token.type) {
            TokenType.PLUS, TokenType.MINUS -> isValidArithmeticOperator(token)
            TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.POWER, TokenType.MODULO -> 
                isValidArithmeticOperator(token)
            TokenType.EQUAL, TokenType.NOT_EQUAL -> isValidComparisonOperator(token)
            TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL -> 
                isValidComparisonOperator(token)
            TokenType.AND, TokenType.OR, TokenType.NOT -> isValidLogicalOperator(token)
            else -> false
        }
    }

    /**
     * 토큰이 유효한 키워드인지 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효한 키워드이면 true
     */
    fun isValidKeyword(token: Token): Boolean {
        if (!token.type.isKeyword) return false
        
        val expectedValues = mapOf(
            TokenType.IF to setOf("if"),
            TokenType.TRUE to setOf("true"),
            TokenType.FALSE to setOf("false"),
            TokenType.AND to setOf("and", "&&"),
            TokenType.OR to setOf("or", "||"),
            TokenType.NOT to setOf("not", "!")
        )
        
        val allowedValues = expectedValues[token.type] ?: return false
        return allowedValues.any { it.equals(token.value, ignoreCase = true) }
    }

    /**
     * 토큰이 유효한 구분자인지 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효한 구분자이면 true
     */
    fun isValidDelimiter(token: Token): Boolean {
        val validDelimiters = mapOf(
            TokenType.LEFT_PAREN to "(",
            TokenType.RIGHT_PAREN to ")",
            TokenType.COMMA to ","
        )
        
        return validDelimiters[token.type] == token.value
    }

    /**
     * 토큰이 비어있지 않은 유효한 구조를 가지는지 검증합니다.
     */
    private fun hasValidStructure(token: Token): Boolean {
        return token.value.isNotEmpty() || token.type == TokenType.DOLLAR
    }

    /**
     * 토큰 타입과 값이 일관성을 가지는지 검증합니다.
     */
    private fun hasConsistentTypeAndValue(token: Token): Boolean {
        return when (token.type) {
            TokenType.NUMBER -> token.value.toDoubleOrNull() != null
            TokenType.TRUE -> token.value.equals("true", ignoreCase = true)
            TokenType.FALSE -> token.value.equals("false", ignoreCase = true)
            TokenType.DOLLAR -> token.value == "$"
            TokenType.LEFT_PAREN -> token.value == "("
            TokenType.RIGHT_PAREN -> token.value == ")"
            TokenType.COMMA -> token.value == ","
            else -> true // 다른 타입들은 별도 검증 로직에서 처리
        }
    }

    /**
     * 토큰이 적절한 길이를 가지는지 검증합니다.
     */
    private fun hasValidLength(token: Token): Boolean {
        val maxLengths = mapOf(
            TokenType.IDENTIFIER to 255,
            TokenType.VARIABLE to 100,
            TokenType.NUMBER to 50
        )
        
        val maxLength = maxLengths[token.type] ?: 1000
        return token.value.length <= maxLength
    }

    /**
     * 토큰이 명명 규칙을 따르는지 검증합니다.
     */
    private fun followsNamingConventions(token: Token): Boolean {
        return when (token.type) {
            TokenType.IDENTIFIER, TokenType.VARIABLE -> 
                token.value.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))
            else -> true
        }
    }

    /**
     * 유효한 숫자 리터럴인지 검증합니다.
     */
    private fun isValidNumberLiteral(token: Token): Boolean {
        return try {
            val value = token.value.toDouble()
            value.isFinite() && 
            token.value.matches(Regex("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$"))
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * 유효한 불린 리터럴인지 검증합니다.
     */
    private fun isValidBooleanLiteral(token: Token): Boolean {
        return token.value.lowercase() in setOf("true", "false")
    }

    /**
     * 유효한 식별자 리터럴인지 검증합니다.
     */
    private fun isValidIdentifierLiteral(token: Token): Boolean {
        return token.value.isNotEmpty() &&
               token.value.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) &&
               !isReservedWord(token.value)
    }

    /**
     * 유효한 변수 리터럴인지 검증합니다.
     */
    private fun isValidVariableLiteral(token: Token): Boolean {
        return token.value.isNotEmpty() &&
               token.value.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) &&
               token.value.length <= 100
    }

    /**
     * 유효한 산술 연산자인지 검증합니다.
     */
    private fun isValidArithmeticOperator(token: Token): Boolean {
        val validOperators = setOf("+", "-", "*", "/", "^", "%")
        return token.value in validOperators
    }

    /**
     * 유효한 비교 연산자인지 검증합니다.
     */
    private fun isValidComparisonOperator(token: Token): Boolean {
        val validOperators = setOf("==", "!=", "<", "<=", ">", ">=")
        return token.value in validOperators
    }

    /**
     * 유효한 논리 연산자인지 검증합니다.
     */
    private fun isValidLogicalOperator(token: Token): Boolean {
        val validOperators = setOf("&&", "||", "!", "and", "or", "not")
        return token.value.lowercase() in validOperators
    }

    /**
     * 예약어인지 확인합니다.
     */
    private fun isReservedWord(value: String): Boolean {
        val reservedWords = setOf(
            "if", "then", "else", "endif",
            "true", "false",
            "and", "or", "not",
            "mod", "div",
            "let", "var", "const",
            "function", "return",
            "while", "for", "break", "continue"
        )
        return value.lowercase() in reservedWords
    }

    /**
     * 토큰 목록의 전체적인 유효성을 검증합니다.
     *
     * @param tokens 검증할 토큰 목록
     * @return 모든 토큰이 유효하면 true
     */
    fun areAllTokensValid(tokens: List<Token>): Boolean {
        return tokens.all { isSatisfiedBy(it) } &&
               hasValidTokenSequence(tokens)
    }

    /**
     * 토큰 시퀀스가 유효한지 검증합니다.
     */
    private fun hasValidTokenSequence(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return true
        
        // 연속된 연산자 검증
        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            
            if (current.type.isOperator && next.type.isOperator) {
                if (!isValidOperatorSequence(current, next)) {
                    return false
                }
            }
        }
        
        // 괄호 균형 검증
        if (!hasBalancedParentheses(tokens)) {
            return false
        }
        
        // EOF 토큰 위치 검증
        val eofTokens = tokens.filter { it.type == TokenType.DOLLAR }
        if (eofTokens.size > 1) return false
        if (eofTokens.size == 1 && tokens.last().type != TokenType.DOLLAR) return false
        
        return true
    }

    /**
     * 연속된 연산자가 유효한 조합인지 검증합니다.
     */
    private fun isValidOperatorSequence(first: Token, second: Token): Boolean {
        // 단항 연산자(!, -, +) 뒤에는 다른 연산자가 올 수 있음
        val unaryOperators = setOf(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)
        return first.type in unaryOperators
    }

    /**
     * 괄호가 균형을 이루는지 검증합니다.
     */
    private fun hasBalancedParentheses(tokens: List<Token>): Boolean {
        var balance = 0
        
        for (token in tokens) {
            when (token.type) {
                TokenType.LEFT_PAREN -> balance++
                TokenType.RIGHT_PAREN -> {
                    balance--
                    if (balance < 0) return false
                }
                else -> { /* 다른 토큰은 무시 */ }
            }
        }
        
        return balance == 0
    }

    /**
     * 명세의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getSpecificationInfo(): Map<String, Any> = mapOf(
        SPEC_KEY_NAME to SPEC_NAME,
        SPEC_KEY_SUPPORTED_TOKEN_TYPES to TokenType.values().map { it.name },
        SPEC_KEY_VALIDATION_RULES to ALL_VALIDATION_RULES,
        SPEC_KEY_MAX_IDENTIFIER_LENGTH to MAX_IDENTIFIER_LENGTH_VALUE,
        SPEC_KEY_MAX_VARIABLE_LENGTH to MAX_VARIABLE_LENGTH_VALUE,
        SPEC_KEY_MAX_NUMBER_LENGTH to MAX_NUMBER_LENGTH_VALUE
    )
}