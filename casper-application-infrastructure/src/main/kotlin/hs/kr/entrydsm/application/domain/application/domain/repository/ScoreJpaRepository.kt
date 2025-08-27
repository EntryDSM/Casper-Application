package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.ScoreJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ScoreJpaRepository : JpaRepository<ScoreJpaEntity, Long> {
    
    fun findByReceiptCode(receiptCode: Long): ScoreJpaEntity?
    
    fun existsByReceiptCode(receiptCode: Long): Boolean
    
    fun deleteByReceiptCode(receiptCode: Long)
}