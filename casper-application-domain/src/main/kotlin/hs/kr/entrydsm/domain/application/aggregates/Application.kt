package hs.kr.entrydsm.domain.application.aggregates

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.time.LocalDateTime
import java.util.UUID

@Aggregate(context = "application")
data class Application(
    val id: UUID,
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
    val submittedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val streetAddress: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)