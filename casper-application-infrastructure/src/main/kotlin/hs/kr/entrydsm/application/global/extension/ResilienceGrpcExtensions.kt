package hs.kr.entrydsm.application.global.extension

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ResilienceGrpcExtensions")

suspend fun <T> executeGrpcCallWithResilience(
    retry: Retry,
    circuitBreaker: CircuitBreaker,
    fallback: suspend () -> T,
    block: suspend () -> T
): T = 
    try {
        retry.executeSuspendFunction {
            circuitBreaker.executeSuspendFunction(block)
        }
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: Exception) {
        log.warn("gRPC 호출 실패, fallback 실행: {}", e.toString())
        fallback()
    }