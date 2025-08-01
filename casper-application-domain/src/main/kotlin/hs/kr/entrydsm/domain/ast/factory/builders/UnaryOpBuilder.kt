package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 단항 연산자 빌더 - 단항 연산 노드를 생성합니다.
 *
 * @property operator 연산자 문자열
 * @property operandIndex 피연산자의 인덱스 (기본값: 1)
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.NORMAL, cache = false)
@Specification(
    name = "Unary Operator Specification",
    description = "단항 연산자는 정확히 하나의 피연산자를 가져야 함",
    domain = "ast",
    priority = Priority.HIGH
)
class UnaryOpBuilder(
    private val operator: String,
    private val operandIndex: Int = 1
) : ASTBuilderContract {
    
    override fun build(children: List<Any>): UnaryOpNode {
        require(children.size >= operandIndex + 1) { 
            "UnaryOp 빌더는 최소 ${operandIndex + 1}개의 자식이 필요합니다: ${children.size}" 
        }
        
        val operand = children[operandIndex] as ASTNode
        return UnaryOpNode(operator, operand)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size >= operandIndex + 1 &&
               operandIndex < children.size && children[operandIndex] is ASTNode
    }
    
    override fun getBuilderName(): String = "UnaryOp($operator)"
}