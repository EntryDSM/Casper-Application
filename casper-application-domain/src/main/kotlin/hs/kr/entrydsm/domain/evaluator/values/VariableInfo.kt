package hs.kr.entrydsm.domain.evaluator.values

import java.time.LocalDateTime

/**
 * 변수 정보 데이터 클래스
 */
data class VariableInfo(
    val name: String,
    val type: VariableType,
    val isReadonly: Boolean,
    val hasValue: Boolean,
    val createdAt: LocalDateTime
)