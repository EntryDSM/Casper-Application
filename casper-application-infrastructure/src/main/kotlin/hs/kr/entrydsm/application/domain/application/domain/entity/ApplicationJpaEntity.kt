package hs.kr.entrydsm.application.domain.application.domain.entity

import hs.kr.entrydsm.application.domain.application.domain.entity.enums.ApplicationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "applications")
class ApplicationJpaEntity(
    @Id
    @Column(name = "application_id", columnDefinition = "BINARY(16)")
    val applicationId: UUID,
    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    val userId: UUID,
    @Column(name = "receipt_code", unique = true, nullable = false)
    val receiptCode: Long,
    @Column(name = "applicant_name", nullable = false, length = 100)
    val applicantName: String,
    @Column(name = "applicant_tel", nullable = false, length = 20)
    val applicantTel: String,
    @Column(name = "parent_name", length = 100)
    val parentName: String?,
    @Column(name = "parent_tel", length = 20)
    val parentTel: String?,
    @Column(name = "birth_date")
    val birthDate: String?,
    @Column(name = "application_type", nullable = false, length = 50)
    val applicationType: String,
    @Column(name = "educational_status", nullable = false, length = 50)
    val educationalStatus: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: ApplicationStatus,
    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime,
    @Column(name = "reviewed_at")
    val reviewedAt: LocalDateTime?,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    protected constructor() : this(
        applicationId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        receiptCode = 0L,
        applicantName = "",
        applicantTel = "",
        parentName = null,
        parentTel = null,
        birthDate = null,
        applicationType = "",
        educationalStatus = "",
        status = ApplicationStatus.SUBMITTED,
        submittedAt = LocalDateTime.now(),
        reviewedAt = null,
    )
}
