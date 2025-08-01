package hs.kr.entrydsm.domain.ast.factory.builders

import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 숫자 빌더 - 숫자 리터럴 노드를 생성합니다.
 *
 * 토큰의 값을 Double로 변환하여 NumberNode를 생성합니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.LOW, cache = false)
@Specification(
    name = "Number Literal Specification",
    description = "숫자 리터럴은 유효한 숫자 형식이어야 하며 Double로 변환 가능해야 함",
    domain = "ast",
    priority = Priority.HIGH
)
object NumberBuilder : ASTBuilderContract {
    override fun build(children: List<Any>): NumberNode {
        require(children.size == 1) { "Number 빌더는 정확히 1개의 자식이 필요합니다: ${children.size}" }
        
        val token = children[0] as Token
        val value = token.value.toDoubleOrNull() 
            ?: throw IllegalArgumentException("유효하지 않은 숫자 형식입니다: ${token.value}")
        
        return NumberNode(value)
    }
    
    override fun validateChildren(children: List<Any>): Boolean {
        return children.size == 1 && children[0] is Token &&
               (children[0] as Token).value.toDoubleOrNull() != null
    }
    
    override fun getBuilderName(): String = "Number"
}