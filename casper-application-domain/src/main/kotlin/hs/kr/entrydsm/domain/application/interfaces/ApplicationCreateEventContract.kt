package hs.kr.entrydsm.domain.application.interfaces

import java.util.UUID

interface ApplicationCreateEventContract {
    fun publishCreateApplication(receiptCode: Long, userId: UUID)
}