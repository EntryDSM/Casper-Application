package hs.kr.entrydsm.domain.ast.values

/**
 * 최적화 레벨 열거형
 */
enum class OptimizationLevel(val description: String) {
    NONE("최적화 없음"),
    BASIC("기본 최적화"),
    FULL("완전 최적화")
}