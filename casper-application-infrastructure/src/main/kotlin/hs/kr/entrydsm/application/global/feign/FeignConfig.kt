package hs.kr.entrydsm.application.global.feign

import feign.Logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableFeignClients
@Configuration
class FeignConfig {
    @Bean
    @ConditionalOnMissingBean(value = [FeignClientErrorDecoder::class])
    fun commonFeignErrorDecoder(): FeignClientErrorDecoder? {
        return FeignClientErrorDecoder()
    }

    @Bean
    fun feignLoggerLevel(): Logger.Level? {
        return Logger.Level.FULL
    }
}
