package hs.kr.entrydsm.application.domain.application.event.model

import java.util.UUID

data class ApplicationSubmittedEvent(
    val receiptCode: Long,
    val userId: UUID,
)
