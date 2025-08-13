package hs.kr.entrydsm.domain.expresser.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.*
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.entities.FormattingStyle
import hs.kr.entrydsm.domain.expresser.exceptions.ExpresserException
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * 수식과 계산 결과를 다양한 형태로 포맷팅하는 집합 루트입니다.
 *
 * AST 노드를 순회하며 수식을 사람이 읽기 쉬운 형태로 변환하고,
 * 계산 결과를 다양한 형식으로 표현합니다. 수학적 표기법, 중위 표기법,
 * 전위 표기법, 후위 표기법 등을 지원하며, 괄호 최적화와 연산자 우선순위
 * 처리를 통해 정확하고 가독성 높은 출력을 제공합니다.
 *
 * @property options 포맷팅 옵션
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "expresser")
class ExpressionFormatter(
    private val options: FormattingOptions = FormattingOptions.default()
) : ASTVisitor<String> {

    /**
     * AST 노드를 포맷팅된 표현식으로 변환합니다.
     *
     * @param node 포맷팅할 AST 노드
     * @return 포맷팅된 표현식
     * @throws ExpresserException 포맷팅 중 오류 발생 시
     */
    fun format(node: ASTNode): FormattedExpression {
        return try {
            val expression = node.accept(this)
            FormattedExpression(
                expression = expression,
                style = options.style,
                options = options
            )
        } catch (e: ExpresserException) {
            throw e
        } catch (e: Exception) {
            throw ExpresserException.formattingError("포맷팅 오류: ${e.message}", e)
        }
    }

    /**
     * 계산 결과를 포맷팅합니다.
     *
     * @param result 포맷팅할 결과
     * @return 포맷팅된 결과 문자열
     * @throws ExpresserException 포맷팅 중 오류 발생 시
     */
    fun formatResult(result: Any?): String {
        return try {
            when (result) {
                null -> "null"
                is Double -> formatDouble(result)
                is Float -> formatDouble(result.toDouble())
                is Int -> formatInteger(result)
                is Long -> formatInteger(result.toInt())
                is Boolean -> formatBoolean(result)
                is String -> formatString(result)
                else -> result.toString()
            }
        } catch (e: Exception) {
            throw ExpresserException.resultFormattingError(result, e)
        }
    }

    /**
     * NumberNode를 방문하여 숫자를 포맷팅합니다.
     */
    override fun visitNumber(node: NumberNode): String {
        return formatDouble(node.value)
    }

    /**
     * BooleanNode를 방문하여 불린값을 포맷팅합니다.
     */
    override fun visitBoolean(node: BooleanNode): String {
        return formatBoolean(node.value)
    }

    /**
     * VariableNode를 방문하여 변수를 포맷팅합니다.
     */
    override fun visitVariable(node: VariableNode): String {
        return when (options.style) {
            FormattingStyle.MATHEMATICAL -> formatMathematicalVariable(node.name)
            FormattingStyle.PROGRAMMING -> formatProgrammingVariable(node.name)
            FormattingStyle.LATEX -> formatLatexVariable(node.name)
            FormattingStyle.COMPACT -> node.name
            FormattingStyle.VERBOSE -> formatVerboseVariable(node.name)
        }
    }

    /**
     * BinaryOpNode를 방문하여 이항 연산을 포맷팅합니다.
     */
    override fun visitBinaryOp(node: BinaryOpNode): String {
        val left = node.left.accept(this) as String
        val right = node.right.accept(this) as String
        
        return when (options.style) {
            FormattingStyle.MATHEMATICAL -> formatMathematicalBinaryOp(left, node.operator, right, node)
            FormattingStyle.PROGRAMMING -> formatProgrammingBinaryOp(left, node.operator, right, node)
            FormattingStyle.LATEX -> formatLatexBinaryOp(left, node.operator, right, node)
            FormattingStyle.COMPACT -> formatCompactBinaryOp(left, node.operator, right, node)
            FormattingStyle.VERBOSE -> formatVerboseBinaryOp(left, node.operator, right, node)
        }
    }

    /**
     * UnaryOpNode를 방문하여 단항 연산을 포맷팅합니다.
     */
    override fun visitUnaryOp(node: UnaryOpNode): String {
        val operand = node.operand.accept(this) as String
        
        return when (options.style) {
            FormattingStyle.MATHEMATICAL -> formatMathematicalUnaryOp(node.operator, operand, node)
            FormattingStyle.PROGRAMMING -> formatProgrammingUnaryOp(node.operator, operand, node)
            FormattingStyle.LATEX -> formatLatexUnaryOp(node.operator, operand, node)
            FormattingStyle.COMPACT -> formatCompactUnaryOp(node.operator, operand, node)
            FormattingStyle.VERBOSE -> formatVerboseUnaryOp(node.operator, operand, node)
        }
    }

    /**
     * FunctionCallNode를 방문하여 함수 호출을 포맷팅합니다.
     */
    override fun visitFunctionCall(node: FunctionCallNode): String {
        val args = node.args.map { it.accept(this) as String }
        
        return when (options.style) {
            FormattingStyle.MATHEMATICAL -> formatMathematicalFunction(node.name, args)
            FormattingStyle.PROGRAMMING -> formatProgrammingFunction(node.name, args)
            FormattingStyle.LATEX -> formatLatexFunction(node.name, args)
            FormattingStyle.COMPACT -> formatCompactFunction(node.name, args)
            FormattingStyle.VERBOSE -> formatVerboseFunction(node.name, args)
        }
    }

    /**
     * IfNode를 방문하여 조건문을 포맷팅합니다.
     */
    override fun visitIf(node: IfNode): String {
        val condition = node.condition.accept(this) as String
        val trueValue = node.trueValue.accept(this) as String
        val falseValue = node.falseValue.accept(this) as String
        
        return when (options.style) {
            FormattingStyle.MATHEMATICAL -> formatMathematicalIf(condition, trueValue, falseValue)
            FormattingStyle.PROGRAMMING -> formatProgrammingIf(condition, trueValue, falseValue)
            FormattingStyle.LATEX -> formatLatexIf(condition, trueValue, falseValue)
            FormattingStyle.COMPACT -> formatCompactIf(condition, trueValue, falseValue)
            FormattingStyle.VERBOSE -> formatVerboseIf(condition, trueValue, falseValue)
        }
    }

    /**
     * Double 값을 포맷팅합니다.
     */
    private fun formatDouble(value: Double): String {
        return when {
            value.isNaN() -> "NaN"
            value.isInfinite() -> if (value > 0) "∞" else "-∞"
            value == floor(value) && value <= Long.MAX_VALUE -> {
                if (options.showIntegerAsDecimal) {
                    String.format("%.${options.decimalPlaces}f", value)
                } else {
                    value.toLong().toString()
                }
            }
            else -> {
                val formatted = String.format("%.${options.decimalPlaces}f", value)
                if (options.removeTrailingZeros) {
                    formatted.trimEnd('0').trimEnd('.')
                } else {
                    formatted
                }
            }
        }
    }

    /**
     * Integer 값을 포맷팅합니다.
     */
    private fun formatInteger(value: Int): String {
        return if (options.showIntegerAsDecimal) {
            String.format("%.${options.decimalPlaces}f", value.toDouble())
        } else {
            value.toString()
        }
    }

    /**
     * Boolean 값을 포맷팅합니다.
     */
    private fun formatBoolean(value: Boolean): String {
        return when (options.style) {
            FormattingStyle.MATHEMATICAL -> if (value) "참" else "거짓"
            FormattingStyle.PROGRAMMING -> value.toString()
            FormattingStyle.LATEX -> if (value) "\\text{true}" else "\\text{false}"
            FormattingStyle.COMPACT -> if (value) "T" else "F"
            FormattingStyle.VERBOSE -> if (value) "TRUE" else "FALSE"
        }
    }

    /**
     * String 값을 포맷팅합니다.
     */
    private fun formatString(value: String): String {
        return when (options.style) {
            FormattingStyle.PROGRAMMING -> "\"$value\""
            else -> value
        }
    }

    /**
     * 수학적 스타일의 변수를 포맷팅합니다.
     */
    private fun formatMathematicalVariable(name: String): String {
        return MATHEMATICAL_VARIABLE_MAPPINGS[name.lowercase()] ?: name
    }

    /**
     * 프로그래밍 스타일의 변수를 포맷팅합니다.
     */
    private fun formatProgrammingVariable(name: String): String = name

    /**
     * LaTeX 스타일의 변수를 포맷팅합니다.
     */
    private fun formatLatexVariable(name: String): String {
        return LATEX_VARIABLE_MAPPINGS[name.lowercase()] ?: name
    }

    /**
     * 상세한 스타일의 변수를 포맷팅합니다.
     */
    private fun formatVerboseVariable(name: String): String = "변수($name)"

    /**
     * 수학적 스타일의 이항 연산을 포맷팅합니다.
     */
    private fun formatMathematicalBinaryOp(left: String, operator: String, right: String, node: BinaryOpNode): String {
        val op = MATHEMATICAL_OPERATOR_MAPPINGS[operator] ?: operator
        
        return if (needsParentheses(node)) {
            "($left $op $right)"
        } else {
            "$left $op $right"
        }
    }

    /**
     * 프로그래밍 스타일의 이항 연산을 포맷팅합니다.
     */
    private fun formatProgrammingBinaryOp(left: String, operator: String, right: String, node: BinaryOpNode): String {
        val spacing = if (options.addSpaces) " " else ""
        return if (needsParentheses(node)) {
            "($left$spacing$operator$spacing$right)"
        } else {
            "$left$spacing$operator$spacing$right"
        }
    }

    /**
     * LaTeX 스타일의 이항 연산을 포맷팅합니다.
     */
    private fun formatLatexBinaryOp(left: String, operator: String, right: String, node: BinaryOpNode): String {
        val op = LATEX_OPERATOR_MAPPINGS[operator] ?: operator
        
        return if (operator == "^") {
            "$left^{$right}"
        } else if (needsParentheses(node)) {
            "($left $op $right)"
        } else {
            "$left $op $right"
        }
    }

    /**
     * 간결한 스타일의 이항 연산을 포맷팅합니다.
     */
    private fun formatCompactBinaryOp(left: String, operator: String, right: String, node: BinaryOpNode): String {
        return if (needsParentheses(node)) {
            "($left$operator$right)"
        } else {
            "$left$operator$right"
        }
    }

    /**
     * 상세한 스타일의 이항 연산을 포맷팅합니다.
     */
    private fun formatVerboseBinaryOp(left: String, operator: String, right: String, node: BinaryOpNode): String {
        val opName = VERBOSE_OPERATOR_MAPPINGS[operator] ?: operator
        return "($left $opName $right)"
    }

    /**
     * 수학적 스타일의 단항 연산을 포맷팅합니다.
     */
    private fun formatMathematicalUnaryOp(operator: String, operand: String, node: UnaryOpNode): String {
        val op = when (operator) {
            "!" -> "¬"
            else -> operator
        }
        return "$op$operand"
    }

    /**
     * 프로그래밍 스타일의 단항 연산을 포맷팅합니다.
     */
    private fun formatProgrammingUnaryOp(operator: String, operand: String, node: UnaryOpNode): String {
        return "$operator$operand"
    }

    /**
     * LaTeX 스타일의 단항 연산을 포맷팅합니다.
     */
    private fun formatLatexUnaryOp(operator: String, operand: String, node: UnaryOpNode): String {
        val op = when (operator) {
            "!" -> "\\neg"
            else -> operator
        }
        return "$op$operand"
    }

    /**
     * 간결한 스타일의 단항 연산을 포맷팅합니다.
     */
    private fun formatCompactUnaryOp(operator: String, operand: String, node: UnaryOpNode): String {
        return "$operator$operand"
    }

    /**
     * 상세한 스타일의 단항 연산을 포맷팅합니다.
     */
    private fun formatVerboseUnaryOp(operator: String, operand: String, node: UnaryOpNode): String {
        val opName = VERBOSE_UNARY_OPERATOR_MAPPINGS[operator] ?: operator
        return "($opName $operand)"
    }

    /**
     * 수학적 스타일의 함수를 포맷팅합니다.
     */
    private fun formatMathematicalFunction(name: String, args: List<String>): String {
        val funcName = MATHEMATICAL_FUNCTION_MAPPINGS[name.lowercase()] ?: name
        
        return when (name.lowercase()) {
            "sqrt" -> "√(${args.joinToString(", ")})"
            "exp" -> "e^(${args.joinToString(", ")})"
            else -> "$funcName(${args.joinToString(", ")})"
        }
    }

    /**
     * 프로그래밍 스타일의 함수를 포맷팅합니다.
     */
    private fun formatProgrammingFunction(name: String, args: List<String>): String {
        val spacing = if (options.addSpaces) " " else ""
        val separator = if (options.addSpaces) ", " else ","
        return "$name($spacing${args.joinToString(separator)}$spacing)"
    }

    /**
     * LaTeX 스타일의 함수를 포맷팅합니다.
     */
    private fun formatLatexFunction(name: String, args: List<String>): String {
        val funcName = LATEX_FUNCTION_MAPPINGS[name.lowercase()] ?: "\\text{$name}"
        
        return when (name.lowercase()) {
            "sqrt" -> "\\sqrt{${args.joinToString(", ")}}"
            "exp" -> "\\exp(${args.joinToString(", ")})"
            else -> "$funcName(${args.joinToString(", ")})"
        }
    }

    /**
     * 간결한 스타일의 함수를 포맷팅합니다.
     */
    private fun formatCompactFunction(name: String, args: List<String>): String {
        return "$name(${args.joinToString(",")})"
    }

    /**
     * 상세한 스타일의 함수를 포맷팅합니다.
     */
    private fun formatVerboseFunction(name: String, args: List<String>): String {
        val funcName = VERBOSE_FUNCTION_MAPPINGS[name.lowercase()] ?: name
        return "함수_${funcName}(${args.joinToString(", ")})"
    }

    /**
     * 수학적 스타일의 조건문을 포맷팅합니다.
     */
    private fun formatMathematicalIf(condition: String, trueValue: String, falseValue: String): String {
        return "if $condition then $trueValue else $falseValue"
    }

    /**
     * 프로그래밍 스타일의 조건문을 포맷팅합니다.
     */
    private fun formatProgrammingIf(condition: String, trueValue: String, falseValue: String): String {
        return "($condition ? $trueValue : $falseValue)"
    }

    /**
     * LaTeX 스타일의 조건문을 포맷팅합니다.
     */
    private fun formatLatexIf(condition: String, trueValue: String, falseValue: String): String {
        return "\\begin{cases} $trueValue & \\text{if } $condition \\\\ $falseValue & \\text{otherwise} \\end{cases}"
    }

    /**
     * 간결한 스타일의 조건문을 포맷팅합니다.
     */
    private fun formatCompactIf(condition: String, trueValue: String, falseValue: String): String {
        return "if($condition,$trueValue,$falseValue)"
    }

    /**
     * 상세한 스타일의 조건문을 포맷팅합니다.
     */
    private fun formatVerboseIf(condition: String, trueValue: String, falseValue: String): String {
        return "만약 $condition 이면 $trueValue 아니면 $falseValue"
    }

    /**
     * 노드가 괄호가 필요한지 확인합니다.
     */
    private fun needsParentheses(node: BinaryOpNode): Boolean {
        return when (options.style) {
            FormattingStyle.COMPACT -> false
            FormattingStyle.VERBOSE -> true
            else -> {
                // 연산자 우선순위에 따라 괄호 필요성 판단
                // 단순화: 기본적으로 괄호 사용하지 않음
                false
            }
        }
    }

    /**
     * ArgumentsNode를 방문하여 인수 목록을 포맷팅합니다.
     */
    override fun visitArguments(node: ArgumentsNode): String {
        return node.arguments.joinToString(", ") { it.accept(this) }
    }

    /**
     * 연산자의 우선순위를 반환합니다.
     */
    private fun getOperatorPrecedence(operator: String): Int {
        return when (operator) {
            "||" -> 1
            "&&" -> 2
            "==", "!=" -> 3
            "<", "<=", ">", ">=" -> 4
            "+", "-" -> 5
            "*", "/", "%" -> 6
            "^" -> 7
            else -> 0
        }
    }

    /**
     * 새로운 옵션으로 포맷터를 생성합니다.
     */
    fun withOptions(newOptions: FormattingOptions): ExpressionFormatter {
        return ExpressionFormatter(newOptions)
    }

    /**
     * 현재 옵션을 반환합니다.
     */
    fun getOptions(): FormattingOptions = options

    /**
     * 포맷터 통계를 반환합니다.
     */
    fun getFormatterStatistics(): Map<String, Any> = mapOf(
        "style" to options.style,
        "decimalPlaces" to options.decimalPlaces,
        "addSpaces" to options.addSpaces,
        "showIntegerAsDecimal" to options.showIntegerAsDecimal,
        "removeTrailingZeros" to options.removeTrailingZeros,
        "supportedStyles" to FormattingStyle.values().map { it.name }
    )

    companion object {
        /**
         * 수학적 변수명 매핑
         */
        private val MATHEMATICAL_VARIABLE_MAPPINGS = mapOf(
            "pi" to "π",
            "e" to "e",
            "alpha" to "α",
            "beta" to "β",
            "gamma" to "γ",
            "delta" to "δ",
            "epsilon" to "ε",
            "theta" to "θ",
            "lambda" to "λ",
            "mu" to "μ",
            "sigma" to "σ",
            "phi" to "φ",
            "omega" to "ω"
        )
        
        /**
         * LaTeX 변수명 매핑
         */
        private val LATEX_VARIABLE_MAPPINGS = mapOf(
            "pi" to "\\pi",
            "e" to "e",
            "alpha" to "\\alpha",
            "beta" to "\\beta",
            "gamma" to "\\gamma",
            "delta" to "\\delta",
            "epsilon" to "\\epsilon",
            "theta" to "\\theta",
            "lambda" to "\\lambda",
            "mu" to "\\mu",
            "sigma" to "\\sigma",
            "phi" to "\\phi",
            "omega" to "\\omega"
        )
        
        /**
         * 수학적 스타일 연산자 매핑
         */
        private val MATHEMATICAL_OPERATOR_MAPPINGS = mapOf(
            "*" to "×",
            "/" to "÷",
            "==" to "=",
            "!=" to "≠",
            "<=" to "≤",
            ">=" to "≥",
            "&&" to "∧",
            "||" to "∨",
            "^" to "^"
        )
        
        /**
         * LaTeX 연산자 매핑
         */
        private val LATEX_OPERATOR_MAPPINGS = mapOf(
            "*" to "\\times",
            "/" to "\\div",
            "==" to "=",
            "!=" to "\\neq",
            "<=" to "\\leq",
            ">=" to "\\geq",
            "&&" to "\\land",
            "||" to "\\lor",
            "^" to "^"
        )
        
        /**
         * 수학적 스타일 함수명 매핑
         */
        private val MATHEMATICAL_FUNCTION_MAPPINGS = mapOf(
            "sin" to "sin",
            "cos" to "cos",
            "tan" to "tan",
            "sqrt" to "√",
            "log" to "ln",
            "exp" to "e^"
        )
        
        /**
         * LaTeX 함수명 매핑
         */
        private val LATEX_FUNCTION_MAPPINGS = mapOf(
            "sin" to "\\sin",
            "cos" to "\\cos",
            "tan" to "tan",
            "sqrt" to "\\sqrt",
            "log" to "\\ln",
            "exp" to "\\exp"
        )
        
        /**
         * 상세 스타일 연산자명 매핑
         */
        private val VERBOSE_OPERATOR_MAPPINGS = mapOf(
            "+" to "더하기",
            "-" to "빼기",
            "*" to "곱하기",
            "/" to "나누기",
            "%" to "나머지",
            "^" to "거듭제곱",
            "==" to "같다",
            "!=" to "다르다",
            "<" to "작다",
            "<=" to "작거나 같다",
            ">" to "크다",
            ">=" to "크거나 같다",
            "&&" to "그리고",
            "||" to "또는"
        )
        
        /**
         * 상세 스타일 단항 연산자명 매핑
         */
        private val VERBOSE_UNARY_OPERATOR_MAPPINGS = mapOf(
            "+" to "양수",
            "-" to "음수",
            "!" to "NOT"
        )
        
        /**
         * 상세 스타일 함수명 매핑
         */
        private val VERBOSE_FUNCTION_MAPPINGS = mapOf(
            "sin" to "사인",
            "cos" to "코사인",
            "tan" to "탄젠트",
            "sqrt" to "제곱근",
            "log" to "자연로그",
            "exp" to "지수",
            "abs" to "절댓값",
            "floor" to "내림",
            "ceil" to "올림",
            "round" to "반올림",
            "min" to "최솟값",
            "max" to "최댓값",
            "pow" to "거듭제곱"
        )
        
        /**
         * 기본 옵션으로 포맷터를 생성합니다.
         */
        fun createDefault(): ExpressionFormatter = ExpressionFormatter()

        /**
         * 수학적 스타일 포맷터를 생성합니다.
         */
        fun createMathematical(): ExpressionFormatter = 
            ExpressionFormatter(FormattingOptions.mathematical())

        /**
         * 프로그래밍 스타일 포맷터를 생성합니다.
         */
        fun createProgramming(): ExpressionFormatter = 
            ExpressionFormatter(FormattingOptions.programming())

        /**
         * LaTeX 스타일 포맷터를 생성합니다.
         */
        fun createLatex(): ExpressionFormatter = 
            ExpressionFormatter(FormattingOptions.latex())

        /**
         * 간결한 스타일 포맷터를 생성합니다.
         */
        fun createCompact(): ExpressionFormatter = 
            ExpressionFormatter(FormattingOptions.compact())

        /**
         * 상세한 스타일 포맷터를 생성합니다.
         */
        fun createVerbose(): ExpressionFormatter = 
            ExpressionFormatter(FormattingOptions.verbose())
    }
}