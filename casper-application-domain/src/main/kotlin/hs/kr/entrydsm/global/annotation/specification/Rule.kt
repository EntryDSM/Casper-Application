package hs.kr.entrydsm.global.annotation.specification

import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * DDD(Domain-Driven Design)의 비즈니스 규칙(Business Rule)을 나타내는 어노테이션입니다.
 *
 * 규칙은 도메인의 비즈니스 로직이나 검증 조건을 명시적으로 표현하는 데 사용됩니다.
 * Specification 패턴과 함께 사용되어 복잡한 비즈니스 조건을 구조화하고 재사용 가능하게 만듭니다.
 *
 * @param name 규칙의 이름
 * @param description 규칙의 상세 설명 및 적용 조건
 * @param domain 규칙이 속한 도메인(Bounded Context)
 * @param priority 규칙의 우선순위 (LOW, NORMAL, HIGH)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처 </a>
 *
 * @author kangeunchan
 * @since 2025.07.08
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Rule(
    val name: String,
    val description: String,
    val domain: String,
    val priority: Priority,
)