package hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request

import java.math.BigDecimal

data class UpdateQualificationCaseRequest(
    val koreanGrade: BigDecimal = BigDecimal.ZERO,
    val socialGrade: BigDecimal = BigDecimal.ZERO,
    val mathGrade: BigDecimal = BigDecimal.ZERO,
    val scienceGrade: BigDecimal = BigDecimal.ZERO,
    val englishGrade: BigDecimal = BigDecimal.ZERO,
    val historyGrade: BigDecimal = BigDecimal.ZERO,

    val extraScore: ExtraScoreRequest
)