package hs.kr.entrydsm.application.domain.application.event.spi

import java.util.UUID

interface ApplicationEventPort {
    fun create(receiptCode: Long, userId: UUID)

    fun createApplicationScoreRollback(receiptCode: Long)

    fun cancelSubmittedApplication(receiptCode: Long)
}
