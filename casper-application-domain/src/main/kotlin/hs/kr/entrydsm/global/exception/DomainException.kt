package hs.kr.entrydsm.global.exception

/**
 * 도메인 계층에서 발생하는 모든 예외의 기본 클래스입니다.
 *
 * DDD(Domain-Driven Design) 아키텍처에서 도메인 규칙 위반이나 비즈니스 로직 오류 등
 * 도메인 계층에서 발생할 수 있는 예외 상황을 처리하기 위한 기본 예외 클래스입니다.
 * 모든 도메인 특화 예외는 이 클래스를 상속받아 구현됩니다.
 *
 * @property errorCode 발생한 오류의 코드 정보
 * @property message 오류 메시지 (기본값: errorCode.description)
 * @property cause 원인이 되는 예외 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
open class DomainException(
    val errorCode: ErrorCode,
    message: String = errorCode.description,
    cause: Throwable? = null,
    val context: Map<String, Any> = emptyMap()
) : RuntimeException(message, cause) {

    /**
     * 오류 코드를 반환합니다.
     *
     * @return 오류 코드 문자열 (예: LEX001, PAR002)
     */
    fun getCode(): String = errorCode.code

    /**
     * 오류가 발생한 도메인 접두사를 반환합니다.
     *
     * @return 도메인 접두사 (예: LEX, PAR, AST)
     */
    fun getDomain(): String = errorCode.getDomainPrefix()

    /**
     * 오류 번호를 반환합니다.
     *
     * @return 오류 번호 (예: 001, 002)
     */
    fun getErrorNumber(): String = errorCode.getErrorNumber()

    /**
     * 특정 도메인에서 발생한 오류인지 확인합니다.
     *
     * @param domainPrefix 확인할 도메인 접두사
     * @return 해당 도메인 오류이면 true, 아니면 false
     */
    fun isFromDomain(domainPrefix: String): Boolean = errorCode.belongsToDomain(domainPrefix)

    /**
     * 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 오류 코드, 메시지, 도메인 정보가 포함된 맵
     */
    fun toErrorInfo(): Map<String, String> = mapOf(
        "code" to getCode(),
        "message" to (message ?: ""),
        "domain" to getDomain(),
        "errorNumber" to getErrorNumber()
    )

    override fun toString(): String {
        return "${this::class.simpleName}(code=${getCode()}, domain=${getDomain()}, message=$message)"
    }
}