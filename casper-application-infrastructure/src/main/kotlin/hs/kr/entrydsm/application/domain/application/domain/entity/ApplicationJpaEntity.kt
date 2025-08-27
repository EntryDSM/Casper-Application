package hs.kr.entrydsm.application.domain.application.domain.entity

import hs.kr.entrydsm.domain.application.values.*
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDate

/**
 * Application JPA 엔티티
 */
@Entity
@Table(name = "tbl_application")
@DynamicInsert
@DynamicUpdate
class ApplicationJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_code")
    val receiptCode: Long,

    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    val userId: ByteArray,

    @Column(name = "applicant_name", nullable = false, length = 50)
    val applicantName: String,

    @Column(name = "applicant_tel", nullable = false, length = 20)
    val applicantTel: String,

    @Column(name = "parent_name", nullable = false, length = 50)
    val parentName: String,

    @Column(name = "parent_tel", nullable = false, length = 20)
    val parentTel: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    val sex: Sex,

    @Column(name = "birth_date", nullable = false)
    val birthDate: LocalDate,

    @Column(name = "street_address", nullable = false, length = 200)
    val streetAddress: String,

    @Column(name = "postal_code", nullable = false, length = 10)
    val postalCode: String,

    @Column(name = "detail_address", nullable = false, length = 200)
    val detailAddress: String,

    @Column(name = "is_daejeon", nullable = false)
    val isDaejeon: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    val applicationType: ApplicationType,

    @Enumerated(EnumType.STRING)
    @Column(name = "application_remark", nullable = false)
    val applicationRemark: ApplicationRemark,

    @Enumerated(EnumType.STRING)
    @Column(name = "educational_status", nullable = false)
    val educationalStatus: EducationalStatus,

    @Column(name = "photo_path", length = 500)
    val photoPath: String? = null,

    @Column(name = "study_plan", columnDefinition = "TEXT")
    val studyPlan: String? = null,

    @Column(name = "self_introduce", columnDefinition = "TEXT")
    val selfIntroduce: String? = null,

    @Column(name = "veterans_number", length = 50)
    val veteransNumber: String? = null
)