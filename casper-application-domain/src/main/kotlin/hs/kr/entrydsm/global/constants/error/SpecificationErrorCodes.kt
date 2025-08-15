package hs.kr.entrydsm.global.constants.error

/**
 * 명세 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object SpecificationErrorCodes {
    const val SPECIFICATION_FAILED = "S8001"
    const val INVALID_SPECIFICATION = "S8002"
    const val SPECIFICATION_COMPOSITION_FAILED = "S8003"
    const val SPECIFICATION_EVALUATION_ERROR = "S8004"
    const val UNSUPPORTED_SPECIFICATION_TYPE = "S8005"
    const val SPECIFICATION_TIMEOUT = "S8006"
    const val SPECIFICATION_DEPENDENCY_ERROR = "S8007"
    const val SPECIFICATION_CACHE_ERROR = "S8008"
    const val COMPLEX_SPECIFICATION_LIMIT = "S8009"
    const val SPECIFICATION_VALIDATION_FAILED = "S8010"
}