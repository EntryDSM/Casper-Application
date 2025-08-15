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
    
    /**
     * 두 명세 중 하나라도 만족되는지 검증합니다.
     *
     * @param candidate 검증할 객체
     * @return 두 명세 중 하나라도 만족하면 true, 모두 실패하면 false
     */
    override fun isSatisfiedBy(candidate: T): Boolean {
        return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate)
    }
    
    /**
     * 결합된 명세의 이름을 반환합니다.
     *
     * @return "[left] OR [right]" 형태의 명세 이름
     */
    override fun getName(): String = "${left.getName()} OR ${right.getName()}"
    
    /**
     * 결합된 명세의 설명을 반환합니다.
     *
     * @return "[left] OR [right]" 형태의 명세 설명
     */
    override fun getDescription(): String = "${left.getDescription()} OR ${right.getDescription()}"
    
    /**
     * 첫 번째 명세의 도메인을 반환합니다.
     *
     * @return 도메인 이름
     */
    override fun getDomain(): String = left.getDomain()
    
    /**
     * 두 명세 중 더 높은 우선순위를 반환합니다.
     *
     * @return 두 명세의 우선순위 중 최대값
     */
    override fun getPriority(): Priority = maxOf(left.getPriority(), right.getPriority())
    
    /**
     * OR 명세에 대한 에러 메시지를 반환합니다.
     *
     * @param candidate 검증에 실패한 객체
     * @return OR 명세 실패에 대한 에러 메시지
     */
    override fun getErrorMessage(candidate: T): String {
        return "객체가 명세 '${getName()}'를 만족하지 않습니다."
    }
}