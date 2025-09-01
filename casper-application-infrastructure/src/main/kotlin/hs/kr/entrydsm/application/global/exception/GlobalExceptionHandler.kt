package hs.kr.entrydsm.application.global.exception

import hs.kr.entrydsm.application.global.error.ErrorDetail
import hs.kr.entrydsm.application.global.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "VALIDATION_ERROR",
                message = ex.message ?: "잘못된 요청 파라미터입니다",
                details = null
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "RESOURCE_NOT_FOUND",
                message = ex.message ?: "요청한 리소스를 찾을 수 없습니다",
                details = null
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    @ExceptionHandler(ClassCastException::class)
    fun handleClassCastException(ex: ClassCastException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "DATA_TYPE_ERROR",
                message = "데이터 타입이 올바르지 않습니다",
                details = mapOf("exception" to (ex.message ?: ""))
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(ex: NullPointerException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "NULL_VALUE_ERROR",
                message = "필수 값이 누락되었습니다",
                details = mapOf("exception" to (ex.message ?: ""))
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(NumberFormatException::class)
    fun handleNumberFormatException(ex: NumberFormatException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "NUMBER_FORMAT_ERROR",
                message = "숫자 형식이 올바르지 않습니다",
                details = mapOf("exception" to (ex.message ?: ""))
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "RUNTIME_ERROR",
                message = ex.message ?: "실행 중 오류가 발생했습니다",
                details = mapOf("exceptionType" to ex.javaClass.simpleName)
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            success = false,
            error = ErrorDetail(
                code = "INTERNAL_SERVER_ERROR",
                message = "서버 내부 오류가 발생했습니다",
                details = mapOf("exceptionType" to ex.javaClass.simpleName)
            ),
            timestamp = LocalDateTime.now().toString()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
}