package hs.kr.entrydsm.application.domain.user.domain

import hs.kr.entrydsm.application.global.grpc.client.user.UserGrpcClient
import hs.kr.entrydsm.domain.user.aggregates.User
import hs.kr.entrydsm.domain.user.interfaces.UserContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 사용자 정보 조회를 위한 퍼시스턴스 어댑터입니다.
 * 
 * gRPC를 통해 외부 User 서비스와 통신하여 사용자 정보를 조회하고,
 * 도메인 모델로 변환하는 역할을 담당합니다.
 */
@Component
class UserPersistenceAdapter(
    private val userGrpcClient: UserGrpcClient
) : UserContract {

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     * 
     * gRPC 클라이언트를 통해 User 서비스에서 사용자 정보를 조회하고,
     * 응답 데이터를 도메인 모델로 변환하여 반환합니다.
     * 
     * @param userId 조회할 사용자의 고유 식별자
     * @return 조회된 사용자 정보가 담긴 User 도메인 모델
     */
    override fun queryUserByUserId(userId: UUID): User = runBlocking {
        userGrpcClient.getUserInfoByUserId(userId).run {
            User(
                id = id,
                phoneNumber = phoneNumber,
                name = name,
                isParent = isParent,
            )
        }

    }
}
