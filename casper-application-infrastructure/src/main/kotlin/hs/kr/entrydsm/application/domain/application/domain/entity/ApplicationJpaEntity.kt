package hs.kr.entrydsm.application.domain.application.domain.entity

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * 입학원서 엔티티
 *
 * 원서의 모든 정보를 단일 테이블에 저장합니다:
 * - 지원자 기본 정보
 * - 성적 데이터 (JSON)
 * - 계산된 점수 결과
 * - 메타데이터
 */
@Entity
@Table(
    name = "applications",
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
        Index(name = "idx_receipt_code", columnList = "receipt_code"),
        Index(name = "idx_status", columnList = "status"),
        Index(name = "idx_application_type", columnList = "application_type"),
        Index(name = "idx_submitted_at", columnList = "submitted_at"),
    ],
)
class ApplicationJpaEntity(
    // ===== 기본 식별 정보 =====
    @Id
    @Column(name = "application_id", columnDefinition = "BINARY(16)")
    val applicationId: UUID,
    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    val userId: UUID,
    @Column(name = "receipt_code", unique = true, nullable = false)
    val receiptCode: Long,
    // ===== 지원자 정보 =====
    @Column(name = "applicant_name", nullable = false, length = 100)
    val applicantName: String,
    @Column(name = "applicant_tel", nullable = false, length = 20)
    val applicantTel: String,
    @Column(name = "birth_date", length = 10)
    val birthDate: String?,
    // ===== 원서 전형 정보 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false, length = 50)
    val applicationType: ApplicationType,
    @Enumerated(EnumType.STRING)
    @Column(name = "educational_status", nullable = false, length = 50)
    val educationalStatus: EducationalStatus,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ApplicationStatus,
    @Column(name = "is_daejeon", nullable = false)
    val isDaejeon: Boolean,
    @Column(name = "is_arrived", nullable = false)
    var isArrived: Boolean = false,
    // ===== 보호자 및 주소 정보 =====
    @Column(name = "parent_name", length = 100)
    val parentName: String?,
    @Column(name = "parent_tel", length = 20)
    val parentTel: String?,
    @Column(name = "parent_relation", length = 50)
    val parentRelation: String?,
    @Column(name = "postal_code", length = 10)
    val postalCode: String?,
    @Column(name = "detail_address", length = 500)
    val detailAddress: String?,
    // ===== 자기소개 및 학업계획 =====
    @Column(name = "study_plan", columnDefinition = "TEXT")
    val studyPlan: String?,
    @Column(name = "self_introduce", columnDefinition = "TEXT")
    val selfIntroduce: String?,
    @Column(name = "school_code", length = 20)
    val schoolCode: String?,
    // ===== 성적 데이터 (JSON) =====
    @Column(name = "scores_data", columnDefinition = "JSON", nullable = false)
    val scoresData: String,
    // ===== 계산된 점수 결과 =====
    @Column(name = "total_score", precision = 10, scale = 2)
    val totalScore: java.math.BigDecimal?,
    @Column(name = "subject_score", precision = 10, scale = 2)
    val subjectScore: java.math.BigDecimal?,
    @Column(name = "attendance_score", precision = 10, scale = 2)
    val attendanceScore: java.math.BigDecimal?,
    @Column(name = "volunteer_score", precision = 10, scale = 2)
    val volunteerScore: java.math.BigDecimal?,
    @Column(name = "bonus_score", precision = 10, scale = 2)
    val bonusScore: java.math.BigDecimal?,
    // ===== 계산 메타데이터 =====
    @Column(name = "calculated_at")
    val calculatedAt: LocalDateTime?,
    @Column(name = "calculation_time_ms")
    val calculationTimeMs: Long?,
    // ===== 타임스탬프 =====
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

    constructor() : this(
        applicationId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        receiptCode = 0L,
        applicantName = "",
        applicantTel = "",
        birthDate = null,
        applicationType = ApplicationType.COMMON,
        educationalStatus = EducationalStatus.PROSPECTIVE_GRADUATE,
        status = ApplicationStatus.SUBMITTED,
        isDaejeon = false,
        isArrived = false,
        parentName = null,
        parentTel = null,
        parentRelation = null,
        postalCode = null,
        detailAddress = null,
        studyPlan = null,
        selfIntroduce = null,
        schoolCode = null,
        scoresData = "{}",
        totalScore = null,
        subjectScore = null,
        attendanceScore = null,
        volunteerScore = null,
        bonusScore = null,
        calculatedAt = null,
        calculationTimeMs = null,
        submittedAt = LocalDateTime.now(),
        reviewedAt = null,
    )
}
