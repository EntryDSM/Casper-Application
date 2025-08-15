package hs.kr.entrydsm.global.interfaces

/**
 * 검증 전략을 위한 인터페이스입니다.
 *
 * 다양한 검증 로직을 추상화합니다.
 *
 * @param T 검증할 객체의 타입
 */
interface ValidationStrategy<T> {
    
    /**
     * 객체를 검증합니다.
     *
     * @param target 검증할 객체
     * @return 검증 결과
     */
    fun validate(target: T): ValidationResult
    
    /**
     * 검증 전략의 우선순위를 반환합니다.
     *
     * @return 우선순위 (낮은 숫자가 높은 우선순위)
     */
    fun getPriority(): Int = Int.MAX_VALUE
}