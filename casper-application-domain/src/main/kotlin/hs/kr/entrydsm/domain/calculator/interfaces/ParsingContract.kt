package hs.kr.entrydsm.domain.calculator.interfaces

/**
 * 수식 파싱 및 분석 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 파싱 및 분석 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface ParsingContract {

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
}