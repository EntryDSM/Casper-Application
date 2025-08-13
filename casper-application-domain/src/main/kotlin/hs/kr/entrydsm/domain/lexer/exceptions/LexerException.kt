package hs.kr.entrydsm.domain.lexer.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Lexer 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * 토큰화 과정에서 발생할 수 있는 예상치 못한 문자, 잘못된 토큰 시퀀스,
 * 숫자 형식 오류 등의 어휘 분석 관련 오류를 처리합니다.
 *
 * @property position 오류가 발생한 입력 위치 (선택사항)
 * @property character 오류를 발생시킨 문자 (선택사항)
 * @property token 오류와 관련된 토큰 정보 (선택사항)
 * @property reason 사유 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class LexerException(
    errorCode: ErrorCode,
    val position: Int? = null,
    val character: Char? = null,
    val token: String? = null,
    val reason: String? = null,
    message: String = buildLexerMessage(errorCode, position, character, token, reason),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * Lexer 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param position 오류 발생 위치
         * @param character 오류 문자
         * @param token 관련 토큰
         * @param reason 사유
         * @return 구성된 메시지
         */
        private fun buildLexerMessage(
            errorCode: ErrorCode,
            position: Int?,
            character: Char?,
            token: String?,
            reason: String?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            position?.let { details.add("위치: $it") }
            character?.let { details.add("문자: '$it'") }
            token?.let { details.add("토큰: $it") }
            reason?.let { details.add("사유: $it") }

            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 예상치 못한 문자 오류를 생성합니다.
         *
         * @param character 예상치 못한 문자
         * @param position 문자 위치
         * @return LexerException 인스턴스
         */
        fun unexpectedCharacter(character: Char, position: Int): LexerException {
            return LexerException(
                errorCode = ErrorCode.UNEXPECTED_CHARACTER,
                character = character,
                position = position
            )
        }

        /**
         * 닫히지 않은 변수 오류를 생성합니다.
         *
         * @param token 닫히지 않은 변수 토큰
         * @param position 시작 위치
         * @return LexerException 인스턴스
         */
        fun unclosedVariable(token: String, position: Int): LexerException {
            return LexerException(
                errorCode = ErrorCode.UNCLOSED_VARIABLE,
                token = token,
                position = position
            )
        }

        /**
         * 잘못된 숫자 형식 오류를 생성합니다.
         *
         * @param token 잘못된 숫자 토큰
         * @param position 토큰 위치
         * @return LexerException 인스턴스
         */
        fun invalidNumberFormat(token: String, position: Int): LexerException {
            return LexerException(
                errorCode = ErrorCode.INVALID_NUMBER_FORMAT,
                token = token,
                position = position
            )
        }

        /**
         * 잘못된 토큰 시퀀스 오류를 생성합니다.
         *
         * @param token 잘못된 토큰
         * @param position 토큰 위치
         * @return LexerException 인스턴스
         */
        fun invalidTokenSequence(token: String, position: Int): LexerException {
            return LexerException(
                errorCode = ErrorCode.INVALID_TOKEN_SEQUENCE,
                token = token,
                position = position
            )
        }

        /**
         * 예상치 못한 문자 오류를 생성합니다 (line, column 정보 포함).
         *
         * @param character 예상치 못한 문자
         * @param position 문자 위치
         * @param line 줄 번호
         * @param column 열 번호
         * @param message 추가 메시지 (선택사항)
         * @param cause 원인 예외 (선택사항)
         * @return LexerException 인스턴스
         */
        fun unexpectedCharacter(
            character: String,
            position: Int,
            line: Int,
            column: Int,
            message: String? = null,
            cause: Throwable? = null
        ): LexerException {
            val finalMessage = message ?: "예상치 못한 문자 '$character' (위치: $position, 줄: $line, 열: $column)"
            return LexerException(
                errorCode = ErrorCode.UNEXPECTED_CHARACTER,
                token = character,
                position = position,
                message = finalMessage,
                cause = cause
            )
        }

        /**
         * 닫히지 않은 변수 오류를 생성합니다 (line, column 정보 포함).
         *
         * @param variableName 변수명
         * @param position 시작 위치
         * @param line 줄 번호
         * @param column 열 번호
         * @return LexerException 인스턴스
         */
        fun unclosedVariable(
            variableName: String,
            position: Int,
            line: Int,
            column: Int
        ): LexerException {
            val message = "닫히지 않은 변수 '$variableName' (위치: $position, 줄: $line, 열: $column)"
            return LexerException(
                errorCode = ErrorCode.UNCLOSED_VARIABLE,
                token = variableName,
                position = position,
                message = message
            )
        }

        /**
         * 잘못된 토큰 오류를 생성합니다.
         *
         * @param token 잘못된 토큰
         * @param position 토큰 위치
         * @param line 줄 번호
         * @param column 열 번호
         * @param message 추가 메시지
         * @return LexerException 인스턴스
         */
        fun invalidToken(
            token: String,
            position: Int,
            line: Int,
            column: Int,
            message: String
        ): LexerException {
            val finalMessage = "$message (토큰: '$token', 위치: $position, 줄: $line, 열: $column)"
            return LexerException(
                errorCode = ErrorCode.INVALID_TOKEN_SEQUENCE,
                token = token,
                position = position,
                message = finalMessage
            )
        }

        /**
         * 닫히지 않은 문자열 오류를 생성합니다.
         *
         * @param content 문자열 내용
         * @param position 시작 위치
         * @param line 줄 번호
         * @param column 열 번호
         * @return LexerException 인스턴스
         */
        fun unclosedString(
            content: String,
            position: Int,
            line: Int,
            column: Int
        ): LexerException {
            val message = "닫히지 않은 문자열 (위치: $position, 줄: $line, 열: $column)"
            return LexerException(
                errorCode = ErrorCode.INVALID_TOKEN_SEQUENCE,
                token = "\"$content",
                position = position,
                message = message
            )
        }

        /**
         * 잘못된 숫자 형식 오류를 생성합니다 (line, column 정보 포함).
         *
         * @param number 잘못된 숫자 문자열
         * @param position 토큰 위치
         * @param line 줄 번호
         * @param column 열 번호
         * @param cause 원인 예외
         * @return LexerException 인스턴스
         */
        fun invalidNumberFormat(
            number: String,
            position: Int,
            line: Int,
            column: Int,
            cause: Throwable? = null
        ): LexerException {
            val message = "잘못된 숫자 형식 '$number' (위치: $position, 줄: $line, 열: $column)"
            return LexerException(
                errorCode = ErrorCode.INVALID_NUMBER_FORMAT,
                token = number,
                position = position,
                message = message,
                cause = cause
            )
        }
    }

    /**
     * Lexer 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 위치, 문자, 토큰 정보가 포함된 맵
     */
    fun getLexerInfo(): Map<String, Any?> = mapOf(
        "position" to position,
        "character" to character,
        "token" to token
    ).filterValues { it != null }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 Lexer 정보가 결합된 맵
     */
    fun getLexerErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val lexerInfo = getLexerInfo()
        
        lexerInfo.forEach { (key, value) ->
            baseInfo[key] = value.toString()
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val lexerDetails = getLexerInfo()
        return if (lexerDetails.isNotEmpty()) {
            "${super.toString()}, lexer=${lexerDetails}"
        } else {
            super.toString()
        }
    }
}