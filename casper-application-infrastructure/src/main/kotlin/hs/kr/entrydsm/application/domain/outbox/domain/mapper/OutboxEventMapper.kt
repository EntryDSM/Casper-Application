package hs.kr.entrydsm.application.domain.outbox.domain.mapper

import hs.kr.entrydsm.application.domain.outbox.domain.entity.OutboxEventJpaEntity
import hs.kr.entrydsm.application.domain.outbox.model.OutboxEvent
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import org.mapstruct.Mapper

/**
 * OutboxEvent 도메인 모델과 JPA 엔티티 간 변환 매퍼
 *
 * MapStruct를 사용하여 자동으로 매핑 코드를 생성합니다.
 */
@Mapper
abstract class OutboxEventMapper : GenericMapper<OutboxEventJpaEntity, OutboxEvent> {
    /**
     * 도메인 모델을 JPA 엔티티로 변환
     *
     * @param model OutboxEvent 도메인 모델
     * @return OutboxEventJpaEntity
     */
    abstract override fun toEntity(model: OutboxEvent): OutboxEventJpaEntity

    /**
     * JPA 엔티티를 도메인 모델로 변환 (null 허용)
     *
     * @param entity OutboxEventJpaEntity (nullable)
     * @return OutboxEvent (nullable)
     */
    abstract override fun toDomain(entity: OutboxEventJpaEntity?): OutboxEvent?

    /**
     * JPA 엔티티를 도메인 모델로 변환 (non-null)
     *
     * @param entity OutboxEventJpaEntity (non-null)
     * @return OutboxEvent (non-null)
     */
    abstract override fun toDomainNotNull(entity: OutboxEventJpaEntity): OutboxEvent
}
