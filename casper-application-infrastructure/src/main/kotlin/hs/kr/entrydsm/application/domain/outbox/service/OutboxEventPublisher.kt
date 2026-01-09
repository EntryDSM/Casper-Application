package hs.kr.entrydsm.application.domain.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.outbox.model.OutboxEvent
import hs.kr.entrydsm.application.domain.outbox.spi.CommandOutboxEventPort
import hs.kr.entrydsm.application.domain.outbox.spi.OutboxEventPublisherPort
import org.springframework.stereotype.Service

/**
 * Outbox EventPublisher
 *
 * UseCase에서 이벤트를 발행할 때 사용하는 구현체입니다.
 * JSON 직렬화 후 Outbox 테이블에 저장합니다.
 *
 * @property commandOutboxEventPort Outbox 저장소 Port
 * @property objectMapper JSON 직렬화
 * @author kimseongwon
 * @date 2025/01/09
 * @version 1.0.0
 */
@Service
class OutboxEventPublisher(
    private val commandOutboxEventPort: CommandOutboxEventPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventPublisherPort {
    /**
     * 이벤트를 Outbox 테이블에 저장합니다.
     *
     * 페이로드 객체를 자동으로 JSON으로 변환합니다.
     * 도메인 엔티티와 동일 트랜잭션으로 저장되어 원자성을 보장합니다.
     *
     * @param T 페이로드 타입
     * @param aggregateType 집계 루트 타입
     * @param aggregateId 집계 식별자
     * @param eventType Kafka Topic 이름
     * @param payload 이벤트 데이터 객체
     */
    override fun <T> publish(
        aggregateType: String,
        aggregateId: String,
        eventType: String,
        payload: T,
    ) {
        val payloadJson = objectMapper.writeValueAsString(payload)

        val outboxEvent =
            OutboxEvent.create(
                aggregateType = aggregateType,
                aggregateId = aggregateId,
                eventType = eventType,
                payload = payloadJson,
            )

        commandOutboxEventPort.save(outboxEvent)
    }
}
