package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * IF 조건문 빌더 - IF 노드를 생성합니다.
 *
 * IF(조건, 참값, 거짓값) 형태의 조건문을 처리합니다.
 * 예: IF ( EXPR , EXPR , EXPR ) -> If
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.HIGH, cache = false)
@Policy(
    name = "Conditional Expression Policy",
    description = "IF 조건문은 조건식, 참값, 거짓값을 모두 가져야 하며 적절한 형식이어야 함",
    domain = "ast",
    scope = Scope.AGGREGATE
)
object IfBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): IfNode {
        if (children.size != 8) {
            throw ASTException.ifChildrenMismatch(8, children.size)
        }
        val condition = children[2] as ASTNode
        val trueValue = children[4] as ASTNode
        val falseValue = children[6] as ASTNode
        
        return IfNode(condition, trueValue, falseValue)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 8 &&
               children[2] is ASTNode &&
               children[4] is ASTNode &&
               children[6] is ASTNode
    }
    
    override fun getBuilderName(): String = "If"
}