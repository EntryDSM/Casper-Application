package hs.kr.entrydsm.application.domain.score.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "tbl_score")
class ScoreJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val attendanceScore: Int?,
    @Column(precision = 6, scale = 3)
    val volunteerScore: BigDecimal?,
    @Column(precision = 6, scale = 3)
    val thirdBeforeBeforeScore: BigDecimal?,
    @Column(precision = 6, scale = 3)
    val thirdBeforeScore: BigDecimal?,
    @Column(precision = 6, scale = 3)
    val thirdGradeScore: BigDecimal?,
    @Column(precision = 6, scale = 3)
    val thirdScore: BigDecimal?,
    @Column(precision = 6, scale = 3)
    val totalGradeScore: BigDecimal?,
    val extraScore: Int?,
    @Column(precision = 6, scale = 3)
    val totalScore: BigDecimal?,
    @Column(unique = true)
    val receiptCode: Long,
)
