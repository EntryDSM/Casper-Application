package hs.kr.entrydsm.application.domain.outbox.domain

import hs.kr.entrydsm.application.domain.outbox.domain.mapper.OutboxEventMapper
import hs.kr.entrydsm.application.domain.outbox.domain.repository.OutboxEventJpaRepository
import hs.kr.entrydsm.application.domain.outbox.model.OutboxEvent
import hs.kr.entrydsm.application.domain.outbox.spi.CommandOutboxEventPort
import org.springframework.stereotype.Component

/**
 * Outbox 이벤트 영속성 어댑터
 *
 * 도메인 계층의 CommandOutboxEventPort를 구현하여
 * 실제 데이터베이스 저장 로직을 담당합니다.
 */
@Component
class OutboxEventPersistenceAdapter(
    private val outboxEventMapper: OutboxEventMapper,
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
) : CommandOutboxEventPort {
    /**
     * Outbox 이벤트를 데이터베이스에 저장합니다.
     *
     * 도메인 모델을 JPA 엔티티로 변환하여 저장하고,
     * 저장된 엔티티를 다시 도메인 모델로 변환하여 반환합니다.
     *
     * @param outboxEvent 저장할 OutboxEvent 도메인 모델
     * @return 저장된 OutboxEvent (ID 포함)
     */
    override fun save(outboxEvent: OutboxEvent): OutboxEvent {
        return outboxEventJpaRepository.save(
            outboxEventMapper.toEntity(outboxEvent),
        ).let(outboxEventMapper::toDomainNotNull)
    }
}
