package hs.kr.entrydsm.domain.evaluator.policies

import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope
import kotlin.reflect.KClass

/**
 * 타입 강제 변환 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 표현식 평가 과정에서 발생하는
 * 타입 변환의 규칙과 정책을 캡슐화합니다. 안전하고 일관된
 * 타입 변환을 보장하며, 타입 호환성 검증을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "TypeCoercion",
    description = "타입 변환과 호환성 검증을 위한 정책으로 안전한 타입 강제를 담당",
    domain = "evaluator",
    scope = Scope.DOMAIN
)
class TypeCoercionPolicy {

    companion object {
        // 숫자 타입 우선순위 (높을수록 우선순위가 높음)
        private val NUMBER_TYPE_PRIORITY = mapOf(
            Byte::class to 1,
            Short::class to 2,
            Int::class to 3,
            Long::class to 4,
            Float::class to 5,
            Double::class to 6
        )
        
        // 허용된 타입들
        private val ALLOWED_TYPES = setOf(
            Boolean::class,
            Byte::class,
            Short::class,
            Int::class,
            Long::class,
            Float::class,
            Double::class,
            String::class,
            List::class,
            Map::class
        )
    }

    /**
     * 값을 Double로 변환합니다.
     *
     * @param value 변환할 값
     * @return 변환된 Double 값
     * @throws TypeCoercionException 변환할 수 없는 경우
     */
    fun toDouble(value: Any?): Double {
        return when (value) {
            null -> throw TypeCoercionException("null 값은 숫자로 변환할 수 없습니다")
            is Double -> value
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Short -> value.toDouble()
            is Byte -> value.toDouble()
            is String -> {
                value.toDoubleOrNull() 
                    ?: throw TypeCoercionException("문자열 '$value'를 숫자로 변환할 수 없습니다")
            }
            is Boolean -> if (value) 1.0 else 0.0
            else -> throw TypeCoercionException(
                "타입 ${value::class.simpleName}을 Double로 변환할 수 없습니다: $value"
            )
        }
    }

    /**
     * 값을 Boolean으로 변환합니다.
     *
     * @param value 변환할 값
     * @return 변환된 Boolean 값
     */
    fun toBoolean(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Double -> value != 0.0 && !value.isNaN()
            is Float -> value != 0.0f && !value.isNaN()
            is Long -> value != 0L
            is Int -> value != 0
            is Short -> value != 0.toShort()
            is Byte -> value != 0.toByte()
            is String -> value.isNotEmpty() && value.lowercase() !in setOf("false", "0", "null", "undefined")
            is Collection<*> -> value.isNotEmpty()
            is Map<*, *> -> value.isNotEmpty()
            else -> true // 기본적으로 null이 아닌 객체는 true
        }
    }

    /**
     * 값을 Int로 변환합니다.
     *
     * @param value 변환할 값
     * @return 변환된 Int 값
     * @throws TypeCoercionException 변환할 수 없는 경우
     */
    fun toInt(value: Any?): Int {
        val doubleValue = toDouble(value)
        if (doubleValue > Int.MAX_VALUE || doubleValue < Int.MIN_VALUE) {
            throw TypeCoercionException("값 ${doubleValue}가 Int 범위를 벗어났습니다")
        }
        return doubleValue.toInt()
    }

    /**
     * 값을 Long으로 변환합니다.
     *
     * @param value 변환할 값
     * @return 변환된 Long 값
     * @throws TypeCoercionException 변환할 수 없는 경우
     */
    fun toLong(value: Any?): Long {
        val doubleValue = toDouble(value)
        if (doubleValue > Long.MAX_VALUE || doubleValue < Long.MIN_VALUE) {
            throw TypeCoercionException("값 ${doubleValue}가 Long 범위를 벗어났습니다")
        }
        return doubleValue.toLong()
    }

    /**
     * 값을 String으로 변환합니다.
     *
     * @param value 변환할 값
     * @return 변환된 String 값
     */
    fun toString(value: Any?): String {
        return when (value) {
            null -> "null"
            is String -> value
            is Double -> {
                if (value == value.toLong().toDouble()) {
                    value.toLong().toString()
                } else {
                    value.toString()
                }
            }
            is Float -> {
                if (value == value.toLong().toFloat()) {
                    value.toLong().toString()
                } else {
                    value.toString()
                }
            }
            else -> value.toString()
        }
    }

    /**
     * 두 값이 타입 호환성을 가지는지 확인합니다.
     *
     * @param value1 첫 번째 값
     * @param value2 두 번째 값
     * @return 호환되면 true
     */
    fun areCompatible(value1: Any?, value2: Any?): Boolean {
        return try {
            getCompatibleType(value1, value2) != null
        } catch (e: TypeCoercionException) {
            false
        }
    }

    /**
     * 두 값의 호환 가능한 공통 타입을 찾습니다.
     *
     * @param value1 첫 번째 값
     * @param value2 두 번째 값
     * @return 공통 타입
     * @throws TypeCoercionException 호환되지 않는 경우
     */
    fun getCompatibleType(value1: Any?, value2: Any?): KClass<*> {
        val type1 = getEffectiveType(value1)
        val type2 = getEffectiveType(value2)
        
        // 같은 타입이면 그대로 반환
        if (type1 == type2) {
            return type1
        }
        
        // 둘 다 숫자 타입인 경우
        if (isNumericType(type1) && isNumericType(type2)) {
            return getHigherPriorityNumericType(type1, type2)
        }
        
        // Boolean과 숫자 타입의 경우
        if ((type1 == Boolean::class && isNumericType(type2)) ||
            (type2 == Boolean::class && isNumericType(type1))) {
            return Double::class // Boolean은 숫자로 변환 가능
        }
        
        // String과 다른 타입의 경우
        if (type1 == String::class || type2 == String::class) {
            return String::class // 모든 타입은 String으로 변환 가능
        }
        
        throw TypeCoercionException(
            "타입 ${type1.simpleName}과 ${type2.simpleName}은 호환되지 않습니다"
        )
    }

    /**
     * 두 값을 공통 타입으로 변환합니다.
     *
     * @param value1 첫 번째 값
     * @param value2 두 번째 값
     * @return 변환된 값들의 Pair
     */
    fun coerceToCommonType(value1: Any?, value2: Any?): Pair<Any, Any> {
        val commonType = getCompatibleType(value1, value2)
        
        return when (commonType) {
            Double::class -> Pair(toDouble(value1), toDouble(value2))
            Boolean::class -> Pair(toBoolean(value1), toBoolean(value2))
            String::class -> Pair(toString(value1), toString(value2))
            Int::class -> Pair(toInt(value1), toInt(value2))
            Long::class -> Pair(toLong(value1), toLong(value2))
            else -> throw TypeCoercionException(
                "타입 ${commonType.simpleName}로의 변환은 지원되지 않습니다"
            )
        }
    }

    /**
     * 타입이 허용되는지 확인합니다.
     *
     * @param value 확인할 값
     * @return 허용되면 true
     */
    fun isAllowedType(value: Any?): Boolean {
        if (value == null) return true
        return ALLOWED_TYPES.any { it.isInstance(value) }
    }

    /**
     * 타입이 숫자 타입인지 확인합니다.
     *
     * @param type 확인할 타입
     * @return 숫자 타입이면 true
     */
    fun isNumericType(type: KClass<*>): Boolean {
        return NUMBER_TYPE_PRIORITY.containsKey(type)
    }

    /**
     * 값이 숫자인지 확인합니다.
     *
     * @param value 확인할 값
     * @return 숫자이면 true
     */
    fun isNumeric(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Number -> true
            is String -> value.toDoubleOrNull() != null
            is Boolean -> true // Boolean은 숫자로 변환 가능
            else -> false
        }
    }

    /**
     * 값이 정수인지 확인합니다.
     *
     * @param value 확인할 값
     * @return 정수이면 true
     */
    fun isInteger(value: Any?): Boolean {
        return when (value) {
            is Byte, is Short, is Int, is Long -> true
            is Double -> value == value.toLong().toDouble()
            is Float -> value == value.toLong().toFloat()
            is String -> {
                val doubleValue = value.toDoubleOrNull()
                doubleValue != null && doubleValue == doubleValue.toLong().toDouble()
            }
            else -> false
        }
    }

    /**
     * 안전한 나눗셈을 수행합니다.
     *
     * @param dividend 피제수
     * @param divisor 제수
     * @return 나눗셈 결과
     * @throws TypeCoercionException 0으로 나누는 경우
     */
    fun safeDivide(dividend: Any?, divisor: Any?): Double {
        val dividendNum = toDouble(dividend)
        val divisorNum = toDouble(divisor)
        
        if (divisorNum == 0.0) {
            throw TypeCoercionException("0으로 나눌 수 없습니다")
        }
        
        val result = dividendNum / divisorNum
        if (!result.isFinite()) {
            throw TypeCoercionException("나눗셈 결과가 유한하지 않습니다: $result")
        }
        
        return result
    }

    // Private helper methods

    /**
     * 값의 실제 타입을 반환합니다.
     */
    private fun getEffectiveType(value: Any?): KClass<*> {
        return when (value) {
            null -> Any::class
            is Boolean -> Boolean::class
            is Byte -> Byte::class
            is Short -> Short::class
            is Int -> Int::class
            is Long -> Long::class
            is Float -> Float::class
            is Double -> Double::class
            is String -> String::class
            else -> value::class
        }
    }

    /**
     * 더 높은 우선순위의 숫자 타입을 반환합니다.
     */
    private fun getHigherPriorityNumericType(type1: KClass<*>, type2: KClass<*>): KClass<*> {
        val priority1 = NUMBER_TYPE_PRIORITY[type1] ?: 0
        val priority2 = NUMBER_TYPE_PRIORITY[type2] ?: 0
        
        return if (priority1 >= priority2) type1 else type2
    }

    /**
     * 타입 강제 변환 예외 클래스입니다.
     */
    class TypeCoercionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "allowedTypes" to ALLOWED_TYPES.map { it.simpleName },
        "numericTypePriority" to NUMBER_TYPE_PRIORITY.mapKeys { it.key.simpleName },
        "strictMode" to false,
        "allowBooleanToNumber" to true,
        "allowStringToNumber" to true,
        "allowImplicitConversion" to true
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "TypeCoercionPolicy",
        "supportedTypes" to ALLOWED_TYPES.size,
        "numericTypes" to NUMBER_TYPE_PRIORITY.size,
        "conversionRules" to listOf(
            "numeric_promotion",
            "boolean_to_numeric",
            "any_to_string",
            "safe_division"
        )
    )
}