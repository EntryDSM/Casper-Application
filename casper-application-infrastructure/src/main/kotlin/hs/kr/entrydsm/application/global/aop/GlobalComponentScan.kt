package hs.kr.entrydsm.application.global.aop

import hs.kr.entrydsm.application.global.annotation.DomainService
import hs.kr.entrydsm.application.global.annotation.Factory
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.annotation.UseCase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = ["hs.kr.entrydsm.application"],
    includeFilters = [
        Filter(
            type = FilterType.ANNOTATION,
            classes = [
                UseCase::class,
                ReadOnlyUseCase::class,
                DomainService::class,
                Factory::class,
            ],
        ),
    ],
)
class GlobalComponentScan
