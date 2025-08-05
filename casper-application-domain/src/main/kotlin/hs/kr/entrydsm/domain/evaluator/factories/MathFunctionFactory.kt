package hs.kr.entrydsm.domain.evaluator.factories

import hs.kr.entrydsm.domain.evaluator.entities.MathFunction
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
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
 * 수학 함수 객체들을 생성하는 팩토리입니다.
 *
 * DDD Factory 패턴을 적용하여 수학 함수들의 생성과 구성을 
 * 체계적으로 관리합니다. 미리 정의된 수학 함수들을 생성하고
 * 사용자 정의 함수의 등록을 지원합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Factory(
    context = "evaluator",
    complexity = Complexity.HIGH,
    cache = true
)
class MathFunctionFactory {

    private val functionCache = mutableMapOf<String, MathFunction>()

    /**
     * 기본 수학 함수들을 생성합니다.
     *
     * @return 기본 수학 함수들의 맵
     */
    fun createStandardFunctions(): Map<String, MathFunction> {
        return mapOf(
            // 기본 산술 함수들
            "ABS" to createAbsFunction(),
            "SQRT" to createSqrtFunction(),
            "ROUND" to createRoundFunction(),
            "MIN" to createMinFunction(),
            "MAX" to createMaxFunction(),
            "SUM" to createSumFunction(),
            "AVG" to createAvgFunction(),
            
            // 지수 및 로그 함수들
            "POW" to createPowFunction(),
            "LOG" to createLogFunction(),
            "LOG10" to createLog10Function(),
            "EXP" to createExpFunction(),
            
            // 삼각 함수들
            "SIN" to createSinFunction(),
            "COS" to createCosFunction(),
            "TAN" to createTanFunction(),
            "ASIN" to createAsinFunction(),
            "ACOS" to createAcosFunction(),
            "ATAN" to createAtanFunction(),
            "ATAN2" to createAtan2Function(),
            
            // 쌍곡 함수들
            "SINH" to createSinhFunction(),
            "COSH" to createCoshFunction(),
            "TANH" to createTanhFunction(),
            "ASINH" to createAsinhFunction(),
            "ACOSH" to createAcoshFunction(),
            "ATANH" to createAtanhFunction(),
            
            // 반올림 및 버림 함수들
            "FLOOR" to createFloorFunction(),
            "CEIL" to createCeilFunction(),
            "TRUNC" to createTruncFunction(),
            "SIGN" to createSignFunction(),
            
            // 유틸리티 함수들
            "IF" to createIfFunction(),
            "RANDOM" to createRandomFunction(),
            "RADIANS" to createRadiansFunction(),
            "DEGREES" to createDegreesFunction(),
            "PI" to createPiFunction(),
            "E" to createEFunction(),
            
            // 고급 수학 함수들
            "MOD" to createModFunction(),
            "GCD" to createGcdFunction(),
            "LCM" to createLcmFunction(),
            "FACTORIAL" to createFactorialFunction(),
            "COMBINATION" to createCombinationFunction(),
            "PERMUTATION" to createPermutationFunction()
        )
    }

    /**
     * 삼각 함수들만 생성합니다.
     *
     * @return 삼각 함수들의 맵
     */
    fun createTrigonometricFunctions(): Map<String, MathFunction> {
        return mapOf(
            "SIN" to createSinFunction(),
            "COS" to createCosFunction(),
            "TAN" to createTanFunction(),
            "ASIN" to createAsinFunction(),
            "ACOS" to createAcosFunction(),
            "ATAN" to createAtanFunction(),
            "ATAN2" to createAtan2Function()
        )
    }

    /**
     * 통계 함수들만 생성합니다.
     *
     * @return 통계 함수들의 맵
     */
    fun createStatisticalFunctions(): Map<String, MathFunction> {
        return mapOf(
            "MIN" to createMinFunction(),
            "MAX" to createMaxFunction(),
            "SUM" to createSumFunction(),
            "AVG" to createAvgFunction(),
            "MEDIAN" to createMedianFunction(),
            "MODE" to createModeFunction(),
            "STDEV" to createStandardDeviationFunction(),
            "VARIANCE" to createVarianceFunction()
        )
    }

