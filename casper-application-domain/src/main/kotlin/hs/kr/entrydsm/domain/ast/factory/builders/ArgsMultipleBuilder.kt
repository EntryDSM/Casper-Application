package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 다중 인수 빌더 - 기존 인수 목록에 새 인수를 추가합니다.
 *
 * 기존 인수 목록과 새로운 인수를 결합하여 확장된 인수 목록을 생성합니다.
 * 예: ARGS , EXPR -> Args
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Policy(
    name = "Multiple Arguments Policy",
    description = "다중 인수는 기존 인수 목록과 새로운 인수를 쉼표로 구분하여 결합해야 함",
    domain = "ast",
    scope = Scope.AGGREGATE
)
object ArgsMultipleBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): List<ASTNode> {
        require(children.size == 3) { "ArgsMultiple 빌더는 정확히 3개의 자식이 필요합니다: ${children.size}" }
        
        @Suppress("UNCHECKED_CAST")
        val existingArgs = children[0] as List<ASTNode>
        val newArg = children[2] as ASTNode
        
        return existingArgs + newArg
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 3 &&
               children[0] is List<*> &&
               (children[0] as List<*>).all { it is ASTNode } &&
               children[2] is ASTNode
    }
    
    override fun getBuilderName(): String = "ArgsMultiple"
}