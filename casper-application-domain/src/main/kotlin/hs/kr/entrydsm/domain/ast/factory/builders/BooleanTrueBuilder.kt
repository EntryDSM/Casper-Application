package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * TRUE 불린 빌더 - true 불린 노드를 생성합니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Specification(
    name = "Boolean True Specification",
    description = "true 불린 리터럴은 항상 참 값을 나타내야 함",
    domain = "ast",
    priority = Priority.NORMAL
)
object BooleanTrueBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): BooleanNode {
        return BooleanNode.TRUE
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return true // 자식 요소가 필요하지 않음
    }
    
    override fun getBuilderName(): String = "BooleanTrue"
}