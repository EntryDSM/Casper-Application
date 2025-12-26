package hs.kr.entrydsm.application.domain.application.spi.dto

data class PagedResult<T>(
    val items: List<T>,
    val hasNextPage: Boolean,
    val totalSize: Int
)
