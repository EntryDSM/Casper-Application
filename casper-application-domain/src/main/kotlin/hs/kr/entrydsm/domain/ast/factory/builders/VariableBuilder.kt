package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 변수 빌더 - 변수 노드를 생성합니다.
 *
 * 토큰의 값을 변수명으로 사용하여 VariableNode를 생성합니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Policy(
    name = "Variable Naming Policy",
    description = "변수명은 유효한 식별자 형식이어야 하며 예약어가 아니어야 함",
    domain = "ast",
    scope = Scope.ENTITY
)
object VariableBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): VariableNode {
        if (children.size != 1) {
            throw ASTException.variableChildrenMismatch(1, children.size)
        }
        if (children[0] !is Token) {
            throw ASTException.variableFirstNotToken(children[0]::class.simpleName)
        }


        val token = children[0] as Token
        return VariableNode(token.value)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 1 && children[0] is Token
    }
    
    override fun getBuilderName(): String = "Variable"
}