package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.math.BigDecimal

/**
 * Score JPA 엔티티
 */
@Entity
@Table(name = "tbl_score")
@DynamicInsert
@DynamicUpdate
class ScoreJpaEntity(
    @Id
    @Column(name = "receipt_code")
    val receiptCode: Long,

    @Column(name = "attendance_score", nullable = true)
    val attendanceScore: Int? = null,

    @Column(name = "volunteer_score", nullable = true, precision = 5, scale = 2)
    val volunteerScore: BigDecimal? = null,

    @Column(name = "third_grade_score", precision = 5, scale = 2)
    val thirdGradeScore: BigDecimal? = null,

    @Column(name = "third_before_score", precision = 5, scale = 2)
    val thirdBeforeScore: BigDecimal? = null,

    @Column(name = "third_before_before_score", precision = 5, scale = 2)
    val thirdBeforeBeforeScore: BigDecimal? = null,

    @Column(name = "third_score", precision = 5, scale = 2)
    val thirdScore: BigDecimal? = null,

    @Column(name = "total_grade_score", nullable = false, precision = 6, scale = 2)
    val totalGradeScore: BigDecimal,

    @Column(name = "extra_score", nullable = false, precision = 4, scale = 2)
    val extraScore: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_score", nullable = false, precision = 6, scale = 2)
    val totalScore: BigDecimal,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "receipt_code", referencedColumnName = "receipt_code")
    val application: ApplicationJpaEntity? = null
)