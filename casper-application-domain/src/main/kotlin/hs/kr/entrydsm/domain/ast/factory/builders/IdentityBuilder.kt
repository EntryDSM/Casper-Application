package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 항등 빌더 - 첫 번째 자식을 그대로 반환합니다.
 *
 * 주로 단일 항목을 감싸는 생성 규칙에서 사용됩니다.
 * 예: EXPR -> AND_EXPR
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Specification(
    name = "Identity Build Rule",
    description = "단일 자식 노드를 그대로 반환하는 항등 변환 규칙",
    domain = "ast",
    priority = Priority.NORMAL
)
object IdentityBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): ASTNode {
        if (children.isEmpty()) {
            throw ASTException.identityChildrenEmpty(actual = children.size)
        }
        if (children[0] !is ASTNode) {
            throw ASTException.identityFirstNotAstNode(children[0]::class.simpleName)
        }

        return children[0] as ASTNode
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 1 && children[0] is ASTNode
    }
    
    override fun getBuilderName(): String = "Identity"
}