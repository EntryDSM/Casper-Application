package hs.kr.entrydsm.application.domain.score.usecase.dto.request

data class UpdateScoreRequest(
    val isCommon: Boolean,
    val receiptCode: Long,
)
