package hs.kr.entrydsm.application.global.web.client

import hs.kr.entrydsm.application.global.web.KakaoProperties
import hs.kr.entrydsm.domain.examcode.interfaces.KakaoGeocodeContract
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

/**
 * 카카오 주소 -> 좌표 변환 API를 사용해 주소를 위경도로 변환하는 Client 입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Component
class KakaoGeocodeClient(
    private val builder: WebClient.Builder,
    private val kakaoBaseProperties: KakaoProperties,
) : KakaoGeocodeContract {

    /**
     * 주소를 위경도로 변환합니다.
     *
     * @param address 변환할 주소
     * @return 변환된 위경도
     */
    override suspend fun geocode(address: String): Pair<Double, Double>? =
        coroutineScope {
            val res =
                webClient.get()
                    .uri { it.queryParam("query", address).build() }
                    .retrieve().bodyToMono<Map<String, Any>>().awaitSingle()

            @Suppress("UNCHECKED_CAST")
            val docs = res["documents"] as? List<Map<String, Any>> ?: return@coroutineScope null
            val first = docs.firstOrNull() ?: return@coroutineScope null
            val y = (first["y"] as String).toDouble()
            val x = (first["x"] as String).toDouble()
            y to x
        }

    private val webClient = builder
            .baseUrl(kakaoBaseProperties.url)
            .defaultHeader("Authorization", "KakaoAK ${kakaoBaseProperties.restKey}")
            .build()
}