package hs.kr.entrydsm.application.global.feign

import feign.Response
import feign.codec.ErrorDecoder
import hs.kr.entrydsm.application.global.feign.exception.FeignException

/**
 * Feign Client의 에러를 디코딩하는 클래스입니다.
 */
class FeignClientErrorDecoder : ErrorDecoder {
    /**
     * Feign 요청 중 발생한 에러를 디코딩합니다.
     *
     * @param methodKey 요청한 메소드 키
     * @param response 응답
     * @return 디코딩된 예외
     */
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
