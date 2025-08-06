package hs.kr.entrydsm.domain.evaluator.entities

import hs.kr.entrydsm.global.annotation.entities.Entity
import java.time.Instant

/**
 * 수학 함수를 나타내는 엔티티입니다.
 *
 * DDD Entity 패턴을 적용하여 수학 함수의 메타데이터와 실행 정보를
 * 캡슐화합니다. 함수 이름, 인수 개수, 도메인 제약사항 등을 관리하며
 * 함수 호출의 유효성 검증과 실행을 담당합니다.
 *
 * @property name 함수 이름
 * @property minArguments 최소 인수 개수
 * @property maxArguments 최대 인수 개수
 * @property description 함수 설명
 * @property category 함수 카테고리
 * @property implementation 함수 구현체
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
data class MathFunction(
    val name: String,
    val minArguments: Int,
    val maxArguments: Int,
    val description: String,
    val category: FunctionCategory,
    val implementation: (List<Any>) -> Any,
    val createdAt: Instant = Instant.now(),
    val metadata: Map<String, Any> = emptyMap()
) {

    init {
        require(name.isNotBlank()) { "함수 이름은 비어있을 수 없습니다" }
        require(minArguments >= 0) { "최소 인수 개수는 음수일 수 없습니다: $minArguments" }
        require(maxArguments >= minArguments) { "최대 인수 개수는 최소 인수 개수보다 작을 수 없습니다: $maxArguments < $minArguments" }
        require(description.isNotBlank()) { "함수 설명은 비어있을 수 없습니다" }
    }

    /**
     * 함수 카테고리를 나타내는 열거형입니다.
     */
    enum class FunctionCategory(val displayName: String, val description: String) {
        ARITHMETIC("산술", "기본 산술 연산 함수"),
        TRIGONOMETRIC("삼각", "삼각 함수"),
        HYPERBOLIC("쌍곡", "쌍곡 함수"),
        LOGARITHMIC("로그", "로그 및 지수 함수"),
        STATISTICAL("통계", "통계 및 집계 함수"),
        LOGICAL("논리", "논리 연산 함수"),
        COMPARISON("비교", "비교 연산 함수"),
        CONVERSION("변환", "타입 및 단위 변환 함수"),
        UTILITY("유틸리티", "기타 유틸리티 함수"),
        CUSTOM("사용자정의", "사용자 정의 함수");

        override fun toString(): String = displayName
    }

    /**
     * 주어진 인수 개수가 유효한지 확인합니다.
     *
     * @param argumentCount 인수 개수
     * @return 유효하면 true
     */
    fun isValidArgumentCount(argumentCount: Int): Boolean {
        return argumentCount in minArguments..maxArguments
    }

    /**
     * 인수 개수를 검증합니다.
     *
     * @param arguments 인수 목록
     * @throws IllegalArgumentException 인수 개수가 유효하지 않은 경우
     */
    fun validateArgumentCount(arguments: List<Any>) {
        if (!isValidArgumentCount(arguments.size)) {
            throw IllegalArgumentException(
                "함수 '$name'의 인수 개수가 잘못되었습니다. " +
                "기대값: $minArguments-$maxArguments, 실제값: ${arguments.size}"
            )
        }
    }

    /**
     * 함수를 실행합니다.
     *
     * @param arguments 인수 목록
     * @return 실행 결과
     * @throws IllegalArgumentException 인수 개수가 유효하지 않은 경우
     */
    fun execute(arguments: List<Any>): Any {
        validateArgumentCount(arguments)
        return try {
            implementation(arguments)
        } catch (e: Exception) {
            throw RuntimeException("함수 '$name' 실행 중 오류 발생: ${e.message}", e)
        }
    }

    /**
     * 가변 인수로 함수를 실행합니다.
     *
     * @param arguments 가변 인수
     * @return 실행 결과
     */
    fun execute(vararg arguments: Any): Any {
        return execute(arguments.toList())
    }

    /**
     * 함수가 고정 인수 개수를 가지는지 확인합니다.
     *
     * @return 고정 인수 개수이면 true
     */
    fun hasFixedArgumentCount(): Boolean {
        return minArguments == maxArguments
    }

    /**
     * 함수가 가변 인수를 받는지 확인합니다.
     *
     * @return 가변 인수이면 true
     */
    fun isVariadic(): Boolean {
        return !hasFixedArgumentCount()
    }

    /**
     * 함수의 인수 범위를 문자열로 반환합니다.
     *
     * @return 인수 범위 문자열
     */
    fun getArgumentRangeString(): String {
        return if (hasFixedArgumentCount()) {
            minArguments.toString()
        } else {
            "$minArguments-$maxArguments"
        }
    }

    /**
     * 함수의 시그니처를 반환합니다.
     *
     * @return 함수 시그니처
     */
    fun getSignature(): String {
        val argRange = getArgumentRangeString()
        return "$name($argRange args)"
    }

    /**
     * 함수의 상세 정보를 반환합니다.
     *
     * @return 상세 정보 맵
     */
    fun getDetails(): Map<String, Any> = mapOf(
        "name" to name,
        "signature" to getSignature(),
        "description" to description,
        "category" to category.displayName,
        "minArguments" to minArguments,
        "maxArguments" to maxArguments,
        "hasFixedArgumentCount" to hasFixedArgumentCount(),
        "isVariadic" to isVariadic(),
        "createdAt" to createdAt,
        "metadata" to metadata
    )

    /**
     * 함수의 사용법 정보를 반환합니다.
     *
     * @return 사용법 문자열
     */
    fun getUsage(): String = buildString {
        appendLine("함수: $name")
        appendLine("카테고리: ${category.displayName}")
        appendLine("설명: $description")
        appendLine("인수: ${getArgumentRangeString()}개")
        appendLine("사용법: ${getSignature()}")
        if (metadata.isNotEmpty()) {
            appendLine("추가 정보:")
            metadata.forEach { (key, value) ->
                appendLine("  $key: $value")
            }
        }
    }

    override fun toString(): String {
        return "MathFunction(name='$name', category=${category.displayName}, args=${getArgumentRangeString()})"
    }

    companion object {
        /**
         * 가변 인수 함수의 실용적 최대 인수 개수 제한
         * 메모리 사용량과 성능을 고려한 합리적인 상한선
         */
        private const val MAX_PRACTICAL_ARGUMENTS = 1000
        
        /**
         * 고정 인수 개수를 가진 함수를 생성합니다.
         *
         * @param name 함수 이름
         * @param argumentCount 인수 개수
         * @param description 함수 설명
         * @param category 함수 카테고리
         * @param implementation 함수 구현체
         * @return MathFunction 인스턴스
         */
        fun fixedArgs(
            name: String,
            argumentCount: Int,
            description: String,
            category: FunctionCategory,
            implementation: (List<Any>) -> Any
        ): MathFunction {
            return MathFunction(
                name = name,
                minArguments = argumentCount,
                maxArguments = argumentCount,
                description = description,
                category = category,
                implementation = implementation
            )
        }

        /**
         * 가변 인수를 가진 함수를 생성합니다.
         *
         * @param name 함수 이름
         * @param minArgs 최소 인수 개수
         * @param maxArgs 최대 인수 개수
         * @param description 함수 설명
         * @param category 함수 카테고리
         * @param implementation 함수 구현체
         * @return MathFunction 인스턴스
         */
        fun varArgs(
            name: String,
            minArgs: Int,
            maxArgs: Int,
            description: String,
            category: FunctionCategory,
            implementation: (List<Any>) -> Any
        ): MathFunction {
            return MathFunction(
                name = name,
                minArguments = minArgs,
                maxArguments = maxArgs,
                description = description,
                category = category,
                implementation = implementation
            )
        }

        /**
         * 인수 개수 제한이 유연한 함수를 생성합니다.
         * 메모리와 성능을 고려하여 실용적인 상한선을 적용합니다.
         *
         * @param name 함수 이름
         * @param description 함수 설명
         * @param category 함수 카테고리
         * @param implementation 함수 구현체
         * @return MathFunction 인스턴스
         */
        fun unlimited(
            name: String,
            description: String,
            category: FunctionCategory,
            implementation: (List<Any>) -> Any
        ): MathFunction {
            return MathFunction(
                name = name,
                minArguments = 0,
                maxArguments = MAX_PRACTICAL_ARGUMENTS,
                description = description,
                category = category,
                implementation = implementation
            )
        }
        
        /**
         * 커스텀 최대 인수 개수로 가변 인수 함수를 생성합니다.
         *
         * @param name 함수 이름
         * @param minArgs 최소 인수 개수
         * @param maxArgs 최대 인수 개수 (MAX_PRACTICAL_ARGUMENTS로 제한됨)
         * @param description 함수 설명
         * @param category 함수 카테고리
         * @param implementation 함수 구현체
         * @return MathFunction 인스턴스
         */
        fun flexibleArgs(
            name: String,
            minArgs: Int,
            maxArgs: Int,
            description: String,
            category: FunctionCategory,
            implementation: (List<Any>) -> Any
        ): MathFunction {
            val safeMaxArgs = minOf(maxArgs, MAX_PRACTICAL_ARGUMENTS)
            return MathFunction(
                name = name,
                minArguments = minArgs,
                maxArguments = safeMaxArgs,
                description = description,
                category = category,
                implementation = implementation
            )
        }
    }
}