package hs.kr.entrydsm.application.global.feign.client

import hs.kr.entrydsm.application.global.feign.FeignConfig
import hs.kr.entrydsm.application.global.feign.client.dto.LocationElement
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "LocationClient",
    url = "\${kakao.url}",
    configuration = [FeignConfig::class]
)
interface LocationClient {

    @GetMapping("/v2/local/search/address.json")
    fun getLocationInfo(
        @RequestParam("query") streetAddress: String,
        @RequestHeader("Authorization") kakaoAuthorization: String
    ): LocationElement
}