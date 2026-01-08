package hs.kr.entrydsm.application.domain.graduationInfo.domain.entity

import hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.vo.StudentNumber
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.YearMonth

@Entity
@Table(name = "tbl_graduation")
class GraduationJpaEntity(
    override val id: Long,
    override val graduateDate: YearMonth?,
    override val isProspectiveGraduate: Boolean,
    override val receiptCode: Long,
    @Embedded
    val studentNumber: StudentNumber?,
    val schoolCode: String?,
    val teacherName: String?,
    @Column(nullable = true)
    val teacherTel: String?
) : GraduationInfoEntity(
    id = id,
    graduateDate = graduateDate,
    isProspectiveGraduate = isProspectiveGraduate,
    receiptCode = receiptCode,
)
