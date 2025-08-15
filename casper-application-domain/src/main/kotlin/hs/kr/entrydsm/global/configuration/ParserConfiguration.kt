package hs.kr.entrydsm.global.configuration

/**
 * 파서의 설정을 관리하는 데이터 클래스입니다.
 *
 * 파서 동작에 필요한 모든 설정값들을 중앙집중식으로 관리하여
 * 설정 상태 일관성을 보장합니다. Infrastructure 계층에서
 * ConfigurationProvider를 통해 런타임에 수정할 수 있습니다.
 *
 * @property debugMode 디버그 모드 활성화 여부
 * @property errorRecoveryMode 오류 복구 모드 활성화 여부
 * @property maxParsingDepth 최대 파싱 깊이
 * @property maxParsingSteps 최대 파싱 단계 수
 * @property maxStackDepth 최대 스택 깊이
 * @property maxTokenCount 최대 토큰 수
 * @property enableOptimizations 최적화 활성화 여부
 * @property cachingEnabled 캐싱 활성화 여부
 * @property streamingBatchSize 스트리밍 배치 크기
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
data class ParserConfiguration(
    val debugMode: Boolean = false,
    val errorRecoveryMode: Boolean = true,
    val maxParsingDepth: Int = 10000,
    val maxParsingSteps: Int = 100000,
    val maxStackDepth: Int = 10000,
    val maxTokenCount: Int = 50000,
    val enableOptimizations: Boolean = true,
    val cachingEnabled: Boolean = true,
    val streamingBatchSize: Int = 100
)