package hs.kr.entrydsm.domain.examcode.interfaces

/**
 * 카카오 주소-좌표 변환 API에 대한 인터페이스입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
interface KakaoGeocodeContract {

    /**
     * 주소를 위도, 경도 좌표로 변환합니다.
     *
     * @param address 변환할 주소
     * @return 변환된 (위도, 경도) 좌표, 변환 실패 시 null
     */
    suspend fun geocode(address: String): Pair<Double, Double>?
}
