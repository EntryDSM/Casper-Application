package hs.kr.entrydsm.global.interfaces

/**
 * 도메인 객체를 식별하는 마커 인터페이스입니다.
 *
 * DDD 아키텍처에서 도메인 계층의 객체들을 명시적으로 표시하기 위한
 * 마커 인터페이스입니다. 컴파일 타임과 런타임에 도메인 객체를 구분하고
 * 검증할 수 있게 해주며, 도메인 규칙의 적용 범위를 명확히 합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface DomainMarker {
    
    /**
     * 도메인 객체의 컨텍스트를 반환합니다.
     *
     * @return 도메인 컨텍스트 (예: "lexer", "parser", "evaluator" 등)
     */
    fun getDomainContext(): String
    
    /**
     * 도메인 객체의 타입을 반환합니다.
     *
     * @return 도메인 객체 타입 (예: "aggregate", "entity", "value", "service" 등)
     */
    fun getDomainType(): String
    
    /**
     * 도메인 객체가 유효한지 검증합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean = true
    
    /**
     * 도메인 객체의 식별자를 반환합니다.
     *
     * @return 객체 식별자 또는 null
     */
    fun getIdentifier(): String? = null
    
    /**
     * 도메인 객체의 버전을 반환합니다.
     *
     * @return 객체 버전
     */
    fun getVersion(): Long = 1L
    
    /**
     * 도메인 규칙이 적용되는지 확인합니다.
     *
     * @return 도메인 규칙이 적용되면 true, 아니면 false
     */
    fun isDomainRuleApplicable(): Boolean = true
    
    /**
     * 도메인 이벤트가 발생 가능한지 확인합니다.
     *
     * @return 도메인 이벤트 발생 가능하면 true, 아니면 false
     */
    fun canRaiseDomainEvents(): Boolean = false
    
    /**
     * 도메인 객체의 상태를 반환합니다.
     *
     * @return 상태 정보 맵
     */
    fun getDomainState(): Map<String, Any> = emptyMap()
}

/**
 * 집합 루트를 나타내는 마커 인터페이스입니다.
 */
interface AggregateRootMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.AGGREGATE
    
    override fun canRaiseDomainEvents(): Boolean = true
    
    /**
     * 집합 루트의 경계를 정의합니다.
     *
     * @return 집합 경계 내의 엔티티 및 값 객체 타입들
     */
    fun getAggregateBoundary(): Set<String> = emptySet()
    
    /**
     * 집합 루트의 불변 조건을 검증합니다.
     *
     * @return 불변 조건이 만족되면 true, 아니면 false
     */
    fun checkInvariants(): Boolean = true
}

/**
 * 엔티티를 나타내는 마커 인터페이스입니다.
 */
interface EntityMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.ENTITY
    
    /**
     * 엔티티의 고유 식별자를 반환합니다.
     *
     * @return 엔티티 식별자
     */
    override fun getIdentifier(): String
    
    /**
     * 엔티티의 동등성을 확인합니다.
     * 엔티티는 식별자로만 비교됩니다.
     *
     * @param other 비교할 객체
     * @return 같은 엔티티면 true, 아니면 false
     */
    fun isSameEntity(other: EntityMarker): Boolean = 
        this.getIdentifier() == other.getIdentifier()
}

/**
 * 값 객체를 나타내는 마커 인터페이스입니다.
 */
interface ValueObjectMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.VALUE
    
    override fun getIdentifier(): String? = null
    
    /**
     * 값 객체의 동등성을 확인합니다.
     * 값 객체는 모든 속성 값으로 비교됩니다.
     *
     * @param other 비교할 객체
     * @return 같은 값이면 true, 아니면 false
     */
    fun isSameValue(other: ValueObjectMarker): Boolean = this == other
    
    /**
     * 값 객체가 불변인지 확인합니다.
     *
     * @return 불변이면 true, 아니면 false
     */
    fun isImmutable(): Boolean = true
}

/**
 * 도메인 서비스를 나타내는 마커 인터페이스입니다.
 */
