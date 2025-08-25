package hs.kr.entrydsm.domain.examcode.interfaces

interface KakaoGecodeContract {
    suspend fun geocode(address: String): Pair<Double, Double>?
}