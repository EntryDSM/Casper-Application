package hs.kr.entrydsm.application.global.exception

abstract class WebException(
    open val status: Int,
    override val message: String,
) : RuntimeException()
