package hs.kr.entrydsm.global.interfaces

/**
 * 생성 전략을 위한 인터페이스입니다.
 *
 * 객체 생성의 복잡한 로직을 추상화합니다.
 *
 * @param T 생성할 객체의 타입
 */
interface CreationStrategy<T> {
    
    /**
     * 매개변수를 받아 객체를 생성합니다.
     *
     * @param params 생성 매개변수들
     * @return 생성된 객체
     */
    fun create(vararg params: Any?): T
    
    /**
     * 생성 전략이 주어진 매개변수를 지원하는지 확인합니다.
     *
     * @param params 확인할 매개변수들
     * @return 지원하면 true, 아니면 false
     */
    fun supports(vararg params: Any?): Boolean
}