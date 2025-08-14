package hs.kr.entrydsm.global.annotation.entities.provider

import hs.kr.entrydsm.global.annotation.aggregates.provider.AggregateProvider
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.annotation.entities.EntityContract
import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.global.exception.ValidationException

/**
 * DDD의 Entity 패턴 관리를 담당하는 Provider 객체입니다.
 *
 * 엔티티의 등록, 조회, 검증을 통해 도메인 엔티티들을
 * 체계적으로 관리하고 애그리게이트와의 관계를 유지합니다.
 *
 * 주요 기능:
 * - 엔티티 클래스 등록 및 관리
 * - 애그리게이트별 엔티티 조회
 * - 컨텍스트별 엔티티 조회
 * - 엔티티 유효성 검증 (어노테이션, 인터페이스, 애그리게이트 루트)
 * - 엔티티-애그리게이트 관계 관리
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
object EntityProvider {

    private val entityRegistry = mutableMapOf<Class<*>, MutableSet<Class<*>>>()
    private val entityCache = mutableMapOf<Class<*>, Class<*>>()
    private val contextCache = mutableMapOf<Class<*>, String>()

    private object ErrorMessages {
        const val ENTITY_ANNOTATION_MISSING = "어노테이션이 없습니다"
        const val ENTITY_CONTRACT_NOT_IMPLEMENTED = "인터페이스를 구현해야 합니다"
        const val INVALID_AGGREGATE_ROOT = "은 유효한 Aggregate Root가 아닙니다"
    }

    /**
     * 타입 안전성을 위한 인라인 함수로 엔티티를 등록합니다.
     *
     * @param T 등록할 엔티티 클래스 타입
     */
    inline fun <reified T : EntityContract> registerEntity() {
        registerEntity(T::class.java)
    }

    /**
     * 엔티티 클래스를 등록합니다.
     *
     * @param entityClass 등록할 엔티티 클래스
     * @param T 엔티티 클래스 타입
     * @throws ValidationException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun <T : EntityContract> registerEntity(entityClass: Class<T>) {
        validateEntity(entityClass)
        val aggregateRoot = getAggregateRootFromAnnotation(entityClass)
        val context = getContextFromAnnotation(entityClass)

        entityRegistry.getOrPut(aggregateRoot) { mutableSetOf() }.add(entityClass)
        entityCache[entityClass] = aggregateRoot
        contextCache[entityClass] = context
    }

    /**
     * 특정 애그리게이트 루트에 속한 모든 엔티티들을 조회합니다.
     *
     * @param aggregateRoot 대상 애그리게이트 루트 클래스
     * @return 해당 애그리게이트에 속한 엔티티 클래스 집합
     */
    fun getEntitiesByAggregate(aggregateRoot: Class<*>): Set<Class<*>> {
        return entityRegistry[aggregateRoot] ?: emptySet()
    }

    /**
     * 특정 컨텍스트에 속한 모든 엔티티들을 조회합니다.
     *
     * @param context 대상 컨텍스트 이름
     * @return 해당 컨텍스트에 속한 엔티티 클래스 집합
     */
    fun getEntitiesByContext(context: String): Set<Class<*>> {
        return contextCache.entries
            .filter { it.value == context }
            .map { it.key }
            .toSet()
    }

    /**
     * 등록된 모든 엔티티들을 조회합니다.
     *
     * @return 등록된 모든 엔티티 클래스 집합
     */
    fun getAllEntities(): Set<Class<*>> {
        return entityCache.keys.toSet()
    }

    /**
     * 엔티티가 속한 애그리게이트 루트를 조회합니다.
     *
     * @param entityClass 대상 엔티티 클래스
     * @return 엔티티가 속한 애그리게이트 루트 클래스
     * @throws ValidationException @Entity 어노테이션이 없는 경우
     */
    fun getEntityAggregateRoot(entityClass: Class<*>): Class<*> {
        return entityCache[entityClass]
            ?: getAggregateRootFromAnnotation(entityClass)
    }

    /**
     * 엔티티가 속한 컨텍스트를 조회합니다.
     *
     * @param entityClass 대상 엔티티 클래스
     * @return 엔티티가 속한 컨텍스트 이름
     * @throws IllegalArgumentException @Entity 어노테이션이 없는 경우
     */
    fun getEntityContext(entityClass: Class<*>): String {
        return contextCache[entityClass]
            ?: getContextFromAnnotation(entityClass)
    }

    /**
     * 주어진 클래스가 엔티티인지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return 엔티티이면 true, 그렇지 않으면 false
     */
    fun isEntity(clazz: Class<*>): Boolean {
        return entityCache.containsKey(clazz) || hasEntityAnnotation(clazz)
    }

    /**
     * 모든 등록된 엔티티 관련 데이터를 제거합니다.
     */
    fun clearAll() {
        entityRegistry.clear()
        entityCache.clear()
        contextCache.clear()
    }

    /**
     * 엔티티 클래스의 유효성을 검증합니다.
     *
     * @param entityClass 검증할 엔티티 클래스
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun validateEntity(entityClass: Class<*>) {
        validateEntityAnnotation(entityClass)
        validateEntityContract(entityClass)
        validateAggregateRoot(entityClass)
    }

    /**
     * 엔티티 클래스에 @Entity 어노테이션이 있는지 검증합니다.
     *
     * @param entityClass 검증할 엔티티 클래스
     * @throws IllegalArgumentException @Entity 어노테이션이 없는 경우
     */
    fun validateEntityAnnotation(entityClass: Class<*>) {
        entityClass.getAnnotation(Entity::class.java)
            ?: throw IllegalArgumentException("클래스 ${entityClass.simpleName}에 @Entity 어노테이션이 없습니다.")
    }

    /**
     * 엔티티 클래스가 EntityContract 인터페이스를 구현하는지 검증합니다.
     *
     * @param entityClass 검증할 엔티티 클래스
     * @throws IllegalArgumentException EntityContract 인터페이스를 구현하지 않은 경우
     */
    fun validateEntityContract(entityClass: Class<*>) {
        if (!EntityContract::class.java.isAssignableFrom(entityClass)) {
            throw IllegalArgumentException("클래스 ${entityClass.simpleName}는 EntityContract 인터페이스를 구현해야 합니다.")
        }
    }

    /**
     * 엔티티의 애그리게이트 루트가 유효한지 검증합니다.
     *
     * @param entityClass 검증할 엔티티 클래스
     * @throws IllegalArgumentException 애그리게이트 루트가 유효하지 않은 경우
     */
    fun validateAggregateRoot(entityClass: Class<*>) {
        val aggregateRoot = getAggregateRootFromAnnotation(entityClass)
        if (!AggregateProvider.isAggregate(aggregateRoot)) {
            throw IllegalArgumentException("${aggregateRoot.simpleName}은 유효한 Aggregate Root가 아닙니다.")
        }
    }

    /**
     * @Entity 어노테이션에서 애그리게이트 루트 클래스를 추출합니다.
     *
     * @param entityClass 대상 엔티티 클래스
     * @return 애그리게이트 루트 클래스
     * @throws IllegalArgumentException @Entity 어노테이션이 없는 경우
     */
    fun getAggregateRootFromAnnotation(entityClass: Class<*>): Class<*> {
        val annotation = entityClass.getAnnotation(Entity::class.java)
            ?: throw IllegalArgumentException("클래스 ${entityClass.simpleName}에 @Entity 어노테이션이 없습니다.")
        return annotation.aggregateRoot.java
    }

    /**
     * @Entity 어노테이션에서 컨텍스트 이름을 추출합니다.
     *
     * @param entityClass 대상 엔티티 클래스
     * @return 컨텍스트 이름
     * @throws IllegalArgumentException @Entity 어노테이션이 없는 경우
     */
    fun getContextFromAnnotation(entityClass: Class<*>): String {
        val annotation = entityClass.getAnnotation(Entity::class.java)
            ?: throw IllegalArgumentException("클래스 ${entityClass.simpleName}에 @Entity 어노테이션이 없습니다.")
        return annotation.context
    }

    /**
     * 클래스에 @Entity 어노테이션이 있는지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return @Entity 어노테이션이 있으면 true, 없으면 false
     */
    fun hasEntityAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Entity::class.java) != null
    }
}