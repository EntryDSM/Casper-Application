package hs.kr.entrydsm.application.domain.application.spi

interface QueryLatitudeAndLongitudePort {
    fun queryLatitudeAndLongitudeByStreetAddress(
        streetAddress: String
    ): Pair<Double, Double>
}