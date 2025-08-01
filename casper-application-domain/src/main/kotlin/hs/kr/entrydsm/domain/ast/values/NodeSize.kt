package hs.kr.entrydsm.domain.ast.values

/**
 * AST 노드의 크기를 나타내는 값 객체입니다.
 *
 * 노드의 크기를 안전하게 관리하며, 크기 제한과 
 * 관련된 비즈니스 로직을 캡슐화합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
data class NodeSize private constructor(val value: Int) {
    
    init {
        require(value >= 0) { "노드 크기는 0 이상이어야 합니다: $value" }
        require(value <= MAX_SIZE) { "노드 크기가 최대값을 초과합니다: $value > $MAX_SIZE" }
    }
    
    /**
     * 크기를 증가시킵니다.
     */
    fun increment(): NodeSize {
        return of(value + 1)
    }
    
    /**
     * 크기를 감소시킵니다.
     */
    fun decrement(): NodeSize {
        return if (value > 0) of(value - 1) else this
    }
    
    /**
     * 지정된 값만큼 크기를 증가시킵니다.
     */
    fun plus(amount: Int): NodeSize {
        return of(value + amount)
    }
    
    /**
     * 지정된 값만큼 크기를 감소시킵니다.
     */
    fun minus(amount: Int): NodeSize {
        return of(maxOf(0, value - amount))
    }
    
    /**
     * 다른 크기와 더합니다.
     */
    fun plus(other: NodeSize): NodeSize {
        return of(value + other.value)
    }
    
    /**
     * 다른 크기와 뺍니다.
     */
    fun minus(other: NodeSize): NodeSize {
        return of(maxOf(0, value - other.value))
    }
    
    /**
     * 다른 크기와 비교합니다.
     */
    fun isGreaterThan(other: NodeSize): Boolean = value > other.value
    
    /**
     * 다른 크기와 비교합니다.
     */
    fun isLessThan(other: NodeSize): Boolean = value < other.value
    
    /**
     * 다른 크기와 비교합니다.
     */
    fun isGreaterOrEqual(other: NodeSize): Boolean = value >= other.value
    
    /**
     * 다른 크기와 비교합니다.
     */
    fun isLessOrEqual(other: NodeSize): Boolean = value <= other.value
    
    /**
     * 빈 크기인지 확인합니다.
     */
    fun isEmpty(): Boolean = value == 0
    
    /**
     * 단일 노드인지 확인합니다.
     */
    fun isSingle(): Boolean = value == 1
    
    /**
     * 작은 크기인지 확인합니다.
     */
    fun isSmall(): Boolean = value <= SMALL_SIZE
    
    /**
     * 중간 크기인지 확인합니다.
     */
    fun isMedium(): Boolean = value in (SMALL_SIZE + 1)..MEDIUM_SIZE
    
    /**
     * 큰 크기인지 확인합니다.
     */
    fun isLarge(): Boolean = value in (MEDIUM_SIZE + 1)..LARGE_SIZE
    
    /**
     * 매우 큰 크기인지 확인합니다.
     */
    fun isVeryLarge(): Boolean = value > LARGE_SIZE
    
    /**
     * 최대 크기와 비교합니다.
     */
    fun isAtLimit(): Boolean = value >= MAX_SIZE
    
    /**
     * 경고 수준인지 확인합니다.
     */
    fun isWarningLevel(): Boolean = value >= WARNING_SIZE
    
    /**
     * 두 크기 중 최대값을 반환합니다.
     */
    fun max(other: NodeSize): NodeSize {
        return if (value >= other.value) this else other
    }
    
    /**
     * 두 크기 중 최소값을 반환합니다.
     */
    fun min(other: NodeSize): NodeSize {
        return if (value <= other.value) this else other
    }
    
    /**
     * 백분율로 변환합니다.
     */
    fun toPercentage(): Double = (value.toDouble() / MAX_SIZE) * 100
    
    /**
     * 크기 레벨을 반환합니다.
     */
    fun getLevel(): SizeLevel {
        return when {
            value == 0 -> SizeLevel.EMPTY
            value <= SMALL_SIZE -> SizeLevel.SMALL
            value <= MEDIUM_SIZE -> SizeLevel.MEDIUM
            value <= LARGE_SIZE -> SizeLevel.LARGE
            value <= WARNING_SIZE -> SizeLevel.VERY_LARGE
            else -> SizeLevel.CRITICAL
        }
    }
    
    /**
     * 메모리 사용량을 추정합니다 (바이트 단위).
     */
    fun estimateMemoryUsage(): Long {
        return value * BYTES_PER_NODE
    }
    
    /**
     * 문자열 표현을 반환합니다.
     */
    override fun toString(): String = "NodeSize($value)"
    
    /**
     * 크기 레벨을 나타내는 열거형
     */
    enum class SizeLevel(val description: String) {
        EMPTY("빈 크기"),
        SMALL("작은 크기"),
        MEDIUM("중간 크기"),
        LARGE("큰 크기"),
        VERY_LARGE("매우 큰 크기"),
        CRITICAL("위험 크기")
    }
    
    companion object {
        private const val MAX_SIZE = 10000
        private const val WARNING_SIZE = 8000
        private const val LARGE_SIZE = 1000
        private const val MEDIUM_SIZE = 100
        private const val SMALL_SIZE = 10
        private const val BYTES_PER_NODE = 64L
        
        private val ZERO = NodeSize(0)
        private val ONE = NodeSize(1)
        
        /**
         * 값으로 NodeSize를 생성합니다.
         */
        fun of(value: Int): NodeSize {
            return when (value) {
                0 -> ZERO
                1 -> ONE
                else -> NodeSize(value)
            }
        }
        
        /**
         * 0 크기를 반환합니다.
         */
        fun zero(): NodeSize = ZERO
        
        /**
         * 1 크기를 반환합니다.
         */
        fun one(): NodeSize = ONE
        
        /**
         * 최대 크기를 반환합니다.
         */
        fun max(): NodeSize = of(MAX_SIZE)
        
        /**
         * 경고 크기를 반환합니다.
         */
        fun warning(): NodeSize = of(WARNING_SIZE)
        
        /**
         * 작은 크기를 반환합니다.
         */
        fun small(): NodeSize = of(SMALL_SIZE)
        
        /**
         * 중간 크기를 반환합니다.
         */
        fun medium(): NodeSize = of(MEDIUM_SIZE)
        
        /**
         * 큰 크기를 반환합니다.
         */
        fun large(): NodeSize = of(LARGE_SIZE)
        
        /**
         * 여러 크기 중 최대값을 반환합니다.
         */
        fun maxOf(vararg sizes: NodeSize): NodeSize {
            return sizes.maxByOrNull { it.value } ?: ZERO
        }
        
        /**
         * 여러 크기 중 최소값을 반환합니다.
         */
        fun minOf(vararg sizes: NodeSize): NodeSize {
            return sizes.minByOrNull { it.value } ?: ZERO
        }
        
        /**
         * 여러 크기의 합을 계산합니다.
         */
        fun sum(sizes: List<NodeSize>): NodeSize {
            if (sizes.isEmpty()) return ZERO
            val total = sizes.sumOf { it.value }
            return of(total)
        }
        
        /**
         * 평균 크기를 계산합니다.
         */
        fun average(sizes: List<NodeSize>): NodeSize {
            if (sizes.isEmpty()) return ZERO
            val avg = sizes.map { it.value }.average().toInt()
            return of(avg)
        }
        
        /**
         * 유효한 크기 범위인지 확인합니다.
         */
        fun isValidRange(size: Int): Boolean {
            return size in 0..MAX_SIZE
        }
        
        /**
         * 안전한 크기 생성 (범위 검증 없음)
         */
        fun tryOf(value: Int): NodeSize? {
            return if (isValidRange(value)) of(value) else null
        }
        
        /**
         * 총 메모리 사용량을 계산합니다.
         */
        fun calculateTotalMemoryUsage(sizes: List<NodeSize>): Long {
            return sizes.sumOf { it.estimateMemoryUsage() }
        }
    }
}