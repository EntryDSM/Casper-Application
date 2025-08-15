package hs.kr.entrydsm.global.annotation

/**
 * 도메인 이벤트임을 나타내는 마커 어노테이션입니다.
 *
 * DDD Event Sourcing 패턴을 적용하여 도메인에서 발생하는 중요한 사건들을
 * 표시합니다. 이 어노테이션이 붙은 클래스는 도메인 이벤트로 취급됩니다.
 *
 * @author kangeunchan
 * @since 2025.07.21
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DomainEvent