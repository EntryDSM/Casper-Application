package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.global.interfaces.EntityMarker
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 추상 구문 트리(AST)의 모든 노드에 대한 기본 sealed 클래스입니다.
 *
 * 계산기 언어의 모든 구문 요소를 나타내는 AST 노드들의 기본 인터페이스로,
 * Visitor 패턴을 지원하여 다양한 연산(평가, 출력, 변환 등)을 수행할 수 있습니다.
 * 모든 AST 노드는 이 클래스를 상속받아야 하며, 불변 객체로 설계됩니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
sealed class ASTNode : EntityMarker {
    
    private val id: String = java.util.UUID.randomUUID().toString()
    
    override fun getDomainContext(): String = "ast"
    
    override fun getIdentifier(): String = id

    /**
     * 이 AST 노드에 포함된 모든 변수 이름을 반환합니다.
     *
     * 표현식에서 사용되는 변수들을 추출하여 변수 의존성 분석이나
     * 변수 값 검증에 활용할 수 있습니다.
     *
     * @return 변수 이름의 집합
     */
    abstract fun getVariables(): Set<String>

    /**
     * AST 노드의 타입을 반환합니다.
     *
     * @return 노드 타입 문자열
     */
    fun getNodeType(): String = this::class.simpleName ?: "UnknownNode"

    /**
     * AST 노드가 리터럴 값인지 확인합니다.
     *
     * @return 리터럴이면 true, 아니면 false
     */
    open fun isLiteral(): Boolean = false

    /**
     * AST 노드가 리프 노드인지 확인합니다.
     * 리프 노드는 자식 노드가 없는 노드입니다.
     *
     * @return 리프 노드이면 true, 아니면 false
     */
    open fun isLeaf(): Boolean = getChildren().isEmpty()

    /**
     * 이 노드의 모든 자식 노드를 반환합니다.
     *
     * @return 자식 노드들의 리스트
     */
    abstract fun getChildren(): List<ASTNode>

    /**
     * AST 노드가 변수인지 확인합니다.
     *
     * @return 변수이면 true, 아니면 false
     */
    open fun isVariable(): Boolean = false

    /**
     * AST 노드가 연산자인지 확인합니다.
     *
     * @return 연산자이면 true, 아니면 false
     */
    open fun isOperator(): Boolean = false

    /**
     * AST 노드가 함수 호출인지 확인합니다.
     *
     * @return 함수 호출이면 true, 아니면 false
     */
    open fun isFunctionCall(): Boolean = false

    /**
     * AST 노드가 조건문인지 확인합니다.
     *
     * @return 조건문이면 true, 아니면 false
     */
    open fun isConditional(): Boolean = false

    /**
     * AST 노드의 깊이를 계산합니다.
     *
     * @return 노드의 최대 깊이
     */
    abstract fun getDepth(): Int

    /**
     * AST 노드의 총 노드 개수를 계산합니다.
     *
     * @return 하위 노드를 포함한 총 노드 개수
     */
    abstract fun getNodeCount(): Int

    /**
     * AST 노드의 크기(총 노드 개수)를 반환합니다.
     * getNodeCount()와 동일하지만 다른 컨텍스트에서 사용됩니다.
     *
     * @return 하위 노드를 포함한 총 노드 개수
     */
    fun getSize(): Int = getNodeCount()

    /**
     * AST 노드를 복제합니다.
     *
     * @return 복제된 AST 노드
     */
    abstract fun copy(): ASTNode

    /**
     * AST 노드의 구조를 트리 형태로 출력합니다.
     *
     * @param indent 들여쓰기 레벨
     * @return 트리 구조 문자열
     */
    open fun toTreeString(indent: Int = 0): String {
        val spaces = "  ".repeat(indent)
        return "$spaces${getNodeType()}: $this"
    }

    /**
     * AST 노드를 괄호 없이 간단한 형태로 출력합니다.
     *
     * @return 간단한 문자열 표현
     */
    abstract fun toSimpleString(): String

    /**
     * Visitor 패턴의 accept 메서드를 구현합니다.
     *
     * @param visitor 방문자 객체
     * @return 방문 결과
     */
    abstract fun <T> accept(visitor: ASTVisitor<T>): T

    /**
     * 두 AST 노드가 구조적으로 동일한지 확인합니다.
     *
     * @param other 비교할 AST 노드
     * @return 구조적으로 동일하면 true, 아니면 false
     */
    abstract fun isStructurallyEqual(other: ASTNode): Boolean

    /**
     * AST 노드의 유효성을 검증합니다.
     * 기본 구현에서는 true를 반환하며, 필요에 따라 하위 클래스에서 재정의할 수 있습니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    open fun validate(): Boolean = true

    /**
     * 조건문의 중첩 깊이를 반환합니다.
     * 기본값은 0이며, 조건문 노드에서 재정의됩니다.
     *
     * @return 중첩 깊이
     */
    open fun getNestingDepth(): Int = 0

    companion object {
        /**
         * AST 노드 타입을 확인하는 유틸리티 메서드입니다.
         *
         * @param node 확인할 노드
         * @return 노드 타입 정보
         */
        fun getNodeInfo(node: ASTNode): Map<String, Any> = mapOf(
            "type" to node.getNodeType(),
            "isLiteral" to node.isLiteral(),
            "isVariable" to node.isVariable(),
            "isOperator" to node.isOperator(),
            "isFunctionCall" to node.isFunctionCall(),
            "isConditional" to node.isConditional(),
            "depth" to node.getDepth(),
            "nodeCount" to node.getNodeCount(),
            "variables" to node.getVariables()
        )
    }
}

