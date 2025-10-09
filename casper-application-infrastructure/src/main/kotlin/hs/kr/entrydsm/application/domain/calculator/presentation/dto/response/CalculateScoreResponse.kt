package hs.kr.entrydsm.application.domain.calculator.presentation.dto.response

/**
 * 성적 계산 응답 DTO
 */
data class CalculateScoreResponse(
    val success: Boolean,
    val data: ScoreResultData,
) {
    data class ScoreResultData(
        val subjectScore: Double, // 교과성적 (80점 만점)
        val attendanceScore: Double, // 출석점수 (15점 만점)
        val volunteerScore: Double, // 봉사활동점수 (15점 만점)
        val bonusScore: Double, // 가산점 (일반 3점, 특별 9점)
        val totalScore: Double, // 총점 (300점 만점)
    )
}
