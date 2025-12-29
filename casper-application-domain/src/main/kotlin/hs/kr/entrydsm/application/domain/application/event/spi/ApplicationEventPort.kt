package hs.kr.entrydsm.application.domain.application.event.spi

import hs.kr.entrydsm.application.domain.application.event.dto.SubmissionData
import java.util.UUID

interface ApplicationEventPort {
    fun create(receiptCode: Long, userId: UUID)

    fun submitApplication(submissionData: SubmissionData)

    // 미사용
    //fun updateEducationalStatus(receiptCode: Long, graduateDate: YearMonth)

    // 미사용
    //fun submitApplicationFinal(receiptCode: Long)

    fun createApplicationScoreRollback(receiptCode: Long)
}
