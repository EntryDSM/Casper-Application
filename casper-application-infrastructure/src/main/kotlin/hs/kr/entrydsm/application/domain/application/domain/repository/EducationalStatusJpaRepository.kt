package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.EducationalStatusJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EducationalStatusJpaRepository : JpaRepository<EducationalStatusJpaEntity, UUID> {
    fun findAllByActiveTrue(): List<EducationalStatusJpaEntity>
    fun findByCodeAndActiveTrue(code: String): EducationalStatusJpaEntity?
}