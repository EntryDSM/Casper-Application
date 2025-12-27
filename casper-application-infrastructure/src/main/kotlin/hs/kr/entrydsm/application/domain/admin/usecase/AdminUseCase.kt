package hs.kr.entrydsm.application.domain.admin.usecase

import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.ApplicationStatisticsByGenderResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.ApplicationStatisticsByRegionResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CompetitionRateResponse
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.domain.application.values.ApplicationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관리자 UseCase
 *
 * ApplicationType과 EducationalStatus는 이제 Enum으로 관리되므로
 * 동적 생성 기능은 더 이상 필요하지 않습니다.
 */
@Service
@Transactional(readOnly = true)
class AdminUseCase(
    private val applicationRepository: ApplicationJpaRepository,
) {

    /**
     * 경쟁률 조회
     *
     * 전형별 경쟁률을 계산합니다.
     * 경쟁률 = 지원자 수 / 모집정원
     */
    fun getCompetitionRate(): CompetitionRateResponse {
        val allApplications = applicationRepository.findAll()
        val totalApplicants = allApplications.size

        // 전형별 모집정원 (실제 값으로 변경 필요)
        val capacityByType =
            mapOf(
                ApplicationType.COMMON to 80,
                ApplicationType.MEISTER to 32,
                ApplicationType.SOCIAL to 8,
            )

        val totalCapacity = capacityByType.values.sum()

        // 전형별 지원자 수 계산
        val applicationsByType = allApplications.groupBy { it.applicationType }

        val byType =
            ApplicationType.entries.map { type ->
                val applicants = applicationsByType[type]?.size ?: 0
                val capacity = capacityByType[type] ?: 0
                val rate = if (capacity > 0) applicants.toDouble() / capacity else 0.0

                CompetitionRateResponse.CompetitionByType(
                    applicationType = type.name,
                    applicants = applicants,
                    capacity = capacity,
                    rate = String.format("%.2f", rate).toDouble(),
                )
            }

        val totalRate =
            if (totalCapacity > 0) {
                totalApplicants.toDouble() / totalCapacity
            } else {
                0.0
            }

        return CompetitionRateResponse(
            success = true,
            data =
                CompetitionRateResponse.CompetitionRateData(
                    total =
                        CompetitionRateResponse.CompetitionInfo(
                            applicants = totalApplicants,
                            capacity = totalCapacity,
                            rate = String.format("%.2f", totalRate).toDouble(),
                        ),
                    byType = byType,
                ),
        )
    }

    /**
     * 지역별 접수현황 조회
     *
     * 대전/전국별 접수 인원과 비율을 조회합니다.
     */
    fun getApplicationStatisticsByRegion(): ApplicationStatisticsByRegionResponse {
        val allApplications = applicationRepository.findAll()
        val total = allApplications.size

        val daejeonCount = allApplications.count { it.isDaejeon }
        val nationwideCount = total - daejeonCount

        val byRegion =
            listOf(
                ApplicationStatisticsByRegionResponse.RegionInfo(
                    region = "DAEJEON",
                    regionName = "대전",
                    count = daejeonCount,
                    percentage =
                        if (total > 0) {
                            String.format("%.2f", (daejeonCount.toDouble() / total * 100)).toDouble()
                        } else {
                            0.0
                        },
                ),
                ApplicationStatisticsByRegionResponse.RegionInfo(
                    region = "NATIONWIDE",
                    regionName = "전국",
                    count = nationwideCount,
                    percentage =
                        if (total > 0) {
                            String.format("%.2f", (nationwideCount.toDouble() / total * 100)).toDouble()
                        } else {
                            0.0
                        },
                ),
            )

        return ApplicationStatisticsByRegionResponse(
            success = true,
            data =
                ApplicationStatisticsByRegionResponse.RegionStatisticsData(
                    total = total,
                    byRegion = byRegion,
                ),
        )
    }

    /**
     * 성별별 접수현황 조회
     *
     * 남/여별 접수 인원과 비율을 조회합니다.
     */
    fun getApplicationStatisticsByGender(): ApplicationStatisticsByGenderResponse {
        val allApplications = applicationRepository.findAll()
        val total = allApplications.size

        val maleCount = allApplications.count { it.applicantGender?.name == "MALE" }
        val femaleCount = allApplications.count { it.applicantGender?.name == "FEMALE" }

        val byGender =
            listOf(
                ApplicationStatisticsByGenderResponse.GenderInfo(
                    gender = "MALE",
                    genderName = "남",
                    count = maleCount,
                    percentage =
                        if (total > 0) {
                            String.format("%.2f", (maleCount.toDouble() / total * 100)).toDouble()
                        } else {
                            0.0
                        },
                ),
                ApplicationStatisticsByGenderResponse.GenderInfo(
                    gender = "FEMALE",
                    genderName = "여",
                    count = femaleCount,
                    percentage =
                        if (total > 0) {
                            String.format("%.2f", (femaleCount.toDouble() / total * 100)).toDouble()
                        } else {
                            0.0
                        },
                ),
            )

        return ApplicationStatisticsByGenderResponse(
            success = true,
            data =
                ApplicationStatisticsByGenderResponse.GenderStatisticsData(
                    total = total,
                    byGender = byGender,
                ),
        )
    }
}
