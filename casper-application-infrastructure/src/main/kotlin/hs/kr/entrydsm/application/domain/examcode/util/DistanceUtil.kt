package hs.kr.entrydsm.application.domain.examcode.util

import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 두 지점 간의 거리를 계산하는 유틸리티 클래스입니다.
 */
@Service(name = "DistanceUtil", type = ServiceType.APPLICATION_SERVICE)
class DistanceUtil {
    companion object {
        /** 지구의 반지름 (미터) */
        private const val R = 6371000.0
    }

    /**
     * Haversine 공식을 사용하여 두 지점 간의 거리를 계산합니다.
     *
     * @param baseLat 기준 위도
     * @param baseLon 기준 경도
     * @param compareLat 비교할 위도
     * @param compareLon 비교할 경도
     * @return 두 지점 간의 거리 (미터)
     */
    fun haversine(baseLat: Double, baseLon: Double, compareLat: Double, compareLon: Double): Int {
        val dLat = Math.toRadians(compareLat - baseLat)
        val dLon = Math.toRadians(compareLon - baseLon)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(baseLat)) *
                cos(Math.toRadians(compareLat)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (R * c).roundToInt()
    }
}