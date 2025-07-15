package hs.kr.entrydsm.global.annotation.entity

/**
 * DDD의 Entity 패턴을 구현하는 클래스가 따라야 하는 계약을 정의합니다.
 *
 * Entity는 고유한 식별자를 가지며, 애그리게이트 내에서
 * 생명주기를 가지는 도메인 객체입니다.
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface EntityContract {
    
    /**
     * 엔티티의 고유 식별자를 반환합니다.
     *
     * @return 엔티티의 고유 식별자
     */
    fun getId(): Any
    
    /**
     * 엔티티가 속한 컨텍스트를 반환합니다.
     *
     * @return 엔티티가 속한 컨텍스트 이름
     */
    fun getContext(): String
    
    /**
     * 엔티티가 속한 애그리게이트 루트 클래스를 반환합니다.
     *
     * @return 애그리게이트 루트 클래스
     */
    fun getAggregateRootClass(): Class<*>
}
