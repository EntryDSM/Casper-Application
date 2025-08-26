package hs.kr.entrydsm.application.domain.examcode

import hs.kr.entrydsm.domain.examcode.interfaces.BaseLocationUseCase
import org.springframework.stereotype.Component

/**
 * 학교의 위치 정보를 외부(properties)에서 가져오는 Persistence Adapter 입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Component
class ExamCodePersistenceAdapter(
    private val kakaoProperties: KakaoProperties,
) : BaseLocationUseCase {

    /**
     * 기준이 되는 장소의 위도입니다.
     * @see kakaoProperties.lat
     */
    override val baseLat: Double get() = kakaoProperties.lat

    /**
     * 기준이 되는 장소의 경도입니다.
     * @see kakaoProperties.lon
     */
    override val baseLon: Double get() = kakaoProperties.lon
}
