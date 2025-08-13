package hs.kr.entrydsm.domain.evaluator.values

import hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException
import java.time.LocalDateTime

/**
 * 변수 바인딩을 나타내는 값 객체입니다.
 *
 * 변수명과 값의 바인딩 정보를 안전하게 관리하며,
 * 타입 검증과 변환 기능을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
data class VariableBinding private constructor(
    val name: String,
    val value: Any?,
    val type: VariableType,
    val isReadonly: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    init {
        if (name.isBlank()) throw EvaluatorException.invalidVariableName(name)
        if (!isValidVariableName(name)) throw EvaluatorException.invalidVariableName(name)
        if (!isValidValue(value, type)) throw EvaluatorException.unsupportedType(type.toString(), value)
    }
    
    /**
     * 숫자 값을 반환합니다.
     *
     * @return Double 숫자 값
     * @throws EvaluatorException 값이 숫자가 아닌 경우
     */
    fun asNumber(): Double {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            else -> throw EvaluatorException.numberConversionError(value)
        }
    }
    
    /**
     * 불리언 값을 반환합니다.
     *
     * @return Boolean 값
     * @throws EvaluatorException 값이 불리언으로 변환할 수 없는 경우
     */
    fun asBoolean(): Boolean {
        return when (value) {
            is Boolean -> value
            is Double -> value != 0.0
            is Int -> value != 0
            else -> throw EvaluatorException.unsupportedType("Boolean", value)
        }
    }
    
    /**
     * 문자열 값을 반환합니다.
     */
    fun asString(): String {
        return value?.toString() ?: "null"
    }
    
    /**
     * 값이 숫자인지 확인합니다.
     */
    fun isNumeric(): Boolean = type == VariableType.NUMBER
    
    /**
     * 값이 불리언인지 확인합니다.
     */
    fun isBoolean(): Boolean = type == VariableType.BOOLEAN
    
    /**
     * 값이 문자열인지 확인합니다.
     */
    fun isString(): Boolean = type == VariableType.STRING
    
    /**
     * 값이 null인지 확인합니다.
     */
    fun isNull(): Boolean = type == VariableType.NULL || value == null
    
    
    /**
     * 변수를 새로운 값으로 바인딩합니다.
     *
     * @param newValue 새로운 값
     * @return 새로운 VariableBinding
     * @throws EvaluatorException 읽기 전용 변수를 수정하려는 경우
     */
    fun withValue(newValue: Any?): VariableBinding {
        if (isReadonly) throw EvaluatorException.invalidVariableName("읽기 전용 변수는 수정할 수 없습니다: $name")
        val newType = determineType(newValue)
        return VariableBinding(
            name = name,
            value = newValue,
            type = newType,
            isReadonly = isReadonly,
            createdAt = createdAt
        )
    }
    
    /**
     * 읽기 전용 변수로 변환합니다.
     */
    fun asReadonly(): VariableBinding {
        return if (isReadonly) this else copy(isReadonly = true)
    }
    
    /**
     * 변수 정보를 반환합니다.
     */
    fun getInfo(): VariableInfo {
        return VariableInfo(
            name = name,
            type = type,
            isReadonly = isReadonly,
            hasValue = value != null,
            createdAt = createdAt
        )
    }
    
    
    companion object {
        /**
         * 숫자 변수를 생성합니다.
         */
        fun ofNumber(name: String, value: Double, isReadonly: Boolean = false): VariableBinding {
            return VariableBinding(
                name = name,
                value = value,
                type = VariableType.NUMBER,
                isReadonly = isReadonly
            )
        }
        
        /**
         * 불리언 변수를 생성합니다.
         */
        fun ofBoolean(name: String, value: Boolean, isReadonly: Boolean = false): VariableBinding {
            return VariableBinding(
                name = name,
                value = value,
                type = VariableType.BOOLEAN,
                isReadonly = isReadonly
            )
        }
        
        /**
         * 문자열 변수를 생성합니다.
         */
        fun ofString(name: String, value: String, isReadonly: Boolean = false): VariableBinding {
            return VariableBinding(
                name = name,
                value = value,
                type = VariableType.STRING,
                isReadonly = isReadonly
            )
        }
        
        /**
         * null 변수를 생성합니다.
         */
        fun ofNull(name: String, isReadonly: Boolean = false): VariableBinding {
            return VariableBinding(
                name = name,
                value = null,
                type = VariableType.NULL,
                isReadonly = isReadonly
            )
        }
        
        /**
         * 자동 타입 결정으로 변수를 생성합니다.
         */
        fun of(name: String, value: Any?, isReadonly: Boolean = false): VariableBinding {
            val type = determineType(value)
            return VariableBinding(
                name = name,
                value = value,
                type = type,
                isReadonly = isReadonly
            )
        }
        
        /**
         * 읽기 전용 변수를 생성합니다.
         */
        fun readonly(name: String, value: Any?): VariableBinding {
            return of(name, value, isReadonly = true)
        }
        
        /**
         * 상수 변수를 생성합니다.
         */
        fun constant(name: String, value: Any?): VariableBinding {
            return readonly(name, value)
        }
        
        /**
         * 변수명이 유효한지 확인합니다.
         */
        private fun isValidVariableName(name: String): Boolean {
            if (name.isEmpty()) return false
            if (!name[0].isLetter() && name[0] != '_') return false
            return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
        }
        
        /**
         * 값이 지정된 타입과 일치하는지 확인합니다.
         */
        private fun isValidValue(value: Any?, type: VariableType): Boolean {
            return when (type) {
                VariableType.NUMBER -> value is Double || value is Int || value is Float || value is Long
                VariableType.BOOLEAN -> value is Boolean
                VariableType.STRING -> value is String
                VariableType.NULL -> value == null
            }
        }
        
        /**
         * 값의 타입을 결정합니다.
         */
        private fun determineType(value: Any?): VariableType {
            return when (value) {
                null -> VariableType.NULL
                is Double, is Float, is Int, is Long -> VariableType.NUMBER
                is Boolean -> VariableType.BOOLEAN
                is String -> VariableType.STRING
                else -> VariableType.STRING
            }
        }
        
        /**
         * 기본 수학 상수들을 반환합니다.
         */
        fun getMathConstants(): List<VariableBinding> {
            return listOf(
                readonly("PI", kotlin.math.PI),
                readonly("E", kotlin.math.E),
                readonly("TRUE", true),
                readonly("FALSE", false),
                readonly("NULL", null)
            )
        }
        
        /**
         * 변수 바인딩 맵을 생성합니다.
         */
        fun createBindingMap(bindings: List<VariableBinding>): Map<String, VariableBinding> {
            return bindings.associateBy { it.name }
        }
        
        /**
         * 값 맵에서 변수 바인딩 리스트를 생성합니다.
         */
        fun fromValueMap(values: Map<String, Any?>): List<VariableBinding> {
            return values.map { (name, value) -> of(name, value) }
        }
    }
}