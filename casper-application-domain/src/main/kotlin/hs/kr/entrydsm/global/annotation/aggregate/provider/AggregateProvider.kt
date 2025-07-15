package hs.kr.entrydsm.global.annotation.aggregate.provider

import hs.kr.entrydsm.global.annotation.aggregate.Aggregate
import hs.kr.entrydsm.global.annotation.aggregate.AggregateContract

/**
 * DDD의 Aggregate Root 패턴 관리를 담당하는 Provider 객체입니다.
 *
 * 애그리게이트의 등록, 조회, 검증을 통해 도메인 애그리게이트들을
 * 체계적으로 관리하고 컨텍스트별 분류를 지원합니다.
 *
 * 주요 기능:
 * - 애그리게이트 클래스 등록 및 관리
 * - 컨텍스트별 애그리게이트 조회
 * - 애그리게이트 유효성 검증 (어노테이션, 인터페이스)
 * - 애그리게이트-컨텍스트 관계 관리
 * - 애그리게이트 존재 여부 확인
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
object AggregateProvider {

    private val aggregateRegistry = mutableMapOf<String, MutableSet<Class<*>>>()
    private val aggregateCache = mutableMapOf<Class<*>, String>()

    /**
     * 타입 안전성을 위한 인라인 함수로 애그리게이트를 등록합니다.
     *
     * @param T 등록할 애그리게이트 클래스 타입
     */
    inline fun <reified T : AggregateContract> registerAggregate() {
        registerAggregate(T::class.java)
    }

    /**
     * 애그리게이트 클래스를 등록합니다.
     *
     * @param aggregateClass 등록할 애그리게이트 클래스
     * @param T 애그리게이트 클래스 타입
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun <T : AggregateContract> registerAggregate(aggregateClass: Class<T>) {
        validateAggregate(aggregateClass)
        val context = getContextFromAnnotation(aggregateClass)
        aggregateRegistry.getOrPut(context) { mutableSetOf() }.add(aggregateClass)
        aggregateCache[aggregateClass] = context
    }

    /**
     * 특정 컨텍스트에 속한 모든 애그리게이트들을 조회합니다.
     *
     * @param context 대상 컨텍스트 이름
     * @return 해당 컨텍스트에 속한 애그리게이트 클래스 집합
     */
    fun getAggregatesByContext(context: String): Set<Class<*>> {
        return aggregateRegistry[context] ?: emptySet()
    }

    /**
     * 등록된 모든 컨텍스트들을 조회합니다.
     *
     * @return 등록된 모든 컨텍스트 이름 집합
     */
    fun getAllContexts(): Set<String> {
        return aggregateRegistry.keys.toSet()
    }

    /**
     * 등록된 모든 애그리게이트들을 조회합니다.
     *
     * @return 등록된 모든 애그리게이트 클래스 집합
     */
    fun getAllAggregates(): Set<Class<*>> {
        return aggregateCache.keys.toSet()
    }

    /**
     * 애그리게이트가 속한 컨텍스트를 조회합니다.
     *
     * @param aggregateClass 대상 애그리게이트 클래스
     * @return 애그리게이트가 속한 컨텍스트 이름
     * @throws IllegalArgumentException @Aggregate 어노테이션이 없는 경우
     */
    fun getAggregateContext(aggregateClass: Class<*>): String {
        return aggregateCache[aggregateClass]
            ?: getContextFromAnnotation(aggregateClass)
    }

    /**
     * 주어진 클래스가 애그리게이트인지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return 애그리게이트이면 true, 그렇지 않으면 false
     */
    fun isAggregate(clazz: Class<*>): Boolean {
        return aggregateCache.containsKey(clazz) || hasAggregateAnnotation(clazz)
    }

    /**
     * 모든 등록된 애그리게이트 관련 데이터를 제거합니다.
     */
    fun clearAll() {
        aggregateRegistry.clear()
        aggregateCache.clear()
    }

    /**
     * 애그리게이트 클래스의 유효성을 검증합니다.
     *
     * @param aggregateClass 검증할 애그리게이트 클래스
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun validateAggregate(aggregateClass: Class<*>) {
        validateAggregateAnnotation(aggregateClass)
        validateAggregateContract(aggregateClass)
    }

    /**
     * 애그리게이트 클래스에 @Aggregate 어노테이션이 있는지 검증합니다.
     *
     * @param aggregateClass 검증할 애그리게이트 클래스
     * @throws IllegalArgumentException @Aggregate 어노테이션이 없는 경우
     */
    fun validateAggregateAnnotation(aggregateClass: Class<*>) {
        aggregateClass.getAnnotation(Aggregate::class.java)
            ?: throw IllegalArgumentException("클래스 ${aggregateClass.simpleName}에 @Aggregate 어노테이션이 없습니다.")
    }

    /**
     * 애그리게이트 클래스가 AggregateContract 인터페이스를 구현하는지 검증합니다.
     *
     * @param aggregateClass 검증할 애그리게이트 클래스
     * @throws IllegalArgumentException AggregateContract 인터페이스를 구현하지 않은 경우
     */
    fun validateAggregateContract(aggregateClass: Class<*>) {
        if (!AggregateContract::class.java.isAssignableFrom(aggregateClass)) {
            throw IllegalArgumentException("클래스 ${aggregateClass.simpleName}는 AggregateContract 인터페이스를 구현해야 합니다.")
        }
    }

    /**
     * @Aggregate 어노테이션에서 컨텍스트 이름을 추출합니다.
     *
     * @param aggregateClass 대상 애그리게이트 클래스
     * @return 컨텍스트 이름
     * @throws IllegalArgumentException @Aggregate 어노테이션이 없는 경우
     */
    fun getContextFromAnnotation(aggregateClass: Class<*>): String {
        val annotation = aggregateClass.getAnnotation(Aggregate::class.java)
            ?: throw IllegalArgumentException("클래스 ${aggregateClass.simpleName}에 @Aggregate 어노테이션이 없습니다.")
        return annotation.context
    }

    /**
     * 클래스에 @Aggregate 어노테이션이 있는지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return @Aggregate 어노테이션이 있으면 true, 없으면 false
     */
    fun hasAggregateAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Aggregate::class.java) != null
    }
}