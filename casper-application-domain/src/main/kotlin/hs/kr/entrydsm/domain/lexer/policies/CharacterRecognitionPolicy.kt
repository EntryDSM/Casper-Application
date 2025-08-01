package hs.kr.entrydsm.domain.lexer.policies

import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 문자 인식 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 어휘 분석 과정에서 문자를 어떻게 분류하고
 * 처리할지에 대한 비즈니스 규칙을 정의합니다. 문자 타입 판별, 특수 문자 처리,
 * 유니코드 지원 등의 정책을 캡슐화합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "CharacterRecognition",
    description = "어휘 분석 과정에서 문자 분류 및 처리에 대한 정책",
    domain = "lexer", 
    scope = Scope.DOMAIN
)
class CharacterRecognitionPolicy {

    companion object {
        private val WHITESPACE_CHARS = setOf(' ', '\t', '\n', '\r', '\u000c')
        private val DIGIT_CHARS = '0'..'9'
        private val LETTER_CHARS = ('a'..'z') + ('A'..'Z')
        private val OPERATOR_START_CHARS = setOf('+', '-', '*', '/', '^', '%', '=', '!', '<', '>', '&', '|')
        private val DELIMITER_CHARS = setOf('(', ')', ',', '{', '}', '[', ']')
        private val IDENTIFIER_START_CHARS = LETTER_CHARS.toSet() + '_'
        private val IDENTIFIER_BODY_CHARS = IDENTIFIER_START_CHARS + DIGIT_CHARS.toSet()
        private val SPECIAL_CHARS = setOf('.', ';', ':', '"', '\'', '\\', '@', '#', '$')
        private val COMMENT_START_CHARS = setOf('/', '#')
        
        // 유니코드 범위 (기본적으로 ASCII만 허용)
        private const val MAX_ASCII_VALUE = 127
    }

    private var allowUnicode: Boolean = false
    private var allowExtendedASCII: Boolean = false
    private var caseSensitive: Boolean = true
    private var allowUnderscoreInNumbers: Boolean = false

    /**
     * 정책 설정을 구성합니다.
     *
     * @param allowUnicode 유니코드 문자 허용 여부
     * @param allowExtendedASCII 확장 ASCII 허용 여부
     * @param caseSensitive 대소문자 구분 여부
     * @param allowUnderscoreInNumbers 숫자에서 언더스코어 구분자 허용 여부
     */
    fun configure(
        allowUnicode: Boolean = false,
        allowExtendedASCII: Boolean = false,
        caseSensitive: Boolean = true,
        allowUnderscoreInNumbers: Boolean = false
    ) {
        this.allowUnicode = allowUnicode
        this.allowExtendedASCII = allowExtendedASCII
        this.caseSensitive = caseSensitive
        this.allowUnderscoreInNumbers = allowUnderscoreInNumbers
    }

    /**
     * 문자가 공백 문자인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 공백 문자이면 true
     */
    fun isWhitespace(char: Char): Boolean {
        return char in WHITESPACE_CHARS || 
               (allowUnicode && char.isWhitespace())
    }

    /**
     * 문자가 숫자인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 숫자이면 true
     */
    fun isDigit(char: Char): Boolean {
        return char in DIGIT_CHARS ||
               (allowUnicode && char.isDigit())
    }

    /**
     * 문자가 문자(알파벳)인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 문자이면 true
     */
    fun isLetter(char: Char): Boolean {
        return char in LETTER_CHARS ||
               (allowUnicode && char.isLetter())
    }

    /**
     * 문자가 식별자 시작에 사용될 수 있는지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 식별자 시작에 사용 가능하면 true
     */
    fun isIdentifierStart(char: Char): Boolean {
        return char in IDENTIFIER_START_CHARS ||
               (allowUnicode && (char.isLetter() || char == '_'))
    }

    /**
     * 문자가 식별자 본문에 사용될 수 있는지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 식별자 본문에 사용 가능하면 true
     */
    fun isIdentifierBody(char: Char): Boolean {
        return char in IDENTIFIER_BODY_CHARS ||
               (allowUnicode && (char.isLetterOrDigit() || char == '_'))
    }

    /**
     * 문자가 연산자 시작에 사용될 수 있는지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 연산자 시작에 사용 가능하면 true
     */
    fun isOperatorStart(char: Char): Boolean {
        return char in OPERATOR_START_CHARS
    }

    /**
     * 문자가 구분자인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 구분자이면 true
     */
    fun isDelimiter(char: Char): Boolean {
        return char in DELIMITER_CHARS
    }

    /**
     * 문자가 특수 문자인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 특수 문자이면 true
     */
    fun isSpecialChar(char: Char): Boolean {
        return char in SPECIAL_CHARS
    }

    /**
     * 문자가 주석 시작 문자인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 주석 시작 문자이면 true
     */
    fun isCommentStart(char: Char): Boolean {
        return char in COMMENT_START_CHARS
    }

    /**
     * 문자가 숫자 리터럴에서 유효한지 판별합니다.
     *
     * @param char 판별할 문자
     * @param isFirstChar 첫 번째 문자인지 여부
     * @return 숫자 리터럴에서 유효하면 true
     */
    fun isValidInNumber(char: Char, isFirstChar: Boolean = false): Boolean {
        return when {
            isDigit(char) -> true
            char == '.' -> !isFirstChar // 소수점은 첫 번째가 아닐 때만
            char == '-' -> isFirstChar // 음수 기호는 첫 번째만
            char == '_' -> allowUnderscoreInNumbers && !isFirstChar // 구분자
            char in setOf('e', 'E') -> !isFirstChar // 과학적 표기법
            else -> false
        }
    }

