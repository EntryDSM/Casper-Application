package hs.kr.entrydsm.application.domain.graduationInfo.domain.entity

import hs.kr.entrydsm.application.global.entity.BaseTimeEntity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotNull
import java.time.YearMonth

@MappedSuperclass
abstract class GraduationInfoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val graduateDate: YearMonth?,
    @get:JvmName("getIsProspectiveGraduate")
    val isProspectiveGraduate: Boolean,
    @NotNull
    val receiptCode: Long,
) : BaseTimeEntity()
