package hs.kr.entrydsm.global.interfaces

import hs.kr.entrydsm.global.values.Result

/**
 * Anti-Corruption Layer의 기본 인터페이스입니다.
 *
 * DDD Anti-Corruption Layer 패턴을 적용하여 외부 시스템과의 통합에서
 * 도메인 모델을 보호하는 인터페이스를 정의합니다. 외부 시스템의 변경이
 * 내부 도메인에 영향을 주지 않도록 격리하며, 도메인 모델의 순수성을 유지합니다.
 *
 * @param Internal 내부 도메인 타입
 * @param External 외부 시스템 타입
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface AntiCorruptionLayer<Internal, External> : AntiCorruptionLayerMarker {
    
    /**
     * 외부 모델을 내부 도메인 모델로 변환합니다.
     *
     * @param external 외부 모델
     * @return 변환된 내부 도메인 모델
     */
    fun translateToInternal(external: External): Result<Internal, TranslationError>
    
    /**
     * 내부 도메인 모델을 외부 모델로 변환합니다.
     *
     * @param internal 내부 도메인 모델
     * @return 변환된 외부 모델
     */
    fun translateToExternal(internal: Internal): Result<External, TranslationError>
    
    /**
     * 변환 규칙이 유효한지 검증합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun validateTranslationRules(): Boolean
    
    /**
     * 외부 모델의 유효성을 검증합니다.
     *
     * @param external 검증할 외부 모델
     * @return 유효하면 true, 아니면 false
     */
    fun validateExternal(external: External): Boolean
    
    /**
     * 내부 도메인 모델의 유효성을 검증합니다.
     *
     * @param internal 검증할 내부 도메인 모델
     * @return 유효하면 true, 아니면 false
     */
    fun validateInternal(internal: Internal): Boolean
    
    /**
     * 변환 매핑 정보를 반환합니다.
     *
     * @return 매핑 정보
     */
    fun getMappingInfo(): Map<String, Any>
    
    /**
     * 지원되는 외부 시스템 버전을 반환합니다.
     *
     * @return 지원 버전 목록
     */
    fun getSupportedExternalVersions(): Set<String>
}

/**
 * 배치 변환을 지원하는 Anti-Corruption Layer 인터페이스입니다.
 */
interface BatchAntiCorruptionLayer<Internal, External> : AntiCorruptionLayer<Internal, External> {
    
    /**
     * 외부 모델들을 내부 도메인 모델들로 일괄 변환합니다.
     *
     * @param externals 외부 모델들
     * @return 변환된 내부 도메인 모델들
     */
    fun translateToInternalBatch(externals: List<External>): Result<List<Internal>, TranslationError>
    
    /**
     * 내부 도메인 모델들을 외부 모델들로 일괄 변환합니다.
     *
     * @param internals 내부 도메인 모델들
     * @return 변환된 외부 모델들
     */
    fun translateToExternalBatch(internals: List<Internal>): Result<List<External>, TranslationError>
    
    /**
     * 배치 처리 시 사용할 청크 크기를 반환합니다.
     *
     * @return 청크 크기
     */
    fun getChunkSize(): Int = 100
}

/**
 * 캐시를 지원하는 Anti-Corruption Layer 인터페이스입니다.
 */
interface CacheableAntiCorruptionLayer<Internal, External> : AntiCorruptionLayer<Internal, External> {
    
    /**
     * 변환 결과를 캐시에서 조회합니다.
     *
     * @param key 캐시 키
     * @return 캐시된 변환 결과 또는 null
     */
    fun getCachedTranslation(key: String): Result<Internal?, TranslationError>
    
    /**
     * 변환 결과를 캐시에 저장합니다.
     *
     * @param key 캐시 키
     * @param value 저장할 값
     */
    fun cacheTranslation(key: String, value: Internal): Result<Unit, TranslationError>
    
    /**
     * 캐시를 무효화합니다.
     *
     * @param key 무효화할 캐시 키
     */
    fun invalidateCache(key: String): Result<Unit, TranslationError>
    
    /**
     * 모든 캐시를 지웁니다.
     */
    fun clearCache(): Result<Unit, TranslationError>
}

/**
 * 비동기 변환을 지원하는 Anti-Corruption Layer 인터페이스입니다.
 */
interface AsyncAntiCorruptionLayer<Internal, External> : AntiCorruptionLayer<Internal, External> {
    
    /**
     * 외부 모델을 내부 도메인 모델로 비동기 변환합니다.
     *
     * @param external 외부 모델
     * @return 변환된 내부 도메인 모델
     */
    suspend fun translateToInternalAsync(external: External): Result<Internal, TranslationError>
    
    /**
     * 내부 도메인 모델을 외부 모델로 비동기 변환합니다.
     *
     * @param internal 내부 도메인 모델
     * @return 변환된 외부 모델
     */
    suspend fun translateToExternalAsync(internal: Internal): Result<External, TranslationError>
    
