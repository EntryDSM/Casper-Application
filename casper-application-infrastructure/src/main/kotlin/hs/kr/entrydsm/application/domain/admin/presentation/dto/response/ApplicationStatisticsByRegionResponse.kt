package hs.kr.entrydsm.application.domain.admin.presentation.dto.response

/**
 * 지역별 접수현황 응답 DTO
 */
data class ApplicationStatisticsByRegionResponse(
    val success: Boolean,
    val data: RegionStatisticsData,
) {
    data class RegionStatisticsData(
        val total: Int, // 전체 접수 인원
        val byRegion: List<RegionInfo>,
    )

    data class RegionInfo(
        val region: String, // 지역 (DAEJEON, NATIONWIDE)
        val regionName: String, // 지역명 (대전, 전국)
        val count: Int, // 해당 지역 접수 인원
        val percentage: Double, // 비율 (%)
    )
}
