package hs.kr.entrydsm.application.global.config

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CalculatorConfig {
    
    @Bean
    fun calculator(): Calculator {
        return Calculator()
    }
}