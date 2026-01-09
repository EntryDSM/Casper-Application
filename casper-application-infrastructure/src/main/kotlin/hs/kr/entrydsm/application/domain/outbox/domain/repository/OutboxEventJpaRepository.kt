package hs.kr.entrydsm.application.domain.outbox.domain.repository

import hs.kr.entrydsm.application.domain.outbox.domain.entity.OutboxEventJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Outbox 이벤트 JPA 저장소
 */
@Repository
interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {
    /**
     * 특정 Aggregate의 이벤트 조회 (디버깅/추적용)
     *
     * @param aggregateType 집계 타입
     * @param aggregateId 집계 식별자
     * @return 이벤트 목록
     */
    fun findByAggregateTypeAndAggregateId(
        aggregateType: String,
        aggregateId: String,
    ): List<OutboxEventJpaEntity>

    /**
     * 특정 시간 이전의 오래된 이벤트 조회 (정리용)
     *
     * @param createdAt 기준 시각
     * @return 오래된 이벤트 목록
     */
    fun findByCreatedAtBefore(createdAt: LocalDateTime): List<OutboxEventJpaEntity>
}
