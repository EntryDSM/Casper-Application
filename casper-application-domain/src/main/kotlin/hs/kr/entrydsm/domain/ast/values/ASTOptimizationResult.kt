package hs.kr.entrydsm.domain.ast.values

import java.time.LocalDateTime

/**
 * 최적화 결과 데이터 클래스
 */
data class ASTOptimizationResult(
    val originalSize: NodeSize,
    val optimizedSize: NodeSize,
    val originalDepth: TreeDepth,
    val optimizedDepth: TreeDepth,
    val level: OptimizationLevel,
    val optimizedAt: LocalDateTime,
    val astId: String
) {
    fun getReductionRatio(): Double {
        return if (originalSize.value > 0) {
            (originalSize.value - optimizedSize.value).toDouble() / originalSize.value.toDouble()
        } else {
            0.0
        }
    }
}