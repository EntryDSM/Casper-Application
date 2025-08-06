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
    val nodeTypeCounts: Map<NodeType, Int>,
    val variables: Set<String>,
    val astId: String,
    val calculatedAt: LocalDateTime
) {
    /**
     * 가장 많은 노드 타입을 반환합니다.
     */
    fun getMostCommonNodeType(): NodeType? {
        return nodeTypeCounts.maxByOrNull { it.value }?.key
    }
    
    /**
     * 특정 노드 타입의 개수를 반환합니다.
     */
    fun getNodeCount(type: NodeType): Int {
        return nodeTypeCounts[type] ?: 0
    }
    
    /**
     * 리프 노드들의 개수를 반환합니다.
     */
    fun getLeafNodeCount(): Int {
        return NodeType.getLeafTypes().sumOf { getNodeCount(it) }
    }
    
    /**
     * 연산자 노드들의 개수를 반환합니다.
     */
    fun getOperatorNodeCount(): Int {
        return NodeType.getOperatorTypes().sumOf { getNodeCount(it) }
    }
}