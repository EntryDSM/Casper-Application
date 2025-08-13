package hs.kr.entrydsm.domain.expresser.entities

import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 수식 포맷팅 옵션을 정의하는 엔티티입니다.
 *
 * 포맷팅 스타일과 함께 사용되어 수식 출력의 세부적인 설정을 제어합니다.
 * 소수점 자릿수, 공백 처리, 괄호 사용 등 다양한 포맷팅 옵션을 제공하며,
 * 각 스타일에 맞는 기본 설정을 포함합니다.
 *
 * @property style 포맷팅 스타일
 * @property decimalPlaces 소수점 자릿수
 * @property addSpaces 공백 추가 여부
 * @property showIntegerAsDecimal 정수를 소수점으로 표시할지 여부
 * @property removeTrailingZeros 뒤쪽 0 제거 여부
 * @property useParentheses 괄호 사용 여부
 * @property showOperatorSpacing 연산자 공백 표시 여부
 * @property preferUnicodeSymbols 유니코드 기호 사용 선호 여부
 * @property compactFunctionCalls 함수 호출 압축 표시 여부
 * @property showFullPrecision 전체 정밀도 표시 여부
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
data class FormattingOptions(
    val style: FormattingStyle,
    val decimalPlaces: Int,
    val addSpaces: Boolean,
    val showIntegerAsDecimal: Boolean,
    val removeTrailingZeros: Boolean,
    val useParentheses: Boolean,
    val showOperatorSpacing: Boolean,
    val preferUnicodeSymbols: Boolean,
    val compactFunctionCalls: Boolean,
    val showFullPrecision: Boolean
) {
    
    init {
        if (decimalPlaces < 0) {
            throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.invalidFormatOption(
                "decimalPlaces=$decimalPlaces", "소수점 자릿수는 0 이상이어야 합니다"
            )
        }
        if (decimalPlaces > 15) {
            throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.invalidFormatOption(
                "decimalPlaces=$decimalPlaces", "소수점 자릿수는 15 이하여야 합니다"
            )
        }
    }

    /**
     * 스타일을 변경한 새로운 옵션을 생성합니다.
     *
     * @param newStyle 새로운 스타일
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withStyle(newStyle: FormattingStyle): FormattingOptions = copy(style = newStyle)

    /**
     * 소수점 자릿수를 변경한 새로운 옵션을 생성합니다.
     *
     * @param places 새로운 소수점 자릿수
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withDecimalPlaces(places: Int): FormattingOptions {
        if (places < 0) {
            throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.invalidFormatOption(
                "decimalPlaces=$places", "소수점 자릿수는 0 이상이어야 합니다"
            )
        }
        if (places > 15) {
            throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.invalidFormatOption(
                "decimalPlaces=$places", "소수점 자릿수는 15 이하여야 합니다"
            )
        }
        return copy(decimalPlaces = places)
    }

    /**
     * 공백 추가 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param add 공백 추가 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withSpaces(add: Boolean): FormattingOptions = copy(addSpaces = add)

    /**
     * 정수 소수점 표시 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param show 정수를 소수점으로 표시할지 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withIntegerAsDecimal(show: Boolean): FormattingOptions = copy(showIntegerAsDecimal = show)

    /**
     * 뒤쪽 0 제거 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param remove 뒤쪽 0 제거 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withTrailingZerosRemoved(remove: Boolean): FormattingOptions = copy(removeTrailingZeros = remove)

    /**
     * 괄호 사용 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param use 괄호 사용 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withParentheses(use: Boolean): FormattingOptions = copy(useParentheses = use)

    /**
     * 연산자 공백 표시 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param show 연산자 공백 표시 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withOperatorSpacing(show: Boolean): FormattingOptions = copy(showOperatorSpacing = show)

    /**
     * 유니코드 기호 사용 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param prefer 유니코드 기호 사용 선호 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withUnicodeSymbols(prefer: Boolean): FormattingOptions = copy(preferUnicodeSymbols = prefer)

    /**
     * 함수 호출 압축 표시 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param compact 함수 호출 압축 표시 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withCompactFunctionCalls(compact: Boolean): FormattingOptions = copy(compactFunctionCalls = compact)

    /**
     * 전체 정밀도 표시 설정을 변경한 새로운 옵션을 생성합니다.
     *
     * @param show 전체 정밀도 표시 여부
     * @return 새로운 FormattingOptions 인스턴스
     */
    fun withFullPrecision(show: Boolean): FormattingOptions = copy(showFullPrecision = show)

    /**
     * 스타일에 따라 자동으로 조정된 옵션을 반환합니다.
     *
     * @return 스타일에 맞게 조정된 FormattingOptions 인스턴스
     */
    fun adjustForStyle(): FormattingOptions = when (style) {
        FormattingStyle.MATHEMATICAL -> copy(
            addSpaces = true,
            preferUnicodeSymbols = true,
            useParentheses = false,
            showOperatorSpacing = true,
            compactFunctionCalls = false
        )
        FormattingStyle.PROGRAMMING -> copy(
            addSpaces = true,
            preferUnicodeSymbols = false,
            useParentheses = true,
            showOperatorSpacing = true,
            compactFunctionCalls = false
        )
        FormattingStyle.LATEX -> copy(
            addSpaces = true,
            preferUnicodeSymbols = false,
            useParentheses = false,
            showOperatorSpacing = true,
            compactFunctionCalls = false
        )
        FormattingStyle.COMPACT -> copy(
            addSpaces = false,
            preferUnicodeSymbols = false,
            useParentheses = false,
            showOperatorSpacing = false,
            compactFunctionCalls = true
        )
        FormattingStyle.VERBOSE -> copy(
            addSpaces = true,
            preferUnicodeSymbols = false,
            useParentheses = true,
            showOperatorSpacing = true,
            compactFunctionCalls = false
        )
    }

    /**
     * 스타일과 옵션이 일치하는지 확인합니다.
     *
     * @return 스타일과 옵션이 일치하면 true, 아니면 false
     */
    fun isConsistentWithStyle(): Boolean {
        val adjusted = adjustForStyle()
        return this.copy(style = adjusted.style) == adjusted
    }

    /**
     * 옵션의 유효성을 검증합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean = try {
        decimalPlaces >= 0 && decimalPlaces <= 15
    } catch (e: Exception) {
        false
    }

    /**
     * 옵션의 상세 정보를 맵으로 반환합니다.
     *
     * @return 옵션 정보 맵
     */
    fun getOptionsInfo(): Map<String, Any> = mapOf(
        "style" to style.name,
        "decimalPlaces" to decimalPlaces,
        "addSpaces" to addSpaces,
        "showIntegerAsDecimal" to showIntegerAsDecimal,
        "removeTrailingZeros" to removeTrailingZeros,
        "useParentheses" to useParentheses,
        "showOperatorSpacing" to showOperatorSpacing,
        "preferUnicodeSymbols" to preferUnicodeSymbols,
        "compactFunctionCalls" to compactFunctionCalls,
        "showFullPrecision" to showFullPrecision,
        "isConsistentWithStyle" to isConsistentWithStyle(),
        "isValid" to isValid()
    )

    /**
     * 옵션을 문자열로 표현합니다.
     *
     * @return 옵션 설명 문자열
     */
    override fun toString(): String = buildString {
        append("FormattingOptions(")
        append("style=${style.name}, ")
        append("decimalPlaces=$decimalPlaces, ")
        append("addSpaces=$addSpaces, ")
        append("showIntegerAsDecimal=$showIntegerAsDecimal, ")
        append("removeTrailingZeros=$removeTrailingZeros, ")
        append("useParentheses=$useParentheses, ")
        append("showOperatorSpacing=$showOperatorSpacing, ")
        append("preferUnicodeSymbols=$preferUnicodeSymbols, ")
        append("compactFunctionCalls=$compactFunctionCalls, ")
        append("showFullPrecision=$showFullPrecision")
        append(")")
    }

    companion object {
        /**
         * 기본 포맷팅 옵션을 반환합니다.
         *
         * @return 기본 FormattingOptions 인스턴스
         */
        fun default(): FormattingOptions = FormattingOptions(
            style = FormattingStyle.MATHEMATICAL,
            decimalPlaces = 6,
            addSpaces = true,
            showIntegerAsDecimal = false,
            removeTrailingZeros = true,
            useParentheses = false,
            showOperatorSpacing = true,
            preferUnicodeSymbols = true,
            compactFunctionCalls = false,
            showFullPrecision = false
        )

        /**
         * 수학적 스타일 옵션을 반환합니다.
         *
         * @return 수학적 스타일 FormattingOptions 인스턴스
         */
        fun mathematical(): FormattingOptions = FormattingOptions(
            style = FormattingStyle.MATHEMATICAL,
            decimalPlaces = 6,
            addSpaces = true,
            showIntegerAsDecimal = false,
            removeTrailingZeros = true,
            useParentheses = false,
            showOperatorSpacing = true,
            preferUnicodeSymbols = true,
            compactFunctionCalls = false,
            showFullPrecision = false
        )

        /**
         * 프로그래밍 스타일 옵션을 반환합니다.
         *
         * @return 프로그래밍 스타일 FormattingOptions 인스턴스
         */
        fun programming(): FormattingOptions = FormattingOptions(
            style = FormattingStyle.PROGRAMMING,
            decimalPlaces = 6,
            addSpaces = true,
            showIntegerAsDecimal = false,
            removeTrailingZeros = true,
            useParentheses = true,
            showOperatorSpacing = true,
            preferUnicodeSymbols = false,
            compactFunctionCalls = false,
            showFullPrecision = false
        )

        /**
         * LaTeX 스타일 옵션을 반환합니다.
         *
         * @return LaTeX 스타일 FormattingOptions 인스턴스
         */
        fun latex(): FormattingOptions = FormattingOptions(
            style = FormattingStyle.LATEX,
            decimalPlaces = 6,
            addSpaces = true,
            showIntegerAsDecimal = false,
            removeTrailingZeros = true,
            useParentheses = false,
            showOperatorSpacing = true,
            preferUnicodeSymbols = false,
            compactFunctionCalls = false,
            showFullPrecision = false
        )

        /**
         * 간결한 스타일 옵션을 반환합니다.
         *
         * @return 간결한 스타일 FormattingOptions 인스턴스
         */
        fun compact(): FormattingOptions = FormattingOptions(
            style = FormattingStyle.COMPACT,
            decimalPlaces = 3,
            addSpaces = false,
            showIntegerAsDecimal = false,
            removeTrailingZeros = true,
            useParentheses = false,
            showOperatorSpacing = false,
            preferUnicodeSymbols = false,
            compactFunctionCalls = true,
            showFullPrecision = false
        )

        /**
         * 상세한 스타일 옵션을 반환합니다.
         *
         * @return 상세한 스타일 FormattingOptions 인스턴스
         */
        fun verbose(): FormattingOptions = FormattingOptions(
            style = FormattingStyle.VERBOSE,
            decimalPlaces = 8,
            addSpaces = true,
            showIntegerAsDecimal = true,
            removeTrailingZeros = false,
            useParentheses = true,
            showOperatorSpacing = true,
            preferUnicodeSymbols = false,
            compactFunctionCalls = false,
            showFullPrecision = true
        )

        /**
         * 고정밀도 옵션을 반환합니다.
         *
         * @return 고정밀도 FormattingOptions 인스턴스
         */
        fun highPrecision(): FormattingOptions = default().copy(
            decimalPlaces = 15,
            showFullPrecision = true,
            removeTrailingZeros = false
        )

        /**
         * 저정밀도 옵션을 반환합니다.
         *
         * @return 저정밀도 FormattingOptions 인스턴스
         */
        fun lowPrecision(): FormattingOptions = default().copy(
            decimalPlaces = 2,
            showFullPrecision = false,
            removeTrailingZeros = true
        )

        /**
         * 특정 스타일의 기본 옵션을 반환합니다.
         *
         * @param style 스타일
         * @return 해당 스타일의 기본 FormattingOptions 인스턴스
         */
        fun forStyle(style: FormattingStyle): FormattingOptions = when (style) {
            FormattingStyle.MATHEMATICAL -> mathematical()
            FormattingStyle.PROGRAMMING -> programming()
            FormattingStyle.LATEX -> latex()
            FormattingStyle.COMPACT -> compact()
            FormattingStyle.VERBOSE -> verbose()
        }

        /**
         * 사용자 정의 옵션을 생성합니다.
         *
         * @param style 스타일
         * @param decimalPlaces 소수점 자릿수
         * @param addSpaces 공백 추가 여부
         * @param showIntegerAsDecimal 정수를 소수점으로 표시할지 여부
         * @param removeTrailingZeros 뒤쪽 0 제거 여부
         * @return 사용자 정의 FormattingOptions 인스턴스
         */
        fun custom(
            style: FormattingStyle = FormattingStyle.MATHEMATICAL,
            decimalPlaces: Int = 6,
            addSpaces: Boolean = true,
            showIntegerAsDecimal: Boolean = false,
            removeTrailingZeros: Boolean = true
        ): FormattingOptions = FormattingOptions(
            style = style,
            decimalPlaces = decimalPlaces,
            addSpaces = addSpaces,
            showIntegerAsDecimal = showIntegerAsDecimal,
            removeTrailingZeros = removeTrailingZeros,
            useParentheses = when(style) {
                FormattingStyle.PROGRAMMING, FormattingStyle.VERBOSE -> true
                else -> false
            },
            showOperatorSpacing = style != FormattingStyle.COMPACT,
            preferUnicodeSymbols = style == FormattingStyle.MATHEMATICAL,
            compactFunctionCalls = style == FormattingStyle.COMPACT,
            showFullPrecision = style == FormattingStyle.VERBOSE
        )
    }
}