    /**
     * 문자가 변수명 구분자(중괄호)인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 변수명 구분자이면 true
     */
    fun isVariableDelimiter(char: Char): Boolean {
        return char == '{' || char == '}'
    }

    /**
     * 문자가 문자열 구분자인지 판별합니다.
     *
     * @param char 판별할 문자
     * @return 문자열 구분자이면 true
     */
    fun isStringDelimiter(char: Char): Boolean {
        return char == '"' || char == '\''
    }

    /**
     * 문자가 허용된 문자인지 검증합니다.
     *
     * @param char 검증할 문자
     * @return 허용된 문자이면 true
     * @throws IllegalArgumentException 허용되지 않은 문자인 경우
     */
    fun validateChar(char: Char): Boolean {
        val codePoint = char.code
        
        when {
            codePoint <= MAX_ASCII_VALUE -> return true // ASCII 범위
            allowExtendedASCII && codePoint <= 255 -> return true // 확장 ASCII
            allowUnicode -> return true // 유니코드 전체
            else -> throw IllegalArgumentException(
                "허용되지 않은 문자입니다: '$char' (코드: $codePoint)"
            )
        }
    }

    /**
     * 문자를 정규화합니다 (대소문자 처리 등).
     *
     * @param char 정규화할 문자
     * @return 정규화된 문자
     */
    fun normalizeChar(char: Char): Char {
        return if (!caseSensitive && char.isLetter()) {
            char.lowercaseChar()
        } else {
            char
        }
    }

    /**
     * 문자열을 정규화합니다.
     *
     * @param text 정규화할 문자열
     * @return 정규화된 문자열
     */
    fun normalizeText(text: String): String {
        return if (!caseSensitive) {
            text.lowercase()
        } else {
            text
        }
    }

    /**
     * 두 문자가 정책에 따라 같은지 비교합니다.
     *
     * @param char1 첫 번째 문자
     * @param char2 두 번째 문자
     * @return 정책에 따라 같으면 true
     */
    fun areCharsEqual(char1: Char, char2: Char): Boolean {
        return normalizeChar(char1) == normalizeChar(char2)
    }

    /**
     * 두 문자열이 정책에 따라 같은지 비교합니다.
     *
     * @param text1 첫 번째 문자열
     * @param text2 두 번째 문자열
     * @return 정책에 따라 같으면 true
     */
    fun areTextsEqual(text1: String, text2: String): Boolean {
        return normalizeText(text1) == normalizeText(text2)
    }

    /**
     * 문자의 카테고리를 반환합니다.
     *
     * @param char 분류할 문자
     * @return 문자 카테고리
     */
    fun getCharCategory(char: Char): CharCategory {
        return when {
            isWhitespace(char) -> CharCategory.WHITESPACE
            isDigit(char) -> CharCategory.DIGIT
            isLetter(char) -> CharCategory.LETTER
            isOperatorStart(char) -> CharCategory.OPERATOR
            isDelimiter(char) -> CharCategory.DELIMITER
            isSpecialChar(char) -> CharCategory.SPECIAL
            isVariableDelimiter(char) -> CharCategory.VARIABLE_DELIMITER
            isStringDelimiter(char) -> CharCategory.STRING_DELIMITER
            isCommentStart(char) -> CharCategory.COMMENT_START
            else -> CharCategory.UNKNOWN
        }
    }

    /**
     * 문자가 특정 카테고리에 속하는지 확인합니다.
     *
     * @param char 확인할 문자
     * @param category 확인할 카테고리
     * @return 해당 카테고리에 속하면 true
     */
    fun isCharInCategory(char: Char, category: CharCategory): Boolean {
        return getCharCategory(char) == category
    }

    /**
     * 정책의 현재 설정을 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "allowUnicode" to allowUnicode,
        "allowExtendedASCII" to allowExtendedASCII,
        "caseSensitive" to caseSensitive,
        "allowUnderscoreInNumbers" to allowUnderscoreInNumbers,
        "supportedCharCategories" to CharCategory.values().map { it.name }
    )

    /**
     * 지원되는 문자 집합의 통계를 반환합니다.
     *
     * @return 문자 집합 통계
     */
    fun getCharSetStatistics(): Map<String, Int> = mapOf(
        "whitespaceChars" to WHITESPACE_CHARS.size,
        "digitChars" to DIGIT_CHARS.count(),
        "letterChars" to LETTER_CHARS.count(),
        "operatorStartChars" to OPERATOR_START_CHARS.size,
        "delimiterChars" to DELIMITER_CHARS.size,
        "identifierStartChars" to IDENTIFIER_START_CHARS.size,
        "identifierBodyChars" to IDENTIFIER_BODY_CHARS.size,
        "specialChars" to SPECIAL_CHARS.size
    )

    /**
     * 문자 카테고리를 나타내는 열거형입니다.
     */
    enum class CharCategory {
        WHITESPACE,
        DIGIT,
        LETTER,
        OPERATOR,
        DELIMITER,
        SPECIAL,
        VARIABLE_DELIMITER,
        STRING_DELIMITER,
        COMMENT_START,
        UNKNOWN
    }
}