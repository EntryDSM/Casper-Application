package hs.kr.entrydsm.application.global.grpc.test

import hs.kr.entrydsm.application.global.extension.executeGrpcCallWithResilience
import hs.kr.entrydsm.application.global.grpc.dto.schedule.InternalScheduleResponse
import hs.kr.entrydsm.application.global.grpc.dto.schedule.ScheduleType
import hs.kr.entrydsm.application.global.grpc.dto.status.ApplicationStatus
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusListResponse
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusResponse
import hs.kr.entrydsm.application.global.grpc.dto.user.InternalUserResponse
import hs.kr.entrydsm.application.global.grpc.dto.user.UserRole
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@RestController
@RequestMapping("/test/dummy-grpc")
class DummyGrpcResilienceTestController(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry
) {

    // 실패 시뮬레이션을 위한 플래그
    private var shouldFailUser = false
    private var shouldFailStatus = false
    private var shouldBeSlowUser = false
    private var shouldBeSlowStatus = false
    private var shouldFailSchedule = false
    private var shouldBeSlowSchedule = false


    @GetMapping("/schedule")
    suspend fun testDummyScheduleGrpc(type: String): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        val retry = retryRegistry.retry("schedule-grpc")
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("schedule-grpc")

        return try {
            val response = executeGrpcCallWithResilience(
                retry = retry,
                circuitBreaker = circuitBreaker,
                fallback = {
                    // Fallback 응답
                    InternalScheduleResponse(
                        type = ScheduleType.FIRST_ANNOUNCEMENT,
                        date = LocalDateTime.now()
                    )
                }
            ) {
                simulateScheduleGrpcCall(type)
            }

            mapOf(
                "success" to true,
                "data" to response,
                "executionTime" to "${System.currentTimeMillis() - startTime}ms",
                "circuitBreakerState" to getCircuitBreakerState("schedule-grpc"),
                "retryMetrics" to getRetryMetrics("schedule-grpc"),
                "source" to if (response.type.name.contains("FIRST_ANNOUNCEMENT")) "fallback" else "grpc"
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to (e.message ?: "Unknown error"),
                "executionTime" to "${System.currentTimeMillis() - startTime}ms",
                "circuitBreakerState" to getCircuitBreakerState("schedule-grpc"),
                "retryMetrics" to getRetryMetrics("schedule-grpc")
            )
        }
    }

    @GetMapping("/user/{userId}")
    suspend fun testDummyUserGrpc(@PathVariable userId: String): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        val retry = retryRegistry.retry("user-grpc")
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("user-grpc")

        return try {
            val response = executeGrpcCallWithResilience(
                retry = retry,
                circuitBreaker = circuitBreaker,
                fallback = {
                    // Fallback 응답
                    InternalUserResponse(
                        id = UUID.fromString(userId),
                        phoneNumber = "N/A",
                        name = "Unknown User (Fallback)",
                        isParent = false,
                        role = UserRole.USER
                    )
                }
            ) {
                // 더미 gRPC 호출 시뮬레이션
                simulateUserGrpcCall(userId)
            }

            mapOf(
                "success" to true,
                "data" to response,
                "executionTime" to "${System.currentTimeMillis() - startTime}ms",
                "circuitBreakerState" to getCircuitBreakerState("user-grpc"),
                "retryMetrics" to getRetryMetrics("user-grpc"),
                "source" to if (response.name.contains("Fallback")) "fallback" else "grpc"
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to (e.message ?: "Unknown error"),
                "executionTime" to "${System.currentTimeMillis() - startTime}ms",
                "circuitBreakerState" to getCircuitBreakerState("user-grpc"),
                "retryMetrics" to getRetryMetrics("user-grpc")
            )
        }
    }

    @GetMapping("/status/list")
    suspend fun testDummyStatusListGrpc(): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        val retry = retryRegistry.retry("status-grpc")
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("status-grpc")

        return try {
            val response = executeGrpcCallWithResilience(
                retry = retry,
                circuitBreaker = circuitBreaker,
                fallback = {
                    // Fallback 응답
                    InternalStatusListResponse(statusList = emptyList())
                }
            ) {
                // 더미 gRPC 호출 시뮬레이션
                simulateStatusListGrpcCall()
            }

            mapOf(
                "success" to true,
                "data" to response,
                "executionTime" to "${System.currentTimeMillis() - startTime}ms",
                "circuitBreakerState" to getCircuitBreakerState("status-grpc"),
                "retryMetrics" to getRetryMetrics("status-grpc"),
                "source" to if (response.statusList.isEmpty()) "fallback" else "grpc"
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to (e.message ?: "Unknown error"),
                "executionTime" to "${System.currentTimeMillis() - startTime}ms",
                "circuitBreakerState" to getCircuitBreakerState("status-grpc"),
                "retryMetrics" to getRetryMetrics("status-grpc")
            )
        }
    }

    // 실패/성공 시뮬레이션 제어 API
    @PostMapping("/control/user/fail/{shouldFail}")
    fun setUserFailure(@PathVariable shouldFail: Boolean): Map<String, Any> {
        shouldFailUser = shouldFail
        return mapOf(
            "message" to "User service failure simulation set to: $shouldFail",
            "currentState" to mapOf(
                "shouldFailUser" to shouldFailUser,
                "shouldBeSlowUser" to shouldBeSlowUser
            )
        )
    }

    @PostMapping("/control/status/fail/{shouldFail}")
    fun setStatusFailure(@PathVariable shouldFail: Boolean): Map<String, Any> {
        shouldFailStatus = shouldFail
        return mapOf(
            "message" to "Status service failure simulation set to: $shouldFail",
            "currentState" to mapOf(
                "shouldFailStatus" to shouldFailStatus,
                "shouldBeSlowStatus" to shouldBeSlowStatus
            )
        )
    }

    @PostMapping("/control/schedule/fail/{shouldFail}")
    fun setScheduleFailure(@PathVariable shouldFail: Boolean): Map<String, Any> {
        shouldFailSchedule = shouldFail
        return mapOf(
            "message" to "Schedule service failure simulation set to: $shouldFail",
            "currentState" to mapOf(
                "shouldFailSchedule" to shouldFailSchedule,
                "shouldBeSlowSchedule" to shouldBeSlowSchedule
            )
        )
    }

    @PostMapping("/control/user/slow/{shouldBeSlow}")
    fun setUserSlow(@PathVariable shouldBeSlow: Boolean): Map<String, Any> {
        shouldBeSlowUser = shouldBeSlow
        return mapOf(
            "message" to "User service slow call simulation set to: $shouldBeSlow",
            "currentState" to mapOf(
                "shouldFailUser" to shouldFailUser,
                "shouldBeSlowUser" to shouldBeSlowUser
            )
        )
    }

    @PostMapping("/control/status/slow/{shouldBeSlow}")
    fun setStatusSlow(@PathVariable shouldBeSlow: Boolean): Map<String, Any> {
        shouldBeSlowStatus = shouldBeSlow
        return mapOf(
            "message" to "Status service slow call simulation set to: $shouldBeSlow",
            "currentState" to mapOf(
                "shouldFailStatus" to shouldFailStatus,
                "shouldBeSlowStatus" to shouldBeSlowStatus
            )
        )
    }

    @PostMapping("/control/schedule/slow/{shouldBeSlow}")
    fun setScheduleSlow(@PathVariable shouldBeSlow: Boolean): Map<String, Any> {
        shouldBeSlowSchedule = shouldBeSlow
        return mapOf(
            "message" to "Schedule service slow call simulation set to: $shouldBeSlow",
            "currentState" to mapOf(
                "shouldFailSchedule" to shouldFailSchedule,
                "shouldBeSlowSchedule" to shouldBeSlowSchedule
            )
        )
    }

    @GetMapping("/control/status")
    fun getControlStatus(): Map<String, Any> {
        return mapOf(
            "userService" to mapOf(
                "shouldFail" to shouldFailUser,
                "shouldBeSlow" to shouldBeSlowUser
            ),
            "statusService" to mapOf(
                "shouldFail" to shouldFailStatus,
                "shouldBeSlow" to shouldBeSlowStatus
            ),
            "scheduleService" to mapOf(
                "shouldFail" to shouldFailSchedule,
                "shouldBeSlow" to shouldBeSlowSchedule
            )
        )
    }

    @PostMapping("/reset")
    fun resetAll(): Map<String, String> {
        // 플래그 리셋
        shouldFailUser = false
        shouldFailStatus = false
        shouldFailSchedule = false
        shouldBeSlowUser = false
        shouldBeSlowStatus = false
        shouldBeSlowSchedule = false

        // 서킷 브레이커 리셋
        circuitBreakerRegistry.circuitBreaker("user-grpc").reset()
        circuitBreakerRegistry.circuitBreaker("status-grpc").reset()
        circuitBreakerRegistry.circuitBreaker("schedule-grpc").reset()

        return mapOf("message" to "All controls and metrics reset successfully")
    }

    @GetMapping("/metrics")
    fun getMetrics(): Map<String, Any> {
        return mapOf(
            "userGrpc" to mapOf(
                "circuitBreaker" to getCircuitBreakerState("user-grpc"),
                "retry" to getRetryMetrics("user-grpc")
            ),
            "statusGrpc" to mapOf(
                "circuitBreaker" to getCircuitBreakerState("status-grpc"),
                "retry" to getRetryMetrics("status-grpc")
            ),
            "scheduleGrpc" to mapOf(
                "circuitBreaker" to getCircuitBreakerState("schedule-grpc"),
                "retry" to getRetryMetrics("schedule-grpc")
            ),
            "controlFlags" to mapOf(
                "shouldFailUser" to shouldFailUser,
                "shouldFailStatus" to shouldFailStatus,
                "shouldFailSchedule" to shouldFailSchedule,
                "shouldBeSlowUser" to shouldBeSlowUser,
                "shouldBeSlowStatus" to shouldBeSlowStatus,
                "shouldBeSlowSchedule" to shouldBeSlowSchedule,
            )
        )
    }

    // 더미 gRPC 호출 시뮬레이션 함수들
    private suspend fun simulateUserGrpcCall(userId: String): InternalUserResponse {
        // 느린 호출 시뮬레이션 (3초 지연)
        if (shouldBeSlowUser) {
            delay(3000)
        }

        // 일반 지연 (실제 네트워크 지연 시뮬레이션)
        delay(Random.nextLong(100, 500))

        // 실패 시뮬레이션
        if (shouldFailUser) {
            throw RuntimeException("Simulated gRPC User Service failure")
        }

        // 성공 응답
        return InternalUserResponse(
            id = UUID.fromString(userId),
            phoneNumber = "010-1234-5678",
            name = "김철수",
            isParent = false,
            role = UserRole.USER
        )
    }

    private suspend fun simulateStatusListGrpcCall(): InternalStatusListResponse {
        // 느린 호출 시뮬레이션 (3초 지연)
        if (shouldBeSlowStatus) {
            delay(3000)
        }

        // 일반 지연 (실제 네트워크 지연 시뮬레이션)
        delay(Random.nextLong(100, 500))

        // 실패 시뮬레이션
        if (shouldFailStatus) {
            throw RuntimeException("Simulated gRPC Status Service failure")
        }

        // 성공 응답
        return InternalStatusListResponse(
            statusList = listOf(
                InternalStatusResponse(
                    id = 1L,
                    applicationStatus = ApplicationStatus.SUBMITTED,
                    examCode = "A123",
                    isFirstRoundPass = true,
                    isSecondRoundPass = false,
                    receiptCode = 12345L
                ),
                InternalStatusResponse(
                    id = 2L,
                    applicationStatus = ApplicationStatus.WRITING,
                    examCode = null,
                    isFirstRoundPass = false,
                    isSecondRoundPass = false,
                    receiptCode = 12346L
                )
            )
        )
    }

    private suspend fun simulateScheduleGrpcCall(type: String): InternalScheduleResponse {
        // 느린 호출 시뮬레이션 (3초 지연)
        if (shouldBeSlowSchedule) {
            delay(3000)
        }

        // 일반 지연 (실제 네트워크 지연 시뮬레이션)
        delay(Random.nextLong(100, 500))

        // 실패 시뮬레이션
        if (shouldFailSchedule) {
            throw RuntimeException("Simulated gRPC Schedule Service failure")
        }

        // 성공 응답
        return InternalScheduleResponse(
            type = ScheduleType.FIRST_ANNOUNCEMENT,
            date = LocalDateTime.now()
        )
    }

    private fun getCircuitBreakerState(name: String): Map<String, Any> {
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker(name)
        val metrics = circuitBreaker.metrics

        return mapOf(
            "state" to circuitBreaker.state.name,
            "failureRate" to String.format("%.2f%%", metrics.failureRate),
            "slowCallRate" to String.format("%.2f%%", metrics.slowCallRate),
            "numberOfCalls" to metrics.numberOfBufferedCalls,
            "numberOfFailedCalls" to metrics.numberOfFailedCalls,
            "numberOfSlowCalls" to metrics.numberOfSlowCalls,
            "numberOfSuccessfulCalls" to metrics.numberOfSuccessfulCalls
        )
    }

    private fun getRetryMetrics(name: String): Map<String, Long> {
        val retry = retryRegistry.retry(name)
        val eventPublisher = retry.eventPublisher
        
        return mapOf(
            "numberOfSuccessfulCalls" to 0L,
            "numberOfFailedCalls" to 0L,
            "numberOfRetryAttempts" to 0L
        )
    }
}