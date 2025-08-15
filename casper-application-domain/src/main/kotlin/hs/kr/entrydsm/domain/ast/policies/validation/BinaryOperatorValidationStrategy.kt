package hs.kr.entrydsm.domain.ast.policies.validation

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import java.util.concurrent.atomic.AtomicLong

/**
 * 이항 연산자별 검증 전략을 정의하는 인터페이스입니다.
 * 
 * Strategy 패턴을 적용하여 연산자별로 서로 다른 검증 로직을
 * 독립적으로 구현할 수 있도록 합니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */
interface BinaryOperatorValidationStrategy {
    
    /**
     * 이 전략이 지원하는 연산자를 반환합니다.
     */
    fun supportedOperator(): String
    
    /**
     * 연산자별 특별 검증을 수행합니다.
     *
     * @param left 좌측 피연산자
     * @param right 우측 피연산자
     * @param optimizationCounter 최적화 카운터 (필요 시 사용)
     */
    fun validate(
        left: ASTNode, 
        right: ASTNode, 
        optimizationCounter: AtomicLong
    )
    
    /**
     * 노드가 0 상수인지 확인합니다.
     */
    fun isZeroConstant(node: ASTNode): Boolean {
        return node.getNodeType() == "NumberNode" && node.toString() == "0"
    }
    
    /**
     * 노드가 1 상수인지 확인합니다.
     */
    fun isOneConstant(node: ASTNode): Boolean {
        return node.getNodeType() == "NumberNode" && node.toString() == "1"
    }
}