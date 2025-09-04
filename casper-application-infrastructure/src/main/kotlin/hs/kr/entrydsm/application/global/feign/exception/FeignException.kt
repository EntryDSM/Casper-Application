package hs.kr.entrydsm.application.global.feign.exception

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Feign 관련 최상위 예외 클래스 입니다.
 */
sealed class FeignException(
    errorCode: ErrorCode,
    message: String,
) : DomainException(errorCode, message) {
    /**
     * Feign 서버 오류시 발생하는 예외입니다.
     */
    class FeignServerErrorException(statusCode: Int, methodKey: String?) : FeignException(
        errorCode = ErrorCode.FEIGN_SERVER_ERROR,
        message = "Feign server error: $statusCode for method: $methodKey",
    )
}
