package hs.kr.entrydsm.global.annotation.entity.provider

import hs.kr.entrydsm.global.annotation.aggregate.provider.AggregateProvider
import hs.kr.entrydsm.global.annotation.entity.Entity
import hs.kr.entrydsm.global.annotation.entity.EntityContract

object EntityProvider {

    private val entityRegistry = mutableMapOf<Class<*>, MutableSet<Class<*>>>()
    private val entityCache = mutableMapOf<Class<*>, Class<*>>()
    private val contextCache = mutableMapOf<Class<*>, String>()

    inline fun <reified T : EntityContract> registerEntity() {
        registerEntity(T::class.java)
    }

    fun <T : EntityContract> registerEntity(entityClass: Class<T>) {
        validateEntity(entityClass)
        val aggregateRoot = getAggregateRootFromAnnotation(entityClass)
        val context = getContextFromAnnotation(entityClass)

        entityRegistry.getOrPut(aggregateRoot) { mutableSetOf() }.add(entityClass)
        entityCache[entityClass] = aggregateRoot
        contextCache[entityClass] = context
    }

    fun getEntitiesByAggregate(aggregateRoot: Class<*>): Set<Class<*>> {
        return entityRegistry[aggregateRoot] ?: emptySet()
    }

    fun getEntitiesByContext(context: String): Set<Class<*>> {
        return contextCache.entries
            .filter { it.value == context }
            .map { it.key }
            .toSet()
    }

    fun getAllEntities(): Set<Class<*>> {
        return entityCache.keys.toSet()
    }

    fun getEntityAggregateRoot(entityClass: Class<*>): Class<*> {
        return entityCache[entityClass]
            ?: getAggregateRootFromAnnotation(entityClass)
    }

    fun getEntityContext(entityClass: Class<*>): String {
        return contextCache[entityClass]
            ?: getContextFromAnnotation(entityClass)
    }

    fun isEntity(clazz: Class<*>): Boolean {
        return entityCache.containsKey(clazz) || hasEntityAnnotation(clazz)
    }

    fun clearAll() {
        entityRegistry.clear()
        entityCache.clear()
        contextCache.clear()
    }

    fun validateEntity(entityClass: Class<*>) {
        validateEntityAnnotation(entityClass)
        validateEntityContract(entityClass)
        validateAggregateRoot(entityClass)
    }

    fun validateEntityAnnotation(entityClass: Class<*>) {
        entityClass.getAnnotation(Entity::class.java)
            ?: throw IllegalArgumentException("클래스 ${entityClass.simpleName}에 @Entity 어노테이션이 없습니다.")
    }

    fun validateEntityContract(entityClass: Class<*>) {
        if (!EntityContract::class.java.isAssignableFrom(entityClass)) {
            throw IllegalArgumentException("클래스 ${entityClass.simpleName}는 EntityContract 인터페이스를 구현해야 합니다.")
        }
    }

    fun validateAggregateRoot(entityClass: Class<*>) {
        val aggregateRoot = getAggregateRootFromAnnotation(entityClass)
        if (!AggregateProvider.isAggregate(aggregateRoot)) {
            throw IllegalArgumentException("${aggregateRoot.simpleName}은 유효한 Aggregate Root가 아닙니다.")
        }
    }

    fun getAggregateRootFromAnnotation(entityClass: Class<*>): Class<*> {
        val annotation = entityClass.getAnnotation(Entity::class.java)
            ?: throw IllegalArgumentException("클래스 ${entityClass.simpleName}에 @Entity 어노테이션이 없습니다.")
        return annotation.aggregateRoot.java
    }

    fun getContextFromAnnotation(entityClass: Class<*>): String {
        val annotation = entityClass.getAnnotation(Entity::class.java)
            ?: throw IllegalArgumentException("클래스 ${entityClass.simpleName}에 @Entity 어노테이션이 없습니다.")
        return annotation.context
    }

    fun hasEntityAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Entity::class.java) != null
    }
}