package hs.kr.entrydsm.application.global.annotation.usecase

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * UseCase는 비즈니스 로직을 정의하는 서비스 클래스를 식별하기 위해 사용되는 커스텀 어노테이션입니다.
 * 이 어노테이션이 적용된 클래스는 애플리케이션의 핵심 비즈니스 규칙을 캡슐화하며,
 * 트랜잭션 경계 내에서 실행됩니다.
 *
 * @see Transactional
 *
 * @author chaedohun
 * @since 2025.08.27
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
@Transactional
annotation class UseCase()
