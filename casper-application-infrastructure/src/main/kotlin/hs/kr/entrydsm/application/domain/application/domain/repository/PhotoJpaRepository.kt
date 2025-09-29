package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.PhotoJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PhotoJpaRepository : JpaRepository<PhotoJpaEntity, UUID> {
    fun findByUserId(userId: UUID): PhotoJpaEntity?
}
