package hs.kr.entrydsm.domain.evaluator.exception

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Evaluator 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * 표현식 평가, 연산자 처리, 함수 호출, 변수 해석, 타입 변환 등의
 * 평가 과정에서 발생하는 오류를 처리합니다.
 *
 * @property operator 오류와 관련된 연산자 (선택사항)
 * @property function 오류와 관련된 함수명 (선택사항)
 * @property variable 오류와 관련된 변수명 (선택사항)
 * @property expectedArgCount 예상된 인수 개수 (선택사항)
 * @property actualArgCount 실제 인수 개수 (선택사항)
 * @property valueType 값의 타입 (선택사항)
 * @property value 오류를 발생시킨 값 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class EvaluatorException(
    errorCode: ErrorCode,
    val operator: String? = null,
    val function: String? = null,
    val variable: String? = null,
    val expectedArgCount: Int? = null,
    val actualArgCount: Int? = null,
    val valueType: String? = null,
    val value: Any? = null,
    message: String = buildEvaluatorMessage(errorCode, operator, function, variable, expectedArgCount, actualArgCount, valueType, value),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * Evaluator 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param operator 연산자
         * @param function 함수명
         * @param variable 변수명
         * @param expectedArgCount 예상 인수 개수
         * @param actualArgCount 실제 인수 개수
         * @param valueType 값 타입
         * @param value 값
         * @return 구성된 메시지
         */
        private fun buildEvaluatorMessage(
            errorCode: ErrorCode,
            operator: String?,
            function: String?,
            variable: String?,
            expectedArgCount: Int?,
            actualArgCount: Int?,
            valueType: String?,
            value: Any?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            operator?.let { details.add("연산자: $it") }
            function?.let { details.add("함수: $it") }
            variable?.let { details.add("변수: $it") }
            if (expectedArgCount != null && actualArgCount != null) {
                details.add("인수: $actualArgCount (예상: $expectedArgCount)")
            }
            valueType?.let { details.add("타입: $it") }
            value?.let { details.add("값: $it") }
            
            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 평가 오류를 생성합니다.
         *
         * @param cause 원인 예외
         * @return EvaluatorException 인스턴스
         */
        fun evaluationError(cause: Throwable? = null): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.EVALUATION_ERROR,
                cause = cause
            )
        }

        /**
         * 0으로 나누기 오류를 생성합니다.
         *
         * @param operator 나누기 연산자
         * @return EvaluatorException 인스턴스
         */
        fun divisionByZero(operator: String = "/"): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.DIVISION_BY_ZERO,
                operator = operator
            )
        }

        /**
         * 정의되지 않은 변수 오류를 생성합니다.
         *
         * @param variable 정의되지 않은 변수명
         * @return EvaluatorException 인스턴스
         */
        fun undefinedVariable(variable: String): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.UNDEFINED_VARIABLE,
                variable = variable
            )
        }

        /**
         * 지원하지 않는 연산자 오류를 생성합니다.
         *
         * @param operator 지원하지 않는 연산자
         * @return EvaluatorException 인스턴스
         */
        fun unsupportedOperator(operator: String): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.UNSUPPORTED_OPERATOR,
                operator = operator
            )
        }

        /**
         * 지원하지 않는 함수 오류를 생성합니다.
         *
         * @param function 지원하지 않는 함수명
         * @return EvaluatorException 인스턴스
         */
        fun unsupportedFunction(function: String): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.UNSUPPORTED_FUNCTION,
                function = function
            )
        }

        /**
         * 잘못된 인수 개수 오류를 생성합니다.
         *
         * @param function 함수명
         * @param expectedCount 예상 인수 개수
         * @param actualCount 실제 인수 개수
         * @return EvaluatorException 인스턴스
         */
        fun wrongArgumentCount(function: String, expectedCount: Int, actualCount: Int): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.WRONG_ARGUMENT_COUNT,
                function = function,
                expectedArgCount = expectedCount,
                actualArgCount = actualCount
            )
        }

        /**
         * 지원하지 않는 타입 오류를 생성합니다.
         *
         * @param valueType 지원하지 않는 타입
         * @param value 해당 값
         * @return EvaluatorException 인스턴스
         */
        fun unsupportedType(valueType: String, value: Any? = null): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.UNSUPPORTED_TYPE,
                valueType = valueType,
                value = value
            )
        }

        /**
         * 숫자 변환 오류를 생성합니다.
         *
         * @param value 변환 실패한 값
         * @param cause 원인 예외
         * @return EvaluatorException 인스턴스
         */
        fun numberConversionError(value: Any?, cause: Throwable? = null): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.NUMBER_CONVERSION_ERROR,
                value = value,
                valueType = value?.javaClass?.simpleName,
                cause = cause
            )
        }

        /**
         * 수학 연산 오류를 생성합니다.
         *
         * @param message 오류 메시지
         * @param cause 원인 예외
         * @return EvaluatorException 인스턴스
         */
        fun mathError(message: String, cause: Throwable? = null): EvaluatorException {
            return EvaluatorException(
                errorCode = ErrorCode.MATH_ERROR,
                cause = cause,
                message = message
            )
        }
    }

    /**
     * Evaluator 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 연산자, 함수, 변수, 인수, 타입, 값 정보가 포함된 맵
     */
    fun getEvaluatorInfo(): Map<String, Any?> {
        val info = mutableMapOf<String, Any?>()
        
        operator?.let { info["operator"] = it }
        function?.let { info["function"] = it }
        variable?.let { info["variable"] = it }
        expectedArgCount?.let { info["expectedArgCount"] = it }
        actualArgCount?.let { info["actualArgCount"] = it }
        valueType?.let { info["valueType"] = it }
        value?.let { info["value"] = it }
        
        return info
    }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 Evaluator 정보가 결합된 맵
     */
    fun toCompleteErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val evaluatorInfo = getEvaluatorInfo()
        
        evaluatorInfo.forEach { (key, value) ->
            baseInfo[key] = value?.toString() ?: ""
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val evaluatorDetails = getEvaluatorInfo()
        return if (evaluatorDetails.isNotEmpty()) {
            "${super.toString()}, evaluator=${evaluatorDetails}"
        } else {
            super.toString()
        }
    }
}