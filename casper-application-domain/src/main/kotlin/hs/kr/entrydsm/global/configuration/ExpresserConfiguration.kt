package hs.kr.entrydsm.global.configuration

/**
 * 표현식 처리기의 설정을 관리하는 데이터 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
data class ExpresserConfiguration(
    val defaultTimeoutMs: Long = 30000,
    val maxRetries: Int = 3,
    val cachingEnabled: Boolean = true,
    val maxCacheSize: Int = 1000,
    val enableQualityCheck: Boolean = true,
    val enableSecurityFilter: Boolean = true,
    val supportedFormats: Set<String> = setOf("mathematical", "latex", "mathml", "html", "json", "xml")
)