package hs.kr.entrydsm.domain.ast.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.ArgumentsNode
import hs.kr.entrydsm.domain.ast.services.TreeTraverser
import hs.kr.entrydsm.domain.ast.services.TreeOptimizer
import hs.kr.entrydsm.domain.ast.factories.ASTNodeFactory
import hs.kr.entrydsm.domain.ast.specifications.ASTValiditySpec
import hs.kr.entrydsm.domain.ast.specifications.NodeStructureSpec
import hs.kr.entrydsm.domain.ast.values.NodeSize
import hs.kr.entrydsm.domain.ast.values.TreeDepth
import hs.kr.entrydsm.domain.ast.values.NodeType
import hs.kr.entrydsm.domain.ast.values.OptimizationLevel
import hs.kr.entrydsm.domain.ast.values.ASTValidationResult
import hs.kr.entrydsm.domain.ast.values.ASTOptimizationResult
import hs.kr.entrydsm.domain.ast.values.TreeStatistics
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import hs.kr.entrydsm.global.annotation.DomainEvent
import java.time.LocalDateTime
import java.util.*

/**
 * 표현식 AST를 관리하는 애그리게이트입니다.
 *
 * AST 트리의 생성, 수정, 검증, 최적화 등의 비즈니스 로직을 
 * 캡슐화하고 일관성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Aggregate(context = "ast")
class ExpressionAST private constructor(
    val id: String,
    private var root: ASTNode,
    private val traverser: TreeTraverser = TreeTraverser(),
    private val optimizer: TreeOptimizer = TreeOptimizer(),
    private val factory: ASTNodeFactory = ASTNodeFactory(),
    private val validitySpec: ASTValiditySpec = ASTValiditySpec(),
    private val structureSpec: NodeStructureSpec = NodeStructureSpec(),
    private val createdAt: LocalDateTime = LocalDateTime.now(),
    private var lastModifiedAt: LocalDateTime = LocalDateTime.now(),
    private var optimizationLevel: OptimizationLevel = OptimizationLevel.NONE,
    private var isValidated: Boolean = false,
    private var validationResult: ASTValidationResult? = null
) {
    
    // 도메인 이벤트
    private val domainEvents = mutableListOf<Any>()
    
    /**
     * 루트 노드를 반환합니다.
     */
    fun getRoot(): ASTNode = root
    
    
    /**
     * 생성 시간을 반환합니다.
     */
    fun getCreatedAt(): LocalDateTime = createdAt
    
    /**
     * 마지막 수정 시간을 반환합니다.
     */
    fun getLastModifiedAt(): LocalDateTime = lastModifiedAt
    
    /**
     * 최적화 레벨을 반환합니다.
     */
    fun getOptimizationLevel(): OptimizationLevel = optimizationLevel
    
    /**
     * 검증 여부를 반환합니다.
     */
    fun isValidated(): Boolean = isValidated
    
    /**
     * 검증 결과를 반환합니다.
     */
    fun getValidationResult(): ASTValidationResult? = validationResult
    
    /**
     * 트리 크기를 반환합니다.
     */
    fun getSize(): NodeSize = NodeSize.of(root.getSize())
    
    /**
     * 트리 깊이를 반환합니다.
     */
    fun getDepth(): TreeDepth = TreeDepth.of(root.getDepth())
    
    /**
     * 변수 목록을 반환합니다.
     */
    fun getVariables(): Set<String> = root.getVariables()
    
    /**
     * 루트 노드를 설정합니다.
     */
    fun setRoot(newRoot: ASTNode) {
        require(validitySpec.isSatisfiedBy(newRoot)) { 
            "새로운 루트 노드가 유효하지 않습니다: ${validitySpec.getWhyNotSatisfied(newRoot)}" 
        }
        
        val oldRoot = this.root
        this.root = newRoot
        this.lastModifiedAt = LocalDateTime.now()
        this.isValidated = false
        this.validationResult = null
        
        // 도메인 이벤트 발생
        addDomainEvent(mapOf(
            "eventType" to "AST_MODIFIED",
            "aggregateId" to id,
            "aggregateType" to "ExpressionAST",
            "payload" to mapOf(
                "oldRoot" to oldRoot.toString(),
                "newRoot" to newRoot.toString(),
                "modifiedAt" to LocalDateTime.now().toString()
            )
        ))
    }
    
    /**
     * AST를 검증합니다.
     */
    fun validate(): ASTValidationResult {
        val violations = mutableListOf<String>()
        
        // 유효성 검증
        if (!validitySpec.isSatisfiedBy(root)) {
            violations.add("AST 유효성 검증 실패: ${validitySpec.getWhyNotSatisfied(root)}")
        }
        
        // 구조 검증
        if (!structureSpec.isSatisfiedBy(root)) {
            violations.add("AST 구조 검증 실패: ${structureSpec.getWhyNotSatisfied(root)}")
        }
        
        // 크기 제한 검증
        if (getSize().isAtLimit()) {
            violations.add("AST 크기가 제한을 초과합니다: ${getSize().value}")
        }
        
        // 깊이 제한 검증
        if (getDepth().isAtLimit()) {
            violations.add("AST 깊이가 제한을 초과합니다: ${getDepth().value}")
        }
        
        val result = ASTValidationResult(
            isValid = violations.isEmpty(),
            violations = violations,
            validatedAt = LocalDateTime.now(),
            astId = id
        )
        
        this.isValidated = true
        this.validationResult = result
        
        // 도메인 이벤트 발생
        addDomainEvent(mapOf(
            "eventType" to "AST_VALIDATED",
            "aggregateId" to id,
            "aggregateType" to "ExpressionAST",
            "payload" to mapOf(
                "isValid" to result.isValid,
                "violations" to result.violations,
                "validatedAt" to result.validatedAt.toString()
            )
        ))
        
        return result
    }
    
    /**
     * AST를 최적화합니다.
     */
    fun optimize(level: OptimizationLevel = OptimizationLevel.FULL): ASTOptimizationResult {
        val originalSize = getSize()
        val originalDepth = getDepth()
        val originalRoot = this.root
        
        // 최적화 수행
        val optimizedRoot = when (level) {
            OptimizationLevel.NONE -> this.root
            OptimizationLevel.BASIC -> performBasicOptimization(this.root)
            OptimizationLevel.FULL -> optimizer.optimize(this.root)
        }
        
        // 루트 업데이트
        this.root = optimizedRoot
        this.optimizationLevel = level
        this.lastModifiedAt = LocalDateTime.now()
        this.isValidated = false
        this.validationResult = null
        
        val optimizedSize = getSize()
        val optimizedDepth = getDepth()
        
        val result = ASTOptimizationResult(
            originalSize = originalSize,
            optimizedSize = optimizedSize,
            originalDepth = originalDepth,
            optimizedDepth = optimizedDepth,
            level = level,
            optimizedAt = LocalDateTime.now(),
            astId = id
        )
        
        // 도메인 이벤트 발생
        addDomainEvent(mapOf(
            "eventType" to "AST_OPTIMIZED",
            "aggregateId" to id,
            "aggregateType" to "ExpressionAST",
            "payload" to mapOf(
                "originalRoot" to originalRoot.toString(),
                "optimizedRoot" to optimizedRoot.toString(),
                "originalSize" to originalSize.value,
                "optimizedSize" to optimizedSize.value,
                "level" to level.name,
                "optimizedAt" to LocalDateTime.now().toString()
            )
        ))
        
        return result
    }
    
    /**
     * 기본 최적화를 수행합니다.
     */
    private fun performBasicOptimization(node: ASTNode): ASTNode {
        // 기본적인 최적화만 수행 (상수 폴딩, 항등원소 제거)
        return when (node) {
            is IfNode -> node.optimize()
            is UnaryOpNode -> node.simplify()
            else -> node
        }
    }
    
    /**
     * 특정 노드를 찾습니다.
     */
    fun findNode(condition: (ASTNode) -> Boolean): ASTNode? {
        return traverser.depthFirstSearch(root, condition)
    }
    
    /**
     * 조건을 만족하는 모든 노드를 찾습니다.
     */
    fun findAllNodes(condition: (ASTNode) -> Boolean): List<ASTNode> {
        return traverser.findAll(root, condition)
    }
    
    /**
     * 특정 타입의 노드를 찾습니다.
     */
    fun <T : ASTNode> findNodesByType(nodeClass: Class<T>): List<T> {
        return traverser.findByType(root, nodeClass)
    }
    
    /**
     * 노드를 방문합니다.
     */
    fun accept(visitor: ASTVisitor<Unit>) {
        traverser.preOrderTraversal(root, visitor)
    }
    
    /**
     * 트리 통계를 계산합니다.
     */
    fun getStatistics(): TreeStatistics {
        val stats = traverser.calculateStatistics(root)
        return TreeStatistics(
            nodeCount = stats.nodeCount,
            leafCount = stats.leafCount,
            maxDepth = stats.maxDepth,
            averageDepth = stats.averageDepth,
            nodeTypeCounts = stats.nodeTypeCounts,
            variables = getVariables(),
            astId = id,
            calculatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 서브트리를 교체합니다.
     */
    fun replaceSubtree(target: ASTNode, replacement: ASTNode) {
        require(validitySpec.isSatisfiedBy(replacement)) { 
            "교체할 노드가 유효하지 않습니다: ${validitySpec.getWhyNotSatisfied(replacement)}" 
        }
        
        val newRoot = replaceSubtreeHelper(root, target, replacement)
        
        if (newRoot != root) {
            setRoot(newRoot)
            addDomainEvent(mapOf(
                "eventType" to "SUBTREE_REPLACED",
                "aggregateId" to id,
                "aggregateType" to "ExpressionAST",
                "payload" to mapOf(
                    "target" to target.toString(),
                    "replacement" to replacement.toString(),
                    "replacedAt" to LocalDateTime.now().toString()
                )
            ))
        }
    }
    
    /**
     * 서브트리 교체 헬퍼 함수
     */
    private fun replaceSubtreeHelper(current: ASTNode, target: ASTNode, replacement: ASTNode): ASTNode {
        if (current == target) {
            return replacement
        }
        
        return when (current) {
            is BinaryOpNode -> {
                val newLeft = replaceSubtreeHelper(current.left, target, replacement)
                val newRight = replaceSubtreeHelper(current.right, target, replacement)
                if (newLeft != current.left || newRight != current.right) {
                    factory.createBinaryOp(newLeft, current.operator, newRight)
                } else {
                    current
                }
            }
            is UnaryOpNode -> {
                val newOperand = replaceSubtreeHelper(current.operand, target, replacement)
                if (newOperand != current.operand) {
                    factory.createUnaryOp(current.operator, newOperand)
                } else {
                    current
                }
            }
            is FunctionCallNode -> {
                val newArgs = current.args.map { replaceSubtreeHelper(it, target, replacement) }
                if (newArgs != current.args) {
                    factory.createFunctionCall(current.name, newArgs)
                } else {
                    current
                }
            }
            is IfNode -> {
                val newCondition = replaceSubtreeHelper(current.condition, target, replacement)
                val newTrueValue = replaceSubtreeHelper(current.trueValue, target, replacement)
                val newFalseValue = replaceSubtreeHelper(current.falseValue, target, replacement)
                if (newCondition != current.condition || newTrueValue != current.trueValue || newFalseValue != current.falseValue) {
                    factory.createIf(newCondition, newTrueValue, newFalseValue)
                } else {
                    current
                }
            }
            is ArgumentsNode -> {
                val newArgs = current.arguments.map { replaceSubtreeHelper(it, target, replacement) }
                if (newArgs != current.arguments) {
                    factory.createArguments(newArgs)
                } else {
                    current
                }
            }
            else -> current
        }
    }
    
    /**
     * 도메인 이벤트를 추가합니다.
     */
    private fun addDomainEvent(event: Any) {
        domainEvents.add(event)
    }
    
    /**
     * 도메인 이벤트들을 반환합니다.
     */
    fun getDomainEvents(): List<Any> = domainEvents.toList()
    
    /**
     * 도메인 이벤트들을 클리어합니다.
     */
    fun clearDomainEvents() {
        domainEvents.clear()
    }
    
    /**
     * 복사본을 생성합니다.
     */
    fun copy(): ExpressionAST {
        return ExpressionAST(
            id = UUID.randomUUID().toString(),
            root = root,
            createdAt = LocalDateTime.now(),
            lastModifiedAt = LocalDateTime.now(),
            optimizationLevel = OptimizationLevel.NONE,
            isValidated = false,
            validationResult = null
        )
    }
    
    /**
     * 문자열 표현을 반환합니다.
     */
    override fun toString(): String {
        return "ExpressionAST(id='$id', size=${getSize().value}, depth=${getDepth().value})"
    }
    
    /**
     * 같은 객체인지 확인합니다.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExpressionAST) return false
        return id == other.id
    }
    
    /**
     * 해시 코드를 반환합니다.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    
    companion object {
        /**
         * 새로운 ExpressionAST를 생성합니다.
         */
        fun create(root: ASTNode): ExpressionAST {
            val ast = ExpressionAST(
                id = UUID.randomUUID().toString(),
                root = root,
                createdAt = LocalDateTime.now(),
                lastModifiedAt = LocalDateTime.now(),
                optimizationLevel = OptimizationLevel.NONE,
                isValidated = false,
                validationResult = null
            )
            
            // 도메인 이벤트 발생
            ast.addDomainEvent(mapOf(
                "eventType" to "AST_CREATED",
                "aggregateId" to ast.id,
                "aggregateType" to "ExpressionAST",
                "payload" to mapOf(
                    "root" to root.toString(),
                    "createdAt" to LocalDateTime.now().toString()
                )
            ))
            
            return ast
        }
        
        /**
         * ID로 ExpressionAST를 생성합니다.
         */
        fun createWithId(id: String, root: ASTNode): ExpressionAST {
            val ast = ExpressionAST(
                id = id,
                root = root,
                createdAt = LocalDateTime.now(),
                lastModifiedAt = LocalDateTime.now(),
                optimizationLevel = OptimizationLevel.NONE,
                isValidated = false,
                validationResult = null
            )
            
            // 도메인 이벤트 발생
            ast.addDomainEvent(mapOf(
                "eventType" to "AST_CREATED",
                "aggregateId" to id,
                "aggregateType" to "ExpressionAST",
                "payload" to mapOf(
                    "root" to root.toString(),
                    "createdAt" to LocalDateTime.now().toString()
                )
            ))
            
            return ast
        }
    }
}