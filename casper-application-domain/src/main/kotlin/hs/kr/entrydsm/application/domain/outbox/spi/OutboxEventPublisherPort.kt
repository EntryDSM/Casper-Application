package hs.kr.entrydsm.application.domain.outbox.spi

/**
 * Outbox 이벤트 발행 포트
 *
 * UseCase에서 이벤트를 발행할 때 사용하는 인터페이스입니다.
 * Infrastructure 계층에서 JSON 직렬화 등의 기술적 관심사를 처리합니다.
 *
 * @author kimseongwon
 * @date 2025/01/09
 * @version 1.0.0
 */
interface OutboxEventPublisherPort {
    /**
     * 이벤트를 Outbox 테이블에 저장합니다.
     *
     * @param T 페이로드 타입
     * @param aggregateType 집계 루트 타입
     * @param aggregateId 집계 식별자
     * @param eventType Kafka Topic 이름
     * @param payload 이벤트 데이터 객체
     */
    fun <T> publish(
        aggregateType: String,
        aggregateId: String,
        eventType: String,
        payload: T
    )
}
