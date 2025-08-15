package hs.kr.entrydsm.global.annotation.factory

/**
 * DDD의 Factory 패턴을 구현하는 클래스가 따라야 하는 계약을 정의합니다.
 *
 * Factory는 복잡한 객체 생성 로직을 캡슐화하고,
 * 도메인 객체의 생성 책임을 분리하는 패턴입니다.
 *
 * @param T 생성할 객체의 타입
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface FactoryContract<T> {
    
    /**
     * 주어진 매개변수를 사용하여 객체를 생성합니다.
     *
     * @param params 객체 생성에 필요한 매개변수들
     * @return 생성된 객체
     */
    fun create(vararg params: Any?): T
    
    /**
     * 팩토리가 속한 컨텍스트를 반환합니다.
     *
     * @return 팩토리가 속한 컨텍스트 이름
     */
    fun getContext(): String
    
    /**
     * 팩토리가 생성하는 대상 객체의 타입을 반환합니다.
     *
     * @return 대상 객체의 클래스 타입
     */
    fun getTargetType(): Class<T>
}