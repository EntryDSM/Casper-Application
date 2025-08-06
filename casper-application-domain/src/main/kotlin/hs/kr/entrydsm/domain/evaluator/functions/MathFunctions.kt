package hs.kr.entrydsm.domain.evaluator.functions

import hs.kr.entrydsm.domain.evaluator.interfaces.FunctionEvaluator
import kotlin.math.*

/**
 * 기본 수학 함수들의 구현체입니다.
 *
 * @author kangeunchan
 * @since 2025.08.06
 */

class AbsFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        return abs(toDouble(args[0]))
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "ABS"
    override fun getDescription(): String = "절대값을 계산합니다"
}

class SqrtFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        val value = toDouble(args[0])
        if (value < 0) throw ArithmeticException("SQRT of negative number")
        return sqrt(value)
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "SQRT"
    override fun getDescription(): String = "제곱근을 계산합니다"
}

class RoundFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        return when (args.size) {
            1 -> round(toDouble(args[0]))
            2 -> {
                val value = toDouble(args[0])
                val places = toDouble(args[1]).toInt()
                val multiplier = 10.0.pow(places.toDouble())
                round(value * multiplier) / multiplier
            }
            else -> throw IllegalArgumentException("Wrong argument count for ROUND: expected 1-2, got ${args.size}")
        }
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1, 2)
    override fun getFunctionName(): String = "ROUND"
    override fun getDescription(): String = "반올림합니다"
}

class MinFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        if (args.isEmpty()) throw IllegalArgumentException("MIN requires at least 1 argument")
        return args.map { toDouble(it) }.minOrNull() ?: 0.0
    }
    
    override fun getSupportedArgumentCounts(): List<Int>? = null // 가변 인수
    override fun getFunctionName(): String = "MIN"
    override fun getDescription(): String = "최솟값을 찾습니다"
}

class MaxFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        if (args.isEmpty()) throw IllegalArgumentException("MAX requires at least 1 argument")
        return args.map { toDouble(it) }.maxOrNull() ?: 0.0
    }
    
    override fun getSupportedArgumentCounts(): List<Int>? = null // 가변 인수
    override fun getFunctionName(): String = "MAX"
    override fun getDescription(): String = "최댓값을 찾습니다"
}

class SumFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        return args.map { toDouble(it) }.sum()
    }
    
    override fun getSupportedArgumentCounts(): List<Int>? = null // 가변 인수
    override fun getFunctionName(): String = "SUM"
    override fun getDescription(): String = "합계를 계산합니다"
}

class AvgFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        if (args.isEmpty()) throw IllegalArgumentException("AVG requires at least 1 argument")
        return args.map { toDouble(it) }.average()
    }
    
    override fun getSupportedArgumentCounts(): List<Int>? = null // 가변 인수
    override fun getFunctionName(): String = "AVG"
    override fun getDescription(): String = "평균을 계산합니다"
}

class AverageFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        if (args.isEmpty()) throw IllegalArgumentException("AVERAGE requires at least 1 argument")
        return args.map { toDouble(it) }.average()
    }
    
    override fun getSupportedArgumentCounts(): List<Int>? = null // 가변 인수
    override fun getFunctionName(): String = "AVERAGE"
    override fun getDescription(): String = "평균을 계산합니다"
}

class IfFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 3)
        val condition = toBoolean(args[0])
        return if (condition) args[1] else args[2]
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(3)
    override fun getFunctionName(): String = "IF"
    override fun getDescription(): String = "조건부 값을 반환합니다"
}

class PowFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 2)
        return toDouble(args[0]).pow(toDouble(args[1]))
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(2)
    override fun getFunctionName(): String = "POW"
    override fun getDescription(): String = "거듭제곱을 계산합니다"
}

// 삼각함수들
class SinFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        return sin(toDouble(args[0]))
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "SIN"
    override fun getDescription(): String = "사인값을 계산합니다"
}

class CosFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        return cos(toDouble(args[0]))
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "COS"
    override fun getDescription(): String = "코사인값을 계산합니다"
}

class TanFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        return tan(toDouble(args[0]))
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "TAN"
    override fun getDescription(): String = "탄젠트값을 계산합니다"
}

// 로그 함수들
class LogFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        val value = toDouble(args[0])
        if (value <= 0) throw ArithmeticException("LOG of non-positive number")
        return ln(value)
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "LOG"
    override fun getDescription(): String = "자연로그를 계산합니다"
}

class Log10Function : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        val value = toDouble(args[0])
        if (value <= 0) throw ArithmeticException("LOG10 of non-positive number")
        return log10(value)
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "LOG10"
    override fun getDescription(): String = "상용로그를 계산합니다"
}

class ExpFunction : FunctionEvaluator {
    override fun evaluate(args: List<Any?>): Any? {
        validateArgumentCount(args, 1)
        return exp(toDouble(args[0]))
    }
    
    override fun getSupportedArgumentCounts(): List<Int> = listOf(1)
    override fun getFunctionName(): String = "EXP"
    override fun getDescription(): String = "지수함수를 계산합니다"
}

// Helper functions
private fun validateArgumentCount(args: List<Any?>, expectedCount: Int) {
    if (args.size != expectedCount) {
        throw IllegalArgumentException("Wrong argument count: expected $expectedCount, got ${args.size}")
    }
}

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