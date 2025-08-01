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
    private val variables: Map<String, Any> = emptyMap()
) : ASTVisitor<Any?> {

    /**
     * 주어진 AST 노드를 평가합니다.
     *
     * @param node 평가할 AST 노드
     * @return 평가 결과
     * @throws IllegalStateException 평가 중 오류 발생 시
     */
    fun evaluate(node: ASTNode): Any? {
        return try {
            node.accept(this)
        } catch (e: Exception) {
            throw IllegalStateException("Evaluation error: ${e.message}", e)
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
     */
    override fun visitVariable(node: VariableNode): Any? {
        return variables[node.name] ?: throw IllegalArgumentException("Undefined variable: ${node.name}")
    }

    /**
     * BinaryOpNode를 방문하여 이항 연산을 수행합니다.
     */
    override fun visitBinaryOp(node: BinaryOpNode): Any? {
        val left = evaluate(node.left)
        val right = evaluate(node.right)

        return when (node.operator) {
            // 산술 연산자
            "+" -> performArithmeticOp(left, right) { a, b -> a + b }
            "-" -> performArithmeticOp(left, right) { a, b -> a - b }
            "*" -> performArithmeticOp(left, right) { a, b -> a * b }
            "/" -> performDivisionOp(left, right)
            "%" -> performArithmeticOp(left, right) { a, b -> a % b }
            "^" -> performArithmeticOp(left, right) { a, b -> a.pow(b) }
            
            // 비교 연산자
            "==" -> performComparisonOp(left, right) { a, b -> a == b }
            "!=" -> performComparisonOp(left, right) { a, b -> a != b }
            "<" -> performComparisonOp(left, right) { a, b -> a < b }
            "<=" -> performComparisonOp(left, right) { a, b -> a <= b }
            ">" -> performComparisonOp(left, right) { a, b -> a > b }
            ">=" -> performComparisonOp(left, right) { a, b -> a >= b }
            
            // 논리 연산자
            "&&" -> performLogicalAnd(left, right)
            "||" -> performLogicalOr(left, right)
            
            else -> throw IllegalArgumentException("Unsupported operator: ${node.operator}")
        }
    }

    /**
     * UnaryOpNode를 방문하여 단항 연산을 수행합니다.
     */
    override fun visitUnaryOp(node: UnaryOpNode): Any? {
        val operand = evaluate(node.operand)

        return when (node.operator) {
            "-" -> when (operand) {
                is Double -> -operand
                is Int -> -operand.toDouble()
                else -> throw IllegalArgumentException("Unsupported type for unary operation: ${operand?.javaClass?.simpleName ?: "null"}")
            }
            "+" -> when (operand) {
                is Double -> operand
                is Int -> operand.toDouble()
                else -> throw IllegalArgumentException("Unsupported type for unary operation: ${operand?.javaClass?.simpleName ?: "null"}")
            }
            "!" -> when (operand) {
                is Boolean -> !operand
                is Double -> operand == 0.0
                is Int -> operand == 0
                else -> throw IllegalArgumentException("Unsupported type for unary operation: ${operand?.javaClass?.simpleName ?: "null"}")
            }
            else -> throw IllegalArgumentException("Unsupported operator: ${node.operator}")
        }
    }

    /**
     * FunctionCallNode를 방문하여 함수 호출을 처리합니다.
     */
    override fun visitFunctionCall(node: FunctionCallNode): Any? {
        val args = node.args.map { evaluate(it) }
        
        return when (node.name.uppercase()) {
            // 기본 수학 함수들
            "ABS" -> {
                validateArgumentCount(node.name, args, 1)
                abs(toDouble(args[0]))
            }
            "SQRT" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value < 0) throw ArithmeticException("SQRT of negative number")
                sqrt(value)
            }
            "ROUND" -> {
                when (args.size) {
                    1 -> round(toDouble(args[0]))
                    2 -> {
                        val value = toDouble(args[0])
                        val places = toDouble(args[1]).toInt()
                        val multiplier = 10.0.pow(places.toDouble())
                        round(value * multiplier) / multiplier
                    }
                    else -> throw IllegalArgumentException("Wrong argument count for ${node.name}: expected 1-2, got ${args.size}")
                }
            }
            "MIN" -> {
                if (args.isEmpty()) throw IllegalArgumentException("Wrong argument count for ${node.name}: expected at least 1, got ${args.size}")
                args.map { toDouble(it) }.minOrNull() ?: 0.0
            }
            "MAX" -> {
                if (args.isEmpty()) throw IllegalArgumentException("Wrong argument count for ${node.name}: expected at least 1, got ${args.size}")
                args.map { toDouble(it) }.maxOrNull() ?: 0.0
            }
            "SUM" -> {
                args.map { toDouble(it) }.sum()
            }
            "AVG", "AVERAGE" -> {
                if (args.isEmpty()) throw IllegalArgumentException("Wrong argument count for ${node.name}: expected at least 1, got ${args.size}")
                args.map { toDouble(it) }.average()
            }
            "IF" -> {
                validateArgumentCount(node.name, args, 3)
                val condition = toBoolean(args[0])
                if (condition) args[1] else args[2]
            }
            "POW" -> {
                validateArgumentCount(node.name, args, 2)
                toDouble(args[0]).pow(toDouble(args[1]))
            }
            "LOG" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value <= 0) throw ArithmeticException("LOG of non-positive number")
                ln(value)
            }
            "LOG10" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value <= 0) throw ArithmeticException("LOG10 of non-positive number")
                log10(value)
            }
            "EXP" -> {
                validateArgumentCount(node.name, args, 1)
                exp(toDouble(args[0]))
            }
            "SIN" -> {
                validateArgumentCount(node.name, args, 1)
                sin(toDouble(args[0]))
            }
            "COS" -> {
                validateArgumentCount(node.name, args, 1)
                cos(toDouble(args[0]))
            }
            "TAN" -> {
                validateArgumentCount(node.name, args, 1)
                tan(toDouble(args[0]))
            }
            "ASIN" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value < -1 || value > 1) throw ArithmeticException("ASIN domain error")
                asin(value)
            }
            "ACOS" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value < -1 || value > 1) throw ArithmeticException("ACOS domain error")
                acos(value)
            }
            "ATAN" -> {
                validateArgumentCount(node.name, args, 1)
                atan(toDouble(args[0]))
            }
            "ATAN2" -> {
                validateArgumentCount(node.name, args, 2)
                atan2(toDouble(args[0]), toDouble(args[1]))
            }
            "SINH" -> {
                validateArgumentCount(node.name, args, 1)
                sinh(toDouble(args[0]))
            }
            "COSH" -> {
                validateArgumentCount(node.name, args, 1)
                cosh(toDouble(args[0]))
            }
            "TANH" -> {
                validateArgumentCount(node.name, args, 1)
                tanh(toDouble(args[0]))
            }
            "ASINH" -> {
                validateArgumentCount(node.name, args, 1)
                asinh(toDouble(args[0]))
            }
            "ACOSH" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value < 1) throw ArithmeticException("ACOSH domain error")
                acosh(value)
            }
            "ATANH" -> {
                validateArgumentCount(node.name, args, 1)
                val value = toDouble(args[0])
                if (value <= -1 || value >= 1) throw ArithmeticException("ATANH domain error")
                atanh(value)
            }
            "FLOOR" -> {
                validateArgumentCount(node.name, args, 1)
                floor(toDouble(args[0]))
            }
            "CEIL", "CEILING" -> {
                validateArgumentCount(node.name, args, 1)
                ceil(toDouble(args[0]))
            }
            "TRUNCATE", "TRUNC" -> {
                validateArgumentCount(node.name, args, 1)
                truncate(toDouble(args[0]))
            }
            "SIGN" -> {
                validateArgumentCount(node.name, args, 1)
                sign(toDouble(args[0]))
            }
            "RANDOM", "RAND" -> {
                validateArgumentCount(node.name, args, 0)
                kotlin.random.Random.nextDouble()
            }
            "RADIANS" -> {
                validateArgumentCount(node.name, args, 1)
                toDouble(args[0]) * PI / 180.0
            }
            "DEGREES" -> {
                validateArgumentCount(node.name, args, 1)
                toDouble(args[0]) * 180.0 / PI
            }
            "PI" -> {
                validateArgumentCount(node.name, args, 0)
                PI
            }
            "E" -> {
                validateArgumentCount(node.name, args, 0)
                E
            }
            "MOD" -> {
                validateArgumentCount(node.name, args, 2)
                val dividend = toDouble(args[0])
                val divisor = toDouble(args[1])
                if (divisor == 0.0) throw ArithmeticException("Division by zero")
                dividend % divisor
            }
            "GCD" -> {
                validateArgumentCount(node.name, args, 2)
                val a = toDouble(args[0]).toLong()
                val b = toDouble(args[1]).toLong()
                gcd(a, b).toDouble()
            }
            "LCM" -> {
                validateArgumentCount(node.name, args, 2)
                val a = toDouble(args[0]).toLong()
                val b = toDouble(args[1]).toLong()
                lcm(a, b).toDouble()
            }
            "FACTORIAL" -> {
                validateArgumentCount(node.name, args, 1)
                val n = toDouble(args[0]).toInt()
                if (n < 0) throw ArithmeticException("FACTORIAL of negative number")
                factorial(n).toDouble()
            }
            "COMBINATION", "COMB" -> {
                validateArgumentCount(node.name, args, 2)
                val n = toDouble(args[0]).toInt()
                val r = toDouble(args[1]).toInt()
                if (n < 0 || r < 0 || r > n) throw ArithmeticException("COMBINATION domain error")
                combination(n, r).toDouble()
            }
            "PERMUTATION", "PERM" -> {
                validateArgumentCount(node.name, args, 2)
                val n = toDouble(args[0]).toInt()
                val r = toDouble(args[1]).toInt()
                if (n < 0 || r < 0 || r > n) throw ArithmeticException("PERMUTATION domain error")
                permutation(n, r).toDouble()
            }
            else -> throw IllegalArgumentException("Unsupported function: ${node.name}")
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
     */
    private fun performDivisionOp(left: Any?, right: Any?): Double {
        val leftNum = toDouble(left)
        val rightNum = toDouble(right)
        
        if (rightNum == 0.0) {
            throw ArithmeticException("Division by zero")
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
     */
    private fun toDouble(value: Any?): Double {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: throw IllegalArgumentException("Cannot convert string to number: $value")
            else -> throw IllegalArgumentException("Cannot convert ${value?.javaClass?.simpleName ?: "null"} to number")
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
     */
    private fun validateArgumentCount(functionName: String, args: List<Any?>, expectedCount: Int) {
        if (args.size != expectedCount) {
            throw IllegalArgumentException("Wrong argument count for $functionName: expected $expectedCount, got ${args.size}")
        }
    }

    /**
     * 변수 바인딩을 추가한 새로운 평가기를 생성합니다.
     */
    fun withVariables(newVariables: Map<String, Any>): ExpressionEvaluator {
        return ExpressionEvaluator(variables + newVariables)
    }

    /**
     * 단일 변수를 추가한 새로운 평가기를 생성합니다.
     */
    fun withVariable(name: String, value: Any): ExpressionEvaluator {
        return ExpressionEvaluator(variables + (name to value))
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
     */
    private fun factorial(n: Int): Long {
        if (n <= 1) return 1
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    /**
     * 조합을 계산합니다.
     */
    private fun combination(n: Int, r: Int): Long {
        if (r > n || r < 0) return 0
        if (r == 0 || r == n) return 1
        
        val k = minOf(r, n - r)
        var result = 1L
        
        for (i in 0 until k) {
            result = result * (n - i) / (i + 1)
        }
        
        return result
    }

    /**
     * 순열을 계산합니다.
     */
    private fun permutation(n: Int, r: Int): Long {
        if (r > n || r < 0) return 0
        if (r == 0) return 1
        
        var result = 1L
        for (i in 0 until r) {
            result *= (n - i)
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
        /**
         * 빈 변수 바인딩으로 평가기를 생성합니다.
         */
        fun create(): ExpressionEvaluator = ExpressionEvaluator()

        /**
         * 변수 바인딩과 함께 평가기를 생성합니다.
         */
        fun create(variables: Map<String, Any>): ExpressionEvaluator = ExpressionEvaluator(variables)
    }
}