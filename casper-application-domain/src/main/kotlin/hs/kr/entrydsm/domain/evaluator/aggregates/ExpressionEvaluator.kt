package hs.kr.entrydsm.domain.evaluator.aggregates

// import hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException
import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.ArgumentsNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import hs.kr.entrydsm.domain.evaluator.registries.FunctionRegistry
import hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.atanh
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.comparisons.minOf
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.math.truncate

/**
 * AST(추상 구문 트리)를 순회하며 수식을 평가하는 집합 루트입니다.
 *
 * Visitor 패턴을 사용하여 각 AST 노드 타입에 대한 평가 로직을 구현하며,
 * 변수 바인딩, 함수 호출, 연산자 처리 등의 모든 평가 기능을 제공합니다.
 * POC 코드의 모든 평가 로직을 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @property variables 변수 바인딩 맵
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "evaluator")
class ExpressionEvaluator(
    private val variables: Map<String, Any> = emptyMap(),
    private val functionRegistry: FunctionRegistry = FunctionRegistry.createDefault()
) : ASTVisitor<Any?> {

    /**
     * 주어진 AST 노드를 평가합니다.
     *
     * @param node 평가할 AST 노드
     * @return 평가 결과
     * @throws EvaluatorException 평가 중 오류 발생 시
     */
    fun evaluate(node: ASTNode): Any? {
        return try {
            node.accept(this)
        } catch (e: EvaluatorException) {
            throw e
        } catch (e: Exception) {
            throw EvaluatorException.evaluationFailed(e)
        }
    }

    /**
     * NumberNode를 방문하여 숫자 값을 반환합니다.
     */
    override fun visitNumber(node: NumberNode): Any? = node.value

    /**
     * BooleanNode를 방문하여 불린 값을 반환합니다.
     */
    override fun visitBoolean(node: BooleanNode): Any? = node.value

    /**
     * VariableNode를 방문하여 변수 값을 반환합니다.
     *
     * @param node 방문할 VariableNode
     * @return 변수 값
     * @throws EvaluatorException 변수가 정의되지 않은 경우
     */
    override fun visitVariable(node: VariableNode): Any? {
        return variables[node.name] ?: throw EvaluatorException.undefinedVariable(node.name)
    }

    /**
     * BinaryOpNode를 방문하여 이항 연산을 수행합니다.
     *
     * @param node 방문할 BinaryOpNode
     * @return 연산 결과
     * @throws EvaluatorException 연산 평가 실패 시
     */
    override fun visitBinaryOp(node: BinaryOpNode): Any? {
        return try {
            val left = evaluate(node.left)
            val right = evaluate(node.right)

            when (node.operator) {
                // 산술 연산자
                PLUS -> performArithmeticOp(left, right) { a, b -> a + b }
                MINUS -> performArithmeticOp(left, right) { a, b -> a - b }
                MULTIPLY -> performArithmeticOp(left, right) { a, b -> a * b }
                DIVIDE -> performDivisionOp(left, right)
                MODULO -> performArithmeticOp(left, right) { a, b -> a % b }
                POWER -> performArithmeticOp(left, right) { a, b -> a.pow(b) }

                // 비교 연산자
                EQUALS -> performComparisonOp(left, right) { a, b -> a == b }
                NOT_EQUALS -> performComparisonOp(left, right) { a, b -> a != b }
                LESS_THAN -> performComparisonOp(left, right) { a, b -> a < b }
                LESS_THAN_OR_EQUAL -> performComparisonOp(left, right) { a, b -> a <= b }
                GREATER_THAN -> performComparisonOp(left, right) { a, b -> a > b }
                GREATER_THAN_OR_EQUAL -> performComparisonOp(left, right) { a, b -> a >= b }

                // 논리 연산자
                AND -> performLogicalAnd(left, right)
                OR -> performLogicalOr(left, right)

                else -> throw EvaluatorException.unsupportedOperator(node.operator)
            }
        } catch (e: EvaluatorException) {
            throw e
        } catch (e: Exception) {
            throw EvaluatorException.operatorEvaluationFailed(node.operator, e)
        }
    }

    /**
     * UnaryOpNode를 방문하여 단항 연산을 수행합니다.
     *
     * @param node 방문할 UnaryOpNode
     * @return 연산 결과
     * @throws EvaluatorException 연산 평가 실패 시
     */
    override fun visitUnaryOp(node: UnaryOpNode): Any? {
        return try {
            val operand = evaluate(node.operand)

            when (node.operator) {
                MINUS -> when (operand) {
                    is Double -> -operand
                    is Int -> -operand.toDouble()
                    else -> throw EvaluatorException.unsupportedType(operand?.javaClass?.simpleName ?: "null", operand)
                }
                PLUS -> when (operand) {
                    is Double -> operand
                    is Int -> operand.toDouble()
                    else -> throw EvaluatorException.unsupportedType(operand?.javaClass?.simpleName ?: "null", operand)
                }
                NOT -> when (operand) {
                    is Boolean -> !operand
                    is Double -> operand == 0.0
                    is Int -> operand == 0
                    else -> throw EvaluatorException.unsupportedType(operand?.javaClass?.simpleName ?: "null", operand)
                }
                else -> throw EvaluatorException.unsupportedOperator(node.operator)
            }
        } catch (e: EvaluatorException) {
            throw e
        } catch (e: Exception) {
            throw EvaluatorException.operatorEvaluationFailed(node.operator, e)
        }
    }

    /**
     * FunctionCallNode를 방문하여 함수 호출을 처리합니다.
     * 함수 레지스트리를 통해 모듈화된 함수 평가기를 사용합니다.
     *
     * @param node 방문할 FunctionCallNode
     * @return 함수 실행 결과
     * @throws EvaluatorException 함수 실행 실패 시
     */
    override fun visitFunctionCall(node: FunctionCallNode): Any? {
        return try {
            val args = node.args.map { evaluate(it) }

            // 레지스트리에서 함수 평가기 조회
            val evaluator = functionRegistry.get(node.name)
            if (evaluator != null) {
                return evaluator.evaluate(args)
            }

            // 레지스트리에 없는 특수 함수들 처리 (상수, 복잡한 함수들)
            when (node.name.uppercase()) {
                PI_CONST -> {
                    validateArgumentCount(node.name, args, 0)
                    PI
                }
                E_CONST -> {
                    validateArgumentCount(node.name, args, 0)
                    E
                }
                ASIN -> {
                    validateArgumentCount(node.name, args, 1)
                    val value = toDouble(args[0])
                    if (value < -1 || value > 1) throw EvaluatorException.mathError("ASIN domain error")
                    asin(value)
                }
                ACOS -> {
                    validateArgumentCount(node.name, args, 1)
                    val value = toDouble(args[0])
                    if (value < -1 || value > 1) throw EvaluatorException.mathError("ACOS domain error")
                    acos(value)
                }
                ATAN -> {
                    validateArgumentCount(node.name, args, 1)
                    atan(toDouble(args[0]))
                }
                ATAN2 -> {
                    validateArgumentCount(node.name, args, 2)
                    atan2(toDouble(args[0]), toDouble(args[1]))
                }
                SINH -> {
                    validateArgumentCount(node.name, args, 1)
                    sinh(toDouble(args[0]))
                }
                COSH -> {
                    validateArgumentCount(node.name, args, 1)
                    cosh(toDouble(args[0]))
                }
                TANH -> {
                    validateArgumentCount(node.name, args, 1)
                    tanh(toDouble(args[0]))
                }
                ASINH -> {
                    validateArgumentCount(node.name, args, 1)
                    asinh(toDouble(args[0]))
                }
                ACOSH -> {
                    validateArgumentCount(node.name, args, 1)
                    val value = toDouble(args[0])
                    if (value < 1) throw EvaluatorException.mathError("ACOSH domain error")
                    acosh(value)
                }
                ATANH -> {
                    validateArgumentCount(node.name, args, 1)
                    val value = toDouble(args[0])
                    if (value <= -1 || value >= 1) throw EvaluatorException.mathError("ATANH domain error")
                    atanh(value)
                }
                FLOOR -> {
                    validateArgumentCount(node.name, args, 1)
                    floor(toDouble(args[0]))
                }
                CEIL, CEILING -> {
                    validateArgumentCount(node.name, args, 1)
                    ceil(toDouble(args[0]))
                }
                TRUNCATE, TRUNC -> {
                    validateArgumentCount(node.name, args, 1)
                    truncate(toDouble(args[0]))
                }
                SIGN -> {
                    validateArgumentCount(node.name, args, 1)
                    sign(toDouble(args[0]))
                }
                RANDOM, RAND -> {
                    validateArgumentCount(node.name, args, 0)
                    kotlin.random.Random.nextDouble()
                }
                RADIANS -> {
                    validateArgumentCount(node.name, args, 1)
                    toDouble(args[0]) * PI / 180.0
                }
                DEGREES -> {
                    validateArgumentCount(node.name, args, 1)
                    toDouble(args[0]) * 180.0 / PI
                }
                MOD -> {
                    validateArgumentCount(node.name, args, 2)
                    val dividend = toDouble(args[0])
                    val divisor = toDouble(args[1])
                    if (divisor == 0.0) throw EvaluatorException.divisionByZero("%")
                    dividend % divisor
                }
                GCD -> {
                    validateArgumentCount(node.name, args, 2)
                    val a = toDouble(args[0]).toLong()
                    val b = toDouble(args[1]).toLong()
                    gcd(a, b).toDouble()
                }
                LCM -> {
                    validateArgumentCount(node.name, args, 2)
                    val a = toDouble(args[0]).toLong()
                    val b = toDouble(args[1]).toLong()
                    lcm(a, b).toDouble()
                }
                FACTORIAL -> {
                    validateArgumentCount(node.name, args, 1)
                    val n = toDouble(args[0]).toInt()
                    if (n < 0) throw EvaluatorException.mathError("FACTORIAL of negative number")
                    factorial(n).toDouble()
                }
                COMBINATION, COMB -> {
                    validateArgumentCount(node.name, args, 2)
                    val n = toDouble(args[0]).toInt()
                    val r = toDouble(args[1]).toInt()
                    if (n < 0 || r < 0 || r > n) throw EvaluatorException.mathError("COMBINATION domain error")
                    combination(n, r).toDouble()
                }
                PERMUTATION, PERM -> {
                    validateArgumentCount(node.name, args, 2)
                    val n = toDouble(args[0]).toInt()
                    val r = toDouble(args[1]).toInt()
                    if (n < 0 || r < 0 || r > n) throw EvaluatorException.mathError("PERMUTATION domain error")
                    permutation(n, r).toDouble()
                }
                else -> throw EvaluatorException.unsupportedFunction(node.name)
            }
        } catch (e: EvaluatorException) {
            throw e
        } catch (e: Exception) {
            throw EvaluatorException.functionExecutionFailed(node.name, e)
        }
    }

    /**
     * IfNode를 방문하여 조건문을 처리합니다.
     */
    override fun visitIf(node: IfNode): Any? {
        val condition = evaluate(node.condition)
        val conditionResult = toBoolean(condition)

        return if (conditionResult) {
            evaluate(node.trueValue)
        } else {
            evaluate(node.falseValue)
        }
    }

    /**
     * 산술 연산을 수행합니다.
     */
    private fun performArithmeticOp(left: Any?, right: Any?, operation: (Double, Double) -> Double): Double {
        val leftNum = toDouble(left)
        val rightNum = toDouble(right)
        return operation(leftNum, rightNum)
    }

    /**
     * 나눗셈 연산을 수행합니다.
     *
     * @param left 왼쪽 피연산자
     * @param right 오른쪽 피연산자
     * @return 나눗셈 결과
     * @throws EvaluatorException 0으로 나누기 또는 타입 변환 실패 시
     */
    private fun performDivisionOp(left: Any?, right: Any?): Double {
        val leftNum = toDouble(left)
        val rightNum = toDouble(right)

        if (rightNum == 0.0) {
            throw EvaluatorException.divisionByZero()
        }

        return leftNum / rightNum
    }

    /**
     * 비교 연산을 수행합니다.
     */
    private fun performComparisonOp(left: Any?, right: Any?, operation: (Double, Double) -> Boolean): Boolean {
        val leftNum = toDouble(left)
        val rightNum = toDouble(right)
        return operation(leftNum, rightNum)
    }

    /**
     * 논리 AND 연산을 수행합니다.
     */
    private fun performLogicalAnd(left: Any?, right: Any?): Boolean {
        val leftBool = toBoolean(left)
        if (!leftBool) return false

        val rightBool = toBoolean(right)
        return rightBool
    }

    /**
     * 논리 OR 연산을 수행합니다.
     */
    private fun performLogicalOr(left: Any?, right: Any?): Boolean {
        val leftBool = toBoolean(left)
        if (leftBool) return true

        val rightBool = toBoolean(right)
        return rightBool
    }

    /**
     * 값을 Double로 변환합니다.
     *
     * @param value 변환할 값
     * @return Double 값
     * @throws EvaluatorException 변환 실패 시
     */
    private fun toDouble(value: Any?): Double {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDoubleOrNull()
                ?: throw EvaluatorException.numberConversionError(value)
            else -> throw EvaluatorException.numberConversionError(value)
        }
    }

    /**
     * 값을 Boolean으로 변환합니다.
     */
    private fun toBoolean(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is Double -> value != 0.0
            is Int -> value != 0
            is Float -> value != 0.0f
            is Long -> value != 0L
            is String -> value.isNotEmpty()
            null -> false
            else -> true
        }
    }

    /**
     * 함수 인수 개수를 검증합니다.
     *
     * @param functionName 함수명
     * @param args 인수 목록
     * @param expectedCount 예상 인수 개수
     * @throws EvaluatorException 인수 개수가 맞지 않는 경우
     */
    private fun validateArgumentCount(functionName: String, args: List<Any?>, expectedCount: Int) {
        if (args.size != expectedCount) {
            throw EvaluatorException.wrongArgumentCount(functionName, expectedCount, args.size)
        }
    }

    /**
     * 변수 바인딩을 추가한 새로운 평가기를 생성합니다.
     */
    fun withVariables(newVariables: Map<String, Any>): ExpressionEvaluator {
        return ExpressionEvaluator(variables + newVariables, functionRegistry)
    }

    /**
     * 단일 변수를 추가한 새로운 평가기를 생성합니다.
     */
    fun withVariable(name: String, value: Any): ExpressionEvaluator {
        return ExpressionEvaluator(variables + (name to value), functionRegistry)
    }

    /**
     * 현재 변수 바인딩을 반환합니다.
     */
    fun getVariables(): Map<String, Any> = variables.toMap()

    /**
     * 특정 변수가 바인딩되어 있는지 확인합니다.
     */
    fun hasVariable(name: String): Boolean = name in variables

    /**
     * 바인딩된 변수 개수를 반환합니다.
     */
    fun getVariableCount(): Int = variables.size

    /**
     * 최대공약수를 계산합니다.
     */
    private fun gcd(a: Long, b: Long): Long {
        return if (b == 0L) a else gcd(b, a % b)
    }

    /**
     * 최소공배수를 계산합니다.
     */
    private fun lcm(a: Long, b: Long): Long {
        return abs(a * b) / gcd(a, b)
    }

    /**
     * 팩토리얼을 계산합니다.
     * Long 오버플로우 방지를 위해 안전한 범위로 제한합니다.
     *
     * @param n 팩토리얼을 계산할 수
     * @return 팩토리얼 결과
     * @throws EvaluatorException 음수이거나 너무 큰 수인 경우
     */
    private fun factorial(n: Int): Long {
        if (n < 0) throw EvaluatorException.mathError("FACTORIAL of negative number: $n")
        if (n > MAX_FACTORIAL_INPUT) {
            throw EvaluatorException.mathError("FACTORIAL input too large: $n (max: $MAX_FACTORIAL_INPUT)")
        }
        if (n <= 1) return 1

        var result = 1L
        for (i in 2..n) {
            // 오버플로우 체크
            if (result > Long.MAX_VALUE / i) {
                throw EvaluatorException.mathError("FACTORIAL overflow detected for input: $n")
            }
            result *= i
        }
        return result
    }

    /**
     * 조합을 계산합니다.
     * Long 오버플로우 방지를 위해 안전한 범위로 제한합니다.
     *
     * @param n 전체 개수
     * @param r 선택할 개수
     * @return 조합 결과
     * @throws EvaluatorException 음수이거나 너무 큰 수인 경우
     */
    private fun combination(n: Int, r: Int): Long {
        if (n < 0 || r < 0) throw EvaluatorException.mathError("COMBINATION with negative inputs: n=$n, r=$r")
        if (r > n) return 0
        if (r == 0 || r == n) return 1

        // 입력 크기 검증 - 조합이 팩토리얼보다 작으므로 더 큰 값 허용
        if (n > MAX_COMBINATION_INPUT) {
            throw EvaluatorException.mathError("COMBINATION input too large: n=$n (max: $MAX_COMBINATION_INPUT)")
        }

        val k = minOf(r, n - r)
        var result = 1L

        for (i in 0 until k) {
            val numerator = n - i
            val denominator = i + 1

            // 오버플로우 체크 - 곱셈 전에 검사
            if (result > Long.MAX_VALUE / numerator) {
                throw EvaluatorException.mathError("COMBINATION overflow detected: n=$n, r=$r")
            }

            result = result * numerator / denominator
        }

        return result
    }

    /**
     * 순열을 계산합니다.
     * Long 오버플로우 방지를 위해 안전한 범위로 제한합니다.
     *
     * @param n 전체 개수
     * @param r 선택할 개수
     * @return 순열 결과
     * @throws EvaluatorException 음수이거나 너무 큰 수인 경우
     */
    private fun permutation(n: Int, r: Int): Long {
        if (n < 0 || r < 0) throw EvaluatorException.mathError("PERMUTATION with negative inputs: n=$n, r=$r")
        if (r > n) return 0
        if (r == 0) return 1

        // 입력 크기 검증 - 순열은 팩토리얼과 유사한 성장률
        if (n > MAX_PERMUTATION_INPUT || r > MAX_PERMUTATION_INPUT) {
            throw EvaluatorException.mathError("PERMUTATION input too large: n=$n, r=$r (max: $MAX_PERMUTATION_INPUT)")
        }

        var result = 1L
        for (i in 0 until r) {
            val factor = n - i

            // 오버플로우 체크
            if (result > Long.MAX_VALUE / factor) {
                throw EvaluatorException.mathError("PERMUTATION overflow detected: n=$n, r=$r")
            }

            result *= factor
        }

        return result
    }

    /**
     * 인수 노드를 방문합니다.
     *
     * @param node 방문할 인수 노드
     * @return 평가된 인수 리스트
     */
    override fun visitArguments(node: ArgumentsNode): Any? {
        return node.arguments.map { it.accept(this) }
    }

    companion object {
        // Operators
        private const val PLUS = "+"
        private const val MINUS = "-"
        private const val MULTIPLY = "*"
        private const val DIVIDE = "/"
        private const val MODULO = "%"
        private const val POWER = "^"
        private const val EQUALS = "=="
        private const val NOT_EQUALS = "!="
        private const val LESS_THAN = "<"
        private const val LESS_THAN_OR_EQUAL = "<="
        private const val GREATER_THAN = ">"
        private const val GREATER_THAN_OR_EQUAL = ">="
        private const val AND = "&&"
        private const val OR = "||"
        private const val NOT = "!"

        // Functions
        private const val PI_CONST = "PI"
        private const val E_CONST = "E"
        private const val ASIN = "ASIN"
        private const val ACOS = "ACOS"
        private const val ATAN = "ATAN"
        private const val ATAN2 = "ATAN2"
        private const val SINH = "SINH"
        private const val COSH = "COSH"
        private const val TANH = "TANH"
        private const val ASINH = "ASINH"
        private const val ACOSH = "ACOSH"
        private const val ATANH = "ATANH"
        private const val FLOOR = "FLOOR"
        private const val CEIL = "CEIL"
        private const val CEILING = "CEILING"
        private const val TRUNCATE = "TRUNCATE"
        private const val TRUNC = "TRUNC"
        private const val SIGN = "SIGN"
        private const val RANDOM = "RANDOM"
        private const val RAND = "RAND"
        private const val RADIANS = "RADIANS"
        private const val DEGREES = "DEGREES"
        private const val MOD = "MOD"
        private const val GCD = "GCD"
        private const val LCM = "LCM"
        private const val FACTORIAL = "FACTORIAL"
        private const val COMBINATION = "COMBINATION"
        private const val COMB = "COMB"
        private const val PERMUTATION = "PERMUTATION"
        private const val PERM = "PERM"

        // Long 오버플로우 방지를 위한 안전한 입력 크기 제한
        private const val MAX_FACTORIAL_INPUT = 20      // 20! = 2,432,902,008,176,640,000 (Long 범위 내)
        private const val MAX_COMBINATION_INPUT = 62    // C(62,31)이 Long 범위 내 최대값
        private const val MAX_PERMUTATION_INPUT = 20    // P(20,20) = 20!과 동일

        /**
         * 빈 변수 바인딩으로 평가기를 생성합니다.
         */
        fun create(): ExpressionEvaluator = ExpressionEvaluator()

        /**
         * 변수 바인딩과 함께 평가기를 생성합니다.
         */
        fun create(variables: Map<String, Any>): ExpressionEvaluator = ExpressionEvaluator(variables)

        /**
         * 커스텀 함수 레지스트리와 함께 평가기를 생성합니다.
         */
        fun create(variables: Map<String, Any>, functionRegistry: FunctionRegistry): ExpressionEvaluator =
            ExpressionEvaluator(variables, functionRegistry)
    }
}
