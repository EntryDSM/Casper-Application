package hs.kr.entrydsm.application.global.feign

import feign.Logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Feign Client 설정을 위한 클래스입니다.
 */
@EnableFeignClients
@Configuration
class FeignConfig {
    /**
     * Feign Client의 에러 디코더를 설정합니다.
     *
     * @return FeignClientErrorDecoder
     */
    @Bean
    @ConditionalOnMissingBean(value = [FeignClientErrorDecoder::class])
    fun commonFeignErrorDecoder(): FeignClientErrorDecoder? {
        return FeignClientErrorDecoder()
    }

    /**
     * Feign Client의 로거 레벨을 설정합니다.
     *
     * @return Logger.Level
     */
    @Bean
    fun feignLoggerLevel(): Logger.Level? {
        return Logger.Level.FULL
    }
}
