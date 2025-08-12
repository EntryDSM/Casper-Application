package hs.kr.entrydsm.domain.ast.values

import hs.kr.entrydsm.domain.ast.exceptions.ASTException

/**
 * AST 트리의 깊이를 나타내는 값 객체입니다.
 *
 * 트리의 깊이를 안전하게 관리하며, 깊이 제한과 
 * 관련된 비즈니스 로직을 캡슐화합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
data class TreeDepth private constructor(val value: Int) {
    
    init {
        if (value < 0) {
            throw ASTException.treeDepthNegative(value)
        }

        if (value > MAX_DEPTH) {
            throw ASTException.treeDepthTooLarge(value, MAX_DEPTH)
        }
    }
    
    /**
     * 깊이를 증가시킵니다.
     */
    fun increment(): TreeDepth {
        return if (value < MAX_DEPTH) of(value + 1) else this
    }
    
    /**
     * 깊이를 감소시킵니다.
     */
    fun decrement(): TreeDepth {
        return if (value > 0) of(value - 1) else this
    }
    
    /**
     * 지정된 값만큼 깊이를 증가시킵니다.
     */
    fun plus(amount: Int): TreeDepth {
        return of(value + amount)
    }
    
    /**
     * 지정된 값만큼 깊이를 감소시킵니다.
     */
    fun minus(amount: Int): TreeDepth {
        return of(maxOf(0, value - amount))
    }
    
    /**
     * 다른 깊이와 비교합니다.
     */
    fun isGreaterThan(other: TreeDepth): Boolean = value > other.value
    
    /**
     * 다른 깊이와 비교합니다.
     */
    fun isLessThan(other: TreeDepth): Boolean = value < other.value
    
    /**
     * 다른 깊이와 비교합니다.
     */
    fun isGreaterOrEqual(other: TreeDepth): Boolean = value >= other.value
    
    /**
     * 다른 깊이와 비교합니다.
     */
    fun isLessOrEqual(other: TreeDepth): Boolean = value <= other.value
    
    /**
     * 최대 깊이와 비교합니다.
     */
    fun isAtLimit(): Boolean = value >= MAX_DEPTH
    
    /**
     * 경고 수준인지 확인합니다.
     */
    fun isWarningLevel(): Boolean = value >= WARNING_DEPTH
    
    /**
     * 얕은 깊이인지 확인합니다.
     */
    fun isShallow(): Boolean = value <= SHALLOW_DEPTH
    
    /**
     * 깊은 깊이인지 확인합니다.
     */
    fun isDeep(): Boolean = value >= DEEP_DEPTH
    
    /**
     * 두 깊이 중 최대값을 반환합니다.
     */
    fun max(other: TreeDepth): TreeDepth {
        return if (value >= other.value) this else other
    }
    
    /**
     * 두 깊이 중 최소값을 반환합니다.
     */
    fun min(other: TreeDepth): TreeDepth {
        return if (value <= other.value) this else other
    }
    
    /**
     * 백분율로 변환합니다.
     */
    fun toPercentage(): Double = (value.toDouble() / MAX_DEPTH) * 100
    
    /**
     * 깊이 레벨을 반환합니다.
     */
    fun getLevel(): DepthLevel {
        return when {
            value <= SHALLOW_DEPTH -> DepthLevel.SHALLOW
            value <= NORMAL_DEPTH -> DepthLevel.NORMAL
            value <= DEEP_DEPTH -> DepthLevel.DEEP
            value <= WARNING_DEPTH -> DepthLevel.WARNING
            else -> DepthLevel.CRITICAL
        }
    }
    
    /**
     * 문자열 표현을 반환합니다.
     */
    override fun toString(): String = "TreeDepth($value)"
    
    /**
     * 깊이 레벨을 나타내는 열거형
     */
    enum class DepthLevel(val description: String) {
        SHALLOW("얕은 깊이"),
        NORMAL("일반 깊이"),
        DEEP("깊은 깊이"),
        WARNING("경고 깊이"),
        CRITICAL("위험 깊이")
    }
    
    companion object {
        private const val MAX_DEPTH = 100
        private const val WARNING_DEPTH = 80
        private const val DEEP_DEPTH = 60
        private const val NORMAL_DEPTH = 40
        private const val SHALLOW_DEPTH = 10
        
        private val ZERO = TreeDepth(0)
        private val ONE = TreeDepth(1)
        
        /**
         * 값으로 TreeDepth를 생성합니다.
         */
        fun of(value: Int): TreeDepth {
            return when (value) {
                0 -> ZERO
                1 -> ONE
                else -> TreeDepth(value)
            }
        }
        
        /**
         * 0 깊이를 반환합니다.
         */
        fun zero(): TreeDepth = ZERO
        
        /**
         * 1 깊이를 반환합니다.
         */
        fun one(): TreeDepth = ONE
        
        /**
         * 최대 깊이를 반환합니다.
         */
        fun max(): TreeDepth = of(MAX_DEPTH)
        
        /**
         * 경고 깊이를 반환합니다.
         */
        fun warning(): TreeDepth = of(WARNING_DEPTH)
        
        /**
         * 여러 깊이 중 최대값을 반환합니다.
         */
        fun maxOf(vararg depths: TreeDepth): TreeDepth {
            return depths.maxByOrNull { it.value } ?: ZERO
        }
        
        /**
         * 여러 깊이 중 최소값을 반환합니다.
         */
        fun minOf(vararg depths: TreeDepth): TreeDepth {
            return depths.minByOrNull { it.value } ?: ZERO
        }
        
        /**
         * 평균 깊이를 계산합니다.
         */
        fun average(depths: List<TreeDepth>): TreeDepth {
            if (depths.isEmpty()) return ZERO
            val avg = depths.map { it.value }.average().toInt()
            return of(avg)
        }
        
        /**
         * 유효한 깊이 범위인지 확인합니다.
         */
        fun isValidRange(depth: Int): Boolean {
            return depth in 0..MAX_DEPTH
        }
        
        /**
         * 안전한 깊이 생성 (범위 검증 없음)
         */
        fun tryOf(value: Int): TreeDepth? {
            return if (isValidRange(value)) of(value) else null
        }
    }
}