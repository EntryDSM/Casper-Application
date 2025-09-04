package hs.kr.entrydsm.application.domain.user.domain

import hs.kr.entrydsm.application.global.grpc.client.user.UserGrpcClient
import hs.kr.entrydsm.domain.user.aggregates.User
import hs.kr.entrydsm.domain.user.interfaces.UserContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val userGrpcClient: UserGrpcClient,
) : UserContract {
    override fun queryUserByUserId(userId: UUID): User =
        runBlocking {
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
