package hs.kr.entrydsm.application.domain.outbox.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 * Outbox 패턴을 위한 이벤트 JPA 엔티티
 *
 * Debezium이 이 테이블을 감시하여 Kafka로 이벤트를 발행합니다.
 * 도메인 엔티티와 동일 트랜잭션으로 저장되어 원자성을 보장합니다.
 *
 * @property id 이벤트 고유 식별자
 * @property aggregateType 집계 루트 타입 (예: "Application", "Score")
 * @property aggregateId 집계 식별자 (예: receiptCode)
 * @property eventType Kafka Topic 이름 (KafkaTopics 상수)
 * @property payload 이벤트 데이터 (JSON 직렬화)
 * @property createdAt 이벤트 생성 시각
 */
@Entity
@Table(
    name = "tbl_outbox_event",
    indexes = [
        Index(name = "idx_aggregate", columnList = "aggregate_type, aggregate_id"),
        Index(name = "idx_created_at", columnList = "created_at"),
    ],
)
class OutboxEventJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(name = "aggregate_type", nullable = false, length = 255)
    val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false, length = 255)
    val aggregateId: String,
    @Column(name = "event_type", nullable = false, length = 255)
    val eventType: String,
    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    val payload: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
)
