package hs.kr.entrydsm.application.domain.photo.domain.repository

import hs.kr.entrydsm.application.domain.photo.domain.entity.PhotoJpaEntity
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface PhotoJpaRepository : CrudRepository<PhotoJpaEntity, UUID> {
    fun findByUserId(userId: UUID): PhotoJpaEntity?
}