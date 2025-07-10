package hs.kr.entrydsm.global.annotation.aggregate.provider

import hs.kr.entrydsm.global.annotation.aggregate.Aggregate
import hs.kr.entrydsm.global.annotation.aggregate.AggregateContract

object AggregateProvider {

    private val aggregateRegistry = mutableMapOf<String, MutableSet<Class<*>>>()
    private val aggregateCache = mutableMapOf<Class<*>, String>()

    inline fun <reified T : AggregateContract> registerAggregate() {
        registerAggregate(T::class.java)
    }

    fun <T : AggregateContract> registerAggregate(aggregateClass: Class<T>) {
        validateAggregate(aggregateClass)
        val context = getContextFromAnnotation(aggregateClass)
        aggregateRegistry.getOrPut(context) { mutableSetOf() }.add(aggregateClass)
        aggregateCache[aggregateClass] = context
    }

    fun getAggregatesByContext(context: String): Set<Class<*>> {
        return aggregateRegistry[context] ?: emptySet()
    }

    fun getAllContexts(): Set<String> {
        return aggregateRegistry.keys.toSet()
    }

    fun getAllAggregates(): Set<Class<*>> {
        return aggregateCache.keys.toSet()
    }

    fun getAggregateContext(aggregateClass: Class<*>): String {
        return aggregateCache[aggregateClass]
            ?: getContextFromAnnotation(aggregateClass)
    }

    fun isAggregate(clazz: Class<*>): Boolean {
        return aggregateCache.containsKey(clazz) || hasAggregateAnnotation(clazz)
    }

    fun clearAll() {
        aggregateRegistry.clear()
        aggregateCache.clear()
    }

    fun validateAggregate(aggregateClass: Class<*>) {
        validateAggregateAnnotation(aggregateClass)
        validateAggregateContract(aggregateClass)
    }

    fun validateAggregateAnnotation(aggregateClass: Class<*>) {
        aggregateClass.getAnnotation(Aggregate::class.java)
            ?: throw IllegalArgumentException("클래스 ${aggregateClass.simpleName}에 @Aggregate 어노테이션이 없습니다.")
    }

    fun validateAggregateContract(aggregateClass: Class<*>) {
        if (!AggregateContract::class.java.isAssignableFrom(aggregateClass)) {
            throw IllegalArgumentException("클래스 ${aggregateClass.simpleName}는 AggregateContract 인터페이스를 구현해야 합니다.")
        }
    }

    fun getContextFromAnnotation(aggregateClass: Class<*>): String {
        val annotation = aggregateClass.getAnnotation(Aggregate::class.java)
            ?: throw IllegalArgumentException("클래스 ${aggregateClass.simpleName}에 @Aggregate 어노테이션이 없습니다.")
        return annotation.context
    }

    fun hasAggregateAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Aggregate::class.java) != null
    }
}