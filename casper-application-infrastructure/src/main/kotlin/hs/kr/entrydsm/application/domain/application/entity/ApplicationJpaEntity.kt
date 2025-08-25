package hs.kr.entrydsm.application.domain.application.entity

import hs.kr.entrydsm.domain.application.values.ApplicationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tbl_application")
class ApplicationJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val receiptCode: Long = 0,
    // sex 컬럼 임시 삭제
    @get:JvmName("getIsDaejeon")
    val isDaejeon: Boolean?,
    @get:JvmName("getIsOutOfHeadcount")
    var isOutOfHeadcount: Boolean?,
    @Column(columnDefinition = "TEXT")
    val photoPath: String?,
    val applicantName: String?,
    @Column(columnDefinition = "char(11)")
    val applicantTel: String?,
    val parentName: String?,
    @Column(columnDefinition = "char(11)")
    val parentTel: String?,
    val parentRelation: String?,
    val streetAddress: String?,
    val postalCode: String?,
    val detailAddress: String?,
    @Enumerated(EnumType.STRING)
    val applicationType: ApplicationType?,
    @Column(length = 1600)
    val studyPlan: String?,
    @Column(length = 1600)
    val selfIntroduce: String?,
//    @field:NotNull
    @Column(unique = true, columnDefinition = "BINARY(16)")
    val userId: UUID,
    val veteransNumber: Int?
)