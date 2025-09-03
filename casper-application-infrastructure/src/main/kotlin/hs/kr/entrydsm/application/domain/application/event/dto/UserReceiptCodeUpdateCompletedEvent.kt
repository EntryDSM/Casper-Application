package hs.kr.entrydsm.application.domain.application.event.dto

import java.util.UUID

data class UserReceiptCodeUpdateCompletedEvent(
    val receiptCode: Long,
    val userId: UUID
)
