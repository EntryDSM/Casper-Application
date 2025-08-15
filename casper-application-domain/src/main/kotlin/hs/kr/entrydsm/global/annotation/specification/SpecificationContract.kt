package hs.kr.entrydsm.global.annotation.specification

import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * DDD의 Specification 패턴을 구현하는 클래스가 따라야 하는 계약을 정의합니다.
 *
 * Specification은 비즈니스 규칙이나 조건을 명시적으로 표현하고,
 * 복잡한 도메인 로직을 구조화하여 재사용 가능하게 만드는 패턴입니다.
 *
 * @param T 검증 대상 객체의 타입
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface SpecificationContract<T> {
    
    /**
     * 주어진 객체가 이 명세를 만족하는지 검증합니다.
     *
     * @param candidate 검증할 객체
     * @return 명세를 만족하면 true, 그렇지 않으면 false
     */
    fun isSatisfiedBy(candidate: T): Boolean
    
    /**
     * 이 명세와 다른 명세를 AND 조건으로 결합합니다.
     *
     * @param other 결합할 다른 명세
     * @return 두 명세를 AND로 결합한 새로운 명세
     */
    fun and(other: SpecificationContract<T>): SpecificationContract<T> {
        return AndSpecification(this, other)
    }
    
    /**
     * 이 명세와 다른 명세를 OR 조건으로 결합합니다.
     *
     * @param other 결합할 다른 명세
     * @return 두 명세를 OR로 결합한 새로운 명세
     */
    fun or(other: SpecificationContract<T>): SpecificationContract<T> {
        return OrSpecification(this, other)
    }
    
    /**
     * 이 명세를 부정(NOT)합니다.
     *
     * @return 이 명세를 부정한 새로운 명세
     */
    fun not(): SpecificationContract<T> {
        return NotSpecification(this)
    }
    
    /**
     * 명세의 이름을 반환합니다.
     *
     * @return 명세의 이름
     */
    fun getName(): String
    
    /**
     * 명세의 설명을 반환합니다.
     *
     * @return 명세의 상세 설명
     */
    fun getDescription(): String
    
    /**
     * 명세가 속한 도메인을 반환합니다.
     *
     * @return 명세가 속한 도메인명
     */
    fun getDomain(): String
    
    /**
     * 명세의 우선순위를 반환합니다.
     *
     * @return 명세의 우선순위 (LOW, NORMAL, HIGH)
     */
    fun getPriority(): Priority
    
    /**
     * 명세 검증에 실패했을 때의 에러 메시지를 반환합니다.
     *
     * @param candidate 검증에 실패한 객체
     * @return 에러 메시지
     */
    fun getErrorMessage(candidate: T): String = "객체가 명세 '${getName()}'를 만족하지 않습니다."
}