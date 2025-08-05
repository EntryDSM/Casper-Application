package hs.kr.entrydsm.domain.calculator.interfaces

/**
 * 계산기 생명주기 관리 기능을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 CalculatorContract에서
 * 생명주기 관리, 도움말, 제약사항 관련 메서드들만 분리한 인터페이스입니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.08.05
 */
interface LifecycleContract {

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