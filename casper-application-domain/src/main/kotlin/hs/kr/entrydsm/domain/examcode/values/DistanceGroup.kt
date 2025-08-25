package hs.kr.entrydsm.domain.examcode.values

data class DistanceGroup(
    val applicationType: String,
    val distanceCode: String,
    val examCodeInfoList: MutableList<ExamCodeInfo>
)
