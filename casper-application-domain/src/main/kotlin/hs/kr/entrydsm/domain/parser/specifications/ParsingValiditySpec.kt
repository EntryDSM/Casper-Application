package hs.kr.entrydsm.domain.parser.specifications

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 구문 분석의 유효성을 검증하는 Specification 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 토큰 시퀀스의 구문적 유효성을
 * 검증하는 복잡한 비즈니스 로직을 캡슐화하고 조합 가능한 형태로
 * 구성합니다. 다양한 파싱 검증 규칙을 독립적으로 정의하고 조합할 수 있습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "ParsingValidity",
    description = "토큰 시퀀스의 구문적 유효성과 파싱 가능성을 검증하는 명세",
    domain = "parser",
    priority = Priority.HIGH
)
class ParsingValiditySpec {

    companion object {
        private const val MAX_TOKEN_SEQUENCE_LENGTH = 10000
        private const val MAX_NESTING_DEPTH = 100
        private const val MAX_EXPRESSION_COMPLEXITY = 500
    }

    /**
     * 토큰 시퀀스가 구문적으로 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 유효하면 true
     */
    fun isSatisfiedBy(tokens: List<Token>): Boolean {
        return hasValidLength(tokens) &&
               hasValidStructure(tokens) &&
               hasBalancedDelimiters(tokens) &&
               hasValidTokenOrder(tokens) &&
               hasValidNestingDepth(tokens) &&
               hasValidExpressionComplexity(tokens)
    }

    /**
     * 토큰 시퀀스가 완전한 표현식인지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 완전한 표현식이면 true
     */
    fun isCompleteExpression(tokens: List<Token>): Boolean {
        if (!isSatisfiedBy(tokens)) return false
        
        return hasValidStartAndEnd(tokens) &&
               hasNoIncompleteOperations(tokens) &&
               hasValidFunctionCalls(tokens)
    }

    /**
     * 토큰 시퀀스가 부분 표현식으로 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 부분 표현식으로 유효하면 true
     */
    fun isValidPartialExpression(tokens: List<Token>): Boolean {
        return hasValidLength(tokens) &&
               hasValidStructure(tokens) &&
               hasValidTokenOrder(tokens, allowIncomplete = true)
    }

    /**
     * 산술 표현식이 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 유효한 산술 표현식이면 true
     */
    fun isValidArithmeticExpression(tokens: List<Token>): Boolean {
        if (!isSatisfiedBy(tokens)) return false
        
        return hasValidArithmeticStructure(tokens) &&
               hasValidOperatorOperandPattern(tokens)
    }

    /**
     * 논리 표현식이 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 유효한 논리 표현식이면 true
     */
    fun isValidLogicalExpression(tokens: List<Token>): Boolean {
        if (!isSatisfiedBy(tokens)) return false
        
        return hasValidLogicalStructure(tokens) &&
               hasValidBooleanOperands(tokens)
    }

    /**
     * 함수 호출이 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 유효한 함수 호출이면 true
     */
    fun isValidFunctionCall(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return false
        
        return hasValidFunctionCallStructure(tokens) &&
               hasValidArgumentList(tokens)
    }

    /**
     * 조건 표현식이 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 시퀀스
     * @return 유효한 조건 표현식이면 true
     */
    fun isValidConditionalExpression(tokens: List<Token>): Boolean {
        if (!isSatisfiedBy(tokens)) return false
        
        return hasValidConditionalStructure(tokens) &&
               hasValidConditionAndBranches(tokens)
    }

    /**
     * 토큰 시퀀스의 길이가 유효한지 확인합니다.
     */
    private fun hasValidLength(tokens: List<Token>): Boolean {
        if (tokens.size > MAX_TOKEN_SEQUENCE_LENGTH) {
            throw IllegalArgumentException(
                "토큰 시퀀스가 최대 길이를 초과했습니다: ${tokens.size} > $MAX_TOKEN_SEQUENCE_LENGTH"
            )
        }
        return true
    }

    /**
     * 기본 구조가 유효한지 확인합니다.
     */
    private fun hasValidStructure(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return true
        
        // 첫 토큰과 마지막 토큰 검증
        val first = tokens.first()
        val last = tokens.last()
        
        // 연산자로 시작하면 안됨 (단항 연산자 제외)
        if (first.type.isOperator && !isUnaryOperator(first.type)) {
            return false
        }
        
        // 이항 연산자로 끝나면 안됨
        if (last.type.isOperator && isBinaryOperator(last.type)) {
            return false
        }
        
        return true
    }

