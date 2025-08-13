package hs.kr.entrydsm.domain.ast.policies.validation

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import java.util.concurrent.atomic.AtomicLong

/**
 * 곱셈 연산자 (*) 검증 전략입니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */
class MultiplicationValidationStrategy : BinaryOperatorValidationStrategy {
    
    override fun supportedOperator(): String = "*"
    
    override fun validate(left: ASTNode, right: ASTNode, optimizationCounter: AtomicLong) {
        // 0과의 곱셈 최적화 감지 (x * 0 = 0, 0 * x = 0)
        if (isZeroConstant(left) || isZeroConstant(right)) {
            optimizationCounter.incrementAndGet()
        }
        // 1과의 곱셈 최적화 감지 (x * 1 = x, 1 * x = x)
        else if (isOneConstant(left) || isOneConstant(right)) {
            optimizationCounter.incrementAndGet()
        }
    }
}