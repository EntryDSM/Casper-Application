package hs.kr.entrydsm.domain.calculator.interfaces

/**
 * 수식 검증 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 검증 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface ValidationContract {

    /**
     * 수식의 유효성을 검증합니다.
     *
     * @param expression 검증할 수식
     * @return 유효하면 true
     */
    fun validateExpression(expression: String): Boolean

    /**
     * 수식의 유효성을 검증합니다 (변수 포함).
     *
     * @param expression 검증할 수식
     * @param variables 변수 맵
     * @return 유효하면 true
     */
    fun validateExpression(expression: String, variables: Map<String, Any>): Boolean
}