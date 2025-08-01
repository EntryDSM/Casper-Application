package hs.kr.entrydsm.global.annotation.service.type

/**
 * 서비스의 타입을 정의하는 열거형입니다.
 *
 * 도메인 주도 설계에서 서비스는 크게 도메인 서비스와 애플리케이션 서비스로 구분됩니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.21
 */
enum class ServiceType(
    val description: String
) {
    /**
     * 도메인 서비스
     * 
     * 도메인 로직을 포함하며, 특정 엔티티에 속하지 않는 
     * 비즈니스 규칙을 구현합니다.
     */
    DOMAIN_SERVICE("도메인 서비스"),
    
    /**
     * 애플리케이션 서비스
     * 
     * 유스케이스를 조율하며, 도메인 객체들 간의 협력을 
     * 관리합니다.
     */
    APPLICATION_SERVICE("애플리케이션 서비스"),
    
    /**
     * 인프라스트럭처 서비스
     * 
     * 외부 시스템과의 연동이나 기술적 관심사를 
     * 처리합니다.
     */
    INFRASTRUCTURE_SERVICE("인프라스트럭처 서비스")
}