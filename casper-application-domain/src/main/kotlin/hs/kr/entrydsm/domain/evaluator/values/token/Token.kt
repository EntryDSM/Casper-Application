package hs.kr.entrydsm.domain.evaluator.values.token

data class Token(
    val symbol: Symbol,
    val value: String,
    val position: Int = 0
)