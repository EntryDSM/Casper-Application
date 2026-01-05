package hs.kr.entrydsm.application.global.grpc.client.user

import hs.kr.entrydsm.application.global.extension.executeGrpcCallWithResilience
import hs.kr.entrydsm.application.global.grpc.dto.user.InternalUserResponse
import hs.kr.entrydsm.application.global.security.jwt.UserRole
import hs.kr.entrydsm.casper.user.proto.UserServiceGrpc
import hs.kr.entrydsm.casper.user.proto.UserServiceProto
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Component
class UserGrpcClient(
    @Qualifier("userGrpcRetry") private val retry: Retry,
    @Qualifier("userGrpcCircuitBreaker") private val circuitBreaker: CircuitBreaker,
) {
    @GrpcClient("user-grpc")
    lateinit var channel: Channel

    suspend fun getUserInfoByUserId(userId: UUID): InternalUserResponse {
        return executeGrpcCallWithResilience(
            retry = retry,
            circuitBreaker = circuitBreaker,
            fallback = {
                InternalUserResponse(
                    id = userId,
                    phoneNumber = "N/A",
                    name = "Unknown User",
                    isParent = false,
                    role = UserRole.USER,
                )
            },
        ) {
            val userStub = UserServiceGrpc.newStub(channel)

            val request =
                UserServiceProto.GetUserInfoRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build()

            val response =
                suspendCancellableCoroutine { continuation ->
                    userStub.getUserInfoByUserId(
                        request,
                        object : StreamObserver<UserServiceProto.GetUserInfoResponse> {
                            override fun onNext(value: UserServiceProto.GetUserInfoResponse) {
                                continuation.resume(value)
                            }

                            override fun onError(t: Throwable) {
                                continuation.resumeWithException(t)
                            }

                            override fun onCompleted() {}
                        },
                    )
                }

            InternalUserResponse(
                id = UUID.fromString(response.id),
                phoneNumber = response.phoneNumber,
                name = response.name,
                isParent = response.isParent,
                role = mapProtoUserRole(response.role),
            )
        }
    }

    private fun mapProtoUserRole(protoUserRole: UserServiceProto.UserRole): UserRole {
        return when (protoUserRole) {
            UserServiceProto.UserRole.ROOT -> UserRole.ROOT
            UserServiceProto.UserRole.USER -> UserRole.USER
            UserServiceProto.UserRole.ADMIN -> UserRole.ADMIN
            UserServiceProto.UserRole.UNSPECIFIED -> UserRole.USER // 기본값으로 USER 설정
            else -> UserRole.USER
        }
    }
}