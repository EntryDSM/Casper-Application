package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import java.time.YearMonth
import jakarta.validation.constraints.NotNull

data class UpdateEducationalStatusWebRequest(
    @NotNull
    val graduateDate: YearMonth,
    @NotNull
    val educationalStatus: EducationalStatus,
)
