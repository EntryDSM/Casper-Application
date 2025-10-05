package hs.kr.entrydsm.application.domain.application.exception

/**
 * 입학원서 관련 예외의 기본 클래스
 */
sealed class ApplicationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 원서를 찾을 수 없을 때 발생하는 예외
 */
class ApplicationNotFoundException(
    message: String = "원서를 찾을 수 없습니다",
    cause: Throwable? = null,
) : ApplicationException(message, cause)

/**
 * 이미 제출된 원서가 있을 때 발생하는 예외
 */
class ApplicationAlreadySubmittedException(
    message: String = "이미 제출된 원서가 있습니다",
    cause: Throwable? = null,
) : ApplicationException(message, cause)

/**
 * 원서 데이터 검증 실패 시 발생하는 예외
 */
class ApplicationValidationException(
    message: String,
    cause: Throwable? = null,
) : ApplicationException(message, cause)

/**
 * 점수 계산 실패 시 발생하는 예외
 */
class ScoreCalculationException(
    message: String,
    cause: Throwable? = null,
) : ApplicationException(message, cause)

/**
 * 원서 데이터 변환 실패 시 발생하는 예외
 */
class ApplicationDataConversionException(
    message: String,
    cause: Throwable? = null,
) : ApplicationException(message, cause)

/**
 * 잘못된 전형 유형 또는 교육 상태 시 발생하는 예외
 */
class InvalidApplicationTypeException(
    message: String,
    cause: Throwable? = null,
) : ApplicationException(message, cause)
