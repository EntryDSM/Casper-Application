package hs.kr.entrydsm.domain.user.interfaces

import hs.kr.entrydsm.domain.user.aggregates.User
import java.util.UUID

interface ApplicationQueryUserContract {
    fun queryUserByUserId(userId: UUID): User
}