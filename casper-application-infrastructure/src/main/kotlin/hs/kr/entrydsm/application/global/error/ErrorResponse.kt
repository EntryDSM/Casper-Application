package hs.kr.entrydsm.application.global.error

data class ErrorResponse(
    val success: Boolean,
    val error: ErrorDetail,
    val timestamp: String
)