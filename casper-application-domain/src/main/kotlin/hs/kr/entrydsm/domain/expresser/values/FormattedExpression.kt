package hs.kr.entrydsm.domain.expresser.values

import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.entities.FormattingStyle

/**
 * 포맷팅된 수식 표현을 담는 값 객체입니다.
 *
 * 포맷팅 과정을 거친 수식 문자열과 함께 적용된 스타일과 옵션 정보를 포함합니다.
 * 불변 객체로 설계되어 안전한 수식 표현을 보장하며, 다양한 분석 및 변환 메서드를 제공합니다.
 *
 * @property expression 포맷팅된 수식 문자열
 * @property style 적용된 포맷팅 스타일
 * @property options 적용된 포맷팅 옵션
 * @property length 수식 문자열의 길이
 * @property createdAt 생성 시각
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
data class FormattedExpression(
    val expression: String,
    val style: FormattingStyle,
    val options: FormattingOptions,
    val length: Int = expression.length,
    val createdAt: Long = System.currentTimeMillis()
) {
    
    init {
        if (expression.isBlank()) {
            throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.formattingError(
                "expression", "포맷팅된 수식은 공백이 될 수 없습니다"
            )
        }
        if (length < 0) {
            throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.invalidFormatOption(
                "length=$length", "수식 길이는 0 이상이어야 합니다"
            )
        }
    }

    /**
     * 수식이 비어있는지 확인합니다.
     *
     * @return 수식이 비어있으면 true, 아니면 false
     */
    fun isEmpty(): Boolean = expression.isBlank()

    /**
     * 수식이 비어있지 않은지 확인합니다.
     *
     * @return 수식이 비어있지 않으면 true, 아니면 false
     */
    fun isNotEmpty(): Boolean = !isEmpty()

    /**
     * 수식이 단일 토큰인지 확인합니다.
     *
     * @return 단일 토큰이면 true, 아니면 false
     */
    fun isSingleToken(): Boolean = !expression.contains(" ") && !expression.contains("(") && !expression.contains(")")

    /**
     * 수식이 복합 표현식인지 확인합니다.
     *
     * @return 복합 표현식이면 true, 아니면 false
     */
    fun isComplex(): Boolean = expression.contains("(") || expression.contains("+") || expression.contains("-") || 
                              expression.contains("*") || expression.contains("/") || expression.contains("^")

    /**
     * 수식에 특정 연산자가 포함되어 있는지 확인합니다.
     *
     * @param operator 확인할 연산자
     * @return 연산자가 포함되어 있으면 true, 아니면 false
     */
    fun containsOperator(operator: String): Boolean = expression.contains(operator)

    /**
     * 수식에 함수 호출이 포함되어 있는지 확인합니다.
     *
     * @return 함수 호출이 포함되어 있으면 true, 아니면 false
     */
    fun containsFunctionCall(): Boolean = expression.contains("(") && expression.contains(")")

    /**
     * 수식에 변수가 포함되어 있는지 확인합니다.
     *
     * @return 변수가 포함되어 있으면 true, 아니면 false
     */
    fun containsVariable(): Boolean = expression.any { it.isLetter() && it != 'e' && it != 'π' }

    /**
     * 수식에 숫자가 포함되어 있는지 확인합니다.
     *
     * @return 숫자가 포함되어 있으면 true, 아니면 false
     */
    fun containsNumber(): Boolean = expression.any { it.isDigit() }

    /**
     * 수식에 특수 문자가 포함되어 있는지 확인합니다.
     *
     * @return 특수 문자가 포함되어 있으면 true, 아니면 false
     */
    fun containsSpecialCharacters(): Boolean = expression.any { 
        it in "×÷≠≤≥∧∨√π∞" || it == '\\' || it == '{' || it == '}'
    }

    /**
     * 수식의 복잡도를 계산합니다.
     *
     * @return 복잡도 점수 (0-100)
     */
    fun calculateComplexity(): Int {
        var complexity = 0
        
        // 길이에 따른 복잡도
        complexity += (length / LENGTH_WEIGHT_DIVISOR).coerceAtMost(MAX_LENGTH_COMPLEXITY)
        
        // 연산자 개수에 따른 복잡도
        complexity += COMPLEXITY_OPERATORS.sumOf { op -> 
            expression.count { it.toString() == op } * OPERATOR_WEIGHT 
        }
        
        // 괄호 개수에 따른 복잡도
        complexity += expression.count { it == '(' } * PARENTHESES_WEIGHT
        
        // 함수 호출 개수에 따한 복잡도
        complexity += expression.count { it.isLetter() && expression.indexOf(it) < expression.indexOf('(') } * FUNCTION_WEIGHT
        
        return complexity.coerceAtMost(MAX_COMPLEXITY_SCORE)
    }

    /**
     * 수식의 가독성을 평가합니다.
     *
     * @return 가독성 점수 (0-100)
     */
    fun calculateReadability(): Int {
        var readability = 100
        
        // 길이에 따른 가독성 감소
        if (length > 50) readability -= (length - 50) / 2
        
        // 중첩 괄호에 따른 가독성 감소
        var maxNesting = 0
        var currentNesting = 0
        expression.forEach { char ->
            when (char) {
                '(' -> currentNesting++
                ')' -> currentNesting--
            }
            maxNesting = maxOf(maxNesting, currentNesting)
        }
        readability -= maxNesting * 5
        
        // 스타일에 따른 가독성 조정
        readability += when(style) {
            FormattingStyle.MATHEMATICAL -> 4
            FormattingStyle.PROGRAMMING -> 3
            FormattingStyle.LATEX -> 2
            FormattingStyle.VERBOSE -> 5
            FormattingStyle.COMPACT -> 1
        } * 5
        
        // 공백 사용에 따른 가독성 향상
        if (options.addSpaces) readability += 10
        
        return readability.coerceIn(0, 100)
    }

    /**
     * 수식을 다른 스타일로 변환합니다.
     *
     * @param newStyle 새로운 스타일
     * @return 새로운 스타일로 변환된 FormattedExpression
     */
    fun convertToStyle(newStyle: FormattingStyle): FormattedExpression {
        val newOptions = options.withStyle(newStyle).adjustForStyle()
        return copy(
            style = newStyle,
            options = newOptions,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * 수식을 압축합니다.
     *
     * @return 압축된 FormattedExpression
     */
    fun compress(): FormattedExpression {
        val compressed = expression
            .replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("≠", "!=")
            .replace("≤", "<=")
            .replace("≥", ">=")
            .replace("∧", "&&")
            .replace("∨", "||")
            .replace("√", "sqrt")
            .replace("π", "pi")
        
        return copy(
            expression = compressed,
            style = FormattingStyle.COMPACT,
            options = FormattingOptions.compact(),
            length = compressed.length,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * 수식을 확장합니다.
     *
     * @return 확장된 FormattedExpression
     */
    fun expand(): FormattedExpression {
        val expanded = expression
            .replace("*", " × ")
            .replace("/", " ÷ ")
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("==", " = ")
            .replace("!=", " ≠ ")
            .replace("<=", " ≤ ")
            .replace(">=", " ≥ ")
            .replace("&&", " ∧ ")
            .replace("||", " ∨ ")
            .replace("sqrt", "√")
            .replace("pi", "π")
        
        return copy(
            expression = expanded,
            style = FormattingStyle.MATHEMATICAL,
            options = FormattingOptions.mathematical(),
            length = expanded.length,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * 수식의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "length" to length,
        "wordCount" to expression.split(" ").size,
        "operatorCount" to expression.count { it in "+-*/^=<>!&|" },
        "parenthesesCount" to expression.count { it == '(' },
        "functionCallCount" to expression.count { it.isLetter() && expression.indexOf(it) < expression.indexOf('(') },
        "variableCount" to expression.count { it.isLetter() && it != 'e' && it != 'π' },
        "numberCount" to expression.count { it.isDigit() },
        "specialCharCount" to expression.count { it in "×÷≠≤≥∧∨√π∞" },
        "complexity" to calculateComplexity(),
        "readability" to calculateReadability(),
        "isSingleToken" to isSingleToken(),
        "isComplex" to isComplex(),
        "containsFunctionCall" to containsFunctionCall(),
        "containsVariable" to containsVariable(),
        "containsNumber" to containsNumber(),
        "containsSpecialCharacters" to containsSpecialCharacters(),
        "style" to style.name,
        "createdAt" to createdAt
    )

    /**
     * 수식을 JSON 형태로 표현합니다.
     *
     * @return JSON 형태의 수식 표현
     */
    fun toJson(): String = buildString {
        append("{")
        append("\"expression\": \"${expression.replace("\"", "\\\"")}\",")
        append("\"style\": \"${style.name}\",")
        append("\"length\": $length,")
        append("\"complexity\": ${calculateComplexity()},")
        append("\"readability\": ${calculateReadability()},")
        append("\"createdAt\": $createdAt")
        append("}")
    }

    /**
     * 수식을 XML 형태로 표현합니다.
     *
     * @return XML 형태의 수식 표현
     */
    fun toXml(): String = buildString {
        appendLine("<formatted-expression>")
        appendLine("  <expression><![CDATA[$expression]]></expression>")
        appendLine("  <style>${style.name}</style>")
        appendLine("  <length>$length</length>")
        appendLine("  <complexity>${calculateComplexity()}</complexity>")
        appendLine("  <readability>${calculateReadability()}</readability>")
        appendLine("  <created-at>$createdAt</created-at>")
        appendLine("</formatted-expression>")
    }

    /**
     * 수식의 해시값을 계산합니다.
     *
     * @return 해시값
     */
    fun calculateHash(): String = (expression + style.name + options.toString()).hashCode().toString(16)

    /**
     * 다른 FormattedExpression과 의미적으로 동일한지 확인합니다.
     *
     * @param other 비교할 FormattedExpression
     * @return 의미적으로 동일하면 true, 아니면 false
     */
    fun isSemanticallyEqual(other: FormattedExpression): Boolean {
        val thisNormalized = this.compress().expression
        val otherNormalized = other.compress().expression
        return thisNormalized == otherNormalized
    }

    /**
     * 수식의 예상 출력 너비를 계산합니다.
     *
     * @return 예상 출력 너비 (문자 수)
     */
    fun calculateDisplayWidth(): Int {
        var width = 0
        expression.forEach { char ->
            width += when (char) {
                in "×÷≠≤≥∧∨√π∞" -> 2 // 특수 문자는 2배 폭
                else -> 1
            }
        }
        return width
    }

    /**
     * 수식을 사람이 읽기 쉬운 형태로 변환합니다.
     *
     * @return 읽기 쉬운 형태의 문자열
     */
    override fun toString(): String = expression

    /**
     * 수식의 상세 정보를 포함한 문자열을 반환합니다.
     *
     * @return 상세 정보가 포함된 문자열
     */
    fun toDetailString(): String = buildString {
        append("FormattedExpression(")
        append("expression=\"$expression\", ")
        append("style=${style.name}, ")
        append("length=$length, ")
        append("complexity=${calculateComplexity()}, ")
        append("readability=${calculateReadability()}")
        append(")")
    }

    companion object {
        /**
         * 복잡도 계산에 사용되는 연산자 목록
         */
        private val COMPLEXITY_OPERATORS = listOf(
            "+", "-", "*", "/", "^", "==", "!=", "<", ">", "<=", ">=", "&&", "||"
        )
        
        /**
         * 복잡도 계산 가중치
         */
        private const val LENGTH_WEIGHT_DIVISOR = 10
        private const val MAX_LENGTH_COMPLEXITY = 20
        private const val OPERATOR_WEIGHT = 5
        private const val PARENTHESES_WEIGHT = 3
        private const val FUNCTION_WEIGHT = 8
        private const val MAX_COMPLEXITY_SCORE = 100
        
        /**
         * 빈 표현식을 생성합니다.
         *
         * @return 빈 FormattedExpression
         */
        fun empty(): FormattedExpression = FormattedExpression(
            expression = " ",
            style = FormattingStyle.MATHEMATICAL,
            options = FormattingOptions.default()
        )

        /**
         * 간단한 표현식을 생성합니다.
         *
         * @param expression 표현식 문자열
         * @return FormattedExpression
         */
        fun simple(expression: String): FormattedExpression = FormattedExpression(
            expression = expression,
            style = FormattingStyle.MATHEMATICAL,
            options = FormattingOptions.default()
        )

        /**
         * 여러 표현식을 결합합니다.
         *
         * @param expressions 결합할 표현식들
         * @param separator 구분자
         * @return 결합된 FormattedExpression
         */
        fun combine(expressions: List<FormattedExpression>, separator: String = ", "): FormattedExpression {
            if (expressions.isEmpty()) {
                throw hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException.formattingError(
                    "combine", "결합할 표현식이 없습니다"
                )
            }
            
            val combined = expressions.joinToString(separator) { it.expression }
            val firstStyle = expressions.first().style
            val firstOptions = expressions.first().options
            
            return FormattedExpression(
                expression = combined,
                style = firstStyle,
                options = firstOptions
            )
        }
    }
}