package hs.kr.entrydsm.domain.util

import kotlin.reflect.KClass

/**
 * 타입 관련 유틸리티 클래스입니다.
 *
 * 타입 검증, 호환성 확인, 변환 등의 공통 기능을 제공합니다.
 * 여러 도메인에서 중복되던 타입 관련 로직을 중앙화하여 
 * 코드 중복을 제거하고 일관성을 보장합니다.
 *
 * @author kangeunchan
 * @since 2025.08.06
 */
object TypeUtils {
    
    /**
     * 숫자 타입들을 정의하는 상수입니다.
     * Kotlin의 모든 숫자 타입을 포함합니다.
     */
    val NUMERIC_TYPES = setOf(
        Byte::class,
        Short::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class
    )
    
    /**
     * 주어진 타입이 숫자 타입인지 확인합니다.
     *
     * @param type 확인할 타입
     * @return 숫자 타입이면 true, 아니면 false
     */
    fun isNumericType(type: KClass<*>): Boolean {
        return NUMERIC_TYPES.contains(type)
    }
    
    /**
     * 주어진 객체가 숫자 타입인지 확인합니다.
     *
     * @param value 확인할 객체
     * @return 숫자 타입이면 true, 아니면 false
     */
    fun isNumericValue(value: Any?): Boolean {
        return when (value) {
            is Byte, is Short, is Int, is Long, is Float, is Double -> true
            else -> false
        }
    }
    
    /**
     * 두 타입이 모두 숫자 타입인지 확인합니다.
     *
     * @param type1 첫 번째 타입
     * @param type2 두 번째 타입
     * @return 둘 다 숫자 타입이면 true, 아니면 false
     */
    fun areBothNumericTypes(type1: KClass<*>, type2: KClass<*>): Boolean {
        return isNumericType(type1) && isNumericType(type2)
    }
    
    /**
     * 주어진 타입이 불린 타입인지 확인합니다.
     *
     * @param type 확인할 타입
     * @return 불린 타입이면 true, 아니면 false
     */
    fun isBooleanType(type: KClass<*>): Boolean {
        return type == Boolean::class
    }
    
    /**
     * 주어진 타입이 문자열 타입인지 확인합니다.
     *
     * @param type 확인할 타입
     * @return 문자열 타입이면 true, 아니면 false
     */
    fun isStringType(type: KClass<*>): Boolean {
        return type == String::class
    }
    
    /**
     * 숫자 타입의 우선순위를 반환합니다.
     * 타입 변환 시 더 높은 우선순위 타입으로 변환됩니다.
     *
     * @param type 숫자 타입
     * @return 우선순위 (높을수록 우선)
     */
    fun getNumericTypePriority(type: KClass<*>): Int {
        return when (type) {
            Byte::class -> 1
            Short::class -> 2
            Int::class -> 3
            Long::class -> 4
            Float::class -> 5
            Double::class -> 6
            else -> 0 // 숫자 타입이 아닌 경우
        }
    }
    
    /**
     * 두 숫자 타입 중 더 높은 우선순위 타입을 반환합니다.
     *
     * @param type1 첫 번째 숫자 타입
     * @param type2 두 번째 숫자 타입
     * @return 더 높은 우선순위의 타입
     */
    fun getHigherPriorityType(type1: KClass<*>, type2: KClass<*>): KClass<*> {
        if (!isNumericType(type1) || !isNumericType(type2)) {
            return Any::class
        }
        
        val priority1 = getNumericTypePriority(type1)
        val priority2 = getNumericTypePriority(type2)
        
        return if (priority1 >= priority2) type1 else type2
    }
}