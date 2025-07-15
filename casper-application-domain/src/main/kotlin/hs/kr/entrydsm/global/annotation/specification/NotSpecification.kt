package hs.kr.entrydsm.global.annotation.specification

import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * NOT 조건을 구현하는 복합 명세입니다.
 *
 * 기존 명세의 결과를 부정(NOT)하여 반대 조건을 표현합니다.
 *
 * @param T 검증 대상 객체의 타입
 * @param specification 부정할 대상 명세
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
internal class NotSpecification<T>(
    private val specification: SpecificationContract<T>
) : SpecificationContract<T> {
    
    override fun isSatisfiedBy(candidate: T): Boolean {
        return !specification.isSatisfiedBy(candidate)
    }
    
    override fun getName(): String = "NOT ${specification.getName()}"
    
    override fun getDescription(): String = "NOT ${specification.getDescription()}"
    
    override fun getDomain(): String = specification.getDomain()
    
    override fun getPriority(): Priority = specification.getPriority()
    
    override fun getErrorMessage(candidate: T): String {
        return "객체가 명세 '${getName()}'를 만족하지 않습니다."
    }
}