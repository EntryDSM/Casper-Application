package hs.kr.entrydsm.global.annotation.specification

import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * OR 조건을 구현하는 복합 명세입니다.
 *
 * 두 개의 명세 중 하나라도 만족되면 전체 명세가 만족되는 조건을 표현합니다.
 *
 * @param T 검증 대상 객체의 타입
 * @param left 첫 번째 명세
 * @param right 두 번째 명세
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
internal class OrSpecification<T>(
    private val left: SpecificationContract<T>,
    private val right: SpecificationContract<T>
) : SpecificationContract<T> {
    
    override fun isSatisfiedBy(candidate: T): Boolean {
        return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate)
    }
    
    override fun getName(): String = "${left.getName()} OR ${right.getName()}"
    
    override fun getDescription(): String = "${left.getDescription()} OR ${right.getDescription()}"
    
    override fun getDomain(): String = left.getDomain()
    
    override fun getPriority(): Priority = maxOf(left.getPriority(), right.getPriority())
    
    override fun getErrorMessage(candidate: T): String {
        return "객체가 명세 '${getName()}'를 만족하지 않습니다."
    }
}