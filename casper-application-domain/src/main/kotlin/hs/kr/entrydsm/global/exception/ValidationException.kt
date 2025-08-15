package hs.kr.entrydsm.global.exception

/**
 * 도메인 객체나 값의 유효성 검사 실패 시 발생하는 예외입니다.
 *
 * 입력 데이터 검증, 비즈니스 규칙 검증, 제약조건 위반 등 도메인에서 정의된
 * 유효성 검사 규칙을 위반했을 때 발생합니다. 검증 실패한 필드 정보와
 * 상세한 오류 내용을 포함하여 클라이언트에게 명확한 피드백을 제공합니다.
 *
 * @property field 검증에 실패한 필드명 (선택사항)
 * @property value 검증에 실패한 값 (선택사항)
 * @property constraint 위반된 제약조건 설명 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class ValidationException(
    errorCode: ErrorCode = ErrorCode.VALIDATION_FAILED,
    val field: String? = null,
    val value: Any? = null,
    val constraint: String? = null,
    message: String = buildValidationMessage(errorCode, field, value, constraint),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * 검증 실패 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param field 검증 실패 필드명
         * @param value 검증 실패 값
         * @param constraint 위반된 제약조건
         * @return 구성된 메시지
         */
        private fun buildValidationMessage(
            errorCode: ErrorCode,
            field: String?,
            value: Any?,
            constraint: String?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            field?.let { details.add("필드: $it") }
            value?.let { details.add("값: $it") }
            constraint?.let { details.add("제약조건: $it") }
            
            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }
    }

    /**
     * 검증 실패 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 필드, 값, 제약조건 정보가 포함된 맵
     */
    fun getValidationInfo(): Map<String, Any?> = mapOf(
        "field" to field,
        "value" to value,
        "constraint" to constraint
    ).filterValues { it != null }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 검증 정보가 결합된 맵
     */
    fun getFullErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val validationInfo = getValidationInfo()
        
        validationInfo.forEach { (key, value) ->
            baseInfo[key] = value.toString()
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val validationDetails = getValidationInfo()
        return if (validationDetails.isNotEmpty()) {
            "${super.toString()}, validation=${validationDetails}"
        } else {
            super.toString()
        }
    }
}