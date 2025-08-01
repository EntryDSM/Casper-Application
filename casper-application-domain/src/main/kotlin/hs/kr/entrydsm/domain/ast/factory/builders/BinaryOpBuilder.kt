package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 이항 연산자 빌더 - 이항 연산 노드를 생성합니다.
 *
 * @property operator 연산자 문자열
 * @property leftIndex 좌측 피연산자의 인덱스 (기본값: 0)
 * @property rightIndex 우측 피연산자의 인덱스 (기본값: 2)
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.NORMAL, cache = false)
@Policy(
    name = "Binary Operator Policy",
    description = "이항 연산자는 정확히 두 개의 피연산자를 가져야 하며, 연산자 우선순위를 준수해야 함",
    domain = "ast",
    scope = Scope.AGGREGATE
)
class BinaryOpBuilder(
    private val operator: String,
    private val leftIndex: Int = 0,
    private val rightIndex: Int = 2
) : ASTBuilderContract {
    
    override fun build(children: List<Any>): BinaryOpNode {
        require(children.size >= maxOf(leftIndex, rightIndex) + 1) { 
            "BinaryOp 빌더는 최소 ${maxOf(leftIndex, rightIndex) + 1}개의 자식이 필요합니다: ${children.size}" 
        }
        
        val left = children[leftIndex] as ASTNode
        val right = children[rightIndex] as ASTNode
        
        return BinaryOpNode(left, operator, right)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size >= maxOf(leftIndex, rightIndex) + 1 &&
               leftIndex < children.size && children[leftIndex] is ASTNode &&
               rightIndex < children.size && children[rightIndex] is ASTNode
    }
    
    override fun getBuilderName(): String = "BinaryOp($operator)"
}