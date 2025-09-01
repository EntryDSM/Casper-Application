package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PrototypeJpaRepository : JpaRepository<PrototypeJpaEntity, UUID> {
    
    @Query("""
        SELECT p FROM PrototypeJpaEntity p 
        WHERE p.applicationType = :applicationType 
        AND p.educationalStatus = :educationalStatus 
        AND (:region IS NULL OR p.region = :region OR p.region IS NULL)
        ORDER BY p.region DESC NULLS LAST
    """)
    fun findByApplicationTypeAndEducationalStatusAndRegion(
        @Param("applicationType") applicationType: String,
        @Param("educationalStatus") educationalStatus: String,
        @Param("region") region: String?
    ): List<PrototypeJpaEntity>
    
    fun findByApplicationTypeAndEducationalStatus(
        applicationType: String,
        educationalStatus: String
    ): List<PrototypeJpaEntity>
}