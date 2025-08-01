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

    /**
     * 표준 예외를 도메인 예외로 변환합니다.
     */
    fun handleException(throwable: Throwable, context: String = "Unknown"): DomainException {
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
            "errorCode" to domainException.errorCode.code,
            "message" to (domainException.message ?: "Unknown error"),
            "type" to domainException.javaClass.simpleName,
            "domain" to domainException.errorCode.code.substringBefore("0"),
            "timestamp" to System.currentTimeMillis(),
            "context" to domainException.context,
            "rootCause" to (getRootCause(throwable).message ?: "Unknown cause")
        )
    }
    
    // Private helper methods
    
    private fun createNumberFormatException(
        throwable: NumberFormatException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Lexer.INVALID_NUMBER_FORMAT,
            message = "숫자 형식이 올바르지 않습니다: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context, "input" to (throwable.message ?: ""))
        )
    }
    
    private fun createIllegalArgumentException(
        throwable: IllegalArgumentException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.INVALID_ARGUMENT,
            message = "잘못된 인수입니다: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createIllegalStateException(
        throwable: IllegalStateException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.ILLEGAL_STATE,
            message = "잘못된 상태입니다: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createNullPointerException(
        throwable: NullPointerException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.NULL_POINTER,
            message = "null 값에 접근했습니다: ${throwable.message ?: ""}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createArithmeticException(
        throwable: ArithmeticException,
        context: String
    ): DomainException {
        val errorCode = if (throwable.message?.contains("zero") == true) {
            ErrorCodes.Evaluator.DIVISION_BY_ZERO
        } else {
            ErrorCodes.Evaluator.ARITHMETIC_OVERFLOW
        }
        
        return DomainException(
            errorCode = errorCode,
            message = "산술 연산 오류: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createTypeMismatchException(
        throwable: ClassCastException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Evaluator.TYPE_MISMATCH,
            message = "타입 불일치: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createIndexOutOfBoundsException(
        throwable: IndexOutOfBoundsException,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Parser.UNEXPECTED_EOF,
            message = "인덱스 범위 초과: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createStackOverflowException(
        throwable: StackOverflowError,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.AST.MAX_DEPTH_EXCEEDED,
            message = "스택 오버플로우: ${throwable.message ?: ""}",
            cause = throwable,
            context = mapOf("context" to context)
        )
    }
    
    private fun createUnknownException(
        throwable: Throwable,
        context: String
    ): DomainException {
        return DomainException(
            errorCode = ErrorCodes.Common.UNKNOWN_ERROR,
            message = "예상치 못한 오류가 발생했습니다: ${throwable.message}",
            cause = throwable,
            context = mapOf("context" to context, "type" to throwable.javaClass.simpleName)
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