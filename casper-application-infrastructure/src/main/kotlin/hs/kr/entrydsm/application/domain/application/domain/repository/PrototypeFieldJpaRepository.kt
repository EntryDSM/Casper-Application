package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeFieldJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PrototypeFieldJpaRepository : JpaRepository<PrototypeFieldJpaEntity, UUID> {
    fun findAllByPrototypeId(prototypeId: UUID): List<PrototypeFieldJpaEntity>

    fun findAllByPrototypeIdAndFieldCategory(
        prototypeId: UUID,
        fieldCategory: String,
    ): List<PrototypeFieldJpaEntity>

    fun deleteAllByPrototypeId(prototypeId: UUID)
}
