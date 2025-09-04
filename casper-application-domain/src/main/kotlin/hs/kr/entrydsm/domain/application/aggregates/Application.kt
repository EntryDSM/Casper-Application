package hs.kr.entrydsm.domain.application.aggregates

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.time.LocalDateTime
import java.util.UUID

@Aggregate(context = "application")
data class Application(
    val applicationId: UUID,
    val userId: UUID,
    val receiptCode: Long,
    val applicantName: String,
    val applicantTel: String,
    val parentName: String?,
    val parentTel: String?,
    val birthDate: String?,
    val applicationType: ApplicationType,
    val educationalStatus: String,
    val status: ApplicationStatus,
    val streetAddress: String?,
    val submittedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    // PDF/Excel 생성을 위해 추가된 필드들
    val isDaejeon: Boolean?,
    val isOutOfHeadcount: Boolean?,
    val photoPath: String?,
    val parentRelation: String?,
    val postalCode: String?,
    val detailAddress: String?,
    val studyPlan: String?,
    val selfIntroduce: String?,
    val veteransNumber: Int?,
    val schoolCode: String?
)
