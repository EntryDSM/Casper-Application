package hs.kr.entrydsm.global.annotation.specification

import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * AND 조건을 구현하는 복합 명세입니다.
 *
 * 두 개의 명세가 모두 만족되어야 전체 명세가 만족되는 조건을 표현합니다.
 *
 * @param T 검증 대상 객체의 타입
 * @param left 첫 번째 명세
 * @param right 두 번째 명세
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
internal class AndSpecification<T>(
    private val left: SpecificationContract<T>,
    private val right: SpecificationContract<T>
) : SpecificationContract<T> {
    
    override fun isSatisfiedBy(candidate: T): Boolean {
        return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate)
    }
    
    override fun getName(): String = "${left.getName()} AND ${right.getName()}"
    
    override fun getDescription(): String = "${left.getDescription()} AND ${right.getDescription()}"
    
    override fun getDomain(): String = left.getDomain()
    
    override fun getPriority(): Priority = maxOf(left.getPriority(), right.getPriority())
    
    override fun getErrorMessage(candidate: T): String {
        val errors = mutableListOf<String>()
        if (!left.isSatisfiedBy(candidate)) {
            errors.add(left.getErrorMessage(candidate))
        }
        if (!right.isSatisfiedBy(candidate)) {
            errors.add(right.getErrorMessage(candidate))
        }
        return errors.joinToString(" AND ")
    }
}