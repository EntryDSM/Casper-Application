package hs.kr.entrydsm.domain.lexer.values

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.lexer.exceptions.LexerException

/**
 * 어휘 분석(Lexing) 결과를 나타내는 값 객체입니다.
 *
 * Lexer의 입력 텍스트 분석 결과로, 생성된 토큰 목록과 분석 과정에서 발생한
 * 메타데이터를 포함합니다. 성공/실패 여부와 관련 정보를 함께 제공하여
 * Parser에서 사용할 수 있는 완전한 토큰 스트림을 구성합니다.
 *
 * @property tokens 생성된 토큰 목록
 * @property isSuccess 분석 성공 여부
 * @property error 분석 실패 시의 오류 정보
 * @property duration 분석 소요 시간 (밀리초)
 * @property inputLength 입력 텍스트 길이
 * @property tokenCount 생성된 토큰 개수
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
data class LexingResult(
    val tokens: List<Token>,
    val isSuccess: Boolean = true,
    val error: LexerException? = null,
    val duration: Long = 0L,
    val inputLength: Int = 0,
    val tokenCount: Int = tokens.size
) {
    
    init {
        if (!(isSuccess || error != null)) {
            throw LexerException.invalidLexingResultErrorState(isSuccess, error)
        }

        if (duration < 0) {
            throw LexerException.negativeAnalysisDuration(duration)
        }

        if (inputLength < 0) {
            throw LexerException.negativeInputLength(inputLength)
        }

        if (tokenCount < 0) {
            throw LexerException.negativeTokenCount(tokenCount)
        }
    }

    companion object {
        /**
         * 성공적인 분석 결과를 생성합니다.
         *
         * @param tokens 생성된 토큰 목록
         * @param duration 분석 소요 시간
         * @param inputLength 입력 텍스트 길이
         * @return 성공 LexingResult
         */
        fun success(
            tokens: List<Token>,
            duration: Long = 0L,
            inputLength: Int = 0
        ): LexingResult = LexingResult(
            tokens = tokens,
            isSuccess = true,
            error = null,
            duration = duration,
            inputLength = inputLength,
            tokenCount = tokens.size
        )

        /**
         * 실패한 분석 결과를 생성합니다.
         *
         * @param error 분석 오류 정보
         * @param partialTokens 부분적으로 생성된 토큰들
         * @param duration 분석 소요 시간
         * @param inputLength 입력 텍스트 길이
         * @return 실패 LexingResult
         */
        fun failure(
            error: LexerException,
            partialTokens: List<Token> = emptyList(),
            duration: Long = 0L,
            inputLength: Int = 0
        ): LexingResult = LexingResult(
            tokens = partialTokens,
            isSuccess = false,
            error = error,
            duration = duration,
            inputLength = inputLength,
            tokenCount = partialTokens.size
        )

        /**
         * 빈 성공 결과를 생성합니다.
         *
         * @param inputLength 입력 텍스트 길이
         * @return 빈 성공 LexingResult
         */
        fun empty(inputLength: Int = 0): LexingResult = success(
            tokens = emptyList(),
            inputLength = inputLength
        )

        private const val UNKNOWN_ERROR = "Unknown error"
    }

    /**
     * 분석 실패 여부를 확인합니다.
     *
     * @return 실패했으면 true
     */
    fun isFailure(): Boolean = !isSuccess

    /**
     * 토큰이 하나도 생성되지 않았는지 확인합니다.
     *
     * @return 토큰이 없으면 true
     */
    fun isEmpty(): Boolean = tokens.isEmpty()

    /**
     * 토큰이 하나 이상 생성되었는지 확인합니다.
     *
     * @return 토큰이 있으면 true
     */
    fun isNotEmpty(): Boolean = tokens.isNotEmpty()

    /**
     * 특정 타입의 토큰들을 필터링합니다.
     *
     * @param type 찾을 토큰 타입
     * @return 해당 타입의 토큰 목록
     */
    fun filterByType(type: TokenType): List<Token> = 
        tokens.filter { it.type == type }

    /**
     * 첫 번째 토큰을 반환합니다.
     *
     * @return 첫 번째 토큰 또는 null
     */
    fun firstToken(): Token? = tokens.firstOrNull()

    /**
     * 마지막 토큰을 반환합니다.
     *
     * @return 마지막 토큰 또는 null
     */
    fun lastToken(): Token? = tokens.lastOrNull()

    /**
     * 특정 인덱스의 토큰을 안전하게 반환합니다.
     *
     * @param index 토큰 인덱스
     * @return 해당 인덱스의 토큰 또는 null
     */
    fun getTokenAt(index: Int): Token? = tokens.getOrNull(index)

    /**
     * 연산자 토큰들만 추출합니다.
     *
     * @return 연산자 토큰 목록
     */
    fun getOperatorTokens(): List<Token> = 
        tokens.filter { it.type.isOperator }

    /**
     * 리터럴 토큰들만 추출합니다.
     *
     * @return 리터럴 토큰 목록
     */
    fun getLiteralTokens(): List<Token> = 
        tokens.filter { it.type.isLiteral }

    /**
     * 키워드 토큰들만 추출합니다.
     *
     * @return 키워드 토큰 목록
     */
    fun getKeywordTokens(): List<Token> = 
        tokens.filter { it.type.isKeyword }

    /**
     * 분석 통계 정보를 맵으로 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> {
        var operatorCount = 0
        var literalCount = 0
        var keywordCount = 0
        
        for (token in tokens) {
            if (token.type.isOperator) operatorCount++
            if (token.type.isLiteral) literalCount++
            if (token.type.isKeyword) keywordCount++
        }
        
        return buildMap {
            put("success", isSuccess)
            put("tokenCount", tokenCount)
            put("inputLength", inputLength)
            put("duration", duration)
            put("operatorCount", operatorCount)
            put("literalCount", literalCount)
            put("keywordCount", keywordCount)
            if (!isSuccess) put("errorMessage", error?.message ?: UNKNOWN_ERROR)
        }
    }

    /**
     * 토큰 목록을 문자열로 표현합니다.
     *
     * @return 토큰 목록 문자열
     */
    fun tokensToString(): String = tokens.joinToString(" ") { it.toString() }

    /**
     * 분석 결과의 요약 정보를 반환합니다.
     *
     * @return 요약 정보 문자열
     */
    fun getSummary(): String = buildString {
        append("LexingResult(")
        append("success=$isSuccess, ")
        append("tokens=$tokenCount, ")
        append("duration=${duration}ms")
        if (inputLength > 0) {
            append(", input=$inputLength chars")
        }
        if (error != null) {
            append(", error=${error.message}")
        }
        append(")")
    }

    /**
     * 분석 결과를 상세 문자열로 표현합니다.
     *
     * @return 상세 정보 문자열
     */
    override fun toString(): String = getSummary()
}