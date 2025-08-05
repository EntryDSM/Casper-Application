package hs.kr.entrydsm.domain.evaluator.services

import hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
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
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * 수학 함수 실행을 담당하는 서비스입니다.
 *
 * 다양한 수학 함수의 실행 로직을 캡슐화하며,
 * 함수별 인수 검증과 오류 처리를 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Service(
    name = "수학 함수 실행 서비스",
    type = ServiceType.DOMAIN_SERVICE
)
class MathFunctionService {
    
    /**
     * 수학 함수를 실행합니다.
     *
     * @param functionName 함수명
     * @param arguments 인수 목록
     * @return 실행 결과
     */
    fun executeFunction(functionName: String, arguments: List<Any?>): Any? {
        val funcName = functionName.uppercase()
        
        return when (funcName) {
            // 기본 수학 함수들
            "ABS" -> executeAbs(arguments)
            "SQRT" -> executeSqrt(arguments)
            "ROUND" -> executeRound(arguments)
            "FLOOR" -> executeFloor(arguments)
            "CEIL" -> executeCeil(arguments)
            "SIGN" -> executeSign(arguments)
            
            // 삼각함수
            "SIN" -> executeSin(arguments)
            "COS" -> executeCos(arguments)
            "TAN" -> executeTan(arguments)
            "ASIN" -> executeAsin(arguments)
            "ACOS" -> executeAcos(arguments)
            "ATAN" -> executeAtan(arguments)
            "ATAN2" -> executeAtan2(arguments)
            
            // 쌍곡함수
            "SINH" -> executeSinh(arguments)
            "COSH" -> executeCosh(arguments)
            "TANH" -> executeTanh(arguments)
            "ASINH" -> executeAsinh(arguments)
            "ACOSH" -> executeAcosh(arguments)
            "ATANH" -> executeAtanh(arguments)
            
            // 지수 및 로그 함수
            "EXP" -> executeExp(arguments)
            "LOG" -> executeLog(arguments)
            "LOG10" -> executeLog10(arguments)
            "LOG2" -> executeLog2(arguments)
            "POW" -> executePow(arguments)
            
            // 통계 함수
            "MIN" -> executeMin(arguments)
            "MAX" -> executeMax(arguments)
            "SUM" -> executeSum(arguments)
            "AVG", "AVERAGE" -> executeAverage(arguments)
            "COUNT" -> executeCount(arguments)
            
            // 조합 및 팩토리얼
            "FACTORIAL" -> executeFactorial(arguments)
            "COMBINATION" -> executeCombination(arguments)
            "PERMUTATION" -> executePermutation(arguments)
            
            // 조건 함수
            "IF" -> executeIf(arguments)
            
            // 변환 함수
            "RADIANS" -> executeRadians(arguments)
            "DEGREES" -> executeDegrees(arguments)
            
            // 기타 함수
            "RANDOM" -> executeRandom(arguments)
            "GCD" -> executeGcd(arguments)
            "LCM" -> executeLcm(arguments)
            
            else -> throw EvaluatorException.unsupportedFunction(funcName)
        }
    }
    
    /**
     * 절댓값 함수를 실행합니다.
     */
    private fun executeAbs(arguments: List<Any?>): Double {
        validateArgumentCount("ABS", arguments, 1)
        return abs(toDouble(arguments[0]))
    }
    
    /**
     * 제곱근 함수를 실행합니다.
     */
    private fun executeSqrt(arguments: List<Any?>): Double {
        validateArgumentCount("SQRT", arguments, 1)
        val value = toDouble(arguments[0])
        if (value < 0) throw EvaluatorException.mathError("음수의 제곱근을 계산할 수 없습니다")
        return sqrt(value)
    }
    
    /**
     * 반올림 함수를 실행합니다.
     */
    private fun executeRound(arguments: List<Any?>): Double {
        return when (arguments.size) {
            1 -> round(toDouble(arguments[0]))
            2 -> {
                val value = toDouble(arguments[0])
                val places = toDouble(arguments[1]).toInt()
                val multiplier = 10.0.pow(places.toDouble())
                round(value * multiplier) / multiplier
            }
            else -> throw EvaluatorException.wrongArgumentCount("ROUND", 1, arguments.size)
        }
    }
    
