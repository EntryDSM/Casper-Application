package hs.kr.entrydsm.domain.examcode.util

import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo

data class DistanceGroup(
    val applicationType: String,
    val distanceCode: String,
    val examCodeInfoList: List<ExamCodeInfo>
)
