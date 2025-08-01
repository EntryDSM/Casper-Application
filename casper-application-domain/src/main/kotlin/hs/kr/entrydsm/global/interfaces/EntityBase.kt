package hs.kr.entrydsm.global.interfaces

/**
 * 엔티티(Entity)를 위한 추상 클래스입니다.
 *
 * 식별자 기반 동등성과 생명주기 관리를 제공합니다.
 *
 * @param T 엔티티의 식별자 타입
 */
abstract class EntityBase<T> : DomainObject<T> {
    
    private var _createdAt: Long = System.currentTimeMillis()
    private var _modifiedAt: Long = System.currentTimeMillis()
    
    /**
     * 엔티티의 생성 시간을 반환합니다.
     *
     * @return 생성 시간 (밀리초)
     */
    fun getCreatedAt(): Long = _createdAt
    
    /**
     * 엔티티의 마지막 수정 시간을 반환합니다.
     *
     * @return 수정 시간 (밀리초)
     */
    fun getModifiedAt(): Long = _modifiedAt
    
    /**
     * 엔티티의 수정 시간을 업데이트합니다.
     */
    protected fun updateModifiedTime() {
        _modifiedAt = System.currentTimeMillis()
    }
    
    /**
     * 엔티티의 동등성은 식별자로 판단합니다.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EntityBase<*>) return false
        return getId() == other.getId()
    }
    
    /**
     * 엔티티의 해시 코드는 식별자로 계산합니다.
     */
    override fun hashCode(): Int {
        return getId()?.hashCode() ?: 0
    }
    
    override fun getMetadata(): Map<String, Any> = mapOf(
        "createdAt" to _createdAt,
        "modifiedAt" to _modifiedAt,
        "type" to getType()
    )
}