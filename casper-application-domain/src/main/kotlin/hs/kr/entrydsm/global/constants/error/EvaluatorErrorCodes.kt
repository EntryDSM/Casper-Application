package hs.kr.entrydsm.global.constants.error

/**
 * 평가기 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object EvaluatorErrorCodes {
    val EVALUATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.EVALUATION_ERROR
    val UNDEFINED_VARIABLE = hs.kr.entrydsm.global.exception.ErrorCode.UNDEFINED_VARIABLE
    val TYPE_MISMATCH = hs.kr.entrydsm.global.exception.ErrorCode.UNSUPPORTED_TYPE
    val DIVISION_BY_ZERO = hs.kr.entrydsm.global.exception.ErrorCode.DIVISION_BY_ZERO
    val FUNCTION_NOT_FOUND = hs.kr.entrydsm.global.exception.ErrorCode.UNSUPPORTED_FUNCTION
    val INVALID_FUNCTION_ARGUMENTS = hs.kr.entrydsm.global.exception.ErrorCode.WRONG_ARGUMENT_COUNT
    val ARITHMETIC_OVERFLOW = hs.kr.entrydsm.global.exception.ErrorCode.MATH_ERROR
    val INVALID_OPERATION = hs.kr.entrydsm.global.exception.ErrorCode.UNSUPPORTED_OPERATOR
    val CONTEXT_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.EVALUATION_ERROR
    val SECURITY_VIOLATION = hs.kr.entrydsm.global.exception.ErrorCode.BUSINESS_RULE_VIOLATION
    val PERFORMANCE_LIMIT_EXCEEDED = hs.kr.entrydsm.global.exception.ErrorCode.EVALUATION_ERROR
    val UNSUPPORTED_TYPE = hs.kr.entrydsm.global.exception.ErrorCode.UNSUPPORTED_TYPE
}