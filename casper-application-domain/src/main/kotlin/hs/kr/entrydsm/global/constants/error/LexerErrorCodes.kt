package hs.kr.entrydsm.global.constants.error

/**
 * 렉서 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object LexerErrorCodes {
    val TOKENIZATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_TOKEN_SEQUENCE
    val INVALID_CHARACTER = hs.kr.entrydsm.global.exception.ErrorCode.UNEXPECTED_CHARACTER
    val UNEXPECTED_TOKEN = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_TOKEN_SEQUENCE
    val INVALID_NUMBER_FORMAT = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_NUMBER_FORMAT
    val INVALID_STRING_LITERAL = hs.kr.entrydsm.global.exception.ErrorCode.UNEXPECTED_CHARACTER
    val UNCLOSED_STRING = hs.kr.entrydsm.global.exception.ErrorCode.UNCLOSED_VARIABLE
    val INVALID_IDENTIFIER = hs.kr.entrydsm.global.exception.ErrorCode.UNEXPECTED_CHARACTER
    val TOKEN_POSITION_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_TOKEN_SEQUENCE
    val LEXER_STATE_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_TOKEN_SEQUENCE
    val CHARACTER_ENCODING_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.UNEXPECTED_CHARACTER
}