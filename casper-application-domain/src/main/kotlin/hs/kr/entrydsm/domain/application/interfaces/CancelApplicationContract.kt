package hs.kr.entrydsm.domain.application.interfaces

import java.util.UUID

interface CancelApplicationContract {
    fun cancelApplication(userId: UUID)
}
