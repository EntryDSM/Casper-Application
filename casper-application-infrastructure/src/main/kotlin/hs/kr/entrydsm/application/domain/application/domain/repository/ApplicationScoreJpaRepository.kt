package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationScoreJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ApplicationScoreJpaRepository : JpaRepository<ApplicationScoreJpaEntity, UUID> {
    
    fun findAllByApplicationId(applicationId: UUID): List<ApplicationScoreJpaEntity>
    
    fun deleteAllByApplicationId(applicationId: UUID)
}