    /**
     * 바닥 함수를 실행합니다.
     */
    private fun executeFloor(arguments: List<Any?>): Double {
        validateArgumentCount("FLOOR", arguments, 1)
        return floor(toDouble(arguments[0]))
    }
    
    /**
     * 천장 함수를 실행합니다.
     */
    private fun executeCeil(arguments: List<Any?>): Double {
        validateArgumentCount("CEIL", arguments, 1)
        return ceil(toDouble(arguments[0]))
    }
    
    /**
     * 부호 함수를 실행합니다.
     */
    private fun executeSign(arguments: List<Any?>): Double {
        validateArgumentCount("SIGN", arguments, 1)
        return sign(toDouble(arguments[0]))
    }
    
    /**
     * 사인 함수를 실행합니다.
     */
    private fun executeSin(arguments: List<Any?>): Double {
        validateArgumentCount("SIN", arguments, 1)
        return sin(toDouble(arguments[0]))
    }
    
    /**
     * 코사인 함수를 실행합니다.
     */
    private fun executeCos(arguments: List<Any?>): Double {
        validateArgumentCount("COS", arguments, 1)
        return cos(toDouble(arguments[0]))
    }
    
    /**
     * 탄젠트 함수를 실행합니다.
     */
    private fun executeTan(arguments: List<Any?>): Double {
        validateArgumentCount("TAN", arguments, 1)
        return tan(toDouble(arguments[0]))
    }
    
    /**
     * 아크사인 함수를 실행합니다.
     */
    private fun executeAsin(arguments: List<Any?>): Double {
        validateArgumentCount("ASIN", arguments, 1)
        val value = toDouble(arguments[0])
        if (value < -1 || value > 1) throw EvaluatorException.mathError("ASIN의 정의역은 [-1, 1]입니다")
        return asin(value)
    }
    
    /**
     * 아크코사인 함수를 실행합니다.
     */
    private fun executeAcos(arguments: List<Any?>): Double {
        validateArgumentCount("ACOS", arguments, 1)
        val value = toDouble(arguments[0])
        if (value < -1 || value > 1) throw EvaluatorException.mathError("ACOS의 정의역은 [-1, 1]입니다")
        return acos(value)
    }
    
    /**
     * 아크탄젠트 함수를 실행합니다.
     */
    private fun executeAtan(arguments: List<Any?>): Double {
        validateArgumentCount("ATAN", arguments, 1)
        return atan(toDouble(arguments[0]))
    }
    
    /**
     * 2인수 아크탄젠트 함수를 실행합니다.
     */
    private fun executeAtan2(arguments: List<Any?>): Double {
        validateArgumentCount("ATAN2", arguments, 2)
        return atan2(toDouble(arguments[0]), toDouble(arguments[1]))
    }
    
    /**
     * 쌍곡사인 함수를 실행합니다.
     */
    private fun executeSinh(arguments: List<Any?>): Double {
        validateArgumentCount("SINH", arguments, 1)
        return sinh(toDouble(arguments[0]))
    }
    
    /**
     * 쌍곡코사인 함수를 실행합니다.
     */
    private fun executeCosh(arguments: List<Any?>): Double {
        validateArgumentCount("COSH", arguments, 1)
        return cosh(toDouble(arguments[0]))
    }
    
    /**
     * 쌍곡탄젠트 함수를 실행합니다.
     */
    private fun executeTanh(arguments: List<Any?>): Double {
        validateArgumentCount("TANH", arguments, 1)
        return tanh(toDouble(arguments[0]))
    }
    
    /**
     * 쌍곡아크사인 함수를 실행합니다.
     */
    private fun executeAsinh(arguments: List<Any?>): Double {
        validateArgumentCount("ASINH", arguments, 1)
        return asinh(toDouble(arguments[0]))
    }
    
    /**
     * 쌍곡아크코사인 함수를 실행합니다.
     */
    private fun executeAcosh(arguments: List<Any?>): Double {
        validateArgumentCount("ACOSH", arguments, 1)
        val value = toDouble(arguments[0])
        if (value < 1) throw EvaluatorException.mathError("ACOSH의 정의역은 [1, ∞)입니다")
        return acosh(value)
    }
    
    /**
     * 쌍곡아크탄젠트 함수를 실행합니다.
     */
    private fun executeAtanh(arguments: List<Any?>): Double {
        validateArgumentCount("ATANH", arguments, 1)
        val value = toDouble(arguments[0])
        if (value <= -1 || value >= 1) throw EvaluatorException.mathError("ATANH의 정의역은 (-1, 1)입니다")
        return atanh(value)
    }
    
