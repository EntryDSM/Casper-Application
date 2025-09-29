package hs.kr.entrydsm.global.exception

abstract class BusinessException(
    open val status: Int,
    override val message: String,
) : RuntimeException()