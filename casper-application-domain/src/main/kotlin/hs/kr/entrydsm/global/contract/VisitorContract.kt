package hs.kr.entrydsm.global.contract

/**
 * Visitor 패턴을 위한 기본 계약 인터페이스입니다.
 *
 * 서로 다른 타입의 객체들에 대해 타입별로 다른 동작을 수행할 수 있도록
 * 하는 Visitor 패턴의 구현을 위한 기본 인터페이스입니다.
 * AST 순회, 표현식 평가 등에서 활용됩니다.
 *
 * @param T 방문할 노드의 기본 타입
 * @param R 방문 결과 타입
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface VisitorContract<T, R> {

    /**
     * 기본 방문 메서드입니다.
     *
     * 특정 타입에 대한 전용 방문 메서드가 없는 경우
     * 이 메서드가 호출됩니다.
     *
     * @param node 방문할 노드
     * @return 방문 결과
     */
    fun visit(node: T): R

    /**
     * 여러 노드를 순차적으로 방문합니다.
     *
     * @param nodes 방문할 노드들
     * @return 각 노드 방문 결과 리스트
     */
    fun visitAll(nodes: List<T>): List<R> = nodes.map { visit(it) }

    /**
     * 특정 조건을 만족하는 노드만 방문합니다.
     *
     * @param nodes 방문할 노드들
     * @param predicate 방문 조건
     * @return 조건을 만족하는 노드들의 방문 결과 리스트
     */
    fun visitIf(nodes: List<T>, predicate: (T) -> Boolean): List<R> = 
        nodes.filter(predicate).map { visit(it) }

    /**
     * 노드 방문 전에 실행되는 전처리 메서드입니다.
     *
     * @param node 방문할 노드
     */
    fun beforeVisit(node: T) {
        // 기본 구현은 아무것도 하지 않음
    }

    /**
     * 노드 방문 후에 실행되는 후처리 메서드입니다.
     *
     * @param node 방문한 노드
     * @param result 방문 결과
     */
    fun afterVisit(node: T, result: R) {
        // 기본 구현은 아무것도 하지 않음
    }
}