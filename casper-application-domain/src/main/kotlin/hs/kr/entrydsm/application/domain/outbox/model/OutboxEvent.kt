package hs.kr.entrydsm.application.domain.outbox.model

import java.time.LocalDateTime

/**
 * Outbox 패턴을 위한 이벤트 도메인 모델
 *
 * 도메인 이벤트를 동일 트랜잭션으로 저장하여
 * 이벤트 발행과 DB 저장의 원자성을 보장합니다.
 *
 * Debezium이 이 이벤트를 감지하여 Kafka로 자동 발행합니다.
 *
 * @property id 이벤트 고유 식별자
 * @property aggregateType 집계 루트 타입 (예: "Application", "Score", "GraduationCase")
 * @property aggregateId 집계 식별자 (예: receiptCode, userId)
 * @property eventType Kafka Topic 이름 (KafkaTopics 상수값)
 * @property payload 이벤트 데이터 (JSON 직렬화)
 * @property createdAt 이벤트 생성 시각
 */
data class OutboxEvent(
    val id: Long? = null,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * Outbox 이벤트 생성 팩토리 메서드
         */
        fun create(
            aggregateType: String,
            aggregateId: String,
            eventType: String,
            payload: String
        ): OutboxEvent {
            return OutboxEvent(
                aggregateType = aggregateType,
                aggregateId = aggregateId,
                eventType = eventType,
                payload = payload
            )
        }
    }
}
