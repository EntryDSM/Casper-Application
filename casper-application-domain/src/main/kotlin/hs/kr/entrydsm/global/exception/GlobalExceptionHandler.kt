package hs.kr.entrydsm.global.exception

import hs.kr.entrydsm.global.constants.ErrorCodes

/**
 * 시스템 전역 예외 처리를 담당하는 핸들러입니다.
 *
 * 모든 표준 예외를 도메인 예외로 변환하여 일관된 에러 처리를 제공합니다.
 * 하드코딩된 에러 메시지 대신 중앙화된 에러 코드를 사용합니다.
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
object GlobalExceptionHandler {

    // 에러 메시지 상수
    const val MSG_INVALID_NUMBER_FORMAT = "숫자 형식이 올바르지 않습니다"
    const val MSG_INVALID_ARGUMENT = "잘못된 인수입니다"
    const val MSG_ILLEGAL_STATE = "잘못된 상태입니다"
    const val MSG_NULL_POINTER = "null 값에 접근했습니다"
    const val MSG_ARITHMETIC_ERROR = "산술 연산 오류"
    const val MSG_TYPE_MISMATCH = "타입 불일치"
    const val MSG_INDEX_OUT_OF_BOUNDS = "인덱스 범위 초과"
    const val MSG_STACK_OVERFLOW = "스택 오버플로우"
    const val MSG_UNKNOWN_ERROR = "예상치 못한 오류가 발생했습니다"

    // 기타 상수
    const val CONTEXT_UNKNOWN = "Unknown"
    const val DEFAULT_ERROR_MESSAGE = "Unknown error"
    const val DEFAULT_CAUSE_MESSAGE = "Unknown cause"
    const val ZERO_KEYWORD = "zero"

    // 맵 키 상수
    const val KEY_ERROR_CODE = "errorCode"
    const val KEY_MESSAGE = "message"
    const val KEY_TYPE = "type"
    const val KEY_DOMAIN = "domain"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_CONTEXT = "context"
    const val KEY_ROOT_CAUSE = "rootCause"
    const val KEY_INPUT = "input"

    /**
     * 표준 예외를 도메인 예외로 변환합니다.
     */
    fun handleException(throwable: Throwable, context: String = CONTEXT_UNKNOWN): DomainException {
        return when (throwable) {
            is DomainException -> throwable
            is NumberFormatException -> createNumberFormatException(throwable, context)
            is IllegalArgumentException -> createIllegalArgumentException(throwable, context)
            is IllegalStateException -> createIllegalStateException(throwable, context)
            is NullPointerException -> createNullPointerException(throwable, context)
            is ArithmeticException -> createArithmeticException(throwable, context)
            is ClassCastException -> createTypeMismatchException(throwable, context)
            is IndexOutOfBoundsException -> createIndexOutOfBoundsException(throwable, context)
            is StackOverflowError -> createStackOverflowException(throwable, context)
            else -> createUnknownException(throwable, context)
        }
    }

    /**
     * 예외 정보를 맵으로 변환합니다.
     */
    fun mapExceptionToInfo(throwable: Throwable): Map<String, Any> {
        val domainException = when (throwable) {
            is DomainException -> throwable
            else -> handleException(throwable)
        }

        return mapOf(
            KEY_ERROR_CODE to domainException.errorCode.code,
            KEY_MESSAGE to (domainException.message ?: DEFAULT_ERROR_MESSAGE),
            KEY_TYPE to domainException.javaClass.simpleName,
            KEY_DOMAIN to domainException.errorCode.code.substringBefore("0"),
            KEY_TIMESTAMP to System.currentTimeMillis(),
            KEY_CONTEXT to domainException.context,
            KEY_ROOT_CAUSE to (getRootCause(throwable).message ?: DEFAULT_CAUSE_MESSAGE)
        )
    }

    // Private helper methods

    private fun createNumberFormatException(
        throwable: NumberFormatException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Lexer.INVALID_NUMBER_FORMAT,
            message = "$MSG_INVALID_NUMBER_FORMAT: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context, KEY_INPUT to (throwable.message ?: ""))
        )
    }

    private fun createIllegalArgumentException(
        throwable: IllegalArgumentException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.INVALID_ARGUMENT,
            message = "$MSG_INVALID_ARGUMENT: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createIllegalStateException(
        throwable: IllegalStateException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.ILLEGAL_STATE,
            message = "$MSG_ILLEGAL_STATE: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createNullPointerException(
        throwable: NullPointerException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.NULL_POINTER,
            message = "$MSG_NULL_POINTER: ${throwable.message ?: ""}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createArithmeticException(
        throwable: ArithmeticException,
        context: String
    ): DomainException {
        val errorCode = if (throwable.message?.contains(ZERO_KEYWORD) == true) {
            ErrorCodes.Evaluator.DIVISION_BY_ZERO
        } else {
            ErrorCodes.Evaluator.ARITHMETIC_OVERFLOW
        }

        return DomainException(
            errorCode = errorCode,
            message = "$MSG_ARITHMETIC_ERROR: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createTypeMismatchException(
        throwable: ClassCastException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Evaluator.TYPE_MISMATCH,
            message = "$MSG_TYPE_MISMATCH: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createIndexOutOfBoundsException(
        throwable: IndexOutOfBoundsException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Parser.UNEXPECTED_EOF,
            message = "$MSG_INDEX_OUT_OF_BOUNDS: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createStackOverflowException(
        throwable: StackOverflowError,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.AST.MAX_DEPTH_EXCEEDED,
            message = "$MSG_STACK_OVERFLOW: ${throwable.message ?: ""}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context)
        )
    }

    private fun createUnknownException(
        throwable: Throwable,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.UNKNOWN_ERROR,
            message = "$MSG_UNKNOWN_ERROR: ${throwable.message}",
            cause = throwable,
            context = mapOf(KEY_CONTEXT to context, KEY_TYPE to throwable.javaClass.simpleName)
        )
    }

    private fun getRootCause(throwable: Throwable): Throwable {
        var cause = throwable
        while (cause.cause != null && cause.cause != cause) {
            cause = cause.cause!!
        }
        return cause
    }
}