package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.application.aggregates.Application
import java.util.UUID

interface ApplicationContract : QueryAllFirstRoundPassedApplicationContract {
    fun getApplicationByUserId(userId: UUID): Application?
    
    fun getApplicationByReceiptCode(receiptCode: Long): Application?
    
    fun delete(application: Application)
}