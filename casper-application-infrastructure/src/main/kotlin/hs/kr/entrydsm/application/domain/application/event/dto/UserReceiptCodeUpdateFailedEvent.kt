package hs.kr.entrydsm.application.domain.application.event.dto

import java.util.UUID

data class UserReceiptCodeUpdateFailedEvent(
    val receiptCode: Long,
    val userId: UUID,
    val reason: String
)
