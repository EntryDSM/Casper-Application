package hs.kr.entrydsm.application.domain.application.presentation.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class GetApplicationStatusByGenderWebResponse(
    @JsonProperty("남자")
    val male: Int = 0,
    @JsonProperty("여자")
    val female: Int = 0,
)
