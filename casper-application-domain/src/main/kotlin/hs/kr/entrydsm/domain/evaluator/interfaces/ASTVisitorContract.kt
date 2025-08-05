package hs.kr.entrydsm.domain.evaluator.interfaces

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.ArgumentsNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor

/**
 * AST 방문자 패턴의 계약을 정의하는 인터페이스입니다.
 *
 * Visitor 패턴을 적용하여 AST 노드의 다양한 처리 로직을 
 * 분리하고 확장 가능한 구조를 제공합니다. 각 AST 노드 타입에 대한
 * 방문 메서드를 정의하여 타입 안전성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface ASTVisitorContract {

    /**
     * NumberNode를 방문합니다.
     *
     * @param node 방문할 NumberNode
     * @return 방문 결과
     */
    fun visitNumber(node: NumberNode): Any?

    /**
     * BooleanNode를 방문합니다.
     *
     * @param node 방문할 BooleanNode
     * @return 방문 결과
     */
    fun visitBoolean(node: BooleanNode): Any?

    /**
     * VariableNode를 방문합니다.
     *
     * @param node 방문할 VariableNode
     * @return 방문 결과
     */
    fun visitVariable(node: VariableNode): Any?

    /**
     * BinaryOpNode를 방문합니다.
     *
     * @param node 방문할 BinaryOpNode
     * @return 방문 결과
     */
    fun visitBinaryOp(node: BinaryOpNode): Any?

    /**
     * UnaryOpNode를 방문합니다.
     *
     * @param node 방문할 UnaryOpNode
     * @return 방문 결과
     */
    fun visitUnaryOp(node: UnaryOpNode): Any?

    /**
     * FunctionCallNode를 방문합니다.
     *
     * @param node 방문할 FunctionCallNode
     * @return 방문 결과
     */
    fun visitFunctionCall(node: FunctionCallNode): Any?

    /**
     * IfNode를 방문합니다.
     *
     * @param node 방문할 IfNode
     * @return 방문 결과
     */
    fun visitIf(node: IfNode): Any?

    /**
     * ArgumentsNode를 방문합니다.
     *
     * @param node 방문할 ArgumentsNode
     * @return 방문 결과
     */
    fun visitArguments(node: ArgumentsNode): Any?

    /**
     * AST 노드 방문을 위한 기본 메서드입니다.
     *
     * @param node 방문할 AST 노드
     * @return 방문 결과
     */
    fun <T> visit(node: ASTNode): T? = node.accept(this as ASTVisitor<T>)
}