package hs.kr.entrydsm.domain.examcode.util

import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Service(name = "DistanceUtil", type = ServiceType.APPLICATION_SERVICE)
class DistanceUtil {
    companion object {
        private const val R = 6371000.0
    }

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (R * c).roundToInt()
    }
}