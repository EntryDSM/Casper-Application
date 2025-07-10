package hs.kr.entrydsm.global.annotation.factory

interface FactoryContract<T> {
    fun create(vararg params: Any?): T
    fun getContext(): String
    fun getTargetType(): Class<T>
}