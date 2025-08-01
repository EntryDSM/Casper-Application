package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 괄호 빌더 - 괄호로 둘러싸인 표현식을 처리합니다.
 *
 * 괄호 안의 표현식을 추출하여 반환합니다.
 * 예: ( EXPR ) -> EXPR
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Policy(
    name = "Parenthesized Expression Policy",
    description = "괄호로 둘러싸인 표현식은 좌괄호, 표현식, 우괄호 순서로 구성되어야 함",
    domain = "ast",
    scope = Scope.AGGREGATE
)
object ParenthesizedBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): ASTNode {
        require(children.size == 3) { "Parenthesized 빌더는 정확히 3개의 자식이 필요합니다: ${children.size}" }
        return children[1] as ASTNode
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 3 && children[1] is ASTNode
    }
    
    override fun getBuilderName(): String = "Parenthesized"
}