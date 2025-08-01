package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 함수 인수 목록을 나타내는 AST 노드입니다.
 *
 * 파싱 과정에서 함수 인수들을 그룹화하고 관리하는 데 사용되며,
 * 최종적으로는 FunctionCallNode에 통합됩니다.
 *
 * @property arguments 인수 목록
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class ArgumentsNode(
    val arguments: List<ASTNode>
) : ASTNode() {
    
    init {
        require(arguments.size <= MAX_ARGUMENTS) { "인수 개수가 최대 허용량을 초과했습니다: ${arguments.size} > $MAX_ARGUMENTS" }
    }
    
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitArguments(this)

    override fun getVariables(): Set<String> = arguments.flatMap { it.getVariables() }.toSet()

    override fun getChildren(): List<ASTNode> = arguments

    override fun getDepth(): Int = 1 + (arguments.maxOfOrNull { it.getDepth() } ?: 0)

    override fun getNodeCount(): Int = 1 + arguments.sumOf { it.getNodeCount() }

    override fun isLiteral(): Boolean = false

    override fun isOperator(): Boolean = false

    override fun isFunctionCall(): Boolean = false

    override fun isConditional(): Boolean = false

    override fun copy(): ASTNode = ArgumentsNode(arguments.map { it.copy() })

    override fun toSimpleString(): String = arguments.joinToString(", ") { it.toSimpleString() }

    override fun isStructurallyEqual(other: ASTNode): Boolean {
        return other is ArgumentsNode && 
               this.arguments.size == other.arguments.size &&
               this.arguments.zip(other.arguments).all { (thisArg, otherArg) -> 
                   thisArg.isStructurallyEqual(otherArg) 
               }
    }

    /**
     * 인수 개수를 반환합니다.
     *
     * @return 인수 개수
     */
    fun getArgumentCount(): Int = arguments.size

    /**
     * 인수가 비어있는지 확인합니다.
     *
     * @return 인수가 비어있으면 true
     */
    fun isEmpty(): Boolean = arguments.isEmpty()

    /**
     * 인수가 하나인지 확인합니다.
     *
     * @return 인수가 하나이면 true
     */
    fun isSingle(): Boolean = arguments.size == 1

    /**
     * 인수가 여러 개인지 확인합니다.
     *
     * @return 인수가 여러 개이면 true
     */
    fun isMultiple(): Boolean = arguments.size > 1

    /**
     * 특정 인덱스의 인수를 반환합니다.
     *
     * @param index 인수 인덱스
     * @return 해당 인덱스의 인수, 범위를 벗어나면 null
     */
    fun getArgument(index: Int): ASTNode? {
        return if (index in 0 until arguments.size) arguments[index] else null
    }

    /**
     * 첫 번째 인수를 반환합니다.
     *
     * @return 첫 번째 인수, 없으면 null
     */
    fun getFirst(): ASTNode? = arguments.firstOrNull()

    /**
     * 마지막 인수를 반환합니다.
     *
     * @return 마지막 인수, 없으면 null
     */
    fun getLast(): ASTNode? = arguments.lastOrNull()

    /**
     * 인수를 추가합니다.
     *
     * @param argument 추가할 인수
     * @return 새로운 ArgumentsNode
     */
    fun addArgument(argument: ASTNode): ArgumentsNode {
        return ArgumentsNode(arguments + argument)
    }

    /**
     * 특정 인덱스에 인수를 삽입합니다.
     *
     * @param index 삽입할 인덱스
     * @param argument 삽입할 인수
     * @return 새로운 ArgumentsNode
     */
    fun insertArgument(index: Int, argument: ASTNode): ArgumentsNode {
        require(index in 0..arguments.size) { "인덱스가 범위를 벗어났습니다: $index" }
        val newArguments = arguments.toMutableList()
        newArguments.add(index, argument)
        return ArgumentsNode(newArguments)
    }

    /**
     * 특정 인덱스의 인수를 제거합니다.
     *
     * @param index 제거할 인수의 인덱스
     * @return 새로운 ArgumentsNode
     */
    fun removeArgument(index: Int): ArgumentsNode {
        require(index in 0 until arguments.size) { "인덱스가 범위를 벗어났습니다: $index" }
        return ArgumentsNode(arguments.filterIndexed { i, _ -> i != index })
    }

    /**
     * 특정 인덱스의 인수를 교체합니다.
     *
     * @param index 교체할 인수의 인덱스
     * @param newArgument 새로운 인수
     * @return 새로운 ArgumentsNode
     */
    fun replaceArgument(index: Int, newArgument: ASTNode): ArgumentsNode {
        require(index in 0 until arguments.size) { "인덱스가 범위를 벗어났습니다: $index" }
        val newArguments = arguments.toMutableList()
        newArguments[index] = newArgument
        return ArgumentsNode(newArguments)
    }

    /**
     * 모든 인수가 리터럴인지 확인합니다.
     *
     * @return 모든 인수가 리터럴이면 true
     */
    fun areAllLiterals(): Boolean = arguments.all { it.isLiteral() }

    /**
     * 모든 인수가 숫자인지 확인합니다.
     *
     * @return 모든 인수가 숫자이면 true
     */
    fun areAllNumbers(): Boolean = arguments.all { it is NumberNode }

    /**
     * 모든 인수가 불리언인지 확인합니다.
     *
     * @return 모든 인수가 불리언이면 true
     */
    fun areAllBooleans(): Boolean = arguments.all { it is BooleanNode }

    /**
     * 모든 인수가 변수인지 확인합니다.
     *
     * @return 모든 인수가 변수이면 true
     */
    fun areAllVariables(): Boolean = arguments.all { it is VariableNode }

    /**
     * 특정 타입의 인수만 있는지 확인합니다.
     *
     * @param nodeType 확인할 노드 타입
     * @return 해당 타입의 인수만 있으면 true
     */
    fun hasOnlyType(nodeType: String): Boolean = arguments.all { it.getNodeType() == nodeType }

    /**
     * 특정 타입의 인수가 포함되어 있는지 확인합니다.
     *
     * @param nodeType 확인할 노드 타입
     * @return 해당 타입의 인수가 있으면 true
     */
    fun hasType(nodeType: String): Boolean = arguments.any { it.getNodeType() == nodeType }

    /**
     * 인수 타입 분포를 반환합니다.
     *
     * @return 인수 타입별 개수
     */
    fun getTypeDistribution(): Map<String, Int> {
        return arguments.groupingBy { it.getNodeType() }.eachCount()
    }

    /**
     * 인수 목록을 리스트로 반환합니다.
     *
     * @return 인수 리스트
     */
    fun toList(): List<ASTNode> = arguments.toList()

    /**
     * 인수 목록을 역순으로 반환합니다.
     *
     * @return 역순 ArgumentsNode
     */
    fun reverse(): ArgumentsNode = ArgumentsNode(arguments.reversed())

    /**
     * 인수 목록을 정렬합니다.
     *
     * @param comparator 비교 함수
     * @return 정렬된 ArgumentsNode
     */
    fun sort(comparator: Comparator<ASTNode>): ArgumentsNode {
        return ArgumentsNode(arguments.sortedWith(comparator))
    }

    /**
     * 조건을 만족하는 인수들을 필터링합니다.
     *
     * @param predicate 조건 함수
     * @return 필터링된 ArgumentsNode
     */
    fun filter(predicate: (ASTNode) -> Boolean): ArgumentsNode {
        return ArgumentsNode(arguments.filter(predicate))
    }

    /**
     * 인수들을 변환합니다.
     *
     * @param transform 변환 함수
     * @return 변환된 ArgumentsNode
     */
    fun map(transform: (ASTNode) -> ASTNode): ArgumentsNode {
        return ArgumentsNode(arguments.map(transform))
    }

    /**
     * 인수 노드의 상세 정보를 반환합니다.
     *
     * @return 인수 정보 맵
     */
    fun getArgumentsInfo(): Map<String, Any> {
        return mapOf(
            "count" to getArgumentCount(),
            "isEmpty" to isEmpty(),
            "isSingle" to isSingle(),
            "isMultiple" to isMultiple(),
            "areAllLiterals" to areAllLiterals(),
            "areAllNumbers" to areAllNumbers(),
            "areAllBooleans" to areAllBooleans(),
            "areAllVariables" to areAllVariables(),
            "typeDistribution" to getTypeDistribution(),
            "argumentTypes" to arguments.map { it.getNodeType() },
            "variables" to getVariables(),
            "totalSize" to getNodeCount(),
            "maxDepth" to (arguments.maxOfOrNull { it.getDepth() } ?: 0)
        )
    }

    override fun toString(): String = arguments.joinToString(", ", "[", "]")

    companion object {
        private const val MAX_ARGUMENTS = 100

        /**
         * 빈 인수 목록을 생성합니다.
         *
         * @return 빈 ArgumentsNode
         */
        fun empty(): ArgumentsNode = ArgumentsNode(emptyList())

        /**
         * 단일 인수로 ArgumentsNode를 생성합니다.
         *
         * @param argument 인수
         * @return ArgumentsNode 인스턴스
         */
        fun single(argument: ASTNode): ArgumentsNode = ArgumentsNode(listOf(argument))

        /**
         * 여러 인수로 ArgumentsNode를 생성합니다.
         *
         * @param arguments 인수들
         * @return ArgumentsNode 인스턴스
         */
        fun of(vararg arguments: ASTNode): ArgumentsNode = ArgumentsNode(arguments.toList())

        /**
         * 리스트로부터 ArgumentsNode를 생성합니다.
         *
         * @param arguments 인수 리스트
         * @return ArgumentsNode 인스턴스
         */
        fun from(arguments: List<ASTNode>): ArgumentsNode = ArgumentsNode(arguments)

        /**
         * 인수 노드 목록의 통계를 계산합니다.
         *
         * @param nodes ArgumentsNode 목록
         * @return 통계 정보
         */
        fun calculateStatistics(nodes: List<ArgumentsNode>): Map<String, Any> {
            if (nodes.isEmpty()) {
                return mapOf(
                    "count" to 0,
                    "isEmpty" to true
                )
            }

            val argumentCounts = nodes.map { it.getArgumentCount() }
            val averageArgCount = argumentCounts.average()
            val maxArgCount = argumentCounts.maxOrNull() ?: 0
            val minArgCount = argumentCounts.minOrNull() ?: 0
            val emptyCount = nodes.count { it.isEmpty() }
            val singleCount = nodes.count { it.isSingle() }
            val multipleCount = nodes.count { it.isMultiple() }

            return mapOf(
                "count" to nodes.size,
                "averageArgCount" to averageArgCount,
                "maxArgCount" to maxArgCount,
                "minArgCount" to minArgCount,
                "emptyCount" to emptyCount,
                "singleCount" to singleCount,
                "multipleCount" to multipleCount,
                "isEmpty" to false,
                "argCountRange" to (maxArgCount - minArgCount)
            )
        }
    }
}