    /**
     * 사용자 정의 함수를 생성합니다.
     *
     * @param name 함수 이름
     * @param minArgs 최소 인수 개수
     * @param maxArgs 최대 인수 개수
     * @param description 함수 설명
     * @param category 함수 카테고리
     * @param implementation 함수 구현
     * @return 생성된 MathFunction
     */
    fun createCustomFunction(
        name: String,
        minArgs: Int,
        maxArgs: Int,
        description: String,
        category: MathFunction.FunctionCategory,
        implementation: (List<Any>) -> Any
    ): MathFunction {
        return MathFunction(
            name = name.uppercase(),
            minArguments = minArgs,
            maxArguments = maxArgs,
            description = description,
            category = category,
            implementation = implementation
        )
    }

    // Standard Math Functions Implementation

    private fun createAbsFunction() = functionCache.getOrPut("ABS") {
        MathFunction.fixedArgs(
            "ABS", 1, "절댓값을 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            abs(toDouble(args[0]))
        }
    }

    private fun createSqrtFunction() = functionCache.getOrPut("SQRT") {
        MathFunction.fixedArgs(
            "SQRT", 1, "제곱근을 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val value = toDouble(args[0])
            if (value < 0) throw IllegalArgumentException("음수의 제곱근은 계산할 수 없습니다")
            sqrt(value)
        }
    }

