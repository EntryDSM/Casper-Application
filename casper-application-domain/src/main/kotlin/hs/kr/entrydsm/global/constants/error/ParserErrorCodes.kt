package hs.kr.entrydsm.global.constants.error

/**
 * 파서 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object ParserErrorCodes {
    val PARSING_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.PARSING_ERROR
    val SYNTAX_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.SYNTAX_ERROR
    val UNEXPECTED_EOF = hs.kr.entrydsm.global.exception.ErrorCode.UNEXPECTED_END_OF_INPUT
    val GRAMMAR_VIOLATION = hs.kr.entrydsm.global.exception.ErrorCode.GRAMMAR_CONFLICT
    val LR_CONFLICT = hs.kr.entrydsm.global.exception.ErrorCode.LR_PARSING_ERROR
    val SHIFT_REDUCE_CONFLICT = hs.kr.entrydsm.global.exception.ErrorCode.GRAMMAR_CONFLICT
    val REDUCE_REDUCE_CONFLICT = hs.kr.entrydsm.global.exception.ErrorCode.GRAMMAR_CONFLICT
    val INVALID_PRODUCTION = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_AST_NODE
    val PARSER_STATE_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.LR_PARSING_ERROR
    val AST_CONSTRUCTION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.AST_BUILD_ERROR
}