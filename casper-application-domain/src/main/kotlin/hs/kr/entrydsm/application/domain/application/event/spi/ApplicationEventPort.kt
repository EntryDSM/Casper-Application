package hs.kr.entrydsm.application.domain.application.event.spi

import java.util.UUID

interface ApplicationEventPort {
    fun sendApplicationCreateCompleted(receiptCode: Long, userId: UUID)

    fun cancelSubmittedApplication(receiptCode: Long)
}
