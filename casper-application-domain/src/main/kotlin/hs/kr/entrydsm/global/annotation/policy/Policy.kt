package hs.kr.entrydsm.global.annotation.policy

import hs.kr.entrydsm.global.annotation.policy.type.Scope

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Policy(
    val name: String,
    val description: String,
    val domain: String,
    val scope: Scope,
)
