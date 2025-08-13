package hs.kr.entrydsm.domain.ast.policies.validation

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import java.util.concurrent.atomic.AtomicLong

/**
 * 거듭제곱 연산자 (^) 검증 전략입니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */
class PowerValidationStrategy : BinaryOperatorValidationStrategy {
    
    override fun supportedOperator(): String = "^"
    
    override fun validate(left: ASTNode, right: ASTNode, optimizationCounter: AtomicLong) {
        if (isZeroConstant(left) && isZeroConstant(right)) {
            optimizationCounter.incrementAndGet()
            throw ASTException.zeroPowerZero()
        }
        
        // 거듭제곱 최적화 감지
        when {
            isOneConstant(left) -> {
                // 1^x = 1
                optimizationCounter.incrementAndGet()
            }
            isZeroConstant(right) -> {
                // x^0 = 1 (x != 0)
                optimizationCounter.incrementAndGet()
            }
            isOneConstant(right) -> {
                // x^1 = x
                optimizationCounter.incrementAndGet()
            }
        }
    }
}