package hs.kr.entrydsm.global.constants.error

/**
 * 팩토리 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object FactoryErrorCodes {
    const val CREATION_FAILED = "F7001"
    const val INVALID_FACTORY_TYPE = "F7002"
    const val FACTORY_CONFIGURATION_ERROR = "F7003"
    const val DEPENDENCY_INJECTION_FAILED = "F7004"
    const val FACTORY_CACHE_ERROR = "F7005"
    const val CIRCULAR_DEPENDENCY = "F7006"
    const val FACTORY_STATE_ERROR = "F7007"
    const val INVALID_FACTORY_CONTEXT = "F7008"
    const val FACTORY_INITIALIZATION_FAILED = "F7009"
    const val FACTORY_TIMEOUT = "F7010"
}