package hs.kr.entrydsm.application.global.aop

import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = ["hs.kr.entrydsm.domain"],
    includeFilters = [
        Filter(
            type = FilterType.ANNOTATION,
            classes = [
                UseCase::class
            ],
        ),
    ],
)
class GlobalComponentScan