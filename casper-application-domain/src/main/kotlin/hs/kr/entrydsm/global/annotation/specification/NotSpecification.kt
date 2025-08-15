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
    
    /**
     * 대상 명세의 결과를 부정하여 검증합니다.
     *
     * @param candidate 검증할 객체
     * @return 대상 명세가 실패하면 true, 성공하면 false
     */
    override fun isSatisfiedBy(candidate: T): Boolean {
        return !specification.isSatisfiedBy(candidate)
    }
    
    /**
     * 부정된 명세의 이름을 반환합니다.
     *
     * @return "NOT [specification]" 형태의 명세 이름
     */
    override fun getName(): String = "NOT ${specification.getName()}"
    
    /**
     * 부정된 명세의 설명을 반환합니다.
     *
     * @return "NOT [specification]" 형태의 명세 설명
     */
    override fun getDescription(): String = "NOT ${specification.getDescription()}"
    
    /**
     * 대상 명세의 도메인을 반환합니다.
     *
     * @return 도메인 이름
     */
    override fun getDomain(): String = specification.getDomain()
    
    /**
     * 대상 명세의 우선순위를 반환합니다.
     *
     * @return 대상 명세의 우선순위
     */
    override fun getPriority(): Priority = specification.getPriority()
    
    /**
     * NOT 명세에 대한 에러 메시지를 반환합니다.
     *
     * @param candidate 검증에 실패한 객체
     * @return NOT 명세 실패에 대한 에러 메시지
     */
    override fun getErrorMessage(candidate: T): String {
        return "객체가 명세 '${getName()}'를 만족하지 않습니다."
    }
}