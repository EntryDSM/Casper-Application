package hs.kr.entrydsm.domain.calculator.interfaces

import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest

/**
 * 일괄 처리 및 비동기 계산 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 일괄 처리 및 비동기 처리 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface BatchContract {

    /**
     * 일괄 계산을 수행합니다.
     *
     * @param requests 계산 요청들
     * @return 계산 결과들
     */
    fun calculateBatch(requests: List<CalculationRequest>): List<CalculationResult>

    /**
     * 비동기 계산을 수행합니다.
     *
     * @param request 계산 요청
     * @param callback 완료 콜백
     */
    fun calculateAsync(request: CalculationRequest, callback: (CalculationResult) -> Unit)
}