package hs.kr.entrydsm.global.constants.error

/**
 * 표현기 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object ExpresserErrorCodes {
    const val FORMATTING_FAILED = "X6001"
    const val INVALID_FORMAT_STYLE = "X6002"
    const val EXPRESSION_TOO_COMPLEX = "X6003"
    const val FORMATTING_TIMEOUT = "X6004"
    const val UNSUPPORTED_NODE_TYPE = "X6005"
    const val FORMAT_VALIDATION_FAILED = "X6006"
    const val STYLE_CONFIGURATION_ERROR = "X6007"
    const val OUTPUT_BUFFER_OVERFLOW = "X6008"
    const val ENCODING_ERROR = "X6009"
    const val FORMAT_TEMPLATE_ERROR = "X6010"
}