    private fun createRoundFunction() = functionCache.getOrPut("ROUND") {
        MathFunction.varArgs(
            "ROUND", 1, 2, "반올림을 수행합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            when (args.size) {
                1 -> round(toDouble(args[0]))
                2 -> {
                    val value = toDouble(args[0])
                    val places = toDouble(args[1]).toInt()
                    val multiplier = 10.0.pow(places.toDouble())
                    round(value * multiplier) / multiplier
                }
                else -> throw IllegalArgumentException("ROUND 함수는 1-2개의 인수를 받습니다")
            }
        }
    }

    private fun createMinFunction() = functionCache.getOrPut("MIN") {
        MathFunction.varArgs(
            "MIN", 1, Int.MAX_VALUE, "최솟값을 찾습니다", MathFunction.FunctionCategory.STATISTICAL
        ) { args ->
            args.map { toDouble(it) }.minOrNull() ?: throw IllegalArgumentException("인수가 없습니다")
        }
    }

    private fun createMaxFunction() = functionCache.getOrPut("MAX") {
        MathFunction.varArgs(
            "MAX", 1, Int.MAX_VALUE, "최댓값을 찾습니다", MathFunction.FunctionCategory.STATISTICAL
        ) { args ->
            args.map { toDouble(it) }.maxOrNull() ?: throw IllegalArgumentException("인수가 없습니다")
        }
    }

    private fun createSumFunction() = functionCache.getOrPut("SUM") {
        MathFunction.varArgs(
            "SUM", 0, Int.MAX_VALUE, "합계를 계산합니다", MathFunction.FunctionCategory.STATISTICAL
        ) { args ->
            args.map { toDouble(it) }.sum()
        }
    }

    private fun createAvgFunction() = functionCache.getOrPut("AVG") {
        MathFunction.varArgs(
            "AVG", 1, Int.MAX_VALUE, "평균을 계산합니다", MathFunction.FunctionCategory.STATISTICAL
        ) { args ->
            args.map { toDouble(it) }.average()
        }
    }

    private fun createPowFunction() = functionCache.getOrPut("POW") {
        MathFunction.fixedArgs(
            "POW", 2, "거듭제곱을 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            toDouble(args[0]).pow(toDouble(args[1]))
        }
    }

    private fun createLogFunction() = functionCache.getOrPut("LOG") {
        MathFunction.fixedArgs(
            "LOG", 1, "자연로그를 계산합니다", MathFunction.FunctionCategory.LOGARITHMIC
        ) { args ->
            val value = toDouble(args[0])
            if (value <= 0) throw IllegalArgumentException("로그의 인수는 양수여야 합니다")
            ln(value)
        }
    }

    private fun createLog10Function() = functionCache.getOrPut("LOG10") {
        MathFunction.fixedArgs(
            "LOG10", 1, "상용로그를 계산합니다", MathFunction.FunctionCategory.LOGARITHMIC
        ) { args ->
            val value = toDouble(args[0])
            if (value <= 0) throw IllegalArgumentException("로그의 인수는 양수여야 합니다")
            log10(value)
        }
    }

    private fun createExpFunction() = functionCache.getOrPut("EXP") {
        MathFunction.fixedArgs(
            "EXP", 1, "지수함수를 계산합니다", MathFunction.FunctionCategory.LOGARITHMIC
        ) { args ->
            exp(toDouble(args[0]))
        }
    }

    private fun createSinFunction() = functionCache.getOrPut("SIN") {
        MathFunction.fixedArgs(
            "SIN", 1, "사인값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            sin(toDouble(args[0]))
        }
    }

    private fun createCosFunction() = functionCache.getOrPut("COS") {
        MathFunction.fixedArgs(
            "COS", 1, "코사인값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            cos(toDouble(args[0]))
        }
    }

    private fun createTanFunction() = functionCache.getOrPut("TAN") {
        MathFunction.fixedArgs(
            "TAN", 1, "탄젠트값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            tan(toDouble(args[0]))
        }
    }

    private fun createAsinFunction() = functionCache.getOrPut("ASIN") {
        MathFunction.fixedArgs(
            "ASIN", 1, "아크사인값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            val value = toDouble(args[0])
            if (value < -1 || value > 1) throw IllegalArgumentException("ASIN 정의역 오류")
            asin(value)
        }
    }

    private fun createAcosFunction() = functionCache.getOrPut("ACOS") {
        MathFunction.fixedArgs(
            "ACOS", 1, "아크코사인값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            val value = toDouble(args[0])
            if (value < -1 || value > 1) throw IllegalArgumentException("ACOS 정의역 오류")
            acos(value)
        }
    }

    private fun createAtanFunction() = functionCache.getOrPut("ATAN") {
        MathFunction.fixedArgs(
            "ATAN", 1, "아크탄젠트값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            atan(toDouble(args[0]))
        }
    }

    private fun createAtan2Function() = functionCache.getOrPut("ATAN2") {
        MathFunction.fixedArgs(
            "ATAN2", 2, "2개 인수의 아크탄젠트값을 계산합니다", MathFunction.FunctionCategory.TRIGONOMETRIC
        ) { args ->
            atan2(toDouble(args[0]), toDouble(args[1]))
        }
    }

    private fun createSinhFunction() = functionCache.getOrPut("SINH") {
        MathFunction.fixedArgs(
            "SINH", 1, "하이퍼볼릭 사인값을 계산합니다", MathFunction.FunctionCategory.HYPERBOLIC
        ) { args ->
            sinh(toDouble(args[0]))
        }
    }

    private fun createCoshFunction() = functionCache.getOrPut("COSH") {
        MathFunction.fixedArgs(
            "COSH", 1, "하이퍼볼릭 코사인값을 계산합니다", MathFunction.FunctionCategory.HYPERBOLIC
        ) { args ->
            cosh(toDouble(args[0]))
        }
    }

    private fun createTanhFunction() = functionCache.getOrPut("TANH") {
        MathFunction.fixedArgs(
            "TANH", 1, "하이퍼볼릭 탄젠트값을 계산합니다", MathFunction.FunctionCategory.HYPERBOLIC
        ) { args ->
            tanh(toDouble(args[0]))
        }
    }

    private fun createAsinhFunction() = functionCache.getOrPut("ASINH") {
        MathFunction.fixedArgs(
            "ASINH", 1, "역 하이퍼볼릭 사인값을 계산합니다", MathFunction.FunctionCategory.HYPERBOLIC
        ) { args ->
            asinh(toDouble(args[0]))
        }
    }

    private fun createAcoshFunction() = functionCache.getOrPut("ACOSH") {
        MathFunction.fixedArgs(
            "ACOSH", 1, "역 하이퍼볼릭 코사인값을 계산합니다", MathFunction.FunctionCategory.HYPERBOLIC
        ) { args ->
            val value = toDouble(args[0])
            if (value < 1) throw IllegalArgumentException("ACOSH 정의역 오류")
            acosh(value)
        }
    }

    private fun createAtanhFunction() = functionCache.getOrPut("ATANH") {
        MathFunction.fixedArgs(
            "ATANH", 1, "역 하이퍼볼릭 탄젠트값을 계산합니다", MathFunction.FunctionCategory.HYPERBOLIC
        ) { args ->
            val value = toDouble(args[0])
            if (value <= -1 || value >= 1) throw IllegalArgumentException("ATANH 정의역 오류")
            atanh(value)
        }
    }

    private fun createFloorFunction() = functionCache.getOrPut("FLOOR") {
        MathFunction.fixedArgs(
            "FLOOR", 1, "내림을 수행합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            floor(toDouble(args[0]))
        }
    }

    private fun createCeilFunction() = functionCache.getOrPut("CEIL") {
        MathFunction.fixedArgs(
            "CEIL", 1, "올림을 수행합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            ceil(toDouble(args[0]))
        }
    }

    private fun createTruncFunction() = functionCache.getOrPut("TRUNC") {
        MathFunction.fixedArgs(
            "TRUNC", 1, "버림을 수행합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            truncate(toDouble(args[0]))
        }
    }

    private fun createSignFunction() = functionCache.getOrPut("SIGN") {
        MathFunction.fixedArgs(
            "SIGN", 1, "부호를 반환합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            sign(toDouble(args[0]))
        }
    }

    private fun createIfFunction() = functionCache.getOrPut("IF") {
        MathFunction.fixedArgs(
            "IF", 3, "조건문을 처리합니다", MathFunction.FunctionCategory.LOGICAL
        ) { args ->
            val condition = toBoolean(args[0])
            if (condition) args[1] else args[2]
        }
    }

    private fun createRandomFunction() = functionCache.getOrPut("RANDOM") {
        MathFunction.fixedArgs(
            "RANDOM", 0, "난수를 생성합니다", MathFunction.FunctionCategory.UTILITY
        ) { _ ->
            kotlin.random.Random.nextDouble()
        }
    }

    private fun createRadiansFunction() = functionCache.getOrPut("RADIANS") {
        MathFunction.fixedArgs(
            "RADIANS", 1, "도를 라디안으로 변환합니다", MathFunction.FunctionCategory.CONVERSION
        ) { args ->
            toDouble(args[0]) * PI / 180.0
        }
    }

    private fun createDegreesFunction() = functionCache.getOrPut("DEGREES") {
        MathFunction.fixedArgs(
            "DEGREES", 1, "라디안을 도로 변환합니다", MathFunction.FunctionCategory.CONVERSION
        ) { args ->
            toDouble(args[0]) * 180.0 / PI
        }
    }

    private fun createPiFunction() = functionCache.getOrPut("PI") {
        MathFunction.fixedArgs(
            "PI", 0, "원주율 π를 반환합니다", MathFunction.FunctionCategory.UTILITY
        ) { _ ->
            PI
        }
    }

    private fun createEFunction() = functionCache.getOrPut("E") {
        MathFunction.fixedArgs(
            "E", 0, "자연상수 e를 반환합니다", MathFunction.FunctionCategory.UTILITY
        ) { _ ->
            E
        }
    }

    private fun createModFunction() = functionCache.getOrPut("MOD") {
        MathFunction.fixedArgs(
            "MOD", 2, "나머지를 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val dividend = toDouble(args[0])
            val divisor = toDouble(args[1])
            if (divisor == 0.0) throw IllegalArgumentException("0으로 나눌 수 없습니다")
            dividend % divisor
        }
    }

    private fun createGcdFunction() = functionCache.getOrPut("GCD") {
        MathFunction.fixedArgs(
            "GCD", 2, "최대공약수를 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val a = toDouble(args[0]).toLong()
            val b = toDouble(args[1]).toLong()
            gcd(a, b).toDouble()
        }
    }

    private fun createLcmFunction() = functionCache.getOrPut("LCM") {
        MathFunction.fixedArgs(
            "LCM", 2, "최소공배수를 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val a = toDouble(args[0]).toLong()
            val b = toDouble(args[1]).toLong()
            lcm(a, b).toDouble()
        }
    }

    private fun createFactorialFunction() = functionCache.getOrPut("FACTORIAL") {
        MathFunction.fixedArgs(
            "FACTORIAL", 1, "팩토리얼을 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val n = toDouble(args[0]).toInt()
            if (n < 0) throw IllegalArgumentException("음수의 팩토리얼은 계산할 수 없습니다")
            factorial(n).toDouble()
        }
    }

    private fun createCombinationFunction() = functionCache.getOrPut("COMBINATION") {
        MathFunction.fixedArgs(
            "COMBINATION", 2, "조합을 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val n = toDouble(args[0]).toInt()
            val r = toDouble(args[1]).toInt()
            if (n < 0 || r < 0 || r > n) throw IllegalArgumentException("조합 정의역 오류")
            combination(n, r).toDouble()
        }
    }

    private fun createPermutationFunction() = functionCache.getOrPut("PERMUTATION") {
        MathFunction.fixedArgs(
            "PERMUTATION", 2, "순열을 계산합니다", MathFunction.FunctionCategory.ARITHMETIC
        ) { args ->
            val n = toDouble(args[0]).toInt()
            val r = toDouble(args[1]).toInt()
            if (n < 0 || r < 0 || r > n) throw IllegalArgumentException("순열 정의역 오류")
            permutation(n, r).toDouble()
        }
    }

    // Statistical Functions

    private fun createMedianFunction() = MathFunction.varArgs(
        "MEDIAN", 1, Int.MAX_VALUE, "중앙값을 계산합니다", MathFunction.FunctionCategory.STATISTICAL
    ) { args ->
        val values = args.map { toDouble(it) }.sorted()
        val size = values.size
        if (size % 2 == 0) {
            (values[size / 2 - 1] + values[size / 2]) / 2.0
        } else {
            values[size / 2]
        }
    }

    private fun createModeFunction() = MathFunction.varArgs(
        "MODE", 1, Int.MAX_VALUE, "최빈값을 찾습니다", MathFunction.FunctionCategory.STATISTICAL
    ) { args ->
        val values = args.map { toDouble(it) }
        val frequency = values.groupBy { it }.mapValues { it.value.size }
        val maxFreq = frequency.maxByOrNull { it.value }?.value ?: 0
        frequency.filter { it.value == maxFreq }.keys.minOrNull() ?: 0.0
    }

    private fun createStandardDeviationFunction() = MathFunction.varArgs(
        "STDEV", 1, Int.MAX_VALUE, "표준편차를 계산합니다", MathFunction.FunctionCategory.STATISTICAL
    ) { args ->
        val values = args.map { toDouble(it) }
        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        sqrt(variance)
    }

    private fun createVarianceFunction() = MathFunction.varArgs(
        "VARIANCE", 1, Int.MAX_VALUE, "분산을 계산합니다", MathFunction.FunctionCategory.STATISTICAL
    ) { args ->
        val values = args.map { toDouble(it) }
        val mean = values.average()
        values.map { (it - mean).pow(2) }.average()
    }

    // Helper Functions

    private fun toDouble(value: Any): Double {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: throw IllegalArgumentException("숫자로 변환할 수 없습니다: $value")
            else -> throw IllegalArgumentException("지원하지 않는 타입: ${value::class.simpleName}")
        }
    }

    private fun toBoolean(value: Any): Boolean {
        return when (value) {
            is Boolean -> value
            is Double -> value != 0.0 && !value.isNaN()
            is Int -> value != 0
            is String -> value.isNotEmpty() && value.lowercase() !in setOf("false", "0")
            else -> true
        }
    }

    private fun gcd(a: Long, b: Long): Long {
        return if (b == 0L) abs(a) else gcd(b, a % b)
    }

    private fun lcm(a: Long, b: Long): Long {
        return abs(a * b) / gcd(a, b)
    }

    private fun factorial(n: Int): Long {
        if (n <= 1) return 1
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }

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
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "factoryName" to "MathFunctionFactory",
        "standardFunctionCount" to createStandardFunctions().size,
        "trigonometricFunctionCount" to createTrigonometricFunctions().size,
        "statisticalFunctionCount" to createStatisticalFunctions().size,
        "cacheEnabled" to true,
        "complexityLevel" to Complexity.HIGH.name
    )

    /**
     * 팩토리의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "cachedFunctions" to functionCache.size,
        "supportedCategories" to MathFunction.FunctionCategory.values().size,
        "totalStandardFunctions" to createStandardFunctions().size
    )
}