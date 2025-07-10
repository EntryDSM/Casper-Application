package hs.kr.entrydsm.global.annotation.entity

interface EntityContract {
    fun getId(): Any
    fun getContext(): String
    fun getAggregateRootClass(): Class<*>
}
