package hs.kr.entrydsm.domain.ast.policies.validation

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import java.util.concurrent.atomic.AtomicLong

/**
 * 나머지 연산자 (%) 검증 전략입니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */
class ModuloValidationStrategy : BinaryOperatorValidationStrategy {
    
    override fun supportedOperator(): String = "%"
    
    override fun validate(left: ASTNode, right: ASTNode, optimizationCounter: AtomicLong) {
        if (isZeroConstant(right)) {
            throw ASTException.moduloByZero()
        }
        if (isOneConstant(right)) {
            optimizationCounter.incrementAndGet()
        }
    }
}