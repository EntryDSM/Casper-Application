package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 시작 빌더 - 문법의 시작 심볼용 빌더입니다.
 *
 * 확장된 문법의 시작 규칙에서 사용됩니다.
 * 예: START -> EXPR
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Policy(
    name = "Start Symbol Policy",
    description = "문법의 시작 심볼은 정확히 하나의 자식 노드를 가져야 함",
    domain = "ast",
    scope = Scope.DOMAIN
)
object StartBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): ASTNode {
        if (children.size != 1) {
            throw ASTException.startChildrenMismatch(1, children.size)
        }
        if (children[0] !is ASTNode) {
            throw ASTException.startFirstNotAst(children[0]::class.simpleName)
        }

        return children[0] as ASTNode
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 1 && children[0] is ASTNode
    }
    
    override fun getBuilderName(): String = "Start"
}