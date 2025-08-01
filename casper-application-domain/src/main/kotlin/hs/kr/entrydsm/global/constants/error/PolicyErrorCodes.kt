package hs.kr.entrydsm.global.constants.error

/**
 * 정책 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object PolicyErrorCodes {
    const val POLICY_VIOLATION = "P9001"
    const val INVALID_POLICY = "P9002"
    const val POLICY_CONFLICT = "P9003"
    const val POLICY_EVALUATION_FAILED = "P9004"
    const val UNSUPPORTED_POLICY_TYPE = "P9005"
    const val POLICY_TIMEOUT = "P9006"
    const val POLICY_DEPENDENCY_ERROR = "P9007"
    const val POLICY_CONFIGURATION_ERROR = "P9008"
    const val POLICY_ENFORCEMENT_FAILED = "P9009"
    const val POLICY_CHAIN_ERROR = "P9010"
}