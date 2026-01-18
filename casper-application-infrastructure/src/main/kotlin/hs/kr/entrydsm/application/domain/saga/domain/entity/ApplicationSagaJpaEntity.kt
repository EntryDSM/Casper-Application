package hs.kr.entrydsm.application.domain.saga.domain.entity

import hs.kr.entrydsm.application.domain.saga.domain.types.SagaStatus
import hs.kr.entrydsm.application.global.entity.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tbl_application_saga")
class ApplicationSagaJpaEntity(
    @Id
    val receiptCode: Long,
    var isStatusCreated: Boolean,
    var isUserUpdated: Boolean,
    @Enumerated(EnumType.STRING)
    var status: SagaStatus,
) : BaseTimeEntity()
