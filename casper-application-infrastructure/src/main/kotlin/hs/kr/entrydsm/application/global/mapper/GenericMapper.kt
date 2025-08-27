package hs.kr.entrydsm.application.global.mapper

/**
 * 범용 매퍼 인터페이스
 * 
 * JPA 엔티티와 도메인 모델 간의 변환을 담당한다.
 * equus-application의 패턴을 그대로 따른다.
 */
interface GenericMapper<E, D> {
    fun toEntity(model: D): E
    fun toDomain(entity: E?): D?
    fun toDomainNotNull(entity: E): D
}