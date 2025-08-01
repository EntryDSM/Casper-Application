package hs.kr.entrydsm.domain.ast.values

import java.time.LocalDateTime

/**
 * 트리 통계 데이터 클래스
 */
data class TreeStatistics(
    val nodeCount: NodeSize,
    val leafCount: NodeSize,
    val maxDepth: TreeDepth,
    val averageDepth: TreeDepth,
    val nodeTypeCounts: Map<String, Int>,
    val variables: Set<String>,
    val astId: String,
    val calculatedAt: LocalDateTime
)