package hs.kr.entrydsm.application.global.feign.exception

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

sealed class FeignException(
    errorCode: ErrorCode,
    message: String,
) : DomainException(errorCode, message) {
    class FeignServerErrorException(statusCode: Int, methodKey: String?) : FeignException(
        errorCode = ErrorCode.FEIGN_SERVER_ERROR,
        message = "Feign server error: $statusCode for method: $methodKey",
    )
}