    /**
     * 외부 모델들을 내부 도메인 모델들로 비동기 일괄 변환합니다.
     *
     * @param externals 외부 모델들
     * @return 변환된 내부 도메인 모델들
     */
    suspend fun translateToInternalBatchAsync(externals: List<External>): Result<List<Internal>, TranslationError>
    
    /**
     * 내부 도메인 모델들을 외부 모델들로 비동기 일괄 변환합니다.
     *
     * @param internals 내부 도메인 모델들
     * @return 변환된 외부 모델들
     */
    suspend fun translateToExternalBatchAsync(internals: List<Internal>): Result<List<External>, TranslationError>
}

/**
 * 버전 호환성을 지원하는 Anti-Corruption Layer 인터페이스입니다.
 */
interface VersionedAntiCorruptionLayer<Internal, External> : AntiCorruptionLayer<Internal, External> {
    
    /**
     * 특정 버전의 외부 모델을 내부 도메인 모델로 변환합니다.
     *
     * @param external 외부 모델
     * @param version 외부 시스템 버전
     * @return 변환된 내부 도메인 모델
     */
    fun translateToInternal(external: External, version: String): Result<Internal, TranslationError>
    
    /**
     * 내부 도메인 모델을 특정 버전의 외부 모델로 변환합니다.
     *
     * @param internal 내부 도메인 모델
     * @param version 목표 외부 시스템 버전
     * @return 변환된 외부 모델
     */
    fun translateToExternal(internal: Internal, version: String): Result<External, TranslationError>
    
    /**
     * 현재 지원하는 기본 버전을 반환합니다.
     *
     * @return 기본 버전
     */
    fun getDefaultVersion(): String
    
    /**
     * 버전 간 호환성을 확인합니다.
     *
     * @param sourceVersion 소스 버전
     * @param targetVersion 대상 버전
     * @return 호환되면 true, 아니면 false
     */
    fun isVersionCompatible(sourceVersion: String, targetVersion: String): Boolean
}

/**
 * 변환 오류를 나타내는 sealed class입니다.
 */
sealed class TranslationError(
    val message: String,
    val cause: Throwable? = null
) {
    companion object {
        private const val MAPPING_ERROR = "매핑 규칙 오류"
        private const val DATA_TYPE_ERROR = "데이터 타입 오류"
        private const val OMISSION_REQUIRE_FIELD = "필수 필드 누락"
        private const val VALID_ERROR = "검증 오류"
        private const val VERSION_ERROR = "버전 호환성 오류"
        private const val EXTERNAL_SYSTEM_ERROR = "외부 시스템 오류"
        private const val CONFIGURATION_ERROR = "설정 오류"
        private const val UNKNOWN_ERROR = "알 수 없는 오류"
    }
    
    /**
     * 매핑 규칙 오류입니다.
     */
    data class MappingRuleError(val field: String, val reason: String) : 
        TranslationError("$MAPPING_ERROR [$field]: $reason")
    
    /**
     * 데이터 타입 오류입니다.
     */
    data class DataTypeError(val expectedType: String, val actualType: String) : 
        TranslationError("$DATA_TYPE_ERROR: 예상 $expectedType, 실제 $actualType")
    
    /**
     * 필수 필드 누락 오류입니다.
     */
    data class MissingFieldError(val fieldName: String) : 
        TranslationError("$OMISSION_REQUIRE_FIELD: $fieldName")
    
    /**
     * 검증 오류입니다.
     */
    data class ValidationError(val violations: List<String>) : 
        TranslationError("$VALID_ERROR: ${violations.joinToString(", ")}")
    
    /**
     * 버전 호환성 오류입니다.
     */
    data class VersionCompatibilityError(val sourceVersion: String, val targetVersion: String) : 
        TranslationError("$VERSION_ERROR: $sourceVersion -> $targetVersion")
    
    /**
     * 외부 시스템 오류입니다.
     */
    data class ExternalSystemError(val systemName: String, val reason: String, val throwable: Throwable? = null) : 
        TranslationError("$EXTERNAL_SYSTEM_ERROR [$systemName]: $reason", throwable)
    
    /**
     * 설정 오류입니다.
     */
    data class ConfigurationError(val reason: String) : 
        TranslationError("$CONFIGURATION_ERROR: $reason")
    
    /**
     * 알 수 없는 오류입니다.
     */
    data class UnknownError(val reason: String, val throwable: Throwable? = null) : 
        TranslationError("$UNKNOWN_ERROR: $reason", throwable)
}

/**
 * 변환 규칙을 정의하는 인터페이스입니다.
 */
interface TranslationRule<Internal, External> {
    
