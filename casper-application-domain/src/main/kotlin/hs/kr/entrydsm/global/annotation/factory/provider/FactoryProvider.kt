package hs.kr.entrydsm.global.annotation.factory.provider

import hs.kr.entrydsm.global.annotation.factory.Factory
import kotlin.jvm.java

object FactoryProvider {
    private val factoryCache = mutableMapOf<String, Any>()
    private val factoryInstances = mutableMapOf<Class<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> create(cache: Boolean) {

    }

    fun <T : Any>  getFactoryInstance(factory: Class<T>): Any? {
        return factoryInstances[factory]
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> getFactory(): T {
        val instance = getFactoryInstance(T::class.java)
            ?: throw IllegalStateException("${T::class.java.simpleName} 이 뱔견되지 않았습니다.")

        return instance as T
    }

    private fun validateFactory(factory: Any) {
        val factoryClass = factory::class.java
        val factoryAnnotation = factoryClass.getAnnotation(Factory::class.java)
            ?: throw IllegalArgumentException(" 클래스 ${factoryClass.name}에 @Factory 어노테이션이 없습니다.")
    }
}