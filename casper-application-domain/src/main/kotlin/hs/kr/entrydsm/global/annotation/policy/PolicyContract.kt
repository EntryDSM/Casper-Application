package hs.kr.entrydsm.global.annotation.policy

import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * DDD의 비즈니스 정책(Business Policy)을 구현하는 클래스가 따라야 하는 계약을 정의합니다.
 *
 * 정책은 도메인의 비즈니스 규칙이나 제약사항을 명시적으로 표현하는 데 사용되며,
 * 이 인터페이스를 통해 정책의 실행, 검증, 메타데이터 관리를 표준화합니다.
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
interface PolicyContract {
    
    /**
     * 정책을 실행합니다.
     *
     * @param context 정책 실행에 필요한 컨텍스트 정보
     * @return 정책 실행 결과
     */
    fun execute(context: Map<String, Any?>): PolicyResult
    
    /**
     * 정책 적용 가능 여부를 검증합니다.
     *
     * @param context 검증에 필요한 컨텍스트 정보
     * @return 정책 적용 가능 여부
     */
    fun isApplicable(context: Map<String, Any?>): Boolean
    
    /**
     * 정책의 이름을 반환합니다.
     *
     * @return 정책의 이름
     */
    fun getName(): String
    
    /**
     * 정책의 설명을 반환합니다.
     *
     * @return 정책의 상세 설명
     */
    fun getDescription(): String
    
    /**
     * 정책이 속한 도메인을 반환합니다.
     *
     * @return 정책이 속한 도메인명
     */
    fun getDomain(): String
    
    /**
     * 정책의 적용 범위를 반환합니다.
     *
     * @return 정책의 적용 범위 (GLOBAL, DOMAIN, AGGREGATE, ENTITY)
     */
    fun getScope(): Scope
    
    /**
     * 정책의 우선순위를 반환합니다.
     *
     * @return 정책의 우선순위 (숫자가 낮을수록 높은 우선순위)
     */
    fun getPriority(): Int = Int.MAX_VALUE
}