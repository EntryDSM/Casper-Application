package hs.kr.entrydsm.global.configuration

/**
 * AST의 설정을 관리하는 데이터 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
data class ASTConfiguration(
    val maxDepth: Int = 1000,
    val maxNodes: Int = 100000,
    val enableOptimization: Boolean = true,
    val cachingEnabled: Boolean = true,
    val maxCacheSize: Int = 1000,
    val enableValidation: Boolean = true,
    val compressionEnabled: Boolean = false
)