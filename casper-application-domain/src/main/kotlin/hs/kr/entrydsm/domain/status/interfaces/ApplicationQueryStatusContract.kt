package hs.kr.entrydsm.domain.status.interfaces

import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.status.aggregates.StatusCache

interface ApplicationQueryStatusContract {
    fun queryStatusByReceiptCode(receiptCode: Long): Status?
    fun queryStatusByReceiptCodeInCache(receiptCode: Long): StatusCache?
}