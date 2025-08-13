package hs.kr.entrydsm.domain.ast.policies.validation

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import java.util.concurrent.atomic.AtomicLong

/**
 * 기본 연산자 (+, -) 검증 전략입니다.
 * 
 * 특별한 검증 로직이 없는 연산자들을 위한 기본 전략입니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */
class DefaultValidationStrategy(private val operator: String) : BinaryOperatorValidationStrategy {
    
    override fun supportedOperator(): String = operator
    
    override fun validate(left: ASTNode, right: ASTNode, optimizationCounter: AtomicLong) {
        when (operator) {
            "+" -> {
                // 0과의 덧셈 최적화 감지 (x + 0 = x, 0 + x = x)
                if (isZeroConstant(left) || isZeroConstant(right)) {
                    optimizationCounter.incrementAndGet()
                }
                // 같은 피연산자 최적화 감지 (x + x = 2*x)
                if (left.isStructurallyEqual(right)) {
                    optimizationCounter.incrementAndGet()
                }
            }
            "-" -> {
                // 0과의 뺄셈 최적화 감지 (x - 0 = x)
                if (isZeroConstant(right)) {
                    optimizationCounter.incrementAndGet()
                }
                // 0에서 빼기 최적화 감지 (0 - x = -x)
                if (isZeroConstant(left)) {
                    optimizationCounter.incrementAndGet()
                }
                // 같은 피연산자 최적화 감지 (x - x = 0)
                if (left.isStructurallyEqual(right)) {
                    optimizationCounter.incrementAndGet()
                }
            }
        }
    }
}