interface DomainServiceMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.SERVICE
    
    /**
     * 도메인 서비스가 상태를 가지는지 확인합니다.
     *
     * @return 상태를 가지면 true, 아니면 false
     */
    fun isStateful(): Boolean = false
    
    /**
     * 도메인 서비스의 수행 가능한 연산들을 반환합니다.
     *
     * @return 연산 이름들
     */
    fun getAvailableOperations(): Set<String> = emptySet()
}

/**
 * 팩토리를 나타내는 마커 인터페이스입니다.
 */
interface FactoryMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.FACTORY
    
    /**
     * 팩토리가 생성할 수 있는 객체 타입들을 반환합니다.
     *
     * @return 생성 가능한 객체 타입들
     */
    fun getCreatableTypes(): Set<String> = emptySet()
    
    /**
     * 팩토리의 복잡도를 반환합니다.
     *
     * @return 복잡도 수준
     */
    fun getComplexity(): String = DomainMarkerObject.SIMPLE
}

/**
 * 정책을 나타내는 마커 인터페이스입니다.
 */
interface PolicyMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.POLICY
    
    /**
     * 정책의 적용 범위를 반환합니다.
     *
     * @return 정책 적용 범위
     */
    fun getPolicyScope(): String = DomainMarkerObject.DOMAIN
    
    /**
     * 정책의 우선순위를 반환합니다.
     *
     * @return 우선순위 (높을수록 우선)
     */
    fun getPriority(): Int = 0
}

/**
 * 명세를 나타내는 마커 인터페이스입니다.
 */
interface SpecificationMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.SPECIFICATION
    
    /**
     * 명세의 우선순위를 반환합니다.
     *
     * @return 우선순위
     */
    fun getSpecificationPriority(): String = DomainMarkerObject.NORMAL
    
    /**
     * 명세가 조합 가능한지 확인합니다.
     *
     * @return 조합 가능하면 true, 아니면 false
     */
    fun isCombinable(): Boolean = true
}

/**
 * 리포지토리를 나타내는 마커 인터페이스입니다.
 */
interface RepositoryMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.REPOSITORY
    
    /**
     * 리포지토리가 관리하는 집합 루트 타입을 반환합니다.
     *
     * @return 집합 루트 타입
     */
    fun getAggregateType(): String
    
    /**
     * 리포지토리가 캐시를 지원하는지 확인합니다.
     *
     * @return 캐시 지원하면 true, 아니면 false
     */
    fun supportsCaching(): Boolean = false
}

/**
 * 도메인 이벤트를 나타내는 마커 인터페이스입니다.
 */
interface DomainEventMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.EVENT
    
    /**
     * 이벤트의 타입을 반환합니다.
     *
     * @return 이벤트 타입
     */
    fun getEventType(): String
    
    /**
     * 이벤트의 발생 시각을 반환합니다.
     *
     * @return 발생 시각 (밀리초)
     */
    fun getOccurredAt(): Long
    
    /**
     * 이벤트가 처리되었는지 확인합니다.
     *
     * @return 처리되었으면 true, 아니면 false
     */
    fun isProcessed(): Boolean = false
}

/**
 * Anti-Corruption Layer를 나타내는 마커 인터페이스입니다.
 */
interface AntiCorruptionLayerMarker : DomainMarker {
    
    override fun getDomainType(): String = DomainMarkerObject.ANTI_CORRUPTION_LAYER
    
    /**
     * 보호하는 도메인 컨텍스트를 반환합니다.
     *
     * @return 보호 대상 도메인 컨텍스트
     */
    fun getProtectedDomain(): String
    
    /**
     * 외부 시스템과의 인터페이스를 정의합니다.
     *
     * @return 외부 인터페이스 정보
     */
    fun getExternalInterface(): Map<String, Any> = emptyMap()
}

object DomainMarkerObject{
    const val AGGREGATE = "aggregate"
    const val SERVICE = "service"
    const val ENTITY = "entity"
    const val VALUE = "value"
    const val FACTORY = "factory"
    const val SIMPLE = "simple"
    const val POLICY = "policy"
    const val DOMAIN = "DOMAIN"
    const val SPECIFICATION = "specification"
    const val NORMAL = "NORMAL"
    const val REPOSITORY = "repository"
    const val EVENT = "event"
    const val ANTI_CORRUPTION_LAYER = "anti-corruption-layer"
}