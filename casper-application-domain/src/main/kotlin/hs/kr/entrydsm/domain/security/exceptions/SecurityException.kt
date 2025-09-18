package hs.kr.entrydsm.domain.security.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * 보안 관련 최상위 예외 클래스입니다.
 * 
 * 인증 및 인가와 관련된 도메인 예외를 정의합니다.
 */
sealed class SecurityException(
    errorCode: ErrorCode,
    message: String
) : DomainException(errorCode, message) {

    /**
     * 유효하지 않은 토큰일 경우 발생하는 예외입니다.
     */
    class InvalidTokenException(
        token: String? = null
    ) : SecurityException(
        errorCode = ErrorCode.SECURITY_INVALID_TOKEN,
        message = "Invalid authentication token${if (token != null) ": $token" else ""}"
    )

    /**
     * 인증되지 않은 사용자일 경우 발생하는 예외입니다.
     */
    class UnauthenticatedException(
        context: String? = null
    ) : SecurityException(
        errorCode = ErrorCode.SECURITY_UNAUTHENTICATED,
        message = "User is not authenticated${if (context != null) ": $context" else ""}"
    )
}

