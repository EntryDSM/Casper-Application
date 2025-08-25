package hs.kr.entrydsm.application.domain.examcode

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakao.base")
class KakaoBaseProperties(
    val lat: Double,
    val lon: Double,
)
