package hs.kr.entrydsm.application.domain.admin.presentation.dto.response

/**
 * 성별별 접수현황 응답 DTO
 */
data class ApplicationStatisticsByGenderResponse(
    val success: Boolean,
    val data: GenderStatisticsData,
) {
    data class GenderStatisticsData(
        val total: Int, // 전체 접수 인원
        val byGender: List<GenderInfo>,
    )

    data class GenderInfo(
        val gender: String, // 성별 (MALE, FEMALE)
        val genderName: String, // 성별명 (남, 여)
        val count: Int, // 해당 성별 접수 인원
        val percentage: Double, // 비율 (%)
    )
}
