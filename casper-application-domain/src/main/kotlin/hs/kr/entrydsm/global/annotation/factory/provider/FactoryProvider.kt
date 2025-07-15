package hs.kr.entrydsm.global.annotation.factory.provider

import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.FactoryContract

/**
 * DDD의 Factory 패턴 관리를 담당하는 Provider 객체입니다.
 *
 * 팩토리의 등록, 객체 생성, 인스턴스 관리, 캐싱을 통해 복잡한 객체 생성 로직을
 * 중앙화하고 체계적으로 관리할 수 있도록 지원합니다.
 *
 * 주요 기능:
 * - 팩토리 클래스 등록 및 관리
 * - 타입 안전한 객체 생성
 * - 팩토리 인스턴스 캐싱
 * - 생성된 객체 캐싱 (선택적)
 * - 팩토리 유효성 검증
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
object FactoryProvider {

    private val factoryCache = mutableMapOf<String, Any>()
    private val factoryInstances = mutableMapOf<Class<*>, Any>()
    private val factoryRegistry = mutableMapOf<Class<*>, Class<*>>()

    /**
     * 타입 안전성을 위한 인라인 함수로 팩토리를 등록합니다.
     *
     * @param T 대상 객체 타입
     * @param F 팩토리 클래스 타입
     */
    inline fun <reified T : Any, reified F : FactoryContract<T>> registerFactory() {
        registerFactory(T::class.java, F::class.java)
    }

    /**
     * 팩토리를 등록합니다.
     *
     * @param targetType 대상 객체 클래스
     * @param factoryClass 팩토리 클래스
     * @param T 대상 객체 타입
     * @param F 팩토리 클래스 타입
     */
    fun <T : Any, F : FactoryContract<T>> registerFactory(targetType: Class<T>, factoryClass: Class<F>) {
        factoryRegistry[targetType] = factoryClass
    }

    /**
     * 타입 안전성을 위한 인라인 함수로 객체를 생성합니다.
     *
     * @param cache 캐싱 사용 여부
     * @param key 캐시 키 (캐싱 사용 시 필수)
     * @param params 생성 시 전달할 매개변수들
     * @param T 생성할 객체 타입
     * @return 생성된 객체
     */
    inline fun <reified T : Any> createObject(cache: Boolean = false, key: String = "", vararg params: Any?): T {
        return createObject(T::class.java, cache, key, *params)
    }

    /**
     * 타입 안전성을 위한 인라인 함수로 팩토리를 조회합니다.
     *
     * @param T 팩토리 타입
     * @return 팩토리 인스턴스
     */
    inline fun <reified T : Any> getFactory(): T {
        return getFactory(T::class.java)
    }

    /**
     * 캐시된 객체들을 모두 제거합니다.
     */
    fun clearCache() {
        factoryCache.clear()
    }

    /**
     * 모든 팩토리 관련 데이터를 제거합니다.
     */
    fun clearAll() {
        factoryCache.clear()
        factoryInstances.clear()
        factoryRegistry.clear()
    }

    /**
     * 지정된 팩토리를 사용하여 객체를 생성합니다.
     *
     * @param targetType 생성할 객체의 클래스
     * @param cache 캐싱 사용 여부
     * @param key 캐시 키 (캐싱 사용 시 필수)
     * @param params 생성 시 전달할 매개변수들
     * @param T 생성할 객체 타입
     * @return 생성된 객체
     * @throws IllegalArgumentException 캐싱 사용 시 키가 비어있거나 팩토리가 등록되지 않은 경우
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> createObject(targetType: Class<T>, cache: Boolean, key: String, vararg params: Any?): T {
        val factory = getFactoryForType(targetType)

        return when {
            cache && key.isNotEmpty() -> getCachedObject(key) { factory.create(*params) } as T
            cache && key.isEmpty() -> throw IllegalArgumentException("캐싱을 사용할 때는 키가 필요합니다.")
            else -> factory.create(*params)
        }
    }

    /**
     * 지정된 타입에 대한 팩토리를 조회합니다.
     *
     * @param targetType 대상 객체의 클래스
     * @param T 팩토리 타입
     * @return 팩토리 인스턴스
     * @throws IllegalArgumentException 팩토리가 등록되지 않은 경우
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFactory(targetType: Class<T>): T {
        val factoryClass = getFactoryClass(targetType)
        return getFactoryInstance(factoryClass) as T
    }

    /**
     * 지정된 타입에 대한 FactoryContract 인스턴스를 조회합니다.
     *
     * @param targetType 대상 객체의 클래스
     * @param T 대상 객체 타입
     * @return FactoryContract 인스턴스
     * @throws IllegalArgumentException 팩토리가 등록되지 않은 경우
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFactoryForType(targetType: Class<T>): FactoryContract<T> {
        val factoryClass = getFactoryClass(targetType)
        return getFactoryInstance(factoryClass) as FactoryContract<T>
    }

    /**
     * 등록된 팩토리 클래스를 조회합니다.
     *
     * @param targetType 대상 객체의 클래스
     * @return 팩토리 클래스
     * @throws IllegalArgumentException 팩토리가 등록되지 않은 경우
     */
    fun getFactoryClass(targetType: Class<*>): Class<*> {
        return factoryRegistry[targetType]
            ?: throw IllegalArgumentException("${targetType.simpleName}에 대한 팩토리가 등록되지 않았습니다.")
    }

    /**
     * 팩토리 인스턴스를 조회하거나 생성합니다.
     *
     * @param factoryClass 팩토리 클래스
     * @return 팩토리 인스턴스 (캐시됨)
     */
    fun getFactoryInstance(factoryClass: Class<*>): Any {
        return factoryInstances.getOrPut(factoryClass) {
            createFactoryInstance(factoryClass)
        }
    }

    /**
     * 팩토리 클래스의 새로운 인스턴스를 생성하고 검증합니다.
     *
     * @param factoryClass 팩토리 클래스
     * @return 생성된 팩토리 인스턴스
     * @throws RuntimeException 인스턴스 생성에 실패한 경우
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun createFactoryInstance(factoryClass: Class<*>): Any {
        val instance = factoryClass.getDeclaredConstructor().newInstance()
        validateFactory(instance)
        return instance
    }

    /**
     * 캐시된 객체를 조회하거나 생성합니다.
     *
     * @param key 캐시 키
     * @param creator 객체 생성 함수
     * @return 캐시된 또는 새로 생성된 객체
     */
    fun getCachedObject(key: String, creator: () -> Any): Any {
        return factoryCache.getOrPut(key) { creator() }
    }

    /**
     * 팩토리 인스턴스의 유효성을 검증합니다.
     *
     * @param factory 검증할 팩토리 인스턴스
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun validateFactory(factory: Any) {
        val factoryClass = factory::class.java

        validateAnnotation(factoryClass)
        validateContract(factoryClass)
    }

    /**
     * 팩토리 클래스에 @Factory 어노테이션이 있는지 검증합니다.
     *
     * @param factoryClass 검증할 팩토리 클래스
     * @throws IllegalArgumentException @Factory 어노테이션이 없는 경우
     */
    fun validateAnnotation(factoryClass: Class<*>) {
        factoryClass.getAnnotation(Factory::class.java)
            ?: throw IllegalArgumentException("클래스 ${factoryClass.simpleName}에 @Factory 어노테이션이 없습니다.")
    }

    /**
     * 팩토리 클래스가 FactoryContract 인터페이스를 구현하는지 검증합니다.
     *
     * @param factoryClass 검증할 팩토리 클래스
     * @throws IllegalArgumentException FactoryContract 인터페이스를 구현하지 않은 경우
     */
    fun validateContract(factoryClass: Class<*>) {
        if (!FactoryContract::class.java.isAssignableFrom(factoryClass)) {
            throw IllegalArgumentException("팩토리 클래스 ${factoryClass.simpleName}는 FactoryContract 인터페이스를 구현해야 합니다.")
        }
    }
}