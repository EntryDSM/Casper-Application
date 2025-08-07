package hs.kr.entrydsm.global.configuration

/**
 * 렉서의 설정을 관리하는 데이터 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
data class LexerConfiguration(
    val maxInputLength: Int = 100000,
    val maxTokens: Int = 50000,
    val bufferSize: Int = 8192,
    val enableValidation: Boolean = true,
    val strictMode: Boolean = false,
    val debugMode: Boolean = false
)