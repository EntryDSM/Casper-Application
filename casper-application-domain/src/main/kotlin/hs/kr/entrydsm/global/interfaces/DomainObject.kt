package hs.kr.entrydsm.global.interfaces

/**
 * 모든 도메인 객체가 구현해야 하는 최상위 인터페이스입니다.
 *
 * DDD의 모든 도메인 객체(Aggregate, Entity, Value Object)가 
 * 공통으로 가져야 하는 기본적인 식별 및 동등성 비교 기능을 정의합니다.
 * 극한 추상화를 통해 결합도를 최소화합니다.
 *
 * @param T 도메인 객체의 식별자 타입
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
interface DomainObject<T> {
    
    /**
     * 도메인 객체의 고유 식별자를 반환합니다.
     *
     * @return 객체의 식별자
     */
    fun getId(): T
    
    /**
     * 도메인 객체의 타입을 반환합니다.
     *
     * @return 객체 타입 문자열
     */
    fun getType(): String
    
    /**
     * 도메인 객체가 유효한지 검증합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean
    
    /**
     * 도메인 객체의 메타데이터를 반환합니다.
     *
     * @return 메타데이터 맵
     */
    fun getMetadata(): Map<String, Any> = emptyMap()
}