package hs.kr.entrydsm.global.annotation.specification.type

/**
 * 비즈니스 규칙(Rule)의 우선순위를 나타내는 열거형입니다.
 *
 * 여러 규칙이 적용될 때 실행 순서나 중요도를 결정하는 데 사용됩니다.
 * 높은 우선순위의 규칙이 먼저 처리되거나 더 엄격하게 적용될 수 있습니다.
 *
 * @author kangeunchan
 * @since 2025.07.08
 */
enum class Priority {
    /** 낮은 우선순위 - 선택적이거나 부차적인 규칙 */
    LOW,

    /** 일반 우선순위 - 표준적인 비즈니스 규칙 */
    NORMAL,

    /** 높은 우선순위 - 필수적이거나 중요한 규칙 */
    HIGH
}