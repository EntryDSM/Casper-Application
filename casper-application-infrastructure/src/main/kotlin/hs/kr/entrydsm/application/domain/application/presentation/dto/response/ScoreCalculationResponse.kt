package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * 점수 계산 응답 DTO
 */
@Schema(description = "점수 계산 응답")
data class ScoreCalculationResponse(
    @Schema(description = "성공 여부", example = "true")
    val success: Boolean,
    @Schema(description = "점수 데이터")
    val data: ScoreData?,
    @Schema(description = "응답 메시지", example = "점수 계산이 완료되었습니다")
    val message: String?,
) {
    @Schema(description = "점수 상세 데이터")
    data class ScoreData(
        @Schema(description = "원서 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        val applicationId: String,
        @Schema(description = "교과성적", example = "120.50")
        val subjectScore: BigDecimal,
        @Schema(description = "출석점수", example = "14.00")
        val attendanceScore: BigDecimal,
        @Schema(description = "봉사활동점수", example = "15.00")
        val volunteerScore: BigDecimal,
        @Schema(description = "가산점", example = "6.00")
        val bonusScore: BigDecimal,
        @Schema(description = "총점", example = "155.50")
        val totalScore: BigDecimal,
        @Schema(description = "최대 점수", example = "173.00")
        val maxScore: BigDecimal,
        @Schema(description = "점수 백분율", example = "89.88")
        val scorePercentage: Double,
        @Schema(description = "전형 유형", example = "일반전형")
        val applicationType: String,
        @Schema(description = "학력 상태", example = "졸업예정")
        val educationalStatus: String,
    )
}
