package hs.kr.entrydsm.application.global.web

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 카카오 API 사용에 필요한 정보들을 담는 Properties 입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@ConfigurationProperties(prefix = "kakao")
data class KakaoProperties(
    /**
     * 카카오 지오코드의 기준이 되는 장소의 위도
     */
    val lat: Double,
    /**
     * 카카오 지오코드의 기준이 되는 장소의 경도
     */
    val lon: Double,
    /**
     * 카카오 REST API KEY
     */
    val restKey: String,
    /**
     * 카카오 Geocode API URL
     */
    val url: String,
)
