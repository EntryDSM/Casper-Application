package hs.kr.entrydsm.application.global.config

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResilienceConfig(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry
) {

    @Bean
    fun userGrpcCircuitBreaker(): CircuitBreaker {
        return circuitBreakerRegistry.circuitBreaker("user-grpc")
    }

    @Bean
    fun statusGrpcCircuitBreaker(): CircuitBreaker {
        return circuitBreakerRegistry.circuitBreaker("status-grpc")
    }

    @Bean
    fun userGrpcRetry(): Retry {
        return retryRegistry.retry("user-grpc")
    }

    @Bean
    fun statusGrpcRetry(): Retry {
        return retryRegistry.retry("status-grpc")
    }
}
