package hs.kr.entrydsm.global.contract

/**
 * Visitor 패턴에서 방문 가능한 객체를 위한 계약 인터페이스입니다.
 *
 * Visitor 패턴의 Element 역할을 하는 객체들이 구현해야 하는 인터페이스로,
 * 방문자(Visitor)를 받아들이고 적절한 방문 메서드를 호출하는 책임을 가집니다.
 * AST 노드, 표현식 등에서 활용됩니다.
 *
 * @param R 방문 결과 타입
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface VisitableContract<R> {

    /**
     * 방문자를 받아들이고 적절한 방문 메서드를 호출합니다.
     *
     * 구현체는 자신의 타입에 맞는 visitor의 방문 메서드를 호출해야 합니다.
     * 이를 통해 Double Dispatch가 구현되어 런타임에 정확한 메서드가 선택됩니다.
     *
     * @param visitor 방문자 객체
     * @return 방문 결과
     */
    fun <T> accept(visitor: VisitorContract<T, R>): R

    /**
     * 방문 가능 여부를 확인합니다.
     *
     * 특정 조건하에서만 방문을 허용하고 싶은 경우 이 메서드를 오버라이드합니다.
     *
     * @return 방문 가능하면 true, 아니면 false
     */
    fun isVisitable(): Boolean = true

    /**
     * 방문자 타입이 지원되는지 확인합니다.
     *
     * @param visitorClass 확인할 방문자 클래스
     * @return 지원되면 true, 아니면 false
     */
    fun supportsVisitor(visitorClass: Class<*>): Boolean = true
}