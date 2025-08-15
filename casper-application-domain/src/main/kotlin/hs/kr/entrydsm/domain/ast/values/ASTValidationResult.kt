package hs.kr.entrydsm.domain.ast.values

import java.time.LocalDateTime

/**
 * 검증 결과 데이터 클래스
 */
data class ASTValidationResult(
    val isValid: Boolean,
    val violations: List<String>,
    val validatedAt: LocalDateTime,
    val astId: String
)