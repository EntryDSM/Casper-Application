package hs.kr.entrydsm.application.domain.user.domain

import hs.kr.entrydsm.application.domain.user.model.User
import hs.kr.entrydsm.application.domain.user.spi.UserPort
import hs.kr.entrydsm.application.global.feign.client.UserClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val userClient: UserClient,
) : UserPort {
    override fun queryUserByUserId(userId: UUID): User {
        return userClient.getUserInfoByUserId(userId).run {
            User(
                id = id,
                phoneNumber = phoneNumber,
                name = name,
                isParent = isParent,
            )
        }
    }
}
