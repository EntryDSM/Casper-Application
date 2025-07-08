package hs.kr.entrydsm.global.annotation.factory

import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * DDD(Domain-Driven Design)의 Factory 패턴을 나타내는 어노테이션입니다.
 *
 * Factory는 복잡한 객체 생성 로직을 캡슐화하여 도메인 객체의 생성을 담당하는
 * 클래스임을 나타냅니다. 특히 Aggregate나 Entity의 생성 과정이 복잡할 때 사용됩니다.
 *
 * @param context Factory가 속한 Bounded Context를 명시합니다.
 * @param complexity 객체 생성의 복잡도를 나타냅니다.
 * @param cache 생성된 객체를 캐시할지 여부를 결정합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처 </a>
 *
 * @author kangeunchan
 * @since 2025.07.08
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Factory(
    val context: String,
    val complexity: Complexity,
    val cache: Boolean,

    )