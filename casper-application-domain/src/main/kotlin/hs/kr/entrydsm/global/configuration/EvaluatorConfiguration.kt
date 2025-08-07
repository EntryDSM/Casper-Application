package hs.kr.entrydsm.global.configuration

/**
 * 평가기의 설정을 관리하는 데이터 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
data class EvaluatorConfiguration(
    val maxEvaluationDepth: Int = 1000,
    val defaultTimeoutMs: Long = 10000,
    val enableTypeChecking: Boolean = true,
    val strictMathMode: Boolean = false,
    val cachingEnabled: Boolean = true,
    val maxCacheSize: Int = 1000,
    val precisionDigits: Int = 15
)