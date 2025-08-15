package hs.kr.entrydsm.domain.lexer.specifications

import hs.kr.entrydsm.domain.lexer.exceptions.LexerException
import hs.kr.entrydsm.domain.lexer.values.LexingContext
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 입력 텍스트의 유효성을 검증하는 Specification 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 어휘 분석 전 입력 데이터의
 * 유효성을 검증하는 비즈니스 규칙을 정의합니다. 입력 길이, 문자 집합,
 * 인코딩, 구조적 제약 등을 검증합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "InputValidity",
    description = "어휘 분석 입력 텍스트의 유효성과 제약 조건을 검증하는 명세",
    domain = "lexer",
    priority = Priority.HIGH
)
class InputValiditySpec {

    companion object {
        private const val MAX_INPUT_LENGTH = 1_000_000  // 1MB
        private const val MAX_LINE_LENGTH = 10_000
        private const val MAX_LINE_COUNT = 50_000
        private const val MAX_TOKEN_LENGTH = 1_000
        private const val MAX_NESTING_DEPTH = 100
        
        // 허용되지 않는 제어 문자들 (일부 제외)
        private val FORBIDDEN_CONTROL_CHARS = (0x00..0x1F).toSet() - setOf(
            0x09, // TAB
            0x0A, // LF (Line Feed)
            0x0D  // CR (Carriage Return)
        )
    }

    private var strictMode: Boolean = true
    private var allowUnicode: Boolean = false
    private var allowExtendedASCII: Boolean = false
    private var maxInputLength: Int = MAX_INPUT_LENGTH
    private var maxLineLength: Int = MAX_LINE_LENGTH
    private var maxLineCount: Int = MAX_LINE_COUNT

    /**
     * 명세 설정을 구성합니다.
     *
     * @param strictMode 엄격 모드 여부
     * @param allowUnicode 유니코드 허용 여부
     * @param allowExtendedASCII 확장 ASCII 허용 여부
     * @param maxInputLength 최대 입력 길이
     * @param maxLineLength 최대 라인 길이
     * @param maxLineCount 최대 라인 수
     */
    fun configure(
        strictMode: Boolean = true,
        allowUnicode: Boolean = false,
        allowExtendedASCII: Boolean = false,
        maxInputLength: Int = MAX_INPUT_LENGTH,
        maxLineLength: Int = MAX_LINE_LENGTH,
        maxLineCount: Int = MAX_LINE_COUNT
    ) {
        this.strictMode = strictMode
        this.allowUnicode = allowUnicode
        this.allowExtendedASCII = allowExtendedASCII
        this.maxInputLength = maxInputLength
        this.maxLineLength = maxLineLength
        this.maxLineCount = maxLineCount
    }

    /**
     * 입력 텍스트가 유효한지 검증합니다.
     *
     * @param input 검증할 입력 텍스트
     * @return 유효하면 true
     */
    fun isSatisfiedBy(input: String): Boolean {
        return hasValidLength(input) &&
               hasValidCharacterSet(input) &&
               hasValidLineStructure(input) &&
               hasValidEncoding(input) &&
               hasValidNestingDepth(input) &&
               hasNoForbiddenPatterns(input)
    }

    /**
     * LexingContext가 유효한지 검증합니다.
     *
     * @param context 검증할 컨텍스트
     * @return 유효하면 true
     */
    fun isValidContext(context: LexingContext): Boolean {
        return isSatisfiedBy(context.input) &&
               hasValidPosition(context) &&
               hasValidConfiguration(context)
    }

    /**
     * 입력이 비어있는지 확인합니다.
     *
     * @param input 확인할 입력
     * @return 비어있으면 true
     */
    fun isEmpty(input: String): Boolean {
        return input.isEmpty()
    }

    /**
     * 입력이 공백만 포함하는지 확인합니다.
     *
     * @param input 확인할 입력
     * @return 공백만 포함하면 true
     */
    fun isBlank(input: String): Boolean {
        return input.isBlank()
    }

    /**
     * 입력이 단일 라인인지 확인합니다.
     *
     * @param input 확인할 입력
     * @return 단일 라인이면 true
     */
    fun isSingleLine(input: String): Boolean {
        return !input.contains('\n') && !input.contains('\r')
    }

    /**
     * 입력이 ASCII 전용인지 확인합니다.
     *
     * @param input 확인할 입력
     * @return ASCII 전용이면 true
     */
    fun isASCIIOnly(input: String): Boolean {
        return input.all { it.code <= 127 }
    }

    /**
     * 입력에 유니코드 문자가 포함되어 있는지 확인합니다.
     *
     * @param input 확인할 입력
     * @return 유니코드 문자가 포함되어 있으면 true
     */
    fun hasUnicodeChars(input: String): Boolean {
        return input.any { it.code > 127 }
    }

    /**
     * 입력이 유효한 길이를 가지는지 검증합니다.
     */
    private fun hasValidLength(input: String): Boolean {
        if (input.length > maxInputLength) {
            throw LexerException.inputLengthExceeded(input.length, maxInputLength)
        }

        return true
    }

    /**
     * 입력이 유효한 문자 집합을 사용하는지 검증합니다.
     */
    private fun hasValidCharacterSet(input: String): Boolean {
        for (char in input) {
            val codePoint = char.code
            
            when {
                codePoint <= 127 -> continue // ASCII
                allowExtendedASCII && codePoint <= 255 -> continue // 확장 ASCII
                allowUnicode -> continue // 유니코드
                else -> throw LexerException.disallowedCharacter(char, codePoint)
            }
            
            // 금지된 제어 문자 검사
            if (codePoint in FORBIDDEN_CONTROL_CHARS) {
                throw LexerException.forbiddenControlCharacter(codePoint)
            }
        }
        return true
    }

