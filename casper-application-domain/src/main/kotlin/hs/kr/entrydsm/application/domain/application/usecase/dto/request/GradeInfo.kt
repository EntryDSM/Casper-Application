package hs.kr.entrydsm.application.domain.application.usecase.dto.request

import java.math.BigDecimal

data class GradeInfo(
    val koreanGrade: String,
    val socialGrade: String,
    val historyGrade: String,
    val mathGrade: String,
    val scienceGrade: String,
    val englishGrade: String,
    val techAndHomeGrade: String,
    val gedKorean: BigDecimal = BigDecimal.ZERO,
    val gedSocial: BigDecimal = BigDecimal.ZERO,
    val gedMath: BigDecimal = BigDecimal.ZERO,
    val gedScience: BigDecimal = BigDecimal.ZERO,
    val gedEnglish: BigDecimal = BigDecimal.ZERO,
    val gedHistory: BigDecimal = BigDecimal.ZERO
)