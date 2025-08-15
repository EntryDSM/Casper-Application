package hs.kr.entrydsm.domain.ast.interfaces

import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.ArgumentsNode

/**
 * AST 노드를 방문하기 위한 인터페이스입니다 (Visitor 패턴).
 *
 * 각 노드 타입에 대한 방문 메서드를 정의하며, 다양한 AST 처리 로직을
 * 노드 클래스와 분리하여 구현할 수 있게 합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
interface ASTVisitor<T> {
    
    /**
     * 숫자 노드를 방문합니다.
     *
     * @param node 방문할 숫자 노드
     * @return 방문 결과
     */
    fun visitNumber(node: NumberNode): T

    /**
     * 불리언 노드를 방문합니다.
     *
     * @param node 방문할 불리언 노드
     * @return 방문 결과
     */
    fun visitBoolean(node: BooleanNode): T

    /**
     * 변수 노드를 방문합니다.
     *
     * @param node 방문할 변수 노드
     * @return 방문 결과
     */
    fun visitVariable(node: VariableNode): T

    /**
     * 이항 연산 노드를 방문합니다.
     *
     * @param node 방문할 이항 연산 노드
     * @return 방문 결과
     */
    fun visitBinaryOp(node: BinaryOpNode): T

    /**
     * 단항 연산 노드를 방문합니다.
     *
     * @param node 방문할 단항 연산 노드
     * @return 방문 결과
     */
    fun visitUnaryOp(node: UnaryOpNode): T

    /**
     * 함수 호출 노드를 방문합니다.
     *
     * @param node 방문할 함수 호출 노드
     * @return 방문 결과
     */
    fun visitFunctionCall(node: FunctionCallNode): T

    /**
     * 조건문 노드를 방문합니다.
     *
     * @param node 방문할 조건문 노드
     * @return 방문 결과
     */
    fun visitIf(node: IfNode): T

    /**
     * 인수 노드를 방문합니다.
     *
     * @param node 방문할 인수 노드
     * @return 방문 결과
     */
    fun visitArguments(node: ArgumentsNode): T
}