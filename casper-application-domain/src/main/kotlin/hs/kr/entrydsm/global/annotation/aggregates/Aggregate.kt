package hs.kr.entrydsm.global.annotation.aggregates

/**
 * Aggregate Root 를 나타내는 어노테이션 입니다.
 *
 * Aggregate는 데이터의 변경의 단위로 취급되는 연관된 객체들의 집합 의미하며,
 *이 어노테이션이 붙은 클래스는 해당 Aggregate의 루트 엔티티임을 나타냅니다.
 *
 * @param context Aggregate가 속한 Bounded Context를 명시합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처 </a>
 *
 *
 * @author kangeunchan
 * @since 2025.07.08
 * */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Aggregate(
    val context: String,
)