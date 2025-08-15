package hs.kr.entrydsm.global.interfaces

/**
 * 집합 루트(Aggregate Root)를 위한 추상 클래스입니다.
 *
 * 도메인 이벤트 발행과 일관성 경계 관리를 제공합니다.
 *
 * @param T 집합 루트의 식별자 타입
 */
abstract class AggregateRoot<T> : EntityBase<T>() {
    
    private val domainEvents = mutableListOf<Any>()
    
    /**
     * 도메인 이벤트를 추가합니다.
     *
     * @param event 추가할 도메인 이벤트
     */
    protected fun addDomainEvent(event: Any) {
        domainEvents.add(event)
    }
    
    /**
     * 발행되지 않은 도메인 이벤트들을 반환합니다.
     *
     * @return 도메인 이벤트 목록
     */
    fun getUncommittedEvents(): List<Any> = domainEvents.toList()
    
    /**
     * 도메인 이벤트들을 발행 완료로 표시합니다.
     */
    fun markEventsAsCommitted() {
        domainEvents.clear()
    }
    
    /**
     * 집합 루트의 불변식을 검증합니다.
     *
     * @return 불변식이 만족되면 true, 아니면 false
     */
    abstract fun checkInvariants(): Boolean
    
    override fun isValid(): Boolean {
        return checkInvariants()
    }
}