package hs.kr.entrydsm.domain.examcode.factories

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.examcode.exceptions.ExamCodeException
import hs.kr.entrydsm.domain.examcode.interfaces.BaseLocationContract
import hs.kr.entrydsm.domain.examcode.interfaces.KakaoGeocodeContract
import hs.kr.entrydsm.domain.examcode.util.DistanceUtil
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * 수험번호 정보를 생성하는 클래스입니다.
 *
 * @property kakaoGeocodeContract 카카오 지오코드 API
 * @property distanceUtil 거리 계산 유틸리티
 * @property baseLocationContract 기준 위치 정보
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Factory(
    context = "examCode",
    complexity = Complexity.HIGH,
    cache = false
)
class ExamCodeInfoFactory(
    private val kakaoGeocodeContract: KakaoGeocodeContract,
    private val distanceUtil: DistanceUtil,
    private val baseLocationContract: BaseLocationContract
) {

    /**
     * 지원서 정보를 바탕으로 수험번호 정보를 생성합니다.
     *
     * @param application 지원서
     * @return 생성된 수험번호 정보
     * @throws ExamCodeException.failedGeocodeConversion 주소 변환에 실패한 경우
     */
    suspend fun create(application: Application): ExamCodeInfo {
        val address = application.streetAddress as String
        val (userLat, userLon) = kakaoGeocodeContract.geocode(address)
            ?: throw ExamCodeException.failedGeocodeConversion(address)

        val distance = distanceUtil.haversine(
            baseLocationContract.baseLat, baseLocationContract.baseLon, userLat, userLon
        )

        return ExamCodeInfo(
            receiptCode = application.receiptCode,
            applicationType = application.applicationType!!,
            distance = distance
        )
    }
}
