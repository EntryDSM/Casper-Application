package hs.kr.entrydsm.domain.ast.services

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.domain.ast.values.NodeSize
import hs.kr.entrydsm.domain.ast.values.NodeType
import hs.kr.entrydsm.domain.ast.values.TreeDepth
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * AST 트리를 순회하는 서비스입니다.
 *
 * 다양한 순회 방법을 제공하며, 순회 중 특정 조건을 만족하는 
 * 노드를 찾거나 변형하는 기능을 지원합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Service(
    name = "AST 트리 순회 서비스",
    type = ServiceType.DOMAIN_SERVICE
)
class TreeTraverser {
    
    /**
     * 전위 순회를 수행합니다.
     *
     * @param root 순회할 루트 노드
     * @param visitor 방문자
     */
    fun preOrderTraversal(root: ASTNode, visitor: ASTVisitor<Unit>) {
        visitNode(root, visitor)
        root.getChildren().forEach { child ->
            preOrderTraversal(child, visitor)
        }
    }
    
    /**
     * 중위 순회를 수행합니다 (이진 트리에 적합).
     *
     * @param root 순회할 루트 노드
     * @param visitor 방문자
     */
    fun inOrderTraversal(root: ASTNode, visitor: ASTVisitor<Unit>) {
        val children = root.getChildren()
        
        if (children.isNotEmpty()) {
            inOrderTraversal(children[0], visitor)
        }
        
        visitNode(root, visitor)
        
        if (children.size > 1) {
            for (i in 1 until children.size) {
                inOrderTraversal(children[i], visitor)
            }
        }
    }
    
    /**
     * 후위 순회를 수행합니다.
     *
     * @param root 순회할 루트 노드
     * @param visitor 방문자
     */
    fun postOrderTraversal(root: ASTNode, visitor: ASTVisitor<Unit>) {
        root.getChildren().forEach { child ->
            postOrderTraversal(child, visitor)
        }
        visitNode(root, visitor)
    }
    
