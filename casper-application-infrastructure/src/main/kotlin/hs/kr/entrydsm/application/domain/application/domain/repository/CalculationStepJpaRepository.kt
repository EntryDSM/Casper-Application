package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.CalculationStepJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CalculationStepJpaRepository : JpaRepository<CalculationStepJpaEntity, UUID> {
    fun findAllByCalculationIdOrderByStepOrder(calculationId: UUID): List<CalculationStepJpaEntity>
}
