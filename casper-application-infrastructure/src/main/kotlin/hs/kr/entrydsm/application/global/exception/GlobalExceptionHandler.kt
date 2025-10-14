package hs.kr.entrydsm.application.global.exception

import hs.kr.entrydsm.application.domain.application.exception.ApplicationAlreadySubmittedException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationCannotCancelException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationValidationException
import hs.kr.entrydsm.application.domain.application.exception.InvalidApplicationTypeException
import hs.kr.entrydsm.application.domain.application.exception.ScoreCalculationException
import hs.kr.entrydsm.application.global.error.ErrorDetail
import hs.kr.entrydsm.application.global.error.ErrorResponse
import hs.kr.entrydsm.global.exception.WebException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    // ===== Application 관련 예외 처리 =====

    @ExceptionHandler(WebException::class)
    fun handleWebException(ex: WebException): ResponseEntity<ErrorResponse> {
        log.warn("WebException: {}", ex.message)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "INVALID_EXTENSION",
                        message = ex.message,
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ApplicationNotFoundException::class)
    fun handleApplicationNotFoundException(ex: ApplicationNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn("ApplicationNotFoundException: {}", ex.message)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "APPLICATION_NOT_FOUND",
                        message = ex.message ?: "원서를 찾을 수 없습니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(ApplicationAlreadySubmittedException::class)
    fun handleApplicationAlreadySubmittedException(ex: ApplicationAlreadySubmittedException): ResponseEntity<ErrorResponse> {
        log.warn("ApplicationAlreadySubmittedException: {}", ex.message)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "APPLICATION_ALREADY_SUBMITTED",
                        message = ex.message ?: "이미 제출된 원서가 있습니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(ApplicationValidationException::class)
    fun handleApplicationValidationException(ex: ApplicationValidationException): ResponseEntity<ErrorResponse> {
        log.warn("ApplicationValidationException: {}", ex.message)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "APPLICATION_VALIDATION_ERROR",
                        message = ex.message ?: "원서 데이터 검증에 실패했습니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ScoreCalculationException::class)
    fun handleScoreCalculationException(ex: ScoreCalculationException): ResponseEntity<ErrorResponse> {
        log.error("ScoreCalculationException: {}", ex.message, ex)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "SCORE_CALCULATION_ERROR",
                        message = ex.message ?: "점수 계산 중 오류가 발생했습니다",
                        details = mapOf("cause" to (ex.cause?.message ?: "")),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidApplicationTypeException::class)
    fun handleInvalidApplicationTypeException(ex: InvalidApplicationTypeException): ResponseEntity<ErrorResponse> {
        log.warn("InvalidApplicationTypeException: {}", ex.message)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "INVALID_APPLICATION_TYPE",
                        message = ex.message ?: "잘못된 전형 유형 또는 교육 상태입니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ApplicationCannotCancelException::class)
    fun handleApplicationCannotCancelException(ex: ApplicationCannotCancelException): ResponseEntity<ErrorResponse> {
        log.warn("ApplicationCannotCancelException: {}", ex.message)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "APPLICATION_CANNOT_CANCEL",
                        message = ex.message ?: "제출된 원서만 취소할 수 있습니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(ex: ApplicationException): ResponseEntity<ErrorResponse> {
        log.error("ApplicationException: {}", ex.message, ex)
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "APPLICATION_ERROR",
                        message = ex.message ?: "원서 처리 중 오류가 발생했습니다",
                        details = mapOf("exceptionType" to ex.javaClass.simpleName),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    // ===== 일반 예외 처리 =====

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "VALIDATION_ERROR",
                        message = ex.message ?: "잘못된 요청 파라미터입니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "RESOURCE_NOT_FOUND",
                        message = ex.message ?: "요청한 리소스를 찾을 수 없습니다",
                        details = null,
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(ClassCastException::class)
    fun handleClassCastException(ex: ClassCastException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "DATA_TYPE_ERROR",
                        message = "데이터 타입이 올바르지 않습니다",
                        details = mapOf("exception" to (ex.message ?: "")),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(ex: NullPointerException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "NULL_VALUE_ERROR",
                        message = "필수 값이 누락되었습니다",
                        details = mapOf("exception" to (ex.message ?: "")),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(NumberFormatException::class)
    fun handleNumberFormatException(ex: NumberFormatException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "NUMBER_FORMAT_ERROR",
                        message = "숫자 형식이 올바르지 않습니다",
                        details = mapOf("exception" to (ex.message ?: "")),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "RUNTIME_ERROR",
                        message = ex.message ?: "실행 중 오류가 발생했습니다",
                        details = mapOf("exceptionType" to ex.javaClass.simpleName),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                success = false,
                error =
                    ErrorDetail(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 내부 오류가 발생했습니다",
                        details = mapOf("exceptionType" to ex.javaClass.simpleName),
                    ),
                timestamp = LocalDateTime.now().toString(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
