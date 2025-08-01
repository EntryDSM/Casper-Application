package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 단일 인수 빌더 - 단일 인수 목록을 생성합니다.
 *
 * 하나의 표현식을 인수 목록으로 변환합니다.
 * 예: EXPR -> Args
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Specification(
    name = "Single Argument Specification",
    description = "단일 인수는 하나의 유효한 표현식이어야 함",
    domain = "ast",
    priority = Priority.NORMAL
)
object ArgsSingleBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): List<ASTNode> {
        require(children.size == 1) { "ArgsSingle 빌더는 정확히 1개의 자식이 필요합니다: ${children.size}" }
        return listOf(children[0] as ASTNode)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 1 && children[0] is ASTNode
    }
    
    override fun getBuilderName(): String = "ArgsSingle"
}