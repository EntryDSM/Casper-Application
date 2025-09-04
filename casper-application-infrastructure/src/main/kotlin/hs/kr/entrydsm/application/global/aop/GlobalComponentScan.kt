package hs.kr.entrydsm.application.global.aop

import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

/**
 * 애플리케이션 전역에서 특정 어노테이션을 스캔하여 컴포넌트로 등록하는 설정을 담당하는 클래스입니다.
 * [UseCase] 어노테이션이 적용된 클래스를 찾아 스프링 컨텍스트에 빈으로 등록합니다.
 *
 * @see UseCase
 * @see ComponentScan
 *
 * @author chaedohun
 * @since 2025.08.27
 */
@Configuration
@ComponentScan(
    basePackages = ["hs.kr.entrydsm.application"],
    includeFilters = [
        Filter(
            type = FilterType.ANNOTATION,
            classes = [
                UseCase::class,
            ],
        ),
    ],
)
class GlobalComponentScan
