package hs.kr.entrydsm.application.domain.graduationInfo.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.YearMonth

@Entity
@Table(name = "tbl_qualification")
class QualificationJpaEntity(
    override val id: Long,
    override val isProspectiveGraduate: Boolean,
    override val receiptCode: Long,
    override val graduateDate: YearMonth?,
) : GraduationInfoEntity(
        id = id,
        graduateDate = graduateDate,
        isProspectiveGraduate = isProspectiveGraduate,
        receiptCode = receiptCode,
    )
