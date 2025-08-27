package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import hs.kr.entrydsm.domain.application.entities.Score
import java.math.BigDecimal

data class ScoreResponse(
    val receiptCode: Long,
    val attendanceScore: Int?,
    val volunteerScore: BigDecimal?,
    val thirdGradeScore: BigDecimal?,
    val thirdBeforeScore: BigDecimal?,
    val thirdBeforeBeforeScore: BigDecimal?,
    val thirdScore: BigDecimal?,
    val totalGradeScore: BigDecimal,
    val extraScore: BigDecimal,
    val totalScore: BigDecimal
) {
    companion object {
        fun from(score: Score): ScoreResponse {
            return ScoreResponse(
                receiptCode = score.id.value,
                attendanceScore = score.attendanceScore,
                volunteerScore = score.volunteerScore,
                thirdGradeScore = score.thirdGradeScore,
                thirdBeforeScore = score.thirdBeforeScore,
                thirdBeforeBeforeScore = score.thirdBeforeBeforeScore,
                thirdScore = score.thirdScore,
                totalGradeScore = score.totalGradeScore,
                extraScore = score.extraScore,
                totalScore = score.totalScore
            )
        }
    }
}