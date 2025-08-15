package hs.kr.entrydsm.domain.lexer.contract

import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.lexer.values.LexingContext
import hs.kr.entrydsm.domain.lexer.values.LexingResult
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

/**
 * Lexer 도메인의 핵심 계약 인터페이스입니다.
 *
 * 입력 텍스트를 토큰 스트림으로 변환하는 렉서의 기본 계약을 정의합니다.
 * 계산기 언어의 모든 토큰화 규칙과 정책을 캡슐화하며,
 * 다양한 렉서 구현체들이 준수해야 할 표준을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "lexer")
interface LexerContract {

    /**
     * 입력 텍스트를 토큰 리스트로 변환합니다.
     */
    fun tokenize(input: String): LexingResult
    
    /**
     * 컨텍스트를 사용하여 어휘 분석을 수행합니다.
     */
    fun tokenize(context: LexingContext): LexingResult
    
    /**
     * 입력 텍스트에서 다음 토큰 하나를 추출합니다.
     */
    fun nextToken(context: LexingContext): Pair<Token?, LexingContext>
    
    /**
     * 현재 위치에서 미리 보기(lookahead) 토큰을 반환합니다.
     */
    fun peekTokens(context: LexingContext, count: Int): List<Token>
    
    /**
     * 주어진 입력이 유효한 토큰들로 구성되어 있는지 검증합니다.
     */
    fun validate(input: String): Boolean
    
    /**
     * 주어진 컨텍스트가 유효한 상태인지 검증합니다.
     */
    fun validateContext(context: LexingContext): Boolean
    
    /**
     * 현재 위치가 특정 토큰 타입으로 시작하는지 확인합니다.
     */
    fun isTokenTypeAt(context: LexingContext, vararg expectedTypes: TokenType): Boolean
    
    /**
     * 공백 문자들을 건너뛰고 다음 유효한 위치로 이동합니다.
     */
    fun skipWhitespace(context: LexingContext): LexingContext
    
    /**
     * 주석을 건너뛰고 다음 유효한 위치로 이동합니다.
     */
    fun skipComments(context: LexingContext): LexingContext
    
    /**
     * 현재 위치에서 EOF 토큰을 생성합니다.
     */
    fun createEOFToken(context: LexingContext): Token
    
    /**
     * Lexer의 현재 설정 정보를 반환합니다.
     */
    fun getConfiguration(): Map<String, Any>
    
    /**
     * Lexer를 초기 상태로 재설정합니다.
     */
    fun reset()
    
    /**
     * 분석 통계 정보를 반환합니다.
     */
    fun getStatistics(): Map<String, Any>
    
    /**
     * 디버그 모드 여부를 설정합니다.
     */
    fun setDebugMode(enabled: Boolean)
    
    /**
     * 오류 복구 모드를 설정합니다.
     */
    fun setErrorRecoveryMode(enabled: Boolean)
    
    /**
     * 스트리밍 모드로 토큰을 하나씩 생성하는 시퀀스를 반환합니다.
     */
    fun tokenizeAsSequence(input: String): Sequence<Token>
    
    /**
     * 비동기적으로 어휘 분석을 수행합니다.
     */
    fun tokenizeAsync(input: String, callback: (LexingResult) -> Unit)
    
    /**
     * 부분 입력에 대한 증분 분석을 수행합니다.
     */
    fun incrementalTokenize(
        previousResult: LexingResult,
        newInput: String,
        changeStartIndex: Int
    ): LexingResult
}

/**
 * 토큰화 통계 정보를 담는 데이터 클래스입니다.
 *
 * @property inputLength 입력 텍스트 길이
 * @property tokenCount 생성된 토큰 개수
 * @property processingTimeMs 처리 시간 (밀리초)
 * @property errorCount 발생한 오류 개수
 * @property lastTokenizationTime 마지막 토큰화 시간
 */
data class TokenizationStats(
    val inputLength: Int,
    val tokenCount: Int,
    val processingTimeMs: Long,
    val errorCount: Int = 0,
    val lastTokenizationTime: Long = System.currentTimeMillis()
) {
    /**
     * 초당 처리된 문자 수를 계산합니다.
     *
     * @return 초당 문자 처리량
     */
    fun getCharactersPerSecond(): Double = if (processingTimeMs > 0) {
        (inputLength * 1000.0) / processingTimeMs
    } else {
        0.0
    }

    /**
     * 초당 생성된 토큰 수를 계산합니다.
     *
     * @return 초당 토큰 생성량
     */
    fun getTokensPerSecond(): Double = if (processingTimeMs > 0) {
        (tokenCount * 1000.0) / processingTimeMs
    } else {
        0.0
    }

    /**
     * 평균 토큰 길이를 계산합니다.
     *
     * @return 평균 토큰 길이
     */
    fun getAverageTokenLength(): Double = if (tokenCount > 0) {
        inputLength.toDouble() / tokenCount
    } else {
        0.0
    }
}

/**
 * 렉서 설정을 담는 데이터 클래스입니다.
 *
 * @property maxInputLength 최대 입력 길이
 * @property strictMode 엄격 모드 (오류시 즉시 중단)
 * @property skipWhitespace 공백 문자 건너뛰기
 * @property caseInsensitiveKeywords 키워드 대소문자 구분 안함
 */
data class LexerConfiguration(
    val maxInputLength: Int = 10000,
    val strictMode: Boolean = true,
    val skipWhitespace: Boolean = true,
    val caseInsensitiveKeywords: Boolean = true
)