package hs.kr.entrydsm.application.domain.applicationCase.domain.entity

import hs.kr.entrydsm.application.domain.applicationCase.domain.entity.vo.ExtraScoreItem
import jakarta.persistence.Embedded
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class ApplicationCaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val receiptCode: Long,
    @Embedded
    val extraScoreItem: ExtraScoreItem
)