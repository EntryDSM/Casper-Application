package hs.kr.entrydsm.global.interfaces

/**
 * 값 객체(Value Object)를 위한 마커 인터페이스입니다.
 *
 * 불변성과 동등성 기반 비교를 보장하는 값 객체임을 명시합니다.
 */
interface ValueObject : DomainObject<String> {
    
    /**
     * 값 객체의 해시 코드를 계산합니다.
     * 모든 속성을 기반으로 계산되어야 합니다.
     *
     * @return 해시 코드
     */
    override fun hashCode(): Int
    
    /**
     * 값 객체의 동등성을 확인합니다.
     * 모든 속성이 같아야 동등한 것으로 판단합니다.
     *
     * @param other 비교할 객체
     * @return 동등하면 true, 아니면 false
     */
    override fun equals(other: Any?): Boolean
}