package hs.kr.entrydsm.application.domain.outbox.spi

import hs.kr.entrydsm.application.domain.outbox.model.OutboxEvent

/**
 * Outbox 이벤트 저장 포트
 *
 * 도메인 이벤트를 Outbox 테이블에 저장하는 인터페이스입니다.
 * Debezium이 이 테이블을 감시하여 Kafka로 이벤트를 발행합니다.
 */
interface CommandOutboxEventPort {
    /**
     * Outbox 이벤트를 저장합니다.
     *
     * 도메인 엔티티와 동일한 트랜잭션으로 저장되어 원자성을 보장합니다.
     *
     * @param outboxEvent 저장할 이벤트
     * @return 저장된 이벤트 (ID 포함)
     */
    fun save(outboxEvent: OutboxEvent): OutboxEvent
}