    /**
     * 변환 규칙을 적용합니다.
     *
     * @param source 소스 객체
     * @return 변환된 객체
     */
    fun apply(source: External): Result<Internal, TranslationError>
    
    /**
     * 역방향 변환 규칙을 적용합니다.
     *
     * @param source 소스 객체
     * @return 변환된 객체
     */
    fun applyReverse(source: Internal): Result<External, TranslationError>
    
    /**
     * 변환 규칙이 적용 가능한지 확인합니다.
     *
     * @param source 소스 객체
     * @return 적용 가능하면 true, 아니면 false
     */
    fun isApplicable(source: External): Boolean
    
    /**
     * 역방향 변환 규칙이 적용 가능한지 확인합니다.
     *
     * @param source 소스 객체
     * @return 적용 가능하면 true, 아니면 false
     */
    fun isReverseApplicable(source: Internal): Boolean
    
    /**
     * 변환 규칙의 우선순위를 반환합니다.
     *
     * @return 우선순위 (높을수록 우선)
     */
    fun getPriority(): Int = 0
}

/**
 * 변환 컨텍스트를 나타내는 데이터 클래스입니다.
 */
data class TranslationContext(
    val sourceSystem: String,
    val targetSystem: String,
    val version: String,
    val metadata: Map<String, Any> = emptyMap(),
    val options: TranslationOptions = TranslationOptions()
) {
    
    /**
     * 컨텍스트에 메타데이터를 추가합니다.
     *
     * @param key 메타데이터 키
     * @param value 메타데이터 값
     * @return 새로운 TranslationContext
     */
    fun withMetadata(key: String, value: Any): TranslationContext = 
        copy(metadata = metadata + (key to value))
    
    /**
     * 컨텍스트의 옵션을 변경합니다.
     *
     * @param newOptions 새로운 옵션
     * @return 새로운 TranslationContext
     */
    fun withOptions(newOptions: TranslationOptions): TranslationContext = 
        copy(options = newOptions)
}

/**
 * 변환 옵션을 나타내는 데이터 클래스입니다.
 */
data class TranslationOptions(
    val strictMode: Boolean = false,
    val ignoreUnknownFields: Boolean = true,
    val useCache: Boolean = true,
    val timeout: Long = 30000, // 30초
    val retryCount: Int = 3,
    val validateResult: Boolean = true
) {
    
    companion object {
        /**
         * 기본 옵션을 반환합니다.
         */
        fun default(): TranslationOptions = TranslationOptions()
        
        /**
         * 엄격 모드 옵션을 반환합니다.
         */
        fun strict(): TranslationOptions = TranslationOptions(
            strictMode = true,
            ignoreUnknownFields = false,
            validateResult = true
        )
        
        /**
         * 관대한 모드 옵션을 반환합니다.
         */
        fun lenient(): TranslationOptions = TranslationOptions(
            strictMode = false,
            ignoreUnknownFields = true,
            validateResult = false
        )
    }
}

/**
 * Anti-Corruption Layer 팩토리 인터페이스입니다.
 */
interface AntiCorruptionLayerFactory {
    
    /**
     * 지정된 타입들에 대한 Anti-Corruption Layer를 생성합니다.
     *
     * @param internalType 내부 도메인 타입
     * @param externalType 외부 시스템 타입
     * @return Anti-Corruption Layer 인스턴스
     */
    fun <Internal, External> create(
        internalType: Class<Internal>,
        externalType: Class<External>
    ): AntiCorruptionLayer<Internal, External>
    
    /**
     * 배치 처리를 지원하는 Anti-Corruption Layer를 생성합니다.
     *
     * @param internalType 내부 도메인 타입
     * @param externalType 외부 시스템 타입
     * @return 배치 Anti-Corruption Layer 인스턴스
     */
    fun <Internal, External> createBatch(
        internalType: Class<Internal>,
        externalType: Class<External>
    ): BatchAntiCorruptionLayer<Internal, External>
    
    /**
     * 캐시를 지원하는 Anti-Corruption Layer를 생성합니다.
     *
     * @param internalType 내부 도메인 타입
     * @param externalType 외부 시스템 타입
     * @return 캐시 지원 Anti-Corruption Layer 인스턴스
     */
    fun <Internal, External> createCacheable(
        internalType: Class<Internal>,
        externalType: Class<External>
    ): CacheableAntiCorruptionLayer<Internal, External>
    
    /**
     * 비동기를 지원하는 Anti-Corruption Layer를 생성합니다.
     *
     * @param internalType 내부 도메인 타입
     * @param externalType 외부 시스템 타입
     * @return 비동기 Anti-Corruption Layer 인스턴스
     */
    fun <Internal, External> createAsync(
        internalType: Class<Internal>,
        externalType: Class<External>
    ): AsyncAntiCorruptionLayer<Internal, External>
}