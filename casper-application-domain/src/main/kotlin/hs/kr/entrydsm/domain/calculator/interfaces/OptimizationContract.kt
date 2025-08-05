package hs.kr.entrydsm.domain.calculator.interfaces

/**
 * 수식 최적화 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 최적화 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface OptimizationContract {

    /**
     * 수식을 최적화합니다.
     *
     * @param expression 최적화할 수식
     * @return 최적화된 수식
     */
    fun optimizeExpression(expression: String): String

    /**
     * 수식의 예상 실행 시간을 추정합니다.
     *
     * @param expression 분석할 수식
     * @return 예상 실행 시간 (밀리초)
     */
    fun estimateExecutionTime(expression: String): Long
}