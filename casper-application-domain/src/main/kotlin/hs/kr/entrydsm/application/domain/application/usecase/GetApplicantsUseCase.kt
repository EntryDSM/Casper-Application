package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicantsResponse
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import kotlin.math.ceil

@ReadOnlyUseCase
class GetApplicantsUseCase(
    private val queryApplicationPort: QueryApplicationPort
) {
    suspend fun execute(
        page: Int,
        size: Int,
        applicationType: ApplicationType?,
        educationalStatus: EducationalStatus?,
        isDaejeon: Boolean?
    ): GetApplicantsResponse {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceAtLeast(1)
        val offset = safePage.toLong() * safeSize.toLong()
        val pageSize = safeSize.toLong()

        val pagedApplicants =  queryApplicationPort.queryAllApplicantsByFilter(
            applicationType = applicationType,
            educationalStatus = educationalStatus,
            isDaejeon = isDaejeon,
            pageSize = pageSize,
            offset = offset,
        )

        val applicants = pagedApplicants.items.map {
            GetApplicantsResponse.ApplicantDto(
                receiptCode = it.receiptCode,
                applicantName = it.name,
                applicationType = it.applicationType,
                educationalStatus = it.educationalStatus,
                isDaejeon = it.isDaejeon,
                isArrived = it.isArrived
            )
        }

        val totalPages = ceil(pagedApplicants.totalElements.toDouble() / safeSize).toInt()

        return GetApplicantsResponse(
            applicants = applicants,
            total = pagedApplicants.totalElements,
            page = safePage,
            size = safeSize,
            totalPages = totalPages,
        )
    }
}
