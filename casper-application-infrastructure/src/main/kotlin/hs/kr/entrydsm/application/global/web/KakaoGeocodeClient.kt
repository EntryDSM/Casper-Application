package hs.kr.entrydsm.application.global.web

import hs.kr.entrydsm.domain.examcode.interfaces.KakaoGecodeContract
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service(name = "KakaoGeocodeClient", type = ServiceType.APPLICATION_SERVICE)
class KakaoGeocodeClient(
    builder: WebClient.Builder,
    @Value("\${kakao.rest-key}") private val apiKey: String,
) : KakaoGecodeContract {
    override suspend fun geocode(address: String): Pair<Double, Double>? =
        coroutineScope {
            val res =
                webClient.get()
                    .uri { it.queryParam("query", address).build() }
                    .retrieve().bodyToMono<Map<String, Any>>().awaitSingle()

            @Suppress("UNCHECKED_CAST")
            val docs = res["documents"] as? List<Map<String, Any>> ?: return@coroutineScope null
            val first = docs.firstOrNull() ?: return@coroutineScope null
            val y = (first["y"] as String).toDouble() // lat
            val x = (first["x"] as String).toDouble() // lon
            y to x
        }

    private val webClient =
        builder
            .baseUrl("https://dapi.kakao.com/v2/local/search/address.json")
            .defaultHeader("Authorization", "KakaoAK $apiKey")
            .build()
}
