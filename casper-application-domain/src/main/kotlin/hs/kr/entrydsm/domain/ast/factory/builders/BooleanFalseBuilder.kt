package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * FALSE 불린 빌더 - false 불린 노드를 생성합니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Specification(
    name = "Boolean False Specification",
    description = "false 불린 리터럴은 항상 거짓 값을 나타내야 함",
    domain = "ast",
    priority = Priority.NORMAL
)
object BooleanFalseBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): BooleanNode {
        require(children.size == 1) { "BooleanFalse 빌더는 정확히 1개의 자식이 필요합니다: ${children.size}" }
        return BooleanNode.FALSE
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 1
    }
    
    override fun getBuilderName(): String = "BooleanFalse"
}