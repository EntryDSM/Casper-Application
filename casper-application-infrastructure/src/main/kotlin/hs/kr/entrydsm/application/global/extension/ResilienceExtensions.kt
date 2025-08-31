package hs.kr.entrydsm.application.global.extension

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import kotlinx.coroutines.coroutineScope

suspend fun <T> executeGrpcCallWithResilience(
    retry: Retry,
    circuitBreaker: CircuitBreaker,
    fallback: suspend () -> T,
    block: suspend () -> T
): T = coroutineScope {
    try {
        circuitBreaker.executeSuspendFunction {
            retry.executeSuspendFunction(block)
        }
    }catch (e: Exception) {
        fallback()
    }
}