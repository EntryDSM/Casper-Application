package hs.kr.entrydsm.domain.lexer.values

import hs.kr.entrydsm.domain.lexer.exceptions.LexerException
import hs.kr.entrydsm.global.values.Position

/**
 * 어휘 분석 컨텍스트를 나타내는 값 객체입니다.
 *
 * Lexer가 텍스트를 분석하는 동안 유지해야 하는 상태 정보를 캡슐화합니다.
 * 현재 위치, 입력 텍스트, 분석 옵션 등을 포함하며, 불변 객체로 설계되어
 * 안전한 상태 전달을 보장합니다.
 *
 * @property input 분석할 입력 텍스트
 * @property currentPosition 현재 분석 위치
 * @property startTime 분석 시작 시간 (밀리초)
 * @property strictMode 엄격 모드 여부 (에러 허용도)
 * @property skipWhitespace 공백 문자 스킵 여부
 * @property allowUnicode 유니코드 문자 허용 여부
 * @property maxTokenLength 최대 토큰 길이 제한
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
data class LexingContext(
    val input: String,
    val currentPosition: Position = Position.START,
    val startTime: Long = System.currentTimeMillis(),
    val strictMode: Boolean = true,
    val skipWhitespace: Boolean = true,
    val allowUnicode: Boolean = false,
    val maxTokenLength: Int = 1000
) {
    
    init {
        if (maxTokenLength <= 0) {
            throw LexerException.maxTokenLengthInvalid(maxTokenLength)
        }

        if (startTime <= 0) {
            throw LexerException.startTimeInvalid(startTime)
        }

    }

    companion object {
        /**
         * 기본 설정으로 컨텍스트를 생성합니다.
         *
         * @param input 분석할 입력 텍스트
         * @param startTime 분석 시작 시간 (기본값: 현재 시간)
         * @return 기본 LexingContext
         */
        fun of(input: String, startTime: Long = System.currentTimeMillis()): LexingContext = LexingContext(
            input = input,
            currentPosition = Position.START,
            startTime = startTime
        )

        /**
         * 엄격 모드 설정으로 컨텍스트를 생성합니다.
         *
         * @param input 분석할 입력 텍스트
         * @param strictMode 엄격 모드 여부
         * @return 설정된 LexingContext
         */
        fun withStrictMode(input: String, strictMode: Boolean): LexingContext = 
            of(input).copy(strictMode = strictMode)

        /**
         * 유니코드 허용 설정으로 컨텍스트를 생성합니다.
         *
         * @param input 분석할 입력 텍스트
         * @param allowUnicode 유니코드 허용 여부
         * @return 설정된 LexingContext
         */
        fun withUnicode(input: String, allowUnicode: Boolean): LexingContext = 
            of(input).copy(allowUnicode = allowUnicode)

        /**
         * 완전한 옵션으로 컨텍스트를 생성합니다.
         *
         * @param input 분석할 입력 텍스트
         * @param strictMode 엄격 모드 여부
         * @param skipWhitespace 공백 스킵 여부
         * @param allowUnicode 유니코드 허용 여부
         * @param maxTokenLength 최대 토큰 길이
         * @param startTime 분석 시작 시간 (기본값: 현재 시간)
         * @return 설정된 LexingContext
         */
        fun create(
            input: String,
            strictMode: Boolean = true,
            skipWhitespace: Boolean = true,
            allowUnicode: Boolean = false,
            maxTokenLength: Int = 1000,
            startTime: Long = System.currentTimeMillis()
        ): LexingContext = LexingContext(
            input = input,
            currentPosition = Position.START,
            startTime = startTime,
            strictMode = strictMode,
            skipWhitespace = skipWhitespace,
            allowUnicode = allowUnicode,
            maxTokenLength = maxTokenLength
        )
    }

    /**
     * 현재 위치가 입력 끝에 도달했는지 확인합니다.
     *
     * @return 끝에 도달했으면 true
     */
    fun isAtEnd(): Boolean = currentPosition.index >= input.length

    /**
     * 더 읽을 문자가 있는지 확인합니다.
     *
     * @return 읽을 문자가 있으면 true
     */
    fun hasNext(): Boolean = !isAtEnd()

    /**
     * 현재 위치의 문자 (캐시됨)
     */
    val currentChar: Char? by lazy {
        if (isAtEnd()) null else input[currentPosition.index]
    }


    /**
     * 다음 위치의 문자를 미리 확인합니다.
     *
     * @param offset 현재 위치에서의 오프셋 (기본값: 1)
     * @return 해당 위치의 문자 또는 null
     */
    fun peekChar(offset: Int = 1): Char? {
        val index = currentPosition.index + offset
        return if (index >= input.length) null else input[index]
    }

    /**
     * 현재 위치에서 지정된 길이만큼의 부분 문자열을 반환합니다.
     *
     * @param length 추출할 길이
     * @return 부분 문자열
     */
    fun peek(length: Int): String {
        val startIndex = currentPosition.index
        val endIndex = minOf(startIndex + length, input.length)
        return input.substring(startIndex, endIndex)
    }

    /**
     * 위치를 앞으로 이동시킨 새로운 컨텍스트를 반환합니다.
     *
     * @param steps 이동할 문자 수 (기본값: 1)
     * @return 이동된 LexingContext
     */
    fun advance(steps: Int = 1): LexingContext {
        if (steps < 0) {
            throw LexerException.stepsNegative(steps)
        }

        var index = currentPosition.index
        var line = currentPosition.line
        var column = currentPosition.column
        
        val maxIndex = input.length
        var moved = 0
        
        while (moved < steps && index < maxIndex) {
            val currentChar = input[index++]
            moved++
            if (currentChar == '\n') {
                line++
                column = 1
            } else {
                column++
            }
        }
        
        return copy(currentPosition = Position(index, line, column))
    }

    /**
     * 특정 위치로 이동한 새로운 컨텍스트를 반환합니다.
     *
     * @param position 이동할 위치
     * @return 이동된 LexingContext
     */
    fun moveTo(position: Position): LexingContext = copy(currentPosition = position)

    /**
     * 현재 위치에서 지정된 끝 위치까지의 텍스트를 추출합니다.
     *
     * @param endPosition 끝 위치
     * @return 추출된 텍스트
     */
    fun extractText(endPosition: Position): String {
        val startIndex = currentPosition.index
        val endIndex = minOf(endPosition.index, input.length)
        return if (startIndex <= endIndex) {
            input.substring(startIndex, endIndex)
        } else {
            ""
        }
    }

    /**
     * 현재 위치에서 지정된 길이만큼의 텍스트를 추출합니다.
     *
     * @param length 추출할 길이
     * @return 추출된 텍스트
     */
    fun extractText(length: Int): String {
        val startIndex = currentPosition.index
        val endIndex = minOf(startIndex + length, input.length)
        return input.substring(startIndex, endIndex)
    }

    /**
     * 현재 위치가 특정 문자인지 확인합니다.
     *
     * @param char 확인할 문자
     * @return 일치하면 true
     */
    fun isCurrentChar(char: Char): Boolean = currentChar == char

    /**
     * 현재 위치가 특정 문자들 중 하나인지 확인합니다.
     *
     * @param chars 확인할 문자들
     * @return 일치하는 문자가 있으면 true
     */
    fun isCurrentCharIn(chars: Set<Char>): Boolean = 
        currentChar?.let { it in chars } ?: false

    /**
     * 현재 위치가 숫자인지 확인합니다.
     *
     * @return 숫자이면 true
     */
    fun isCurrentDigit(): Boolean = currentChar?.isDigit() ?: false

    /**
     * 현재 위치가 문자인지 확인합니다.
     *
     * @return 문자이면 true
     */
    fun isCurrentLetter(): Boolean = currentChar?.isLetter() ?: false

    /**
     * 현재 위치가 공백 문자인지 확인합니다.
     *
     * @return 공백 문자이면 true
     */
    fun isCurrentWhitespace(): Boolean = currentChar?.isWhitespace() ?: false

    /**
     * 다음 N개 문자가 특정 문자열과 일치하는지 확인합니다.
     *
     * @param text 확인할 문자열
     * @return 일치하면 true
     */
    fun matchesNext(text: String): Boolean = peek(text.length) == text

    /**
     * 분석 경과 시간을 반환합니다.
     *
     * @return 경과 시간 (밀리초)
     */
    fun getElapsedTime(): Long = System.currentTimeMillis() - startTime

    /**
     * 남은 입력 텍스트 길이를 반환합니다.
     *
     * @return 남은 텍스트 길이
     */
    fun getRemainingLength(): Int = maxOf(0, input.length - currentPosition.index)

    /**
     * 진행률을 백분율로 반환합니다.
     *
     * @return 진행률 (0.0 ~ 100.0)
     */
    fun getProgress(): Double = 
        if (input.isEmpty()) 100.0 
        else (currentPosition.index.toDouble() / input.length) * 100.0

    /**
     * 컨텍스트의 상태 정보를 맵으로 반환합니다.
     *
     * @return 상태 정보 맵
     */
    fun getState(): Map<String, Any> = mapOf(
        "inputLength" to input.length,
        "currentIndex" to currentPosition.index,
        "currentLine" to currentPosition.line,
        "currentColumn" to currentPosition.column,
        "elapsedTime" to getElapsedTime(),
        "progress" to getProgress(),
        "strictMode" to strictMode,
        "skipWhitespace" to skipWhitespace,
        "allowUnicode" to allowUnicode,
        "maxTokenLength" to maxTokenLength
    )

    /**
     * 컨텍스트 정보를 문자열로 표현합니다.
     *
     * @return 컨텍스트 정보 문자열
     */
    override fun toString(): String = buildString {
        append("LexingContext(")
        append("pos=${currentPosition.toShortString()}, ")
        append("input=${input.length} chars, ")
        append("progress=${String.format("%.1f", getProgress())}%")
        append(")")
    }
}