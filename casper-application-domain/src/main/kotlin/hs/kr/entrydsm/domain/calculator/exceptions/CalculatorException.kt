package hs.kr.entrydsm.domain.calculator.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Calculator 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * 수식 처리, 계산 단계 실행, 변수 관리 등의 계산기 핵심 기능에서
 * 발생하는 오류를 처리합니다.
 *
 * @property formula 오류와 관련된 수식 (선택사항)
 * @property step 오류가 발생한 계산 단계 (선택사항)
 * @property variableCount 변수 개수 (선택사항)
 * @property maxAllowed 허용된 최대값 (선택사항)
 * @property missingVariables 누락된 변수 리스트 (선택사항)
 * @property reason 사유 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class CalculatorException(
    errorCode: ErrorCode,
    val formula: String? = null,
    val step: Int? = null,
    val variableCount: Int? = null,
    val maxAllowed: Int? = null,
    val missingVariables: List<String> = emptyList(),
    val reason: String? = null,
    message: String = buildCalculatorMessage(errorCode, formula, step, variableCount, maxAllowed, missingVariables, reason),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * Calculator 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param formula 수식
         * @param step 계산 단계
         * @param variableCount 변수 개수
         * @param maxAllowed 최대 허용값
         * @param missingVariables 누락된 변수들
         * @param reason 사유
         * @return 구성된 메시지
         */
        private fun buildCalculatorMessage(
            errorCode: ErrorCode,
            formula: String?,
            step: Int?,
            variableCount: Int?,
            maxAllowed: Int?,
            missingVariables: List<String>,
            reason: String?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            formula?.let { details.add("수식: $it") }
            step?.let { details.add("단계: $it") }
            if (variableCount != null && maxAllowed != null) {
                details.add("변수: $variableCount (최대: $maxAllowed)")
            } else {
                variableCount?.let { details.add("변수개수: $it") }
                maxAllowed?.let { details.add("최대허용: $it") }
            }
            if (missingVariables.isNotEmpty()) {
                details.add("누락변수: ${missingVariables.joinToString(", ")}")
            }
            
            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 빈 수식 오류를 생성합니다.
         *
         * @return CalculatorException 인스턴스
         */
        fun emptyFormula(): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.EMPTY_FORMULA
            )
        }

        /**
         * 수식이 너무 긴 오류를 생성합니다.
         *
         * @param formula 너무 긴 수식
         * @param maxLength 최대 허용 길이
         * @return CalculatorException 인스턴스
         */
        fun formulaTooLong(formula: String, maxLength: Int): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.FORMULA_TOO_LONG,
                formula = formula.take(50) + if (formula.length > 50) "..." else "",
                maxAllowed = maxLength
            )
        }

        /**
         * 빈 계산 단계 오류를 생성합니다.
         *
         * @return CalculatorException 인스턴스
         */
        fun emptySteps(): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.EMPTY_STEPS
            )
        }

        /**
         * 계산 단계가 너무 많은 오류를 생성합니다.
         *
         * @param stepCount 실제 단계 수
         * @param maxSteps 최대 허용 단계 수
         * @return CalculatorException 인스턴스
         */
        fun tooManySteps(stepCount: Int, maxSteps: Int): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.TOO_MANY_STEPS,
                variableCount = stepCount,
                maxAllowed = maxSteps
            )
        }

        /**
         * 변수가 너무 많은 오류를 생성합니다.
         *
         * @param variableCount 실제 변수 개수
         * @param maxVariables 최대 허용 변수 개수
         * @return CalculatorException 인스턴스
         */
        fun tooManyVariables(variableCount: Int, maxVariables: Int): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.TOO_MANY_VARIABLES,
                variableCount = variableCount,
                maxAllowed = maxVariables
            )
        }

        /**
         * 필수 변수 누락 오류를 생성합니다.
         *
         * @param missingVariables 누락된 변수들
         * @return CalculatorException 인스턴스
         */
        fun missingVariables(missingVariables: List<String>): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.MISSING_VARIABLES,
                missingVariables = missingVariables
            )
        }

        /**
         * 단계 실행 오류를 생성합니다.
         *
         * @param step 실행 실패한 단계
         * @param cause 원인 예외
         * @return CalculatorException 인스턴스
         */
        fun stepExecutionError(step: Int, cause: Throwable? = null): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.STEP_EXECUTION_ERROR,
                step = step,
                cause = cause
            )
        }

        /**
         * 수식 검증 오류를 생성합니다.
         *
         * @param formula 검증 실패한 수식
         * @param cause 원인 예외
         * @return CalculatorException 인스턴스
         */
        fun formulaValidationError(formula: String, cause: Throwable? = null): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.FORMULA_VALIDATION_ERROR,
                formula = formula,
                cause = cause
            )
        }

        /**
         * 변수 추출 오류를 생성합니다.
         *
         * @param formula 변수 추출 실패한 수식
         * @param cause 원인 예외
         * @return CalculatorException 인스턴스
         */
        fun variableExtractionError(formula: String, cause: Throwable? = null): CalculatorException {
            return CalculatorException(
                errorCode = ErrorCode.VARIABLE_EXTRACTION_ERROR,
                formula = formula,
                cause = cause
            )
        }

        /**
         * 세션 ID가 비어 있을 때의 오류를 생성합니다.
         *
         * @param actual 입력된 세션 ID
         * @return CalculatorException 인스턴스
         */
        fun sessionIdEmpty(actual: String?): CalculatorException =
            CalculatorException(
                errorCode = ErrorCode.SESSION_ID_EMPTY,
                reason = "actual=${actual ?: "null"}"
            )

        /**
         * 계산 이력 개수가 최대 크기를 초과했을 때의 오류를 생성합니다.
         *
         * @param actual 현재 계산 이력 크기
         * @param max 허용되는 최대 크기
         * @return CalculatorException 인스턴스
         */
        fun calculationHistoryTooLarge(actual: Int, max: Int): CalculatorException =
            CalculatorException(
                errorCode = ErrorCode.CALCULATION_HISTORY_TOO_LARGE,
                reason = "actual=$actual, max=$max"
            )

        /**
         * 변수 이름이 비어 있을 때의 오류를 생성합니다.
         *
         * @param actual 입력된 변수명
         * @return CalculatorException 인스턴스
         */
        fun variableNameEmpty(actual: String?): CalculatorException =
            CalculatorException(
                errorCode = ErrorCode.VARIABLE_NAME_EMPTY,
                reason = "actual=${actual ?: "null"}"
            )

        /**
         * 사용자 ID가 비어 있을 때의 오류를 생성합니다.
         *
         * @param actual 입력된 사용자 ID
         * @return CalculatorException 인스턴스
         */
        fun userIdEmpty(actual: String?): CalculatorException =
            CalculatorException(
                errorCode = ErrorCode.USER_ID_EMPTY,
                reason = "actual=${actual ?: "null"}"
            )
    }

    /**
     * Calculator 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 수식, 단계, 변수 정보가 포함된 맵
     */
    fun getCalculatorInfo(): Map<String, Any?> {
        val info = mutableMapOf<String, Any?>()
        
        formula?.let { info["formula"] = it }
        step?.let { info["step"] = it }
        variableCount?.let { info["variableCount"] = it }
        maxAllowed?.let { info["maxAllowed"] = it }
        if (missingVariables.isNotEmpty()) { info["missingVariables"] = missingVariables }
        
        return info
    }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 Calculator 정보가 결합된 맵
     */
    fun getFullErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val calculatorInfo = getCalculatorInfo()
        
        calculatorInfo.forEach { (key, value) ->
            when (value) {
                is List<*> -> baseInfo[key] = value.joinToString(", ")
                else -> baseInfo[key] = value?.toString() ?: ""
            }
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val calculatorDetails = getCalculatorInfo()
        return if (calculatorDetails.isNotEmpty()) {
            "${super.toString()}, calculator=${calculatorDetails}"
        } else {
            super.toString()
        }
    }
}