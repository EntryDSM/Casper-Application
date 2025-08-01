package hs.kr.entrydsm.domain.evaluator.entities

// Removed VariableBinding import for simplicity
import hs.kr.entrydsm.global.annotation.entities.Entity
import java.time.Instant

/**
 * 표현식 평가 컨텍스트를 관리하는 엔티티입니다.
 *
 * DDD Entity 패턴을 적용하여 평가 과정에서 필요한 상태와 설정을
 * 캡슐화합니다. 변수 바인딩, 함수 등록, 평가 설정 등을 관리하며
 * 평가 세션의 일관성을 보장합니다.
 *
 * @property id 컨텍스트 고유 식별자
 * @property variables 변수 바인딩
 * @property createdAt 생성 시간
 * @property lastModified 마지막 수정 시간
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Entity(
    aggregateRoot = hs.kr.entrydsm.domain.evaluator.aggregates.ExpressionEvaluator::class,
    context = "evaluator"
)
data class EvaluationContext(
    val id: String,
    val variables: Map<String, Any> = emptyMap(),
    val createdAt: Instant = Instant.now(),
    val lastModified: Instant = Instant.now(),
    val maxDepth: Int = 100,
    val maxVariables: Int = 1000,
    val enableOptimization: Boolean = true,
    val enableCaching: Boolean = true,
    val strictMode: Boolean = false,
    val metadata: Map<String, Any> = emptyMap()
) {

    /**
     * 변수를 추가합니다.
     *
     * @param name 변수 이름
     * @param value 변수 값
     * @return 새로운 컨텍스트
     */
    fun addVariable(name: String, value: Any): EvaluationContext {
        require(name.isNotBlank()) { "변수 이름은 비어있을 수 없습니다" }
        require(variables.size < maxVariables) { "최대 변수 개수를 초과했습니다: $maxVariables" }
        
        return copy(
            variables = variables + (name to value),
            lastModified = Instant.now()
        )
    }

    /**
     * 여러 변수를 일괄 추가합니다.
     *
     * @param newVariables 추가할 변수 맵
     * @return 새로운 컨텍스트
     */
    fun addVariables(newVariables: Map<String, Any>): EvaluationContext {
        require(variables.size + newVariables.size <= maxVariables) { 
            "최대 변수 개수를 초과했습니다: $maxVariables" 
        }
        
        return copy(
            variables = variables + newVariables,
            lastModified = Instant.now()
        )
    }

    /**
     * 변수를 제거합니다.
     *
     * @param name 제거할 변수 이름
     * @return 새로운 컨텍스트
     */
    fun removeVariable(name: String): EvaluationContext {
        return copy(
            variables = variables - name,
            lastModified = Instant.now()
        )
    }

    /**
     * 변수가 존재하는지 확인합니다.
     *
     * @param name 확인할 변수 이름
     * @return 존재하면 true
     */
    fun hasVariable(name: String): Boolean {
        return variables.containsKey(name)
    }

    /**
     * 변수 값을 조회합니다.
     *
     * @param name 조회할 변수 이름
     * @return 변수 값 (없으면 null)
     */
    fun getVariable(name: String): Any? {
        return variables[name]
    }

    /**
     * 모든 변수 이름을 반환합니다.
     *
     * @return 변수 이름 집합
     */
    fun getVariableNames(): Set<String> {
        return variables.keys
    }

    /**
     * 변수 개수를 반환합니다.
     *
     * @return 변수 개수
     */
    fun getVariableCount(): Int {
        return variables.size
    }

    /**
     * 컨텍스트를 초기화합니다.
     *
     * @return 초기화된 새로운 컨텍스트
     */
    fun clear(): EvaluationContext {
        return copy(
            variables = emptyMap(),
            lastModified = Instant.now()
        )
    }

    /**
     * 메타데이터를 추가합니다.
     *
     * @param key 메타데이터 키
     * @param value 메타데이터 값
     * @return 새로운 컨텍스트
     */
    fun withMetadata(key: String, value: Any): EvaluationContext {
        return copy(
            metadata = metadata + (key to value),
            lastModified = Instant.now()
        )
    }

    /**
     * 여러 메타데이터를 일괄 추가합니다.
     *
     * @param newMetadata 추가할 메타데이터 맵
     * @return 새로운 컨텍스트
     */
    fun withMetadata(newMetadata: Map<String, Any>): EvaluationContext {
        return copy(
            metadata = metadata + newMetadata,
            lastModified = Instant.now()
        )
    }

    /**
     * 최대 깊이를 설정합니다.
     *
     * @param depth 최대 깊이
     * @return 새로운 컨텍스트
     */
    fun withMaxDepth(depth: Int): EvaluationContext {
        require(depth > 0) { "최대 깊이는 양수여야 합니다: $depth" }
        return copy(
            maxDepth = depth,
            lastModified = Instant.now()
        )
    }

    /**
     * 엄격 모드를 설정합니다.
     *
     * @param strict 엄격 모드 활성화 여부
     * @return 새로운 컨텍스트
     */
    fun withStrictMode(strict: Boolean): EvaluationContext {
        return copy(
            strictMode = strict,
            lastModified = Instant.now()
        )
    }

    /**
     * 최적화 모드를 설정합니다.
     *
     * @param enabled 최적화 활성화 여부
     * @return 새로운 컨텍스트
     */
    fun withOptimization(enabled: Boolean): EvaluationContext {
        return copy(
            enableOptimization = enabled,
            lastModified = Instant.now()
        )
    }

    /**
     * 캐싱 모드를 설정합니다.
     *
     * @param enabled 캐싱 활성화 여부
     * @return 새로운 컨텍스트
     */
    fun withCaching(enabled: Boolean): EvaluationContext {
        return copy(
            enableCaching = enabled,
            lastModified = Instant.now()
        )
    }

    /**
     * 컨텍스트의 유효성을 검증합니다.
     *
     * @return 유효하면 true
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               maxDepth > 0 &&
               maxVariables > 0 &&
               variables.size <= maxVariables
    }

    /**
     * 컨텍스트의 요약 정보를 반환합니다.
     *
     * @return 요약 정보 맵
     */
    fun getSummary(): Map<String, Any> = mapOf(
        "id" to id,
        "variableCount" to getVariableCount(),
        "maxDepth" to maxDepth,
        "maxVariables" to maxVariables,
        "enableOptimization" to enableOptimization,
        "enableCaching" to enableCaching,
        "strictMode" to strictMode,
        "createdAt" to createdAt,
        "lastModified" to lastModified,
        "isValid" to isValid()
    )

    companion object {
        /**
         * 기본 컨텍스트를 생성합니다.
         *
         * @param id 컨텍스트 ID
         * @return 기본 컨텍스트
         */
        fun create(id: String): EvaluationContext {
            require(id.isNotBlank()) { "컨텍스트 ID는 비어있을 수 없습니다" }
            return EvaluationContext(
                id = id,
                variables = emptyMap()
            )
        }

        /**
         * 변수와 함께 컨텍스트를 생성합니다.
         *
         * @param id 컨텍스트 ID
         * @param variables 초기 변수 맵
         * @return 컨텍스트
         */
        fun create(id: String, variables: Map<String, Any>): EvaluationContext {
            return create(id).addVariables(variables)
        }

        /**
         * 빈 컨텍스트를 생성합니다.
         *
         * @return 빈 컨텍스트
         */
        fun empty(): EvaluationContext {
            return create("empty")
        }
    }
}