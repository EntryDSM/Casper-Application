package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import java.time.LocalDateTime
import java.util.UUID

/**
 * 원서 생성 응답 DTO
 *
 * 원서 생성 성공 시 반환되는 응답 데이터입니다.
 */
data class CreateApplicationResponse(
    val success: Boolean,
    val data: ApplicationData?,
    val message: String? = null,
) {
    data class ApplicationData(
        val applicationId: UUID,
        val receiptCode: Long,
        val applicantName: String,
        val applicationType: String,
        val educationalStatus: String,
        val status: String,
        val submittedAt: LocalDateTime,
        val createdAt: LocalDateTime,
    )
}
