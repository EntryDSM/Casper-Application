package hs.kr.entrydsm.global.annotation.factory

interface FactoryContract<T> {
    fun create(vararg params: Any?): T
}