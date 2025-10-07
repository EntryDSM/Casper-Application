package hs.kr.entrydsm.application.domain.admin.presentation.dto.response

/**
 * 경쟁률 응답 DTO
 */
data class CompetitionRateResponse(
    val success: Boolean,
    val data: CompetitionRateData,
) {
    data class CompetitionRateData(
        val total: CompetitionInfo,
        val byType: List<CompetitionByType>,
    )

    data class CompetitionInfo(
        val applicants: Int, // 지원자 수
        val capacity: Int, // 모집정원
        val rate: Double, // 경쟁률
    )

    data class CompetitionByType(
        val applicationType: String, // 전형 유형
        val applicants: Int, // 지원자 수
        val capacity: Int, // 모집정원
        val rate: Double, // 경쟁률
    )
}
