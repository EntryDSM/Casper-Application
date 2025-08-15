package hs.kr.entrydsm.global.constants.error

/**
 * 공통 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object CommonErrorCodes {
    val UNKNOWN_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.UNKNOWN_ERROR
    val INVALID_ARGUMENT = hs.kr.entrydsm.global.exception.ErrorCode.VALIDATION_FAILED
    val NULL_POINTER = hs.kr.entrydsm.global.exception.ErrorCode.INTERNAL_SERVER_ERROR
    val ILLEGAL_STATE = hs.kr.entrydsm.global.exception.ErrorCode.BUSINESS_RULE_VIOLATION
    val TIMEOUT = hs.kr.entrydsm.global.exception.ErrorCode.INTERNAL_SERVER_ERROR
    val PERMISSION_DENIED = hs.kr.entrydsm.global.exception.ErrorCode.BUSINESS_RULE_VIOLATION
    val RESOURCE_NOT_FOUND = hs.kr.entrydsm.global.exception.ErrorCode.VALIDATION_FAILED
    val RESOURCE_ALREADY_EXISTS = hs.kr.entrydsm.global.exception.ErrorCode.BUSINESS_RULE_VIOLATION
    val CONFIGURATION_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.INTERNAL_SERVER_ERROR
    val VALIDATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.VALIDATION_FAILED
}