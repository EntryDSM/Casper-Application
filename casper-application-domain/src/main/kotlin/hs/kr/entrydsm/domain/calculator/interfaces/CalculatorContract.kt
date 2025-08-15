package hs.kr.entrydsm.domain.calculator.interfaces

/**
 * 계산기의 핵심 계약을 정의하는 인터페이스입니다.
 *
 * Interface Segregation Principle을 적용하여 기능별로 분리된 인터페이스들을
 * 모두 상속하는 통합 인터페이스입니다. Anti-Corruption Layer 역할을 수행하여
 * 다양한 계산기 구현체들 간의 호환성을 보장하며, 계산기의 핵심 기능을
 * 표준화된 방식으로 제공합니다.
 *
 * 클라이언트는 필요에 따라 개별 인터페이스를 직접 사용하거나
 * 모든 기능이 필요한 경우 이 통합 인터페이스를 사용할 수 있습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface CalculatorContract :
    CalculationContract,
    ValidationContract,
    ParsingContract,
    OptimizationContract,
    BatchContract,
    MetadataContract,
    LifecycleContract