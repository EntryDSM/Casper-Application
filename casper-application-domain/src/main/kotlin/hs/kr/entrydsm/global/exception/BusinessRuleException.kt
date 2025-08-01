package hs.kr.entrydsm.global.exception

import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.global.exception.DomainException

/**
 * 비즈니스 규칙 위반 시 발생하는 예외입니다.
 *
 * 도메인의 핵심 비즈니스 로직이나 정책을 위반했을 때 발생하는 예외로,
 * 단순한 유효성 검사와는 달리 복잡한 비즈니스 규칙이나 제약사항을
 * 위반한 경우에 사용됩니다. 위반된 규칙의 이름과 상세 정보를 포함합니다.
 *
 * @property ruleName 위반된 비즈니스 규칙의 이름
 * @property ruleDescription 규칙에 대한 상세 설명 (선택사항)
 * @property context 규칙 위반이 발생한 컨텍스트 정보 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class BusinessRuleException(
    errorCode: ErrorCode = ErrorCode.BUSINESS_RULE_VIOLATION,
    val ruleName: String,
    val ruleDescription: String? = null,
    val ruleContext: Map<String, Any?> = emptyMap(),
    message: String = buildBusinessRuleMessage(errorCode, ruleName, ruleDescription),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * 비즈니스 규칙 위반 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param ruleName 위반된 규칙명
         * @param ruleDescription 규칙 설명
         * @return 구성된 메시지
         */
        private fun buildBusinessRuleMessage(
            errorCode: ErrorCode,
            ruleName: String,
            ruleDescription: String?
        ): String {
            val baseMessage = errorCode.description
            val ruleInfo = if (ruleDescription != null) {
                "규칙: $ruleName ($ruleDescription)"
            } else {
                "규칙: $ruleName"
            }
            
            return "$baseMessage - $ruleInfo"
        }
    }

    /**
     * 비즈니스 규칙 위반 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 규칙명, 설명, 컨텍스트 정보가 포함된 맵
     */
    fun getBusinessRuleInfo(): Map<String, Any?> {
        val info = mutableMapOf<String, Any?>(
            "ruleName" to ruleName
        )
        
        ruleDescription?.let { info["ruleDescription"] = it }
        
        if (context.isNotEmpty()) {
            info["context"] = context
        }
        
        return info
    }

    /**
     * 특정 컨텍스트 값을 조회합니다.
     *
     * @param key 조회할 컨텍스트 키
     * @return 컨텍스트 값 (없으면 null)
     */
    fun getContextValue(key: String): Any? = context[key]

    /**
     * 컨텍스트에 특정 키가 존재하는지 확인합니다.
     *
     * @param key 확인할 키
     * @return 존재하면 true, 아니면 false
     */
    fun hasContextKey(key: String): Boolean = context.containsKey(key)

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 비즈니스 규칙 정보가 결합된 맵
     */
    fun getFullErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val businessRuleInfo = getBusinessRuleInfo()
        
        businessRuleInfo.forEach { (key, value) ->
            when (value) {
                is Map<*, *> -> baseInfo[key] = value.toString()
                else -> baseInfo[key] = value?.toString() ?: ""
            }
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val businessRuleDetails = getBusinessRuleInfo()
        return "${super.toString()}, businessRule=${businessRuleDetails}"
    }
}