    /**
     * 레벨 순회를 수행합니다.
     *
     * @param root 순회할 루트 노드
     * @param visitor 방문자
     */
    fun levelOrderTraversal(root: ASTNode, visitor: ASTVisitor<Unit>) {
        val queue = mutableListOf<ASTNode>()
        queue.add(root)
        
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            visitNode(current, visitor)
            queue.addAll(current.getChildren())
        }
    }
    
    /**
     * 깊이 우선 탐색을 수행합니다.
     *
     * @param root 탐색할 루트 노드
     * @param condition 조건 함수
     * @return 조건을 만족하는 첫 번째 노드
     */
    fun depthFirstSearch(root: ASTNode, condition: (ASTNode) -> Boolean): ASTNode? {
        if (condition(root)) {
            return root
        }
        
        for (child in root.getChildren()) {
            val result = depthFirstSearch(child, condition)
            if (result != null) {
                return result
            }
        }
        
        return null
    }
    
    /**
     * 너비 우선 탐색을 수행합니다.
     *
     * @param root 탐색할 루트 노드
     * @param condition 조건 함수
     * @return 조건을 만족하는 첫 번째 노드
     */
    fun breadthFirstSearch(root: ASTNode, condition: (ASTNode) -> Boolean): ASTNode? {
        val queue = mutableListOf<ASTNode>()
        queue.add(root)
        
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (condition(current)) {
                return current
            }
            queue.addAll(current.getChildren())
        }
        
        return null
    }
    
    /**
     * 조건을 만족하는 모든 노드를 찾습니다.
     *
     * @param root 탐색할 루트 노드
     * @param condition 조건 함수
     * @return 조건을 만족하는 모든 노드
     */
    fun findAll(root: ASTNode, condition: (ASTNode) -> Boolean): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        
        if (condition(root)) {
            result.add(root)
        }
        
        for (child in root.getChildren()) {
            result.addAll(findAll(child, condition))
        }
        
        return result
    }
    
    /**
     * 특정 타입의 모든 노드를 찾습니다.
     *
     * @param root 탐색할 루트 노드
     * @param nodeClass 찾을 노드 클래스
     * @return 해당 타입의 모든 노드
     */
    fun <T : ASTNode> findByType(root: ASTNode, nodeClass: Class<T>): List<T> {
        val result = mutableListOf<T>()
        
        if (nodeClass.isInstance(root)) {
            @Suppress("UNCHECKED_CAST")
            result.add(root as T)
        }
        
        for (child in root.getChildren()) {
            result.addAll(findByType(child, nodeClass))
        }
        
        return result
    }
    
    /**
     * 노드의 경로를 찾습니다.
     *
     * @param root 루트 노드
     * @param target 찾을 노드
     * @return 루트에서 타겟까지의 경로
     */
    fun findPath(root: ASTNode, target: ASTNode): List<ASTNode>? {
        val path = mutableListOf<ASTNode>()
        
        if (findPathHelper(root, target, path)) {
            return path
        }
        
        return null
    }
    
    /**
     * 경로 찾기 헬퍼 함수
     */
    private fun findPathHelper(current: ASTNode, target: ASTNode, path: MutableList<ASTNode>): Boolean {
        path.add(current)
        
        if (current == target) {
            return true
        }
        
        for (child in current.getChildren()) {
            if (findPathHelper(child, target, path)) {
                return true
            }
        }
        
        path.removeAt(path.size - 1)
        return false
    }
    
    /**
     * 특정 깊이의 모든 노드를 찾습니다.
     *
     * @param root 루트 노드
     * @param depth 찾을 깊이
     * @return 해당 깊이의 모든 노드
     */
    fun findAtDepth(root: ASTNode, depth: Int): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        findAtDepthHelper(root, depth, 0, result)
        return result
    }
    
    /**
     * 깊이별 노드 찾기 헬퍼 함수
     */
    private fun findAtDepthHelper(current: ASTNode, targetDepth: Int, currentDepth: Int, result: MutableList<ASTNode>) {
        if (currentDepth == targetDepth) {
            result.add(current)
            return
        }
        
        for (child in current.getChildren()) {
            findAtDepthHelper(child, targetDepth, currentDepth + 1, result)
        }
    }
    
    /**
     * 리프 노드들을 찾습니다.
     *
     * @param root 루트 노드
     * @return 모든 리프 노드
     */
    fun findLeaves(root: ASTNode): List<ASTNode> {
        return findAll(root) { it.isLeaf() }
    }
    
    /**
     * 가장 깊은 노드를 찾습니다.
     *
     * @param root 루트 노드
     * @return 가장 깊은 노드와 그 깊이
     */
    fun findDeepestNode(root: ASTNode): Pair<ASTNode, TreeDepth> {
        var deepestNode = root
        var maxDepth = TreeDepth.zero()
        
        findDeepestNodeHelper(root, TreeDepth.zero(), { node, depth ->
            if (depth.isGreaterThan(maxDepth)) {
                deepestNode = node
                maxDepth = depth
            }
        })
        
        return Pair(deepestNode, maxDepth)
    }
    
    /**
     * 가장 깊은 노드 찾기 헬퍼 함수
     */
    private fun findDeepestNodeHelper(current: ASTNode, currentDepth: TreeDepth, callback: (ASTNode, TreeDepth) -> Unit) {
        callback(current, currentDepth)
        
        for (child in current.getChildren()) {
            findDeepestNodeHelper(child, currentDepth.increment(), callback)
        }
    }
    
    /**
     * 트리의 통계를 계산합니다.
     *
     * @param root 루트 노드
     * @return 트리 통계
     */
    fun calculateStatistics(root: ASTNode): TreeStatistics {
        var nodeCount = 0
        var leafCount = 0
        var maxDepth = TreeDepth.zero()
        var totalDepth = 0
        val nodeTypeCounts = mutableMapOf<NodeType, Int>()
        
        // 깊이를 추적하면서 순회하는 헬퍼 함수
        fun traverseWithDepth(node: ASTNode, currentDepth: Int) {
            nodeCount++
            totalDepth += currentDepth
            
            // 최대 깊이 업데이트
            if (currentDepth > maxDepth.value) {
                maxDepth = TreeDepth.of(currentDepth)
            }
            
            // 노드 타입별 처리
            when (node) {
                is hs.kr.entrydsm.domain.ast.entities.NumberNode -> {
                    leafCount++
                    updateNodeTypeCount(NodeType.NUMBER, nodeTypeCounts)
                }
                is hs.kr.entrydsm.domain.ast.entities.BooleanNode -> {
                    leafCount++
                    updateNodeTypeCount(NodeType.BOOLEAN, nodeTypeCounts)
                }
                is hs.kr.entrydsm.domain.ast.entities.VariableNode -> {
                    leafCount++
                    updateNodeTypeCount(NodeType.VARIABLE, nodeTypeCounts)
                }
                is hs.kr.entrydsm.domain.ast.entities.BinaryOpNode -> {
                    updateNodeTypeCount(NodeType.BINARY_OP, nodeTypeCounts)
                    // 자식 노드들을 더 깊은 레벨에서 순회
                    traverseWithDepth(node.left, currentDepth + 1)
                    traverseWithDepth(node.right, currentDepth + 1)
                }
                is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode -> {
                    updateNodeTypeCount(NodeType.UNARY_OP, nodeTypeCounts)
                    // 자식 노드를 더 깊은 레벨에서 순회
                    traverseWithDepth(node.operand, currentDepth + 1)
                }
                is hs.kr.entrydsm.domain.ast.entities.FunctionCallNode -> {
                    updateNodeTypeCount(NodeType.FUNCTION_CALL, nodeTypeCounts)
                    // 모든 인수들을 더 깊은 레벨에서 순회
                    node.args.forEach { arg ->
                        traverseWithDepth(arg, currentDepth + 1)
                    }
                }
                is hs.kr.entrydsm.domain.ast.entities.IfNode -> {
                    updateNodeTypeCount(NodeType.IF, nodeTypeCounts)
                    // 조건, 참 값, 거짓 값을 더 깊은 레벨에서 순회
                    traverseWithDepth(node.condition, currentDepth + 1)
                    traverseWithDepth(node.trueValue, currentDepth + 1)
                    traverseWithDepth(node.falseValue, currentDepth + 1)
                }
                is hs.kr.entrydsm.domain.ast.entities.ArgumentsNode -> {
                    updateNodeTypeCount(NodeType.ARGUMENTS, nodeTypeCounts)
                    // 모든 인수들을 더 깊은 레벨에서 순회
                    node.arguments.forEach { arg ->
                        traverseWithDepth(arg, currentDepth + 1)
                    }
                }
            }
        }
        
        // 루트 노드부터 깊이 0에서 시작
        traverseWithDepth(root, 0)
        
        return TreeStatistics(
            nodeCount = NodeSize.of(nodeCount),
            leafCount = NodeSize.of(leafCount),
            maxDepth = maxDepth,
            averageDepth = if (nodeCount > 0) TreeDepth.of(totalDepth / nodeCount) else TreeDepth.zero(),
            nodeTypeCounts = nodeTypeCounts.toMap()
        )
    }
    
    /**
     * 노드 타입 카운트 업데이트
     */
    private fun updateNodeTypeCount(type: NodeType, counts: MutableMap<NodeType, Int>) {
        counts[type] = counts.getOrDefault(type, 0) + 1
    }
    
    /**
     * 노드 방문 처리
     */
    private fun visitNode(node: ASTNode, visitor: ASTVisitor<Unit>) {
        node.accept(visitor)
    }
    
    /**
     * 트리 통계 데이터 클래스
     */
    data class TreeStatistics(
        val nodeCount: NodeSize,
        val leafCount: NodeSize,
        val maxDepth: TreeDepth,
        val averageDepth: TreeDepth,
        val nodeTypeCounts: Map<NodeType, Int>
    ) {
        /**
         * 리프 노드 비율을 계산합니다.
         */
        fun getLeafRatio(): Double {
            return if (nodeCount.value > 0) {
                leafCount.value.toDouble() / nodeCount.value.toDouble()
            } else {
                0.0
            }
        }
        
        /**
         * 가장 많은 노드 타입을 반환합니다.
         */
        fun getMostCommonNodeType(): NodeType? {
            return nodeTypeCounts.maxByOrNull { it.value }?.key
        }
        
        /**
         * 트리 밀도를 계산합니다.
         */
        fun getTreeDensity(): Double {
            return if (maxDepth.value > 0) {
                nodeCount.value.toDouble() / maxDepth.value.toDouble()
            } else {
                0.0
            }
        }
    }
}