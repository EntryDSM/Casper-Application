package hs.kr.entrydsm.domain.expresser.entities

import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 수식 포맷팅 스타일을 정의하는 열거형 엔티티입니다.
 *
 * 다양한 출력 형태와 사용 환경에 맞춰 수식을 표현하는 방식을 정의합니다.
 * 각 스타일은 고유한 표기법과 규칙을 가지며, 특정 용도에 최적화되어 있습니다.
 *
 * @property displayName 스타일의 표시 이름
 * @property description 스타일에 대한 설명
 * @property example 스타일 적용 예시
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(
    aggregateRoot = hs.kr.entrydsm.domain.expresser.aggregates.ExpressionFormatter::class,
    context = "expresser"
)
enum class FormattingStyle(
    val displayName: String,
    val description: String,
    val example: String
) {
    /**
     * 수학적 표기법 스타일
     * 
     * 전통적인 수학 표기법을 사용하며, 유니코드 수학 기호를 활용합니다.
     * 가독성이 높고 직관적인 표현이 특징입니다.
     */
    MATHEMATICAL(
        displayName = "수학적",
        description = "전통적인 수학 표기법을 사용하며 유니코드 수학 기호를 활용합니다",
        example = "x × y + √(z² + 1) ≤ π"
    ),

    /**
     * 프로그래밍 표기법 스타일
     * 
     * 프로그래밍 언어에서 사용되는 표기법을 따릅니다.
     * 키보드로 입력 가능한 문자만을 사용하며, 코드 작성에 적합합니다.
     */
    PROGRAMMING(
        displayName = "프로그래밍",
        description = "프로그래밍 언어에서 사용되는 표기법을 따릅니다",
        example = "x * y + sqrt(z^2 + 1) <= 3.14159"
    ),

    /**
     * LaTeX 표기법 스타일
     * 
     * LaTeX 문서 작성 시 사용되는 표기법입니다.
     * 학술 논문이나 전문 문서 작성에 적합합니다.
     */
    LATEX(
        displayName = "LaTeX",
        description = "LaTeX 문서 작성 시 사용되는 표기법입니다",
        example = "x \\times y + \\sqrt{z^{2} + 1} \\leq \\pi"
    ),

    /**
     * 간결한 표기법 스타일
     * 
     * 최소한의 문자로 표현하는 간결한 스타일입니다.
     * 공간이 제한된 환경이나 빠른 입력이 필요한 경우에 적합합니다.
     */
    COMPACT(
        displayName = "간결한",
        description = "최소한의 문자로 표현하는 간결한 스타일입니다",
        example = "x*y+sqrt(z^2+1)<=pi"
    ),

    /**
     * 상세한 표기법 스타일
     * 
     * 모든 연산과 구조를 명시적으로 표현하는 상세한 스타일입니다.
     * 교육용이나 명확한 설명이 필요한 경우에 적합합니다.
     */
    VERBOSE(
        displayName = "상세한",
        description = "모든 연산과 구조를 명시적으로 표현하는 상세한 스타일입니다",
        example = "(변수(x) 곱하기 변수(y)) 더하기 함수_제곱근(변수(z) 거듭제곱 2 더하기 1) 작거나 같다 π"
    );

    /**
     * 스타일이 유니코드 문자를 사용하는지 확인합니다.
     *
     * @return 유니코드 문자 사용 시 true, 아니면 false
     */
    fun usesUnicodeCharacters(): Boolean = when (this) {
        MATHEMATICAL -> true
        PROGRAMMING -> false
        LATEX -> false
        COMPACT -> false
        VERBOSE -> false
    }

    /**
     * 스타일이 공백을 포함하는지 확인합니다.
     *
     * @return 공백 포함 시 true, 아니면 false
     */
    fun includesSpaces(): Boolean = when (this) {
        MATHEMATICAL -> true
        PROGRAMMING -> true
        LATEX -> true
        COMPACT -> false
        VERBOSE -> true
    }

    /**
     * 스타일이 괄호를 자주 사용하는지 확인합니다.
     *
     * @return 괄호 자주 사용 시 true, 아니면 false
     */
    fun usesParenthesesFrequently(): Boolean = when (this) {
        MATHEMATICAL -> false
        PROGRAMMING -> true
        LATEX -> false
        COMPACT -> false
        VERBOSE -> true
    }

    /**
     * 스타일이 특수 문자를 사용하는지 확인합니다.
     *
     * @return 특수 문자 사용 시 true, 아니면 false
     */
    fun usesSpecialCharacters(): Boolean = when (this) {
        MATHEMATICAL -> true
        PROGRAMMING -> false
        LATEX -> true
        COMPACT -> false
        VERBOSE -> false
    }

    /**
     * 스타일의 길이 특성을 반환합니다.
     *
     * @return 길이 특성 (짧음, 보통, 긺)
     */
    fun getLengthCharacteristic(): String = when (this) {
        MATHEMATICAL -> "보통"
        PROGRAMMING -> "보통"
        LATEX -> "긺"
        COMPACT -> "짧음"
        VERBOSE -> "긺"
    }

    /**
     * 스타일의 주요 사용 분야를 반환합니다.
     *
     * @return 주요 사용 분야 목록
     */
    fun getPrimaryUseCases(): List<String> = when (this) {
        MATHEMATICAL -> listOf("수학 교육", "일반 문서", "표준 표기")
        PROGRAMMING -> listOf("소프트웨어 개발", "코드 생성", "API 문서")
        LATEX -> listOf("학술 논문", "수학 문서", "출판물")
        COMPACT -> listOf("제한된 공간", "빠른 입력", "간단한 표시")
        VERBOSE -> listOf("교육용", "상세 설명", "디버깅")
    }

    /**
     * 스타일의 가독성 수준을 반환합니다.
     *
     * @return 가독성 수준 (1-5, 높을수록 가독성이 좋음)
     */
    fun getReadabilityLevel(): Int = when (this) {
        MATHEMATICAL -> 5
        PROGRAMMING -> 4
        LATEX -> 3
        COMPACT -> 2
        VERBOSE -> 4
    }

    /**
     * 스타일의 입력 편의성을 반환합니다.
     *
     * @return 입력 편의성 (1-5, 높을수록 입력하기 쉬움)
     */
    fun getInputConvenience(): Int = when (this) {
        MATHEMATICAL -> 2
        PROGRAMMING -> 5
        LATEX -> 3
        COMPACT -> 4
        VERBOSE -> 1
    }

    /**
     * 스타일의 상세 정보를 맵으로 반환합니다.
     *
     * @return 스타일 정보 맵
     */
    fun getStyleInfo(): Map<String, Any> = mapOf(
        "name" to name,
        "displayName" to displayName,
        "description" to description,
        "example" to example,
        "usesUnicode" to usesUnicodeCharacters(),
        "includesSpaces" to includesSpaces(),
        "usesParentheses" to usesParenthesesFrequently(),
        "usesSpecialChars" to usesSpecialCharacters(),
        "lengthCharacteristic" to getLengthCharacteristic(),
        "primaryUseCases" to getPrimaryUseCases(),
        "readabilityLevel" to getReadabilityLevel(),
        "inputConvenience" to getInputConvenience()
    )

    companion object {
        /**
         * 기본 스타일을 반환합니다.
         *
         * @return 기본 스타일 (MATHEMATICAL)
         */
        fun default(): FormattingStyle = MATHEMATICAL

        /**
         * 표시 이름으로 스타일을 찾습니다.
         *
         * @param displayName 찾을 표시 이름
         * @return 해당 스타일 또는 null
         */
        fun findByDisplayName(displayName: String): FormattingStyle? =
            values().find { it.displayName == displayName }

        /**
         * 가독성이 가장 높은 스타일을 반환합니다.
         *
         * @return 가장 가독성이 높은 스타일
         */
        fun mostReadable(): FormattingStyle =
            values().maxByOrNull { it.getReadabilityLevel() } ?: MATHEMATICAL

        /**
         * 입력이 가장 편리한 스타일을 반환합니다.
         *
         * @return 가장 입력하기 편리한 스타일
         */
        fun mostInputFriendly(): FormattingStyle =
            values().maxByOrNull { it.getInputConvenience() } ?: PROGRAMMING

        /**
         * 특정 사용 분야에 적합한 스타일들을 반환합니다.
         *
         * @param useCase 사용 분야
         * @return 해당 분야에 적합한 스타일들
         */
        fun getStylesForUseCase(useCase: String): List<FormattingStyle> =
            values().filter { it.getPrimaryUseCases().contains(useCase) }

        /**
         * 모든 스타일의 통계를 반환합니다.
         *
         * @return 스타일 통계 맵
         */
        fun getStatistics(): Map<String, Any> = mapOf(
            "totalStyles" to values().size,
            "unicodeStyles" to values().count { it.usesUnicodeCharacters() },
            "spacedStyles" to values().count { it.includesSpaces() },
            "parenthesesStyles" to values().count { it.usesParenthesesFrequently() },
            "specialCharStyles" to values().count { it.usesSpecialCharacters() },
            "averageReadability" to values().map { it.getReadabilityLevel() }.average(),
            "averageInputConvenience" to values().map { it.getInputConvenience() }.average(),
            "lengthDistribution" to values().groupingBy { it.getLengthCharacteristic() }.eachCount()
        )
    }
}