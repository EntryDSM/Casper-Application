package hs.kr.entrydsm.domain.calculator.interfaces

/**
 * 메타데이터 및 상태 조회 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 메타데이터, 성능 통계, 설정 정보 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface MetadataContract {

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
}