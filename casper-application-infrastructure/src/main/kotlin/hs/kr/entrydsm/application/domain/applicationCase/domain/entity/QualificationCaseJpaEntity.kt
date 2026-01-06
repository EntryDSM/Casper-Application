package hs.kr.entrydsm.application.domain.applicationCase.domain.entity

import hs.kr.entrydsm.application.domain.applicationCase.domain.entity.vo.ExtraScoreItem
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "tbl_qualification_case")
class QualificationCaseJpaEntity(
    override val id: Long,
    override val receiptCode: Long,
    @Embedded
    override val extraScoreItem: ExtraScoreItem,
    val koreanGrade: BigDecimal,
    val socialGrade: BigDecimal,
    val historyGrade: BigDecimal,
    val mathGrade: BigDecimal,
    val scienceGrade: BigDecimal,
    val englishGrade: BigDecimal,
) : ApplicationCaseEntity(
    id = id,
    receiptCode = receiptCode,
    extraScoreItem = extraScoreItem
)