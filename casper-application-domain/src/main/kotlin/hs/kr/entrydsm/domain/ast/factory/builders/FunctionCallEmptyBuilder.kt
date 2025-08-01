package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 빈 함수 호출 빌더 - 인수가 없는 함수 호출 노드를 생성합니다.
 *
 * 함수명만 받아서 빈 인수 목록을 가진 FunctionCallNode를 생성합니다.
 * 예: IDENTIFIER ( ) -> FunctionCall
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Specification(
    name = "Empty Function Call Specification",
    description = "인수가 없는 함수 호출은 함수명과 빈 괄호로 구성되어야 함",
    domain = "ast",
    priority = Priority.NORMAL
)
object FunctionCallEmptyBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): FunctionCallNode {
        require(children.size == 2) { "FunctionCallEmpty 빌더는 정확히 2개의 자식이 필요합니다: ${children.size}" }
        
        val nameToken = children[0] as Token
        return FunctionCallNode(nameToken.value, emptyList())
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 2 && children[0] is Token
    }
    
    override fun getBuilderName(): String = "FunctionCallEmpty"
}