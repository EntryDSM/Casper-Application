package hs.kr.entrydsm.global.annotation.entities

import kotlin.reflect.KClass

/**
 * DDD(Domain-Driven Design)의 Entity를 나타내는 어노테이션입니다.
 *
 * Entity는 고유한 식별자를 가지며 생명주기 동안 상태가 변할 수 있는 도메인 객체입니다.
 * Aggregate Root가 아닌 Entity는 반드시 Aggregate Root를 통해서만 접근되어야 합니다.
 *
 * @param aggregateRoot 이 Entity가 속한 Aggregate Root 클래스
 * @param context Entity가 속한 Bounded Context를 명시합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처 </a>
 *
 * @author kangeunchan
 * @since 2025.07.08
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Entity(
    val aggregateRoot: KClass<*>,
    val context: String,
)