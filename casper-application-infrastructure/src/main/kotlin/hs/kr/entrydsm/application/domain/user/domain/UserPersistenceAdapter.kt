package hs.kr.entrydsm.application.domain.user.domain

import hs.kr.entrydsm.application.domain.user.model.User
import hs.kr.entrydsm.application.domain.user.spi.UserPort
import hs.kr.entrydsm.application.global.grpc.client.user.UserGrpcClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val userGrpcClient: UserGrpcClient
) : UserPort {
    override suspend fun queryUserByUserId(userId: UUID): User {
        return userGrpcClient.getUserInfoByUserId(userId).run {
            User(
                id = id,
                phoneNumber = phoneNumber,
                name = name,
                isParent = isParent,
            )
        }
    }
}