    /**
     * 지수 함수를 실행합니다.
     */
    private fun executeExp(arguments: List<Any?>): Double {
        validateArgumentCount("EXP", arguments, 1)
        return exp(toDouble(arguments[0]))
    }
    
    /**
     * 자연로그 함수를 실행합니다.
     */
    private fun executeLog(arguments: List<Any?>): Double {
        validateArgumentCount("LOG", arguments, 1)
        val value = toDouble(arguments[0])
        if (value <= 0) throw EvaluatorException.mathError("양수에 대해서만 로그를 계산할 수 있습니다")
        return ln(value)
    }
    
    /**
     * 상용로그 함수를 실행합니다.
     */
    private fun executeLog10(arguments: List<Any?>): Double {
        validateArgumentCount("LOG10", arguments, 1)
        val value = toDouble(arguments[0])
        if (value <= 0) throw EvaluatorException.mathError("양수에 대해서만 로그를 계산할 수 있습니다")
        return log10(value)
    }
    
    /**
     * 이진로그 함수를 실행합니다.
     */
    private fun executeLog2(arguments: List<Any?>): Double {
        validateArgumentCount("LOG2", arguments, 1)
        val value = toDouble(arguments[0])
        if (value <= 0) throw EvaluatorException.mathError("양수에 대해서만 로그를 계산할 수 있습니다")
        return log2(value)
    }
    
    /**
     * 거듭제곱 함수를 실행합니다.
     */
    private fun executePow(arguments: List<Any?>): Double {
        validateArgumentCount("POW", arguments, 2)
        return toDouble(arguments[0]).pow(toDouble(arguments[1]))
    }
    
    /**
     * 최솟값 함수를 실행합니다.
     */
    private fun executeMin(arguments: List<Any?>): Double {
        if (arguments.isEmpty()) throw EvaluatorException.wrongArgumentCount("MIN", 1, arguments.size)
        return arguments.map { toDouble(it) }.minOrNull() ?: 0.0
    }
    
    /**
     * 최댓값 함수를 실행합니다.
     */
    private fun executeMax(arguments: List<Any?>): Double {
        if (arguments.isEmpty()) throw EvaluatorException.wrongArgumentCount("MAX", 1, arguments.size)
        return arguments.map { toDouble(it) }.maxOrNull() ?: 0.0
    }
    
    /**
     * 합계 함수를 실행합니다.
     */
    private fun executeSum(arguments: List<Any?>): Double {
        return arguments.map { toDouble(it) }.sum()
    }
    
    /**
     * 평균 함수를 실행합니다.
     */
    private fun executeAverage(arguments: List<Any?>): Double {
        if (arguments.isEmpty()) throw EvaluatorException.wrongArgumentCount("AVG", 1, arguments.size)
        return arguments.map { toDouble(it) }.average()
    }
    
    /**
     * 개수 함수를 실행합니다.
     */
    private fun executeCount(arguments: List<Any?>): Double {
        return arguments.size.toDouble()
    }
    
    /**
     * 팩토리얼 함수를 실행합니다.
     */
    private fun executeFactorial(arguments: List<Any?>): Double {
        validateArgumentCount("FACTORIAL", arguments, 1)
        val n = toDouble(arguments[0]).toInt()
        if (n < 0) throw EvaluatorException.mathError("음수의 팩토리얼은 정의되지 않습니다")
        if (n > 170) throw EvaluatorException.mathError("팩토리얼 값이 너무 큽니다")
        
        var result = 1.0
        for (i in 1..n) {
            result *= i
        }
        return result
    }
    
    /**
     * 조합 함수를 실행합니다.
     */
    private fun executeCombination(arguments: List<Any?>): Double {
        validateArgumentCount("COMBINATION", arguments, 2)
        val n = toDouble(arguments[0]).toInt()
        val k = toDouble(arguments[1]).toInt()
        
        if (n < 0 || k < 0) throw EvaluatorException.mathError("음수에 대한 조합은 정의되지 않습니다")
        if (k > n) return 0.0
        
        return executeFactorial(listOf(n)) / (executeFactorial(listOf(k)) * executeFactorial(listOf(n - k)))
    }
    
