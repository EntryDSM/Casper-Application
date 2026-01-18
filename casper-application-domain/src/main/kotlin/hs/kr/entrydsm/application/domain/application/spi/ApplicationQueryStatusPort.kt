package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.status.model.Status
import hs.kr.entrydsm.application.domain.status.model.StatusCache

interface ApplicationQueryStatusPort {
    suspend fun queryStatusByReceiptCode(receiptCode: Long): Status?
    fun queryStatusByReceiptCodeInCache(receiptCode: Long): StatusCache?
}