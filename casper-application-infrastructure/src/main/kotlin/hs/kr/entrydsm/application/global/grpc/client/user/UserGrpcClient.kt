package hs.kr.entrydsm.application.global.grpc.client.user

import hs.kr.entrydsm.application.global.extension.executeGrpcCallWithResilience
import hs.kr.entrydsm.application.global.grpc.dto.user.InternalUserResponse
import hs.kr.entrydsm.domain.user.value.UserRole
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

/**
 * 사용자 서비스와의 gRPC 통신을 담당하는 클라이언트 클래스입니다.
 *
 * @property channel gRPC 통신을 위한 채널 (user-service로 자동 주입됨)
 */
@Component
class UserGrpcClient(
    @Qualifier("userGrpcRetry") private val retry: Retry,
    @Qualifier("userGrpcCircuitBreaker") private val circuitBreaker: CircuitBreaker
) {
    @GrpcClient("user-service")
    lateinit var channel: Channel

    /**
     * 사용자 ID를 기반으로 사용자 정보를 비동기적으로 조회합니다.
     * gRPC 비동기 스트리밍을 사용하여 사용자 서비스로부터 정보를 가져옵니다.
     *
     * @param userId 조회할 사용자의 고유 식별자(UUID)
     * @return 조회된 사용자 정보를 담은 [InternalUserResponse] 객체
     * @throws io.grpc.StatusRuntimeException gRPC 서버에서 오류가 발생한 경우
     * @throws java.util.concurrent.CancellationException 코루틴이 취소된 경우
     */
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
                    role = UserRole.USER
                )
            }
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

    /**
     * gRPC 프로토콜 사용자 역할을 도메인 사용자 역할로 변환합니다.
     *
     * @param protoUserRole 변환할 gRPC 프로토콜 사용자 역할
     * @return 도메인 사용자 역할
     */
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
