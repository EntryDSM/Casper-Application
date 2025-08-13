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

        /**
         * EOF 토큰을 제외하고 토큰 값이 비어있을 때 오류를 생성합니다.
         *
         * @param type 토큰 타입
         * @return LexerException 인스턴스
         */
        fun tokenValueEmptyExceptEof(type: String): LexerException =
            LexerException(
                errorCode = ErrorCode.TOKEN_VALUE_EMPTY_EXCEPT_EOF,
                reason = "type=$type"
            )

        /**
         * 변수명이 비어있을 때 오류를 생성합니다.
         *
         * @param actual 입력된 변수명
         * @return LexerException 인스턴스
         */
        fun variableNameEmpty(actual: String?): LexerException =
            LexerException(
                errorCode = ErrorCode.LEXER_VARIABLE_NAME_EMPTY,
                token = actual,
                reason = "actual=${actual ?: "null"}"
            )

        /**
         * 연산자 타입이 아닐 때 오류를 생성합니다.
         *
         * @param type 토큰 타입
         * @return LexerException 인스턴스
         */
        fun notOperatorType(type: String): LexerException =
            LexerException(
                errorCode = ErrorCode.NOT_OPERATOR_TYPE,
                reason = "type=$type"
            )

        /**
         * 숫자 토큰이 아닐 때 오류를 생성합니다.
         *
         * @param type 토큰 타입
         * @return LexerException 인스턴스
         */
        fun notNumberToken(type: String): LexerException =
            LexerException(
                errorCode = ErrorCode.NOT_NUMBER_TOKEN,
                reason = "type=$type"
            )

        /**
         * 불린 토큰이 아닐 때 오류를 생성합니다.
         *
         * @param type 토큰 타입
         * @return LexerException 인스턴스
         */
        fun notBooleanToken(type: String): LexerException =
            LexerException(
                errorCode = ErrorCode.NOT_BOOLEAN_TOKEN,
                reason = "type=$type"
            )

        /**
         * 예상치 못한 불린 토큰 타입일 때 오류를 생성합니다.
         *
         * @param type 토큰 타입
         * @return LexerException 인스턴스
         */
        fun unexpectedBooleanTokenType(type: String): LexerException =
            LexerException(
                errorCode = ErrorCode.UNEXPECTED_BOOLEAN_TOKEN_TYPE,
                reason = "type=$type"
            )

        /**
         * 잘못된 숫자 형식일 때 오류를 생성합니다.
         *
         * @param tokenStr 잘못된 숫자 토큰 값
         * @return LexerException 인스턴스
         */
        fun invalidNumberFormat(tokenStr: String): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_NUMBER_FORMAT,
                token = tokenStr
            )

        /**
         * 유효하지 않은 식별자 형식일 때 오류를 생성합니다.
         *
         * @param value 잘못된 식별자 값
         * @return LexerException 인스턴스
         */
        fun invalidIdentifierFormat(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_IDENTIFIER_FORMAT,
                token = value
            )

        /**
         * 유효하지 않은 식별자일 때 오류를 생성합니다.
         *
         * @param value 입력된 식별자 값
         * @return LexerException 인스턴스
         */
        fun invalidIdentifier(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_IDENTIFIER,
                token = value
            )

        /**
         * 유효하지 않은 변수명일 때 오류를 생성합니다.
         *
         * @param value 입력된 변수명
         * @return LexerException 인스턴스
         */
        fun invalidVariableName(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_VARIABLE_NAME,
                token = value
            )

        /**
         * 지원하지 않는 연산자일 때 오류를 생성합니다.
         *
         * @param operator 연산자 문자열
         * @return LexerException 인스턴스
         */
        fun unsupportedOperator(operator: String): LexerException =
            LexerException(
                errorCode = ErrorCode.LEXER_UNSUPPORTED_OPERATOR,
                token = operator
            )

        /**
         * 지원하지 않는 구분자일 때 오류를 생성합니다.
         *
         * @param delimiter 구분자 문자열
         * @return LexerException 인스턴스
         */
        fun unsupportedDelimiter(delimiter: String): LexerException =
            LexerException(
                errorCode = ErrorCode.UNSUPPORTED_DELIMITER,
                token = delimiter
            )

        /**
         * 유효하지 않은 불린 값일 때 오류를 생성합니다.
         *
         * @param value 입력된 값
         * @return LexerException 인스턴스
         */
        fun invalidBooleanValue(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.LEXER_INVALID_BOOLEAN_VALUE,
                token = value
            )

        /**
         * 인식할 수 없는 토큰 값일 때 오류를 생성합니다.
         *
         * @param value 입력된 값
         * @return LexerException 인스턴스
         */
        fun unrecognizedTokenValue(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.UNRECOGNIZED_TOKEN_VALUE,
                token = value
            )

        /**
         * NUMBER 타입 토큰 값이 유효한 숫자가 아닐 때 오류를 생성합니다.
         *
         * @param value 입력된 값
         * @return LexerException 인스턴스
         */
        fun numberTokenInvalid(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.NUMBER_TOKEN_INVALID_NUMBER,
                token = value
            )

        /**
         * IDENTIFIER 타입 토큰 값이 유효한 식별자가 아닐 때 오류를 생성합니다.
         *
         * @param value 입력된 값
         * @return LexerException 인스턴스
         */
        fun identifierTokenInvalid(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.IDENTIFIER_TOKEN_INVALID,
                token = value
            )

        /**
         * VARIABLE 타입 토큰 값이 유효한 변수명이 아닐 때 오류를 생성합니다.
         *
         * @param value 입력된 값
         * @return LexerException 인스턴스
         */
        fun variableTokenInvalid(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.VARIABLE_TOKEN_INVALID,
                token = value
            )

        /**
         * 불린 타입 토큰 값이 'true' 또는 'false'가 아닐 때 오류를 생성합니다.
         *
         * @param value 입력된 값
         * @return LexerException 인스턴스
         */
        fun booleanTokenInvalid(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.BOOLEAN_TOKEN_INVALID,
                token = value
            )
        /**
         * 허용되지 않은 문자가 발견되었을 때 오류를 생성합니다.
         *
         * @param char 발견된 문자
         * @param codePoint 해당 문자의 코드 포인트
         */
        fun unallowedCharacter(char: Char, codePoint: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.UNALLOWED_CHARACTER,
                reason = "char='$char', codePoint=$codePoint"
            )

        /**
         * 예상치 못한 문자가 발견되었을 때 오류를 생성합니다.
         *
         * @param ch 발견된 문자
         * @return LexerException 인스턴스
         */
        fun unexpectedCharacter(ch: Char): LexerException =
            LexerException(errorCode = ErrorCode.UNEXPECTED_CHARACTER, character = ch)

        /**
         * 변수가 닫히지 않았을 때 오류를 생성합니다.
         *
         * @param tokenStr 관련 토큰 값
         * @return LexerException 인스턴스
         */
        fun unclosedVariable(tokenStr: String? = null): LexerException =
            LexerException(errorCode = ErrorCode.UNCLOSED_VARIABLE, token = tokenStr)

        /**
         * 잘못된 토큰 시퀀스일 때 오류를 생성합니다.
         *
         * @param reason 오류 사유
         * @return LexerException 인스턴스
         */
        fun invalidTokenSequence(reason: String): LexerException =
            LexerException(errorCode = ErrorCode.INVALID_TOKEN_SEQUENCE, reason = reason)

        // ── 확장(검증/규칙) 팩토리 ─────────────────────────────────────

        /**
         * 검증할 토큰 목록이 비어있을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun tokensEmpty(): LexerException =
            LexerException(errorCode = ErrorCode.TOKENS_EMPTY)

        /**
         * 토큰 타입이 기대한 타입과 일치하지 않을 때 오류를 생성합니다.
         *
         * @param expected 기대한 타입
         * @param actual 실제 타입
         * @return LexerException 인스턴스
         */
        fun tokenTypeMismatch(expected: String, actual: String): LexerException =
            LexerException(errorCode = ErrorCode.TOKEN_TYPE_MISMATCH, reason = "expected=$expected, actual=$actual")

        /**
         * 숫자 값이 유한하지 않을 때 오류를 생성합니다.
         *
         * @param value 유한하지 않은 값
         * @return LexerException 인스턴스
         */
        fun numberNotFinite(value: Double): LexerException =
            LexerException(errorCode = ErrorCode.NUMBER_NOT_FINITE, reason = "value=$value")

        /**
         * 숫자 값이 허용 범위를 벗어났을 때 오류를 생성합니다.
         *
         * @param value 실제 값
         * @param min 최소 허용 값
         * @param max 최대 허용 값
         * @return LexerException 인스턴스
         */
        fun numberOutOfRange(value: Double, min: Double, max: Double): LexerException =
            LexerException(errorCode = ErrorCode.NUMBER_OUT_OF_RANGE, reason = "value=$value, range=$min..$max")

        /**
         * 식별자 토큰이 아닐 때 오류를 생성합니다.
         *
         * @param actualType 실제 토큰 타입
         * @return LexerException 인스턴스
         */
        fun notIdentifierToken(actualType: String): LexerException =
            LexerException(errorCode = ErrorCode.NOT_IDENTIFIER_TOKEN, reason = "actual=$actualType")

        /**
         * 식별자 값이 비어있을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun identifierEmpty(): LexerException =
            LexerException(errorCode = ErrorCode.IDENTIFIER_EMPTY)

        /**
         * 식별자 길이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 길이
         * @param max 허용 최대 길이
         * @return LexerException 인스턴스
         */
        fun identifierTooLong(actual: Int, max: Int): LexerException =
            LexerException(errorCode = ErrorCode.IDENTIFIER_TOO_LONG, reason = "length=$actual, max=$max")

        /**
         * 식별자 형식이 잘못되었을 때 오류를 생성합니다.
         *
         * @param value 잘못된 식별자 값
         * @return LexerException 인스턴스
         */
        fun identifierInvalidFormat(value: String): LexerException =
            LexerException(errorCode = ErrorCode.IDENTIFIER_INVALID_FORMAT, token = value)

        /**
         * 변수 토큰이 아닐 때 오류를 생성합니다.
         *
         * @param actualType 실제 토큰 타입
         * @return LexerException 인스턴스
         */
        fun notVariableToken(actualType: String): LexerException =
            LexerException(errorCode = ErrorCode.NOT_VARIABLE_TOKEN, reason = "actual=$actualType")

        /**
         * 변수명 길이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 길이
         * @param max 허용 최대 길이
         * @return LexerException 인스턴스
         */
        fun variableNameTooLong(actual: Int, max: Int): LexerException =
            LexerException(errorCode = ErrorCode.VARIABLE_NAME_TOO_LONG, reason = "length=$actual, max=$max")

        /**
         * 변수명 형식이 잘못되었을 때 오류를 생성합니다.
         *
         * @param value 잘못된 변수명 값
         * @return LexerException 인스턴스
         */
        fun variableNameInvalidFormat(value: String): LexerException =
            LexerException(errorCode = ErrorCode.VARIABLE_NAME_INVALID_FORMAT, token = value)

        /**
         * 연산자 값이 비어있을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun operatorValueEmpty(): LexerException =
            LexerException(errorCode = ErrorCode.OPERATOR_VALUE_EMPTY)

        /**
         * 유효하지 않은 연산자 시퀀스일 때 오류를 생성합니다.
         *
         * @param current 현재 연산자 값
         * @param next 다음 연산자 값
         * @return LexerException 인스턴스
         */
        fun invalidOperatorSequence(current: String, next: String): LexerException =
            LexerException(errorCode = ErrorCode.INVALID_OPERATOR_SEQUENCE, reason = "seq='$current $next'")

        /**
         * 키워드 토큰이 아닐 때 오류를 생성합니다.
         *
         * @param actualType 실제 토큰 타입
         * @return LexerException 인스턴스
         */
        fun notKeywordToken(actualType: String): LexerException =
            LexerException(errorCode = ErrorCode.NOT_KEYWORD_TOKEN, reason = "actual=$actualType")

        /**
         * 토큰 길이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 길이
         * @param max 허용 최대 길이
         * @return LexerException 인스턴스
         */
        fun tokenTooLong(actual: Int, max: Int): LexerException =
            LexerException(errorCode = ErrorCode.TOKEN_TOO_LONG, reason = "length=$actual, max=$max")

        /**
         * EOF 토큰이 여러 개 존재할 때 오류를 생성합니다.
         *
         * @param count EOF 토큰 개수
         * @return LexerException 인스턴스
         */
        fun multipleEofTokens(count: Int): LexerException =
            LexerException(errorCode = ErrorCode.MULTIPLE_EOF_TOKENS, reason = "count=$count")

        /**
         * EOF 토큰이 마지막 위치에 있지 않을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun eofNotAtEnd(): LexerException =
            LexerException(errorCode = ErrorCode.EOF_NOT_AT_END)

        /**
         * DOLLAR 토큰 값이 '$'가 아닐 때 오류를 생성합니다.
         *
         * @param actual 실제 토큰 값
         * @return LexerException 인스턴스
         */
        fun dollarTokenInvalidValue(actual: String): LexerException =
            LexerException(errorCode = ErrorCode.DOLLAR_TOKEN_INVALID_VALUE, token = actual)

        /**
         * NUMBER 타입이지만 숫자가 아닐 때 오류를 생성합니다.
         *
         * @param value 잘못된 숫자 값
         * @return LexerException 인스턴스
         */
        fun numberTokenNotNumeric(value: String): LexerException =
            LexerException(
                errorCode = ErrorCode.NUMBER_TOKEN_NOT_NUMERIC,
                token = value
            )

        /**
         * 키워드 값이 기대와 일치하지 않을 때 오류를 생성합니다.
         *
         * @param expected 기대하는 키워드 값
         * @param actual 실제 키워드 값
         * @return LexerException 인스턴스
         */
        fun keywordValueMismatch(expected: String?, actual: String): LexerException =
            LexerException(
                errorCode = ErrorCode.KEYWORD_VALUE_MISMATCH,
                reason = "expected=$expected, actual=$actual"
            )

        /**
         * 입력 길이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 입력 길이
         * @param max 최대 허용 길이
         * @return LexerException 인스턴스
         */
        fun inputLengthExceeded(actual: Int, max: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.INPUT_LENGTH_EXCEEDED,
                reason = "actual=$actual, max=$max"
            )

        /**
         * 허용되지 않은 문자가 포함되었을 때 오류를 생성합니다.
         *
         * @param char 문제의 문자
         * @param codePoint 문자 코드 포인트
         * @return LexerException 인스턴스
         */
        fun disallowedCharacter(char: Char, codePoint: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.DISALLOWED_CHARACTER,
                reason = "char='$char', codePoint=$codePoint"
            )

        /**
         * 금지된 제어 문자가 포함되었을 때 오류를 생성합니다.
         *
         * @param codePoint 제어 문자 코드 포인트
         * @return LexerException 인스턴스
         */
        fun forbiddenControlCharacter(codePoint: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.FORBIDDEN_CONTROL_CHARACTER,
                reason = "codePoint=$codePoint"
            )

        /**
         * 라인 수가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 라인 수
         * @param max 최대 허용 라인 수
         * @return LexerException 인스턴스
         */
        fun lineCountExceeded(actual: Int, max: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.LINE_COUNT_EXCEEDED,
                reason = "actual=$actual, max=$max"
            )

        /**
         * 라인 길이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param lineIndex 라인 인덱스
         * @param actual 실제 길이
         * @param max 최대 허용 길이
         * @return LexerException 인스턴스
         */
        fun lineLengthExceeded(lineIndex: Int, actual: Int, max: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.LINE_LENGTH_EXCEEDED,
                reason = "line=${lineIndex + 1}, actual=$actual, max=$max"
            )

        /**
         * BOM 문자가 감지되었을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun bomCharacterDetected(): LexerException =
            LexerException(errorCode = ErrorCode.BOM_CHARACTER_DETECTED)

        /**
         * 널 문자가 포함되었을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun nullCharacterDetected(): LexerException =
            LexerException(errorCode = ErrorCode.NULL_CHARACTER_DETECTED)

        /**
         * 중첩 깊이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 깊이
         * @param max 최대 허용 깊이
         * @return LexerException 인스턴스
         */
        fun maxNestingDepthExceeded(actual: Int, max: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.MAX_NESTING_DEPTH_EXCEEDED,
                reason = "actual=$actual, max=$max"
            )

        /**
         * 과도한 연속 공백이 감지되었을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun excessiveWhitespaceDetected(): LexerException =
            LexerException(errorCode = ErrorCode.EXCESSIVE_WHITESPACE_DETECTED)

        /**
         * 의심스러운 반복 패턴이 감지되었을 때 오류를 생성합니다.
         *
         * @return LexerException 인스턴스
         */
        fun suspiciousRepeatPattern(): LexerException =
            LexerException(errorCode = ErrorCode.SUSPICIOUS_REPEAT_PATTERN)

        /**
         * 위치 인덱스가 유효하지 않을 때 오류를 생성합니다.
         *
         * @param actual 실제 인덱스
         * @param max 최대 허용 인덱스
         * @return LexerException 인스턴스
         */
        fun invalidPositionIndex(actual: Int, max: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_POSITION_INDEX,
                reason = "actual=$actual, max=$max"
            )

        /**
         * 라인 번호가 유효하지 않을 때 오류를 생성합니다.
         *
         * @param actual 실제 라인 번호
         * @return LexerException 인스턴스
         */
        fun invalidPositionLine(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_POSITION_LINE,
                reason = "actual=$actual"
            )

        /**
         * 열 번호가 유효하지 않을 때 오류를 생성합니다.
         *
         * @param actual 실제 열 번호
         * @return LexerException 인스턴스
         */
        fun invalidPositionColumn(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_POSITION_COLUMN,
                reason = "actual=$actual"
            )

        /**
         * 최대 토큰 길이 값이 유효하지 않을 때 오류를 생성합니다.
         *
         * @param actual 실제 설정값
         * @return LexerException 인스턴스
         */
        fun invalidMaxTokenLength(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_MAX_TOKEN_LENGTH,
                reason = "actual=$actual"
            )

        /**
         * 최대 토큰 길이가 제한을 초과했을 때 오류를 생성합니다.
         *
         * @param actual 실제 최대 토큰 길이
         * @param max 시스템 최대 허용 길이
         * @return LexerException 인스턴스
         */
        fun maxTokenLengthExceeded(actual: Int, max: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.MAX_TOKEN_LENGTH_EXCEEDED,
                reason = "actual=$actual, max=$max"
            )

        /**
         * 최대 토큰 길이가 1 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 최대 토큰 길이
         * @return LexerException 인스턴스
         */
        fun maxTokenLengthInvalid(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.MAX_TOKEN_LENGTH_INVALID,
                reason = "actual=$actual"
            )

        /**
         * 시작 시간이 유효하지 않을 때 오류를 생성합니다.
         *
         * @param actual 실제 시작 시간
         * @return LexerException 인스턴스
         */
        fun startTimeInvalid(actual: Long): LexerException =
            LexerException(
                errorCode = ErrorCode.START_TIME_INVALID,
                reason = "actual=$actual"
            )

        /**
         * 이동 거리가 0 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 이동 거리
         * @return LexerException 인스턴스
         */
        fun stepsNegative(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.STEPS_NEGATIVE,
                reason = "actual=$actual"
            )

        /**
         * 실패한 LexingResult에 error 정보가 없을 때 오류를 생성합니다.
         *
         * @param isSuccess 성공 여부
         * @param error 실제 에러 객체
         * @return LexerException 인스턴스
         */
        fun invalidLexingResultErrorState(isSuccess: Boolean, error: Throwable?): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_LEXING_RESULT_ERROR_STATE,
                reason = "isSuccess=$isSuccess, error=${error?.javaClass?.simpleName ?: "null"}"
            )

        /**
         * 분석 소요 시간이 0 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 분석 소요 시간
         * @return LexerException 인스턴스
         */
        fun negativeAnalysisDuration(actual: Long): LexerException =
            LexerException(
                errorCode = ErrorCode.NEGATIVE_ANALYSIS_DURATION,
                reason = "actual=$actual"
            )

        /**
         * 입력 텍스트 길이가 0 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 입력 길이
         * @return LexerException 인스턴스
         */
        fun negativeInputLength(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.NEGATIVE_INPUT_LENGTH,
                reason = "actual=$actual"
            )

        /**
         * 토큰 개수가 0 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 토큰 개수
         * @return LexerException 인스턴스
         */
        fun negativeTokenCount(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.NEGATIVE_TOKEN_COUNT,
                reason = "actual=$actual"
            )

        /**
         * 시작 위치가 끝 위치보다 늦을 때 오류를 생성합니다.
         *
         * @param startIndex 시작 인덱스
         * @param endIndex 끝 인덱스
         * @return LexerException 인스턴스
         */
        fun invalidPositionOrder(startIndex: Int, endIndex: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.INVALID_POSITION_ORDER,
                reason = "startIndex=$startIndex, endIndex=$endIndex"
            )

        /**
         * 토큰 길이가 0 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 토큰 길이
         * @return LexerException 인스턴스
         */
        fun negativeTokenLength(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.NEGATIVE_TOKEN_LENGTH,
                reason = "actual=$actual"
            )

        /**
         * 추가 길이가 0 미만일 때 오류를 생성합니다.
         *
         * @param actual 실제 추가 길이
         * @return LexerException 인스턴스
         */
        fun negativeAdditionalLength(actual: Int): LexerException =
            LexerException(
                errorCode = ErrorCode.NEGATIVE_ADDITIONAL_LENGTH,
                reason = "actual=$actual"
            )
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