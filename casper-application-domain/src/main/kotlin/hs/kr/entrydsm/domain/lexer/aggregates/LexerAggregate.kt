package hs.kr.entrydsm.domain.lexer.aggregates

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenPosition
import hs.kr.entrydsm.domain.lexer.exceptions.LexerException
import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.domain.lexer.factories.TokenFactory
import hs.kr.entrydsm.domain.lexer.contract.LexerContract
import hs.kr.entrydsm.domain.lexer.policies.CharacterRecognitionPolicy
import hs.kr.entrydsm.domain.lexer.policies.TokenValidationPolicy
import hs.kr.entrydsm.domain.lexer.specifications.InputValiditySpec
import hs.kr.entrydsm.domain.lexer.specifications.TokenValidationSpec
import hs.kr.entrydsm.domain.lexer.values.LexingContext
import hs.kr.entrydsm.domain.lexer.values.LexingResult
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import hs.kr.entrydsm.global.values.Position

/**
 * Lexer 도메인의 핵심 Aggregate Root입니다.
 *
 * DDD Aggregate 패턴을 적용하여 어휘 분석의 모든 비즈니스 로직과
 * 규칙을 캡슐화합니다. 입력 텍스트를 토큰 스트림으로 변환하는
 * 핵심 책임을 가지며, 일관성 경계를 유지합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Aggregate(context = "lexer")
class LexerAggregate : LexerContract {

    private val tokenFactory = TokenFactory()
    private val tokenValidationPolicy = TokenValidationPolicy()
    private val characterRecognitionPolicy = CharacterRecognitionPolicy()
    private val inputValiditySpec = InputValiditySpec()
    private val tokenValidationSpec = TokenValidationSpec()
    
    private var debugMode: Boolean = false
    private var errorRecoveryMode: Boolean = true
    private var statistics = mutableMapOf<String, Any>()

    init {
        resetStatistics()
    }

    /**
     * 주어진 입력 텍스트를 어휘 분석하여 토큰 목록을 생성합니다.
     */
    override fun tokenize(input: String): LexingResult {
        val context = LexingContext.of(input)
        return tokenize(context)
    }

    /**
     * 컨텍스트를 사용하여 어휘 분석을 수행합니다.
     */
    override fun tokenize(context: LexingContext): LexingResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // 입력 유효성 검증
            if (!inputValiditySpec.isValidContext(context)) {
                throw LexerException(ErrorCode.VALIDATION_FAILED)
            }

            val tokens = mutableListOf<Token>()
            var currentContext = context

            // 토큰화 수행
            while (currentContext.hasNext()) {
                val (token, newContext) = nextToken(currentContext)
                
                if (token != null) {
                    // 토큰 검증
                    if (tokenValidationSpec.isSatisfiedBy(token)) {
                        tokens.add(token)
                        updateStatistics("tokensGenerated", (statistics["tokensGenerated"] as Int) + 1)
                    }
                }
                
                currentContext = newContext
            }

            // EOF 토큰 추가
            val eofToken = createEOFToken(currentContext)
            tokens.add(eofToken)

            val duration = System.currentTimeMillis() - startTime
            updateStatistics("totalProcessingTime", (statistics["totalProcessingTime"] as Long) + duration)
            updateStatistics("inputsProcessed", (statistics["inputsProcessed"] as Int) + 1)

            return LexingResult.success(tokens, duration, context.input.length)

        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            val lexerException = if (e is LexerException) e else
                LexerException(ErrorCode.UNKNOWN_ERROR, message = "어휘 분석 중 오류 발생: ${e.message}", cause = e)
            
            updateStatistics("errorsOccurred", (statistics["errorsOccurred"] as Int) + 1)
            
            return LexingResult.failure(lexerException, emptyList(), duration, context.input.length)
        }
    }

    /**
     * 입력 텍스트에서 다음 토큰 하나를 추출합니다.
     */
    override fun nextToken(context: LexingContext): Pair<Token?, LexingContext> {
        var currentContext = skipWhitespace(context)
        
        if (!currentContext.hasNext()) {
            return null to currentContext
        }

        val currentChar = currentContext.currentChar ?: return null to currentContext
        
        return when {
            characterRecognitionPolicy.isDigit(currentChar) -> parseNumber(currentContext)
            characterRecognitionPolicy.isIdentifierStart(currentChar) -> parseIdentifier(currentContext)
            characterRecognitionPolicy.isVariableDelimiter(currentChar) && currentChar == '{' -> parseVariable(currentContext)
            characterRecognitionPolicy.isOperatorStart(currentChar) -> parseOperator(currentContext)
            characterRecognitionPolicy.isDelimiter(currentChar) -> parseDelimiter(currentContext)
            else -> throw LexerException.unexpectedCharacter(currentChar, currentContext.currentPosition.index)
        }
    }

    /**
     * 현재 위치에서 미리 보기(lookahead) 토큰을 반환합니다.
     */
    override fun peekTokens(context: LexingContext, count: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        var currentContext = context
        
        repeat(count) {
            if (currentContext.hasNext()) {
                val (token, newContext) = nextToken(currentContext)
                if (token != null) {
                    tokens.add(token)
                    currentContext = newContext
                }
            }
        }
        
        return tokens
    }

    /**
     * 주어진 입력이 유효한 토큰들로 구성되어 있는지 검증합니다.
     */
    override fun validate(input: String): Boolean {
        return try {
            inputValiditySpec.isSatisfiedBy(input)
            val result = tokenize(input)
            result.isSuccess && tokenValidationSpec.areAllTokensValid(result.tokens)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 주어진 컨텍스트가 유효한 상태인지 검증합니다.
     */
    override fun validateContext(context: LexingContext): Boolean {
        return inputValiditySpec.isValidContext(context)
    }

    /**
     * 현재 위치가 특정 토큰 타입으로 시작하는지 확인합니다.
     */
    override fun isTokenTypeAt(context: LexingContext, vararg expectedTypes: TokenType): Boolean {
        if (!context.hasNext()) return false
        
        val (token, _) = nextToken(context)
        return token?.type in expectedTypes
    }

    /**
     * 공백 문자들을 건너뛰고 다음 유효한 위치로 이동합니다.
     */
    override fun skipWhitespace(context: LexingContext): LexingContext {
        if (!context.skipWhitespace) return context
        
        var currentContext = context
        
        while (currentContext.hasNext()) {
            val currentChar = currentContext.currentChar
            if (currentChar != null && characterRecognitionPolicy.isWhitespace(currentChar)) {
                currentContext = currentContext.advance()
            } else {
                break
            }
        }
        
        return currentContext
    }

    /**
     * 주석을 건너뛰고 다음 유효한 위치로 이동합니다.
     */
    override fun skipComments(context: LexingContext): LexingContext {
        var currentContext = context
        
        while (currentContext.hasNext()) {
            val currentChar = currentContext.currentChar ?: break
            
            if (characterRecognitionPolicy.isCommentStart(currentChar)) {
                currentContext = when (currentChar) {
                    '/' -> {
                        val nextChar = currentContext.peekChar()
                        when (nextChar) {
                            '/' -> skipLineComment(currentContext)
                            '*' -> skipBlockComment(currentContext)
                            else -> break
                        }
                    }
                    '#' -> skipLineComment(currentContext)
                    else -> break
                }
            } else {
                break
            }
        }
        
        return currentContext
    }

    /**
     * 현재 위치에서 EOF 토큰을 생성합니다.
     */
    override fun createEOFToken(context: LexingContext): Token {
        return tokenFactory.createEOFToken(context.currentPosition)
    }

    /**
     * Lexer의 현재 설정 정보를 반환합니다.
     */
    override fun getConfiguration(): Map<String, Any> = mapOf(
        "debugMode" to debugMode,
        "errorRecoveryMode" to errorRecoveryMode,
        "characterRecognitionPolicy" to characterRecognitionPolicy.getConfiguration(),
        "tokenValidationPolicy" to tokenValidationPolicy.getConfiguration(),
        "inputValiditySpec" to inputValiditySpec.getConfiguration()
    )

    /**
     * Lexer를 초기 상태로 재설정합니다.
     */
    override fun reset() {
        resetStatistics()
        debugMode = false
        errorRecoveryMode = true
    }

    /**
     * 분석 통계 정보를 반환합니다.
     */
    override fun getStatistics(): Map<String, Any> = statistics.toMap()

    /**
     * 디버그 모드 여부를 설정합니다.
     */
    override fun setDebugMode(enabled: Boolean) {
        this.debugMode = enabled
    }

    /**
     * 오류 복구 모드를 설정합니다.
     */
    override fun setErrorRecoveryMode(enabled: Boolean) {
        this.errorRecoveryMode = enabled
    }

    /**
     * 스트리밍 모드로 토큰을 하나씩 생성하는 시퀀스를 반환합니다.
     */
    override fun tokenizeAsSequence(input: String): Sequence<Token> = sequence {
        var context = LexingContext.of(input)
        
        while (context.hasNext()) {
            val (token, newContext) = nextToken(context)
            if (token != null) {
                yield(token)
            }
            context = newContext
        }
        
        yield(createEOFToken(context))
    }

    /**
     * 비동기적으로 어휘 분석을 수행합니다.
     */
    override fun tokenizeAsync(input: String, callback: (LexingResult) -> Unit) {
        Thread {
            try {
                val result = tokenize(input)
                callback(result)
            } catch (e: Exception) {
                val error = LexerException(ErrorCode.UNKNOWN_ERROR, message = "비동기 분석 중 오류 발생: ${e.message}", cause = e)
                callback(LexingResult.failure(error))
            }
        }.start()
    }

    /**
     * 부분 입력에 대한 증분 분석을 수행합니다.
     */
    override fun incrementalTokenize(
        previousResult: LexingResult,
        newInput: String,
        changeStartIndex: Int
    ): LexingResult {
        // 간단한 구현: 전체 재분석
        // 실제로는 변경된 부분만 재분석하는 최적화 필요
        return tokenize(newInput)
    }

    // Private helper methods

    private fun parseNumber(context: LexingContext): Pair<Token, LexingContext> {
        val startPosition = context.currentPosition
        val value = StringBuilder()
        var currentContext = context
        
        while (currentContext.hasNext()) {
            val char = currentContext.currentChar
            if (char != null && characterRecognitionPolicy.isValidInNumber(char, value.isEmpty())) {
                value.append(char)
                currentContext = currentContext.advance()
            } else {
                break
            }
        }
        
        val token = tokenFactory.createNumberToken(value.toString(), startPosition)
        return token to currentContext
    }

    private fun parseIdentifier(context: LexingContext): Pair<Token, LexingContext> {
        val startPosition = context.currentPosition
        val value = StringBuilder()
        var currentContext = context
        
        while (currentContext.hasNext()) {
            val char = currentContext.currentChar
            if (char != null && characterRecognitionPolicy.isIdentifierBody(char)) {
                value.append(char)
                currentContext = currentContext.advance()
            } else {
                break
            }
        }
        
        val token = tokenFactory.createIdentifierToken(value.toString(), startPosition)
        return token to currentContext
    }

    private fun parseVariable(context: LexingContext): Pair<Token, LexingContext> {
        val startPosition = context.currentPosition
        var currentContext = context.advance() // '{' 건너뛰기
        
        val value = StringBuilder()
        while (currentContext.hasNext()) {
            val char = currentContext.currentChar
            if (char == null) {
                throw LexerException(ErrorCode.VALIDATION_FAILED, message = "변수 종료 전에 입력이 끝났습니다")
            } else if (char == '}') {
                currentContext = currentContext.advance() // '}' 건너뛰기
                break
            } else if (characterRecognitionPolicy.isIdentifierBody(char)) {
                value.append(char)
                currentContext = currentContext.advance()
            } else {
                throw LexerException.unexpectedCharacter(char, currentContext.currentPosition.index)
            }
        }
        
        if (value.isEmpty()) {
            throw LexerException(ErrorCode.VALIDATION_FAILED, message = "빈 변수명입니다")
        }
        
        val token = tokenFactory.createVariableToken(value.toString(), startPosition)
        return token to currentContext
    }

    private fun parseOperator(context: LexingContext): Pair<Token, LexingContext> {
        val startPosition = context.currentPosition
        val currentChar = context.currentChar ?: throw LexerException(ErrorCode.VALIDATION_FAILED, message = "연산자 파싱 중 비어있는 문자")
        var operator = currentChar.toString()
        var currentContext = context.advance()
        
        // 2문자 연산자 확인
        if (currentContext.hasNext()) {
            val nextChar = currentContext.currentChar
            if (nextChar != null) {
                val twoCharOperator = operator + nextChar
                
                if (tokenFactory.isOperator(twoCharOperator)) {
                    operator = twoCharOperator
                    currentContext = currentContext.advance()
                }
            }
        }
        
        val token = tokenFactory.createOperatorToken(operator, startPosition)
        return token to currentContext
    }

    private fun parseDelimiter(context: LexingContext): Pair<Token, LexingContext> {
        val startPosition = context.currentPosition
        val delimiter = context.currentChar?.toString() ?: throw LexerException(ErrorCode.VALIDATION_FAILED, message = "구분자 파싱 중 비어있는 문자")
        val currentContext = context.advance()
        
        val token = tokenFactory.createDelimiterToken(delimiter, startPosition)
        return token to currentContext
    }

    private fun skipLineComment(context: LexingContext): LexingContext {
        var currentContext = context
        
        while (currentContext.hasNext()) {
            val char = currentContext.currentChar
            currentContext = currentContext.advance()
            if (char == '\n') break
        }
        
        return currentContext
    }

    private fun skipBlockComment(context: LexingContext): LexingContext {
        var currentContext = context.advance(2) // "/*" 건너뛰기
        
        while (currentContext.hasNext()) {
            val char = currentContext.currentChar
            if (char == '*' && currentContext.peekChar() == '/') {
                currentContext = currentContext.advance(2) // "*/" 건너뛰기
                break
            }
            currentContext = currentContext.advance()
        }
        
        return currentContext
    }

    private fun resetStatistics() {
        statistics.clear()
        statistics["tokensGenerated"] = 0
        statistics["inputsProcessed"] = 0
        statistics["errorsOccurred"] = 0
        statistics["totalProcessingTime"] = 0L
        statistics["lastResetTime"] = System.currentTimeMillis()
    }

    private fun updateStatistics(key: String, value: Any) {
        statistics[key] = value
        statistics["lastUpdatedTime"] = System.currentTimeMillis()
    }
}