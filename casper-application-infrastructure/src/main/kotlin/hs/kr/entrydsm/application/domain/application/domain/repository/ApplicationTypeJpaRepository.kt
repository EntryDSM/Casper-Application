package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationTypeJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ApplicationTypeJpaRepository : JpaRepository<ApplicationTypeJpaEntity, UUID> {
    fun findAllByActiveTrue(): List<ApplicationTypeJpaEntity>
    fun findByCodeAndActiveTrue(code: String): ApplicationTypeJpaEntity?
}