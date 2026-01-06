package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.NotNull

data class AwardAndCertificateInfoWebRequest(
    @field:NotNull(message = "알고리즘 수상 여부는 필수입니다")
    val algorithmAward: Boolean,

    @field:NotNull(message = "자격증 여부는 필수입니다")
    val infoProcessingCert: Boolean
)