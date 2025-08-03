package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 함수 호출 빌더 - 인수가 있는 함수 호출 노드를 생성합니다.
 *
 * 함수명과 인수 목록을 받아서 FunctionCallNode를 생성합니다.
 * 예: IDENTIFIER ( ARGS ) -> FunctionCall
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.NORMAL, cache = false)
@Policy(
    name = "Function Call Policy",
    description = "함수 호출은 유효한 함수명과 적절한 인수 목록을 가져야 함",
    domain = "ast",
    scope = Scope.AGGREGATE
)
object FunctionCallBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): FunctionCallNode {
        require(children.size == 3) { "FunctionCall 빌더는 정확히 3개의 자식이 필요합니다: ${children.size}" }
        require(children[0] is Token) { "첫 번째 자식은 Token이어야 합니다: ${children[0]::class.simpleName}" }
        require(children[2] is List<*>) { "세 번째 자식은 List여야 합니다: ${children[2]::class.simpleName}" }
        require((children[2] as List<*>).all { it is ASTNode }) { "인수 목록의 모든 요소는 ASTNode여야 합니다" }
        
        val nameToken = children[0] as Token
        val args = children[2] as List<ASTNode>
        
        return FunctionCallNode(nameToken.value, args)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 3 && 
               children[0] is Token &&
               children[2] is List<*> &&
               (children[2] as List<*>).all { it is ASTNode }
    }
    
    override fun getBuilderName(): String = "FunctionCall"
}