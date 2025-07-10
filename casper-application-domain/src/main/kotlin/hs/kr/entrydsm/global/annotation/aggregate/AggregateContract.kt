package hs.kr.entrydsm.global.annotation.aggregate

interface AggregateContract {
    fun getContext(): String
    fun getId(): Any
}