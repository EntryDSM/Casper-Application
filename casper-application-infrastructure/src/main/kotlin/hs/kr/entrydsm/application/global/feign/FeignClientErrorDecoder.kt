package hs.kr.entrydsm.application.global.feign

import feign.Response
import feign.codec.ErrorDecoder
import hs.kr.entrydsm.application.global.feign.exception.FeignException

class FeignClientErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String?,
        response: Response,
    ): Exception? {
        if (response.status() >= 400) {
            throw FeignException.FeignServerErrorException(response.status(), methodKey)
        }
        return feign.FeignException.errorStatus(methodKey, response)
    }
}
