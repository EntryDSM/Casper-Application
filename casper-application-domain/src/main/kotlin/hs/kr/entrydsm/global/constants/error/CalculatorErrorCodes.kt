package hs.kr.entrydsm.global.constants.error

/**
 * 계산기 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object CalculatorErrorCodes {
    val CALCULATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.EVALUATION_ERROR
    val FORMULA_TOO_LONG = hs.kr.entrydsm.global.exception.ErrorCode.FORMULA_TOO_LONG
    val TOO_MANY_VARIABLES = hs.kr.entrydsm.global.exception.ErrorCode.TOO_MANY_VARIABLES
    val INVALID_FORMULA = hs.kr.entrydsm.global.exception.ErrorCode.EMPTY_FORMULA
    val CALCULATION_TIMEOUT = hs.kr.entrydsm.global.exception.ErrorCode.INTERNAL_SERVER_ERROR
    val MEMORY_LIMIT_EXCEEDED = hs.kr.entrydsm.global.exception.ErrorCode.INTERNAL_SERVER_ERROR
    val STEP_LIMIT_EXCEEDED = hs.kr.entrydsm.global.exception.ErrorCode.TOO_MANY_STEPS
    val RECURSIVE_CALCULATION = hs.kr.entrydsm.global.exception.ErrorCode.BUSINESS_RULE_VIOLATION
    val INVALID_RESULT = hs.kr.entrydsm.global.exception.ErrorCode.EVALUATION_ERROR
    val CALCULATION_INTERRUPTED = hs.kr.entrydsm.global.exception.ErrorCode.INTERNAL_SERVER_ERROR
}