    /**
     * 구분자가 균형을 이루는지 확인합니다.
     */
    private fun hasBalancedDelimiters(tokens: List<Token>): Boolean {
        var parenBalance = 0
        var braceBalance = 0
        
        for (token in tokens) {
            when (token.type) {
                TokenType.LEFT_PAREN -> parenBalance++
                TokenType.RIGHT_PAREN -> {
                    parenBalance--
                    if (parenBalance < 0) return false
                }
                // 필요시 다른 구분자들 추가
                else -> { /* 무시 */ }
            }
        }
        
        return parenBalance == 0 && braceBalance == 0
    }

    /**
     * 토큰 순서가 유효한지 확인합니다.
     */
    private fun hasValidTokenOrder(tokens: List<Token>, allowIncomplete: Boolean = false): Boolean {
        if (tokens.isEmpty()) return true
        
        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            
            if (!isValidTokenPair(current, next, allowIncomplete)) {
                return false
            }
        }
        
        return true
    }

    /**
     * 중첩 깊이가 유효한지 확인합니다.
     */
    private fun hasValidNestingDepth(tokens: List<Token>): Boolean {
        var depth = 0
        var maxDepth = 0
        
        for (token in tokens) {
            when (token.type) {
                TokenType.LEFT_PAREN -> {
                    depth++
                    maxDepth = maxOf(maxDepth, depth)
                }
                TokenType.RIGHT_PAREN -> depth--
                else -> { /* 무시 */ }
            }
            
            if (maxDepth > MAX_NESTING_DEPTH) {
                throw IllegalArgumentException(
                    "중첩 깊이가 최대값을 초과했습니다: $maxDepth > $MAX_NESTING_DEPTH"
                )
            }
        }
        
        return true
    }

    /**
     * 표현식 복잡도가 유효한지 확인합니다.
     */
    private fun hasValidExpressionComplexity(tokens: List<Token>): Boolean {
        val operatorCount = tokens.count { it.type.isOperator }
        val complexity = calculateComplexity(tokens)
        
        if (complexity > MAX_EXPRESSION_COMPLEXITY) {
            throw IllegalArgumentException(
                "표현식 복잡도가 최대값을 초과했습니다: $complexity > $MAX_EXPRESSION_COMPLEXITY"
            )
        }
        
        return true
    }

    /**
     * 시작과 끝이 유효한지 확인합니다.
     */
    private fun hasValidStartAndEnd(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return false
        
        val first = tokens.first()
        val last = tokens.last()
        
        // 유효한 시작 토큰들
        val validStarts = setOf(
            TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE,
            TokenType.TRUE, TokenType.FALSE, TokenType.LEFT_PAREN,
            TokenType.MINUS, TokenType.NOT // 단항 연산자들
        )
        
        // 유효한 끝 토큰들
        val validEnds = setOf(
            TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE,
            TokenType.TRUE, TokenType.FALSE, TokenType.RIGHT_PAREN
        )
        
        return first.type in validStarts && last.type in validEnds
    }

    /**
     * 불완전한 연산이 없는지 확인합니다.
     */
    private fun hasNoIncompleteOperations(tokens: List<Token>): Boolean {
        // 연속된 연산자 검사
        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            
            if (current.type.isOperator && next.type.isOperator) {
                // 단항 연산자 다음의 경우는 허용
                if (!isUnaryOperator(next.type)) {
                    return false
                }
            }
        }
        
        return true
    }

    /**
     * 함수 호출이 유효한지 확인합니다.
     */
    private fun hasValidFunctionCalls(tokens: List<Token>): Boolean {
        var i = 0
        while (i < tokens.size) {
            if (tokens[i].type == TokenType.IDENTIFIER && 
                i + 1 < tokens.size && 
                tokens[i + 1].type == TokenType.LEFT_PAREN) {
                
                // 함수 호출 구조 검증
                val closeIndex = findMatchingParen(tokens, i + 1)
                if (closeIndex == -1) return false
                
                val argsTokens = tokens.subList(i + 2, closeIndex)
                if (!hasValidArgumentList(argsTokens)) return false
                
                i = closeIndex + 1
            } else {
                i++
            }
        }
        
        return true
    }

    /**
     * 산술 표현식 구조가 유효한지 확인합니다.
     */
    private fun hasValidArithmeticStructure(tokens: List<Token>): Boolean {
        val arithmeticTokens = setOf(
            TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE,
            TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY,
            TokenType.DIVIDE, TokenType.POWER, TokenType.MODULO,
            TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN
        )
        
        return tokens.all { it.type in arithmeticTokens }
    }

    /**
     * 연산자-피연산자 패턴이 유효한지 확인합니다.
     */
    private fun hasValidOperatorOperandPattern(tokens: List<Token>): Boolean {
        var expectingOperand = true // 시작은 피연산자를 기대
        
        for (token in tokens) {
            when {
                token.type in setOf(TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE) -> {
                    if (!expectingOperand) return false
                    expectingOperand = false
                }
                token.type in setOf(TokenType.PLUS, TokenType.MINUS, 
                                   TokenType.MULTIPLY, TokenType.DIVIDE, 
                                   TokenType.POWER, TokenType.MODULO) -> {
                    if (expectingOperand && !isUnaryOperator(token.type)) return false
                    expectingOperand = true
                }
                token.type == TokenType.LEFT_PAREN -> {
                    // 괄호는 패턴을 변경하지 않음
                }
                token.type == TokenType.RIGHT_PAREN -> {
                    if (expectingOperand) return false
                    expectingOperand = false
                }
            }
        }
        
        return !expectingOperand // 마지막은 피연산자여야 함
    }

    /**
     * 논리 표현식 구조가 유효한지 확인합니다.
     */
    private fun hasValidLogicalStructure(tokens: List<Token>): Boolean {
        val logicalTokens = setOf(
            TokenType.TRUE, TokenType.FALSE, TokenType.IDENTIFIER, TokenType.VARIABLE,
            TokenType.AND, TokenType.OR, TokenType.NOT,
            TokenType.EQUAL, TokenType.NOT_EQUAL,
            TokenType.LESS, TokenType.LESS_EQUAL,
            TokenType.GREATER, TokenType.GREATER_EQUAL,
            TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN
        )
        
        return tokens.any { it.type in logicalTokens }
    }

    /**
     * 불린 피연산자가 유효한지 확인합니다.
     */
    private fun hasValidBooleanOperands(tokens: List<Token>): Boolean {
        // 간단한 검증: 논리 연산자 앞뒤에 적절한 피연산자가 있는지 확인
        for (i in tokens.indices) {
            val token = tokens[i]
            if (token.type in setOf(TokenType.AND, TokenType.OR)) {
                // 앞뒤로 유효한 피연산자가 있는지 확인
                val hasPrevOperand = i > 0 && isValidLogicalOperand(tokens[i - 1])
                val hasNextOperand = i < tokens.size - 1 && isValidLogicalOperand(tokens[i + 1])
                
                if (!hasPrevOperand || !hasNextOperand) return false
            }
        }
        
        return true
    }

    /**
     * 함수 호출 구조가 유효한지 확인합니다.
     */
    private fun hasValidFunctionCallStructure(tokens: List<Token>): Boolean {
        if (tokens.size < 3) return false // 최소: identifier ( )
        
        return tokens[0].type == TokenType.IDENTIFIER &&
               tokens[1].type == TokenType.LEFT_PAREN &&
               tokens.last().type == TokenType.RIGHT_PAREN
    }

    /**
     * 인수 목록이 유효한지 확인합니다.
     */
    private fun hasValidArgumentList(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return true // 빈 인수 목록은 유효
        
        var expectingArg = true
        for (token in tokens) {
            when (token.type) {
                TokenType.COMMA -> {
                    if (expectingArg) return false
                    expectingArg = true
                }
                in setOf(TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE,
                        TokenType.TRUE, TokenType.FALSE) -> {
                    if (!expectingArg) return false
                    expectingArg = false
                }
                TokenType.LEFT_PAREN -> { /* 중첩 함수 호출 허용 */ }
                TokenType.RIGHT_PAREN -> { /* 중첩 함수 호출 허용 */ }
                else -> { /* ignore other tokens for argument validation */ }
            }
        }
        
        return !expectingArg // 마지막은 인수여야 함
    }

    /**
     * 조건 표현식 구조가 유효한지 확인합니다.
     */
    private fun hasValidConditionalStructure(tokens: List<Token>): Boolean {
        return tokens.any { it.type == TokenType.IF }
    }

    /**
     * 조건과 분기가 유효한지 확인합니다.
     */
    private fun hasValidConditionAndBranches(tokens: List<Token>): Boolean {
        // IF ( condition , true_branch , false_branch ) 구조 검증
        val ifIndex = tokens.indexOfFirst { it.type == TokenType.IF }
        if (ifIndex == -1) return false
        
        if (ifIndex + 1 >= tokens.size || tokens[ifIndex + 1].type != TokenType.LEFT_PAREN) {
            return false
        }
        
        val closeIndex = findMatchingParen(tokens, ifIndex + 1)
        if (closeIndex == -1) return false
        
        val innerTokens = tokens.subList(ifIndex + 2, closeIndex)
        val commaIndices = innerTokens.mapIndexedNotNull { index, token ->
            if (token.type == TokenType.COMMA) index else null
        }
        
        // 정확히 2개의 쉼표가 있어야 함 (3개 부분으로 나뉨)
        return commaIndices.size == 2
    }

    // Helper methods

    private fun isUnaryOperator(type: TokenType): Boolean {
        return type in setOf(TokenType.MINUS, TokenType.NOT, TokenType.PLUS)
    }

    private fun isBinaryOperator(type: TokenType): Boolean {
        return type.isOperator && !isUnaryOperator(type)
    }

    private fun isValidTokenPair(current: Token, next: Token, allowIncomplete: Boolean): Boolean {
        // 기본적인 토큰 쌍 유효성 검사
        val currentType = current.type
        val nextType = next.type
        
        // 연속된 피연산자는 허용하지 않음
        if (isOperand(currentType) && isOperand(nextType)) {
            return false
        }
        
        // 이항 연산자 뒤에는 피연산자가 와야 함
        if (isBinaryOperator(currentType) && !isOperand(nextType) && nextType != TokenType.LEFT_PAREN) {
            return allowIncomplete
        }
        
        return true
    }

    private fun isOperand(type: TokenType): Boolean {
        return type in setOf(
            TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE,
            TokenType.TRUE, TokenType.FALSE
        )
    }

    private fun isValidLogicalOperand(token: Token): Boolean {
        return token.type in setOf(
            TokenType.TRUE, TokenType.FALSE, TokenType.IDENTIFIER, 
            TokenType.VARIABLE, TokenType.RIGHT_PAREN
        )
    }

    private fun findMatchingParen(tokens: List<Token>, startIndex: Int): Int {
        var depth = 1
        for (i in startIndex + 1 until tokens.size) {
            when (tokens[i].type) {
                TokenType.LEFT_PAREN -> depth++
                TokenType.RIGHT_PAREN -> {
                    depth--
                    if (depth == 0) return i
                }
                else -> { /* ignore other tokens */ }
            }
        }
        return -1
    }

    private fun calculateComplexity(tokens: List<Token>): Int {
        var complexity = 0
        var nestingLevel = 0
        
        for (token in tokens) {
            when (token.type) {
                TokenType.LEFT_PAREN -> {
                    nestingLevel++
                    complexity += nestingLevel
                }
                TokenType.RIGHT_PAREN -> nestingLevel--
                in setOf(TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY,
                        TokenType.DIVIDE, TokenType.POWER, TokenType.MODULO) -> {
                    complexity += 1 + nestingLevel
                }
                in setOf(TokenType.AND, TokenType.OR) -> {
                    complexity += 2 + nestingLevel
                }
                TokenType.IF -> complexity += 5 + nestingLevel
                else -> { /* ignore other tokens for complexity calculation */ }
            }
        }
        
        return complexity
    }

    /**
     * 명세의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getSpecificationInfo(): Map<String, Any> = mapOf(
        "name" to "ParsingValiditySpec",
        "maxTokenSequenceLength" to MAX_TOKEN_SEQUENCE_LENGTH,
        "maxNestingDepth" to MAX_NESTING_DEPTH,
        "maxExpressionComplexity" to MAX_EXPRESSION_COMPLEXITY,
        "supportedValidations" to listOf(
            "length", "structure", "balancedDelimiters", "tokenOrder",
            "nestingDepth", "expressionComplexity", "completeness",
            "arithmeticExpression", "logicalExpression", "functionCall",
            "conditionalExpression"
        )
    )
}