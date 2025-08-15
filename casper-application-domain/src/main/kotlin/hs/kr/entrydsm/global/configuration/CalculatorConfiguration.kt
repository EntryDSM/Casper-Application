package hs.kr.entrydsm.global.configuration

/**
 * 계산기의 설정을 관리하는 데이터 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
data class CalculatorConfiguration(
    val maxFormulaLength: Int = 5000,
    val maxVariables: Int = 100,
    val defaultTimeoutMs: Long = 30000,
    val maxRetries: Int = 3,
    val concurrency: Int = 10,
    val cachingEnabled: Boolean = true,
    val maxCacheSize: Int = 1000,
    val enableParallelProcessing: Boolean = true
)