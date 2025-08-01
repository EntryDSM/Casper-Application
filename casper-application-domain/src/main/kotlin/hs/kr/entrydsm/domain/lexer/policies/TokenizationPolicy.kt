package hs.kr.entrydsm.domain.lexer.policies

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.values.Position

/**
 * POC 코드의 CalculatorLexer 기능을 DDD Policy 패턴으로 구현한 클래스입니다.
 *
 * POC 코드의 tokenize 메서드에서 제공하는 토큰화 규칙, 문자 인식 정책,
 * 키워드 매핑, 연산자 우선순위 등의 기능을 정책으로 캡슐화합니다.
 * 완전한 토큰화 프로세스에 대한 비즈니스 규칙을 관리합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
@Policy(
    name = "Tokenization",
    description = "POC 코드 기반의 토큰화 처리 정책",
    domain = "lexer",
    scope = Scope.DOMAIN
)
class TokenizationPolicy {

    companion object {
        // POC 코드의 CalculatorLexer에서 정의된 키워드들
        private val KEYWORDS = mapOf(
            "true" to TokenType.TRUE,
            "false" to TokenType.FALSE,
            "if" to TokenType.IF,
            "PI" to TokenType.VARIABLE,
            "E" to TokenType.VARIABLE,
            "ABS" to TokenType.IDENTIFIER,
            "SQRT" to TokenType.IDENTIFIER,
            "ROUND" to TokenType.IDENTIFIER,
            "MIN" to TokenType.IDENTIFIER,
            "MAX" to TokenType.IDENTIFIER,
            "SUM" to TokenType.IDENTIFIER,
            "AVG" to TokenType.IDENTIFIER,
            "AVERAGE" to TokenType.IDENTIFIER,
            "POW" to TokenType.IDENTIFIER,
            "LOG" to TokenType.IDENTIFIER,
            "LOG10" to TokenType.IDENTIFIER,
            "EXP" to TokenType.IDENTIFIER,
            "SIN" to TokenType.IDENTIFIER,
            "COS" to TokenType.IDENTIFIER,
            "TAN" to TokenType.IDENTIFIER,
            "ASIN" to TokenType.IDENTIFIER,
            "ACOS" to TokenType.IDENTIFIER,
            "ATAN" to TokenType.IDENTIFIER,
            "ATAN2" to TokenType.IDENTIFIER,
            "SINH" to TokenType.IDENTIFIER,
            "COSH" to TokenType.IDENTIFIER,
            "TANH" to TokenType.IDENTIFIER,
            "ASINH" to TokenType.IDENTIFIER,
            "ACOSH" to TokenType.IDENTIFIER,
            "ATANH" to TokenType.IDENTIFIER,
            "FLOOR" to TokenType.IDENTIFIER,
            "CEIL" to TokenType.IDENTIFIER,
            "CEILING" to TokenType.IDENTIFIER,
            "TRUNCATE" to TokenType.IDENTIFIER,
            "TRUNC" to TokenType.IDENTIFIER,
            "SIGN" to TokenType.IDENTIFIER,
            "RANDOM" to TokenType.IDENTIFIER,
            "RAND" to TokenType.IDENTIFIER,
            "RADIANS" to TokenType.IDENTIFIER,
            "DEGREES" to TokenType.IDENTIFIER,
            "MOD" to TokenType.IDENTIFIER,
            "GCD" to TokenType.IDENTIFIER,
            "LCM" to TokenType.IDENTIFIER,
            "FACTORIAL" to TokenType.IDENTIFIER,
            "COMBINATION" to TokenType.IDENTIFIER,
            "COMB" to TokenType.IDENTIFIER,
            "PERMUTATION" to TokenType.IDENTIFIER,
            "PERM" to TokenType.IDENTIFIER
        )

        // POC 코드의 연산자 매핑
        private val OPERATORS = mapOf(
            '+' to TokenType.PLUS,
            '-' to TokenType.MINUS,
            '*' to TokenType.MULTIPLY,
            '/' to TokenType.DIVIDE,
            '%' to TokenType.MODULO,
            '^' to TokenType.POWER,
            '(' to TokenType.LEFT_PAREN,
            ')' to TokenType.RIGHT_PAREN,
            ',' to TokenType.COMMA,
            '?' to TokenType.IDENTIFIER,
            ':' to TokenType.COMMA,
            '!' to TokenType.NOT
        )

        // POC 코드의 두 문자 연산자들
        private val TWO_CHAR_OPERATORS = mapOf(
            "==" to TokenType.EQUAL,
            "!=" to TokenType.NOT_EQUAL,
            "<=" to TokenType.LESS_EQUAL,
            ">=" to TokenType.GREATER_EQUAL,
            "&&" to TokenType.AND,
            "||" to TokenType.OR
        )

        // 토큰화 정책 상수들
        private const val MAX_TOKEN_LENGTH = 1000
        private const val MAX_NUMBER_PRECISION = 15
        private const val MAX_IDENTIFIER_LENGTH = 100
    }

    /**
     * POC 코드의 문자별 토큰화 정책 적용
     */
    fun applyCharacterTokenizationPolicy(char: Char, position: Int): TokenizationDecision {
        return when {
            char.isWhitespace() -> TokenizationDecision.SKIP
            char.isDigit() -> TokenizationDecision.START_NUMBER
            char.isLetter() || char == '_' -> TokenizationDecision.START_IDENTIFIER
            char == '.' -> TokenizationDecision.DECIMAL_POINT
            char in OPERATORS -> TokenizationDecision.SINGLE_CHAR_OPERATOR
            char == '<' || char == '>' -> TokenizationDecision.POSSIBLE_TWO_CHAR_OPERATOR
            char == '=' || char == '!' -> TokenizationDecision.POSSIBLE_TWO_CHAR_OPERATOR
            char == '&' || char == '|' -> TokenizationDecision.POSSIBLE_TWO_CHAR_OPERATOR
            else -> TokenizationDecision.INVALID_CHARACTER
        }
    }

    /**
     * POC 코드의 숫자 토큰 인식 정책
     */
    fun applyNumberTokenPolicy(numberString: String, position: Int): NumberTokenResult {
        if (numberString.length > MAX_TOKEN_LENGTH) {
            return NumberTokenResult.error(
                ErrorCodes.Lexer.INVALID_NUMBER_FORMAT.code,
                "숫자 토큰이 너무 깁니다: ${numberString.length}자"
            )
        }

        return try {
            val value = numberString.toDouble()
            
            // POC 코드의 숫자 유효성 검사
            when {
                !value.isFinite() -> NumberTokenResult.error(
                    ErrorCodes.Lexer.INVALID_NUMBER_FORMAT.code,
                    "유효하지 않은 숫자: $numberString"
                )
                numberString.count { it == '.' } > 1 -> NumberTokenResult.error(
                    ErrorCodes.Lexer.INVALID_NUMBER_FORMAT.code,
                    "소수점이 여러 개 포함됨: $numberString"
                )
                else -> NumberTokenResult.success(
                    Token(TokenType.NUMBER, numberString, Position(position, 1, position + 1))
                )
            }
        } catch (e: NumberFormatException) {
            NumberTokenResult.error(
                ErrorCodes.Lexer.INVALID_NUMBER_FORMAT.code,
                "숫자 변환 실패: $numberString"
            )
        }
    }

    /**
     * POC 코드의 식별자 토큰 인식 정책
     */
    fun applyIdentifierTokenPolicy(identifier: String, position: Int): IdentifierTokenResult {
        if (identifier.length > MAX_IDENTIFIER_LENGTH) {
            return IdentifierTokenResult.error(
                ErrorCodes.Lexer.INVALID_IDENTIFIER.code,
                "식별자가 너무 깁니다: ${identifier.length}자"
            )
        }

        if (!isValidIdentifier(identifier)) {
            return IdentifierTokenResult.error(
                ErrorCodes.Lexer.INVALID_IDENTIFIER.code,
                "유효하지 않은 식별자: $identifier"
            )
        }

        // POC 코드의 키워드 매핑 적용
        val tokenType = KEYWORDS[identifier.uppercase()] ?: TokenType.VARIABLE
        val token = Token(tokenType, identifier, Position(position, 1, position + 1))

        return IdentifierTokenResult.success(token)
    }

    /**
     * POC 코드의 연산자 토큰 인식 정책
     */
    fun applyOperatorTokenPolicy(
        currentChar: Char,
        nextChar: Char?,
        position: Int
    ): OperatorTokenResult {
        // 두 문자 연산자 우선 확인
        if (nextChar != null) {
            val twoCharOp = "$currentChar$nextChar"
            TWO_CHAR_OPERATORS[twoCharOp]?.let { tokenType ->
                return OperatorTokenResult.success(
                    Token(tokenType, twoCharOp, Position(position, 1, position + 1)),
                    consumedChars = 2
                )
            }
        }

        // 단일 문자 연산자 확인
        val tokenType = OPERATORS[currentChar]
        return if (tokenType != null) {
            OperatorTokenResult.success(
                Token(tokenType, currentChar.toString(), Position(position, 1, position + 1)),
                consumedChars = 1
            )
        } else {
            OperatorTokenResult.error(
                ErrorCodes.Lexer.INVALID_CHARACTER.code,
                "인식되지 않는 연산자: $currentChar"
            )
        }
    }

    /**
     * POC 코드의 토큰 시퀀스 유효성 검증 정책
     */
    fun applyTokenSequenceValidationPolicy(tokens: List<Token>): SequenceValidationResult {
        val errors = mutableListOf<String>()

        // 빈 토큰 시퀀스 검사
        if (tokens.isEmpty()) {
            errors.add("토큰 시퀀스가 비어있습니다")
            return SequenceValidationResult(false, errors)
        }

        // 연속된 연산자 검사
        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            
            if (isInvalidOperatorSequence(current, next)) {
                errors.add("유효하지 않은 연산자 시퀀스: ${current.value} ${next.value}")
            }
        }

        // 괄호 균형 검사
        val parenthesesBalance = validateParenthesesBalance(tokens)
        if (!parenthesesBalance.isValid) {
            errors.addAll(parenthesesBalance.errors)
        }

        // 함수 호출 구문 검사
        val functionValidation = validateFunctionCalls(tokens)
        if (!functionValidation.isValid) {
            errors.addAll(functionValidation.errors)
        }

        return SequenceValidationResult(errors.isEmpty(), errors)
    }

    /**
     * 토큰화 품질 평가 정책
     */
    fun evaluateTokenizationQuality(
        originalInput: String,
        tokens: List<Token>
    ): TokenizationQualityReport {
        val report = TokenizationQualityReport()

        // 토큰 밀도 계산
        report.tokenDensity = tokens.size.toDouble() / originalInput.length

        // 토큰 타입 분포 계산
        report.tokenTypeDistribution = tokens.groupBy { it.type }
            .mapValues { (_, tokens) -> tokens.size }

        // 복잡도 점수 계산
        report.complexityScore = calculateComplexityScore(tokens)

        // 파싱 난이도 평가
        report.parsingDifficulty = assessParsingDifficulty(tokens)

        // 최적화 권장사항
        report.optimizationRecommendations = generateOptimizationRecommendations(tokens)

        return report
    }

    // Private helper methods

    private fun isValidIdentifier(identifier: String): Boolean {
        if (identifier.isEmpty()) return false
        if (!identifier[0].isLetter() && identifier[0] != '_') return false
        
        return identifier.all { char ->
            char.isLetterOrDigit() || char == '_'
        }
    }

    private fun isInvalidOperatorSequence(current: Token, next: Token): Boolean {
        val binaryOperators = setOf(
            TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE,
            TokenType.MODULO, TokenType.POWER, TokenType.EQUAL, TokenType.NOT_EQUAL,
            TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER,
            TokenType.GREATER_EQUAL, TokenType.AND, TokenType.OR
        )

        // 연속된 이항 연산자는 허용되지 않음 (단항 마이너스 제외)
        return current.type in binaryOperators && 
               next.type in binaryOperators &&
               !(current.type == TokenType.LEFT_PAREN && next.type == TokenType.MINUS)
    }

    private fun validateParenthesesBalance(tokens: List<Token>): ValidationResult {
        var balance = 0
        val errors = mutableListOf<String>()

        for ((index, token) in tokens.withIndex()) {
            when (token.type) {
                TokenType.LEFT_PAREN -> balance++
                TokenType.RIGHT_PAREN -> {
                    balance--
                    if (balance < 0) {
                        errors.add("위치 ${index}에서 닫는 괄호가 여는 괄호보다 많습니다")
                    }
                }
                else -> { /* 다른 토큰은 무시 */ }
            }
        }

        if (balance > 0) {
            errors.add("$balance 개의 여는 괄호가 닫히지 않았습니다")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun validateFunctionCalls(tokens: List<Token>): ValidationResult {
        val errors = mutableListOf<String>()

        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]

            if (current.type == TokenType.IDENTIFIER && next.type != TokenType.LEFT_PAREN) {
                errors.add("함수 ${current.value} 다음에 여는 괄호가 없습니다")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun calculateComplexityScore(tokens: List<Token>): Int {
        var score = 0
        
        for (token in tokens) {
            score += when (token.type) {
                TokenType.NUMBER, TokenType.VARIABLE -> 1
                TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE -> 2
                TokenType.POWER, TokenType.MODULO -> 3
                TokenType.IDENTIFIER -> 4
                TokenType.IF, TokenType.IDENTIFIER, TokenType.COMMA -> 5
                else -> 1
            }
        }
        
        return score
    }

    private fun assessParsingDifficulty(tokens: List<Token>): ParsingDifficulty {
        val functionCount = tokens.count { it.type == TokenType.IDENTIFIER }
        val operatorCount = tokens.count { it.type in setOf(
            TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE,
            TokenType.POWER, TokenType.MODULO
        )}
        val conditionalCount = tokens.count { it.type in setOf(TokenType.IF, TokenType.IDENTIFIER) }

        return when {
            functionCount > 5 || conditionalCount > 2 -> ParsingDifficulty.VERY_HARD
            functionCount > 2 || operatorCount > 10 -> ParsingDifficulty.HARD
            operatorCount > 5 -> ParsingDifficulty.MEDIUM
            else -> ParsingDifficulty.EASY
        }
    }

    private fun generateOptimizationRecommendations(tokens: List<Token>): List<String> {
        val recommendations = mutableListOf<String>()

        val functionTokens = tokens.filter { it.type == TokenType.IDENTIFIER }
        if (functionTokens.size > 10) {
            recommendations.add("함수 호출이 많습니다 (${functionTokens.size}개). 중간 결과를 변수에 저장하는 것을 고려하세요.")
        }

        val powerOperations = tokens.count { it.type == TokenType.POWER }
        if (powerOperations > 3) {
            recommendations.add("거듭제곱 연산이 많습니다 ($powerOperations 개). 성능에 영향을 줄 수 있습니다.")
        }

        val parenthesesDepth = calculateMaxParenthesesDepth(tokens)
        if (parenthesesDepth > 5) {
            recommendations.add("괄호 중첩이 깊습니다 (깊이: $parenthesesDepth). 가독성을 위해 단순화를 고려하세요.")
        }

        return recommendations
    }

    private fun calculateMaxParenthesesDepth(tokens: List<Token>): Int {
        var maxDepth = 0
        var currentDepth = 0

        for (token in tokens) {
            when (token.type) {
                TokenType.LEFT_PAREN -> {
                    currentDepth++
                    maxDepth = maxOf(maxDepth, currentDepth)
                }
                TokenType.RIGHT_PAREN -> currentDepth--
                else -> { /* 무시 */ }
            }
        }

        return maxDepth
    }

    // Enums and Data Classes

    enum class TokenizationDecision {
        SKIP, START_NUMBER, START_IDENTIFIER, DECIMAL_POINT,
        SINGLE_CHAR_OPERATOR, POSSIBLE_TWO_CHAR_OPERATOR, INVALID_CHARACTER
    }

    enum class ParsingDifficulty {
        EASY, MEDIUM, HARD, VERY_HARD
    }

    data class NumberTokenResult(
        val success: Boolean,
        val token: Token? = null,
        val errorCode: String? = null,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success(token: Token) = NumberTokenResult(true, token)
            fun error(code: String, message: String) = NumberTokenResult(false, null, code, message)
        }
    }

    data class IdentifierTokenResult(
        val success: Boolean,
        val token: Token? = null,
        val errorCode: String? = null,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success(token: Token) = IdentifierTokenResult(true, token)
            fun error(code: String, message: String) = IdentifierTokenResult(false, null, code, message)
        }
    }

    data class OperatorTokenResult(
        val success: Boolean,
        val token: Token? = null,
        val consumedChars: Int = 1,
        val errorCode: String? = null,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success(token: Token, consumedChars: Int) = 
                OperatorTokenResult(true, token, consumedChars)
            fun error(code: String, message: String) = 
                OperatorTokenResult(false, null, 1, code, message)
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    data class SequenceValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    data class TokenizationQualityReport(
        var tokenDensity: Double = 0.0,
        var tokenTypeDistribution: Map<TokenType, Int> = emptyMap(),
        var complexityScore: Int = 0,
        var parsingDifficulty: ParsingDifficulty = ParsingDifficulty.EASY,
        var optimizationRecommendations: List<String> = emptyList()
    )

    /**
     * 정책의 설정 정보를 반환합니다.
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "name" to "TokenizationPolicy",
        "based_on" to "POC_CalculatorLexer",
        "maxTokenLength" to MAX_TOKEN_LENGTH,
        "maxNumberPrecision" to MAX_NUMBER_PRECISION,
        "maxIdentifierLength" to MAX_IDENTIFIER_LENGTH,
        "supportedKeywords" to KEYWORDS.size,
        "supportedOperators" to (OPERATORS.size + TWO_CHAR_OPERATORS.size),
        "features" to listOf(
            "character_tokenization", "number_recognition", "identifier_recognition",
            "operator_recognition", "sequence_validation", "quality_evaluation"
        )
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "TokenizationPolicy",
        "keywordCount" to KEYWORDS.size,
        "singleCharOperatorCount" to OPERATORS.size,
        "twoCharOperatorCount" to TWO_CHAR_OPERATORS.size,
        "supportedTokenTypes" to TokenType.values().size,
        "validationRules" to 3,
        "pocCompatibility" to true
    )
}