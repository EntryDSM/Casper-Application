package hs.kr.entrydsm.application.domain.examcode

import hs.kr.entrydsm.domain.examcode.interfaces.BaseLocationContract
import org.springframework.stereotype.Component

@Component
class ExamCodePersistenceAdapter(
    private val kakaoBaseProperties: KakaoBaseProperties
) : BaseLocationContract {

    override val baseLat: Double get() = kakaoBaseProperties.lat

    override val baseLon: Double get() = kakaoBaseProperties.lon
}