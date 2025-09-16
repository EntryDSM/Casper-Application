package hs.kr.entrydsm.application.global.error

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null,
)
