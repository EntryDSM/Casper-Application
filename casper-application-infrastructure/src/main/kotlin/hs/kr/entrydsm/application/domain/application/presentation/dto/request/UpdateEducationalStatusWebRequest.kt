package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import jakarta.validation.constraints.NotNull
import java.time.YearMonth

data class UpdateEducationalStatusWebRequest(
    @NotNull
    val graduateDate: YearMonth,
    @NotNull
    val educationalStatus: EducationalStatus,
)
