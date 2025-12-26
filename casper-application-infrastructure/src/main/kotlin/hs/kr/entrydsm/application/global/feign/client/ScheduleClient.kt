package hs.kr.entrydsm.application.global.feign.client

import hs.kr.entrydsm.application.global.feign.FeignConfig
import hs.kr.entrydsm.application.global.feign.client.dto.response.ScheduleInfoElement
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "ScheduleClient", url = "\${url.schedule}", configuration = [FeignConfig::class])
interface ScheduleClient {
    @GetMapping("/schedule")
    fun queryScheduleByType(@RequestParam type: String): ScheduleInfoElement?
}