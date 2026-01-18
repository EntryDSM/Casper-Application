package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Applicant
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.spi.dto.PagedResult
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationCountResponse
import java.util.*
import kotlin.collections.List

interface QueryApplicationPort {
    fun queryApplicationByUserId(userId: UUID): Application?

    fun isExistsApplicationByUserId(userId: UUID): Boolean

    suspend fun queryApplicationCountByApplicationTypeAndIsDaejeon(
        applicationType: ApplicationType,
        isDaejeon: Boolean,
    ): GetApplicationCountResponse

    fun queryApplicationByReceiptCode(receiptCode: Long): Application?

    fun queryAllByReceiptCode(receiptCodeList: List<Long>): List<Application?>

    suspend fun queryAllApplicantsByFilter(
        applicationType: ApplicationType?,
        educationalStatus: EducationalStatus?,
        isDaejeon: Boolean?,
        pageSize: Long,
        offset: Long,
    ): PagedResult<Applicant>

    suspend fun queryAllFirstRoundPassedApplication(): List<Application>
}
