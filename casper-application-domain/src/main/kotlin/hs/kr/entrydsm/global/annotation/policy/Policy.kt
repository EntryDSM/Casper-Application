package hs.kr.entrydsm.global.annotation.policy

import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * DDD(Domain-Driven Design)의 비즈니스 정책(Business Policy)을 나타내는 어노테이션입니다.
 *
 * 정책은 도메인의 비즈니스 규칙이나 제약사항을 명시적으로 표현하는 데 사용됩니다.
 * 이 어노테이션을 통해 정책의 내용, 적용 범위, 소속 도메인을 명확히 문서화할 수 있습니다.
 *
 * @param name 정책의 이름
 * @param description 정책의 상세 설명 및 비즈니스 규칙
 * @param domain 정책이 속한 도메인(Bounded Context)
 * @param scope 정책의 적용 범위 (GLOBAL, DOMAIN, AGGREGATE, ENTITY)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처 </a>
 *
 * @author kangeunchan
 * @since 2025.07.08
 */
annotation class Policy(
    val name: String,
    val description: String,
    val domain: String,
    val scope: Scope,
)