    /**
     * 순열 함수를 실행합니다.
     */
    private fun executePermutation(arguments: List<Any?>): Double {
        validateArgumentCount("PERMUTATION", arguments, 2)
        val n = toDouble(arguments[0]).toInt()
        val k = toDouble(arguments[1]).toInt()
        
        if (n < 0 || k < 0) throw EvaluatorException.mathError("음수에 대한 순열은 정의되지 않습니다")
        if (k > n) return 0.0
        
        return executeFactorial(listOf(n)) / executeFactorial(listOf(n - k))
    }
    
    /**
     * 조건 함수를 실행합니다.
     */
    private fun executeIf(arguments: List<Any?>): Any? {
        validateArgumentCount("IF", arguments, 3)
        val condition = toBoolean(arguments[0])
        return if (condition) arguments[1] else arguments[2]
    }
    
    /**
     * 라디안 변환 함수를 실행합니다.
     */
    private fun executeRadians(arguments: List<Any?>): Double {
        validateArgumentCount("RADIANS", arguments, 1)
        return toDouble(arguments[0]) * PI / 180.0
    }
    
    /**
     * 도 변환 함수를 실행합니다.
     */
    private fun executeDegrees(arguments: List<Any?>): Double {
        validateArgumentCount("DEGREES", arguments, 1)
        return toDouble(arguments[0]) * 180.0 / PI
    }
    
    /**
     * 랜덤 함수를 실행합니다.
     */
    private fun executeRandom(arguments: List<Any?>): Double {
        return when (arguments.size) {
            0 -> kotlin.random.Random.nextDouble()
            1 -> kotlin.random.Random.nextDouble(toDouble(arguments[0]))
            2 -> kotlin.random.Random.nextDouble(toDouble(arguments[0]), toDouble(arguments[1]))
            else -> throw EvaluatorException.wrongArgumentCount("RANDOM", 0, arguments.size)
        }
    }
    
    /**
     * 최대공약수 함수를 실행합니다.
     */
    private fun executeGcd(arguments: List<Any?>): Double {
        validateArgumentCount("GCD", arguments, 2)
        val a = toDouble(arguments[0]).toInt()
        val b = toDouble(arguments[1]).toInt()
        
        return gcd(a, b).toDouble()
    }
    
    /**
     * 최소공배수 함수를 실행합니다.
     */
    private fun executeLcm(arguments: List<Any?>): Double {
        validateArgumentCount("LCM", arguments, 2)
        val a = toDouble(arguments[0]).toInt()
        val b = toDouble(arguments[1]).toInt()
        
        return (a * b / gcd(a, b)).toDouble()
    }
    
    /**
     * 최대공약수를 계산합니다.
     */
    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) abs(a) else gcd(b, a % b)
    }
    
    /**
     * 인수 개수를 검증합니다.
     */
    private fun validateArgumentCount(functionName: String, arguments: List<Any?>, expected: Int) {
        if (arguments.size != expected) {
            throw EvaluatorException.wrongArgumentCount(functionName, expected, arguments.size)
        }
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
            is String -> value.toDoubleOrNull() ?: throw EvaluatorException.numberConversionError(value)
            else -> throw EvaluatorException.unsupportedType(value?.javaClass?.simpleName ?: "null", value)
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
            is String -> value.lowercase() in setOf("true", "1", "yes", "on")
            else -> false
        }
    }
    
    /**
     * 지원되는 함수 목록을 반환합니다.
     */
    fun getSupportedFunctions(): Set<String> {
        return setOf(
            "ABS", "SQRT", "ROUND", "FLOOR", "CEIL", "SIGN",
            "SIN", "COS", "TAN", "ASIN", "ACOS", "ATAN", "ATAN2",
            "SINH", "COSH", "TANH", "ASINH", "ACOSH", "ATANH",
            "EXP", "LOG", "LOG10", "LOG2", "POW",
            "MIN", "MAX", "SUM", "AVG", "AVERAGE", "COUNT",
            "FACTORIAL", "COMBINATION", "PERMUTATION",
            "IF", "RADIANS", "DEGREES", "RANDOM", "GCD", "LCM"
        )
    }
    
    /**
     * 함수가 지원되는지 확인합니다.
     */
    fun isSupported(functionName: String): Boolean {
        return getSupportedFunctions().contains(functionName.uppercase())
    }
}