package hs.kr.entrydsm.domain.calculator.interfaces

import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest

/**
 * 기본 계산 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 기본 계산 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface CalculationContract {

    /**
     * 수식을 계산합니다.
     *
     * @param request 계산 요청
     * @return 계산 결과
     */
    fun calculate(request: CalculationRequest): CalculationResult

    /**
     * 수식 문자열을 직접 계산합니다.
     *
     * @param expression 수식 문자열
     * @return 계산 결과
     */
    fun calculate(expression: String): CalculationResult

    /**
     * 변수와 함께 수식을 계산합니다.
     *
     * @param expression 수식 문자열
     * @param variables 변수 맵
     * @return 계산 결과
     */
    fun calculate(expression: String, variables: Map<String, Any>): CalculationResult

    /**
     * 세션을 사용하여 계산합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션
     * @return 계산 결과와 업데이트된 세션
     */
    fun calculateWithSession(request: CalculationRequest, session: CalculationSession): Pair<CalculationResult, CalculationSession>
}