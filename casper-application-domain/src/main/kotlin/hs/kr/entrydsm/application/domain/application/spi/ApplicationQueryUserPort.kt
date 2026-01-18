package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.user.model.User
import java.util.UUID

interface ApplicationQueryUserPort {
    suspend fun queryUserByUserId(userId: UUID): User
}
