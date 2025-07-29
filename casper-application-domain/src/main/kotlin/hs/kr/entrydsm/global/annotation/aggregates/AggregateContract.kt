package hs.kr.entrydsm.global.annotation.aggregates

/**
 * DDD의 Aggregate Root 패턴을 구현하는 클래스가 따라야 하는 계약을 정의합니다.
 *
 * Aggregate Root는 애그리게이트의 진입점 역할을 하며,
 * 비즈니스 규칙의 일관성을 보장하는 도메인 객체입니다.
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface AggregateContract {
    
    /**
     * 애그리게이트가 속한 컨텍스트를 반환합니다.
     *
     * @return 애그리게이트가 속한 컨텍스트 이름
     */
    fun getContext(): String
    
    /**
     * 애그리게이트의 고유 식별자를 반환합니다.
     *
     * @return 애그리게이트의 고유 식별자
     */
    fun getId(): Any
}