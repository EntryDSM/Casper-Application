package hs.kr.entrydsm.domain.application.interfaces

import java.util.UUID

interface ApplicationCreateEventContract {
    fun publishCreateApplication(receiptCode: Long, userId: UUID)
    fun submitApplicationFinal(receiptCode: Long)
    fun publishCreateApplicationScoreRollback(receiptCode: Long)
    fun publishUpdateEducationalStatus(receiptCode: Long, graduateDate: java.time.YearMonth)
}