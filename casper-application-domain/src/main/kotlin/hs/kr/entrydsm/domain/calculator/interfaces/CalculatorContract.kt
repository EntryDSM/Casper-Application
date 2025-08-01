package hs.kr.entrydsm.domain.calculator.interfaces

import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest

/**
 * 계산기의 핵심 계약을 정의하는 인터페이스입니다.
 *
 * Anti-Corruption Layer 역할을 수행하여 다양한 계산기 구현체들 간의
 * 호환성을 보장하며, 계산기의 핵심 기능을 표준화된 방식으로
 * 제공합니다. DDD 인터페이스 패턴을 적용하여 구현체와 클라이언트 간의
 * 결합도를 낮춥니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface CalculatorContract {

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

    /**
     * 수식을 파싱하고 구문 분석 결과를 반환합니다.
     *
     * @param expression 파싱할 수식
     * @return 파싱 결과 정보
     */
    fun parseExpression(expression: String): Map<String, Any>

    /**
     * 수식에 사용된 변수들을 추출합니다.
     *
     * @param expression 분석할 수식
     * @return 사용된 변수 집합
     */
    fun extractVariables(expression: String): Set<String>

    /**
     * 수식에 사용된 함수들을 추출합니다.
     *
     * @param expression 분석할 수식
     * @return 사용된 함수 집합
     */
    fun extractFunctions(expression: String): Set<String>

    /**
     * 수식의 복잡도를 계산합니다.
     *
     * @param expression 분석할 수식
     * @return 복잡도 수치
     */
    fun calculateComplexity(expression: String): Int

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

    /**
     * 지원되는 함수 목록을 반환합니다.
     *
     * @return 지원되는 함수 이름 집합
     */
    fun getSupportedFunctions(): Set<String>

    /**
     * 지원되는 연산자 목록을 반환합니다.
     *
     * @return 지원되는 연산자 집합
     */
    fun getSupportedOperators(): Set<String>

    /**
     * 지원되는 상수 목록을 반환합니다.
     *
     * @return 지원되는 상수 맵
     */
    fun getSupportedConstants(): Map<String, Any>

    /**
     * 계산기의 기능을 확인합니다.
     *
     * @param feature 확인할 기능 이름
     * @return 지원되면 true
     */
    fun supportsFeature(feature: String): Boolean

    /**
     * 계산기의 성능 통계를 반환합니다.
     *
     * @return 성능 통계 맵
     */
    fun getPerformanceStatistics(): Map<String, Any>

    /**
     * 계산기의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any>

    /**
     * 계산기의 상태를 확인합니다.
     *
     * @return 상태 정보 맵
     */
    fun getStatus(): Map<String, Any>

    /**
     * 계산기를 초기화합니다.
     */
    fun reset()

    /**
     * 계산기가 활성 상태인지 확인합니다.
     *
     * @return 활성 상태이면 true
     */
    fun isActive(): Boolean

    /**
     * 계산기를 종료합니다.
     */
    fun shutdown()

    /**
     * 계산기의 버전 정보를 반환합니다.
     *
     * @return 버전 정보
     */
    fun getVersion(): String

    /**
     * 계산기의 도움말 정보를 반환합니다.
     *
     * @return 도움말 문자열
     */
    fun getHelp(): String

    /**
     * 특정 함수의 도움말을 반환합니다.
     *
     * @param functionName 함수 이름
     * @return 함수 도움말
     */
    fun getFunctionHelp(functionName: String): String

    /**
     * 계산기의 한계와 제약사항을 반환합니다.
     *
     * @return 제약사항 정보 맵
     */
    fun getLimitations(): Map<String, Any>
}