package hs.kr.entrydsm.global.annotation.factory.provider

import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.FactoryContract

object FactoryProvider {

    private val factoryCache = mutableMapOf<String, Any>()
    private val factoryInstances = mutableMapOf<Class<*>, Any>()
    private val factoryRegistry = mutableMapOf<Class<*>, Class<*>>()

    inline fun <reified T : Any, reified F : FactoryContract<T>> registerFactory() {
        registerFactory(T::class.java, F::class.java)
    }

    fun <T : Any, F : FactoryContract<T>> registerFactory(targetType: Class<T>, factoryClass: Class<F>) {
        factoryRegistry[targetType] = factoryClass
    }

    inline fun <reified T : Any> createObject(cache: Boolean = false, key: String = "", vararg params: Any?): T {
        return createObject(T::class.java, cache, key, *params)
    }

    inline fun <reified T : Any> getFactory(): T {
        return getFactory(T::class.java)
    }

    fun clearCache() {
        factoryCache.clear()
    }

    fun clearAll() {
        factoryCache.clear()
        factoryInstances.clear()
        factoryRegistry.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> createObject(targetType: Class<T>, cache: Boolean, key: String, vararg params: Any?): T {
        val factory = getFactoryForType(targetType)

        return when {
            cache && key.isNotEmpty() -> getCachedObject(key) { factory.create(*params) } as T
            cache && key.isEmpty() -> throw IllegalArgumentException("캐싱을 사용할 때는 키가 필요합니다.")
            else -> factory.create(*params)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFactory(targetType: Class<T>): T {
        val factoryClass = getFactoryClass(targetType)
        return getFactoryInstance(factoryClass) as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFactoryForType(targetType: Class<T>): FactoryContract<T> {
        val factoryClass = getFactoryClass(targetType)
        return getFactoryInstance(factoryClass) as FactoryContract<T>
    }

    fun getFactoryClass(targetType: Class<*>): Class<*> {
        return factoryRegistry[targetType]
            ?: throw IllegalArgumentException("${targetType.simpleName}에 대한 팩토리가 등록되지 않았습니다.")
    }

    fun getFactoryInstance(factoryClass: Class<*>): Any {
        return factoryInstances.getOrPut(factoryClass) {
            createFactoryInstance(factoryClass)
        }
    }

    fun createFactoryInstance(factoryClass: Class<*>): Any {
        val instance = factoryClass.getDeclaredConstructor().newInstance()
        validateFactory(instance)
        return instance
    }

    fun getCachedObject(key: String, creator: () -> Any): Any {
        return factoryCache.getOrPut(key) { creator() }
    }

    fun validateFactory(factory: Any) {
        val factoryClass = factory::class.java

        validateAnnotation(factoryClass)
        validateContract(factoryClass)
    }

    fun validateAnnotation(factoryClass: Class<*>) {
        factoryClass.getAnnotation(Factory::class.java)
            ?: throw IllegalArgumentException("클래스 ${factoryClass.simpleName}에 @Factory 어노테이션이 없습니다.")
    }

    fun validateContract(factoryClass: Class<*>) {
        if (!FactoryContract::class.java.isAssignableFrom(factoryClass)) {
            throw IllegalArgumentException("팩토리 클래스 ${factoryClass.simpleName}는 FactoryContract 인터페이스를 구현해야 합니다.")
        }
    }
}