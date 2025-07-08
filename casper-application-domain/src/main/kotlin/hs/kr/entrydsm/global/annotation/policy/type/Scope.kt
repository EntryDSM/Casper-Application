package hs.kr.entrydsm.global.annotation.policy.type

/**
 * 정책(Policy)의 적용 범위를 나타내는 열거형입니다.
 *
 * DDD에서 비즈니스 정책이나 도메인 규칙이 어느 레벨에서 적용되는지를
 * 명시적으로 표현하는 데 사용됩니다.
 *
 * @author kangeunchan
 * @since 2025.07.08
 */
enum class Scope {
    /** 전역 범위 - 시스템 전체에 적용되는 정책 */
    GLOBAL,

    /** 도메인 범위 - 특정 도메인(Bounded Context) 내에서만 적용되는 정책 */
    DOMAIN,

    /** 집합체 범위 - 특정 Aggregate 내에서만 적용되는 정책 */
    AGGREGATE,

    /** 엔티티 범위 - 특정 Entity에서만 적용되는 정책 */
    ENTITY
}