    /**
     * 입력이 유효한 라인 구조를 가지는지 검증합니다.
     */
    private fun hasValidLineStructure(input: String): Boolean {
        val lines = input.split('\n', '\r')
        
        if (lines.size > maxLineCount) {
            throw LexerException.lineCountExceeded(lines.size, maxLineCount)
        }
        
        lines.forEachIndexed { index, line ->
            if (line.length > maxLineLength) {
                throw LexerException.lineLengthExceeded(index, line.length, maxLineLength)
            }
        }
        
        return true
    }

    /**
     * 입력이 유효한 인코딩을 가지는지 검증합니다.
     */
    private fun hasValidEncoding(input: String): Boolean {
        // BOM (Byte Order Mark) 검사
        if (input.startsWith('\uFEFF')) {
            if (strictMode) {
                throw LexerException.bomCharacterDetected()
            }
        }
        
        // 널 문자 검사
        if (input.contains('\u0000')) {
            throw LexerException.nullCharacterDetected()
        }
        
        return true
    }

    /**
     * 입력이 유효한 중첩 깊이를 가지는지 검증합니다.
     */
    private fun hasValidNestingDepth(input: String): Boolean {
        var depth = 0
        var maxDepth = 0
        
        for (char in input) {
            when (char) {
                '(', '{', '[' -> {
                    depth++
                    maxDepth = maxOf(maxDepth, depth)
                }
                ')', '}', ']' -> {
                    depth--
                }
            }
            
            if (maxDepth > MAX_NESTING_DEPTH) {
                throw LexerException.maxNestingDepthExceeded(maxDepth, MAX_NESTING_DEPTH)
            }
        }
        
        return true
    }

    /**
     * 입력에 금지된 패턴이 없는지 검증합니다.
     */
    private fun hasNoForbiddenPatterns(input: String): Boolean {
        // 연속된 공백이 너무 많은 경우
        if (input.contains(Regex("\\s{100,}"))) {
            if (strictMode) {
                throw LexerException.excessiveWhitespaceDetected()
            }
        }
        
        // 의심스러운 반복 패턴
        if (input.contains(Regex("(.{1,10})\\1{50,}"))) {
            if (strictMode) {
                throw LexerException.suspiciousRepeatPattern()
            }
        }
        
        return true
    }

    /**
     * 컨텍스트의 위치가 유효한지 검증합니다.
     */
    private fun hasValidPosition(context: LexingContext): Boolean {
        val position = context.currentPosition

        if (position.index < 0 || position.index > context.input.length) {
            throw LexerException.invalidPositionIndex(position.index, context.input.length)
        }

        if (position.line < 1) {
            throw LexerException.invalidPositionLine(position.line)
        }
        
        if (position.column < 1) {
            throw LexerException.invalidPositionColumn(position.column)
        }
        
        return true
    }

    /**
     * 컨텍스트의 설정이 유효한지 검증합니다.
     */
    private fun hasValidConfiguration(context: LexingContext): Boolean {
        if (context.maxTokenLength <= 0) {
            throw LexerException.invalidMaxTokenLength(context.maxTokenLength)
        }
        
        if (context.maxTokenLength > MAX_TOKEN_LENGTH) {
            throw LexerException.maxTokenLengthExceeded(context.maxTokenLength, MAX_TOKEN_LENGTH)
        }
        
        return true
    }

    /**
     * 입력의 통계 정보를 반환합니다.
     *
     * @param input 분석할 입력
     * @return 통계 정보 맵
     */
    fun getInputStatistics(input: String): Map<String, Any> {
        val lines = input.split('\n', '\r')
        
        return mapOf(
            "totalLength" to input.length,
            "lineCount" to lines.size,
            "maxLineLength" to (lines.maxOfOrNull { it.length } ?: 0),
            "avgLineLength" to if (lines.isNotEmpty()) input.length / lines.size else 0,
            "hasUnicode" to hasUnicodeChars(input),
            "isASCIIOnly" to isASCIIOnly(input),
            "isSingleLine" to isSingleLine(input),
            "isEmpty" to isEmpty(input),
            "isBlank" to isBlank(input),
            "whitespaceRatio" to if (input.isNotEmpty()) 
                input.count { it.isWhitespace() }.toDouble() / input.length else 0.0
        )
    }

    /**
     * 명세의 현재 설정을 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "strictMode" to strictMode,
        "allowUnicode" to allowUnicode,
        "allowExtendedASCII" to allowExtendedASCII,
        "maxInputLength" to maxInputLength,
        "maxLineLength" to maxLineLength,
        "maxLineCount" to maxLineCount,
        "maxTokenLength" to MAX_TOKEN_LENGTH,
        "maxNestingDepth" to MAX_NESTING_DEPTH
    )

    /**
     * 입력의 유효성 검증 결과와 상세 정보를 반환합니다.
     *
     * @param input 검증할 입력
     * @return 검증 결과 맵 (isValid, errors, warnings, statistics)
     */
    fun validateWithDetails(input: String): Map<String, Any> {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        var isValid = true
        
        try {
            isSatisfiedBy(input)
        } catch (e: IllegalArgumentException) {
            errors.add(e.message ?: "알 수 없는 검증 오류")
            isValid = false
        }
        
        // 경고 수준의 검사들
        if (input.length > maxInputLength * 0.8) {
            warnings.add("입력 길이가 권장 크기에 근접했습니다")
        }
        
        if (hasUnicodeChars(input) && !allowUnicode) {
            warnings.add("유니코드 문자가 포함되어 있지만 허용되지 않습니다")
        }
        
        return mapOf(
            "isValid" to isValid,
            "errors" to errors,
            "warnings" to warnings,
            "statistics" to getInputStatistics(input)
        )
    }
}