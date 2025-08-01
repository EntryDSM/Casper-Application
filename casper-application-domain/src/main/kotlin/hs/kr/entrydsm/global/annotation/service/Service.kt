package hs.kr.entrydsm.global.annotation.service

import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * 도메인 서비스를 표시하는 어노테이션입니다.
 *
 * 도메인 서비스는 여러 엔티티나 값 객체에 걸쳐 있는 비즈니스 로직을 캡슐화하며,
 * 특정 엔티티에 속하지 않는 도메인 연산을 수행합니다.
 *
 * @param name 서비스의 이름 (설명)
 * @param type 서비스의 타입 (도메인 서비스, 애플리케이션 서비스 등)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.21
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Service(
    val name: String,
    val type: ServiceType
)