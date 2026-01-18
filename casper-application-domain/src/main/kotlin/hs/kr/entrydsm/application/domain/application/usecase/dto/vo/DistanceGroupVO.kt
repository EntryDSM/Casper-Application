package hs.kr.entrydsm.application.domain.application.usecase.dto.vo

data class DistanceGroupVO(
    val applicationType: String,
    val distanceCode: String,
    val examCodeInfoList: List<ExamCodeInfoVO>
)