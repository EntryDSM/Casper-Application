package hs.kr.entrydsm.application.domain.application.entity

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tbl_application")
class ApplicationJpaEntity(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val applicationId: UUID,
    
    @Column(unique = true, columnDefinition = "BINARY(16)")
    val userId: UUID,
    
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    val receiptCode: Long,
    
    @Column(nullable = false)
    val applicantName: String,
    
    @Column(columnDefinition = "char(11)", nullable = false)
    val applicantTel: String,
    
    val parentName: String?,
    
    @Column(columnDefinition = "char(11)")
    val parentTel: String?,
    
    val birthDate: String?,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val applicationType: ApplicationType,
    
    @Column(nullable = false)
    val educationalStatus: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ApplicationStatus,
    
    val streetAddress: String?,
    
    @Column(nullable = false)
    val submittedAt: LocalDateTime,
    
    val reviewedAt: LocalDateTime?,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime,
    
    // PDF/Excel 생성을 위한 추가 필드들
    @get:JvmName("getIsDaejeon")
    val isDaejeon: Boolean?,
    
    @get:JvmName("getIsOutOfHeadcount")
    var isOutOfHeadcount: Boolean?,
    
    @Column(columnDefinition = "TEXT")
    val photoPath: String?,
    
    val parentRelation: String?,
    
    val postalCode: String?,
    
    val detailAddress: String?,
    
    @Column(length = 1600)
    val studyPlan: String?,
    
    @Column(length = 1600)
    val selfIntroduce: String?,
    
    val veteransNumber: Int?,
    
    val schoolCode: String?
)
