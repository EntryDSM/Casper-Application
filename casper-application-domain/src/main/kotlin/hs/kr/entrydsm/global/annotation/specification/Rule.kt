package hs.kr.entrydsm.global.annotation.specification

import hs.kr.entrydsm.global.annotation.specification.type.Priority

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Rule(
    val name: String,
    val description: String,
    val domain: String,
    val priority: Priority,
)