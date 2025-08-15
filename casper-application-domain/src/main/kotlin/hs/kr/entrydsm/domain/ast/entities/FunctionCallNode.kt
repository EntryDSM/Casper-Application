package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 함수 호출을 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 함수 호출을 표현하며, 함수명과 인수 목록으로
 * 구성됩니다. 수학 함수(sin, cos, sqrt 등)와 사용자 정의 함수를 모두 지원하며,
 * 가변 인수와 선택적 인수도 처리할 수 있습니다. 불변 객체로 설계됩니다.
 *
 * @property name 호출할 함수의 이름
 * @property args 함수에 전달될 인수 목록 (AST 노드)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class FunctionCallNode(
    val name: String,
    val args: List<ASTNode>
) : ASTNode() {
    
    init {
        if (name.isBlank()) {
            throw ASTException.functionNameEmpty()
        }
        if (!isValidFunctionName(name)) {
            throw ASTException.invalidFunctionName(name)
        }
        if (args.size > MAX_ARGUMENTS) {
            throw ASTException.argumentCountExceeded()
        }
    }

    override fun getVariables(): Set<String> = args.flatMap { it.getVariables() }.toSet()

    override fun getChildren(): List<ASTNode> = args

    override fun isFunctionCall(): Boolean = true

    override fun getDepth(): Int = (args.maxOfOrNull { it.getDepth() } ?: 0) + 1

    override fun getNodeCount(): Int = args.sumOf { it.getNodeCount() } + 1

    override fun copy(): FunctionCallNode = FunctionCallNode(name, args.map { it.copy() })

    override fun toSimpleString(): String = "$name(${args.joinToString(", ") { it.toSimpleString() }})"

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitFunctionCall(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is FunctionCallNode && 
        this.name == other.name &&
        this.args.size == other.args.size &&
        this.args.zip(other.args).all { (a, b) -> a.isStructurallyEqual(b) }

    /**
     * 함수명이 유효한 식별자인지 확인합니다.
     *
     * @param functionName 확인할 함수명
     * @return 유효하면 true, 아니면 false
     */
    private fun isValidFunctionName(functionName: String): Boolean {
        if (functionName.isEmpty()) return false
        
        // 첫 문자는 영문자 또는 밑줄이어야 함
        if (!functionName.first().isLetter() && functionName.first() != '_') return false
        
        // 나머지 문자는 영문자, 숫자, 밑줄이어야 함
        return functionName.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * 인수 개수를 반환합니다.
     *
     * @return 인수 개수
     */
    fun getArgumentCount(): Int = args.size

    /**
     * 인수가 없는지 확인합니다.
     *
     * @return 인수가 없으면 true, 있으면 false
     */
    fun hasNoArguments(): Boolean = args.isEmpty()

    /**
     * 단일 인수를 가지는지 확인합니다.
     *
     * @return 단일 인수이면 true, 아니면 false
     */
    fun hasSingleArgument(): Boolean = args.size == 1

    /**
     * 다중 인수를 가지는지 확인합니다.
     *
     * @return 다중 인수이면 true, 아니면 false
     */
    fun hasMultipleArguments(): Boolean = args.size > 1

    /**
     * 특정 인덱스의 인수를 반환합니다.
     *
     * @param index 인수 인덱스
     * @return 해당 인덱스의 인수
     * @throws IndexOutOfBoundsException 인덱스가 범위를 벗어난 경우
     */
    fun getArgument(index: Int): ASTNode {
        if (index !in args.indices) {
            throw ASTException.indexOutOfRange()
        }
        return args[index]
    }

    /**
     * 첫 번째 인수를 반환합니다.
     *
     * @return 첫 번째 인수
     * @throws IllegalStateException 인수가 없는 경우
     */
    fun getFirstArgument(): ASTNode {
        if (args.isEmpty()) {
            throw ASTException.argumentsEmpty()
        }
        return args[0]
    }

    /**
     * 마지막 인수를 반환합니다.
     *
     * @return 마지막 인수
     * @throws IllegalStateException 인수가 없는 경우
     */
    fun getLastArgument(): ASTNode {
        if (args.isEmpty()) {
            throw ASTException.argumentsEmpty()
        }
        return args.last()
    }

    /**
     * 함수가 수학 함수인지 확인합니다.
     *
     * @return 수학 함수이면 true, 아니면 false
     */
    fun isMathFunction(): Boolean = name.lowercase() in MATH_FUNCTIONS

    /**
     * 함수가 집계 함수인지 확인합니다.
     *
     * @return 집계 함수이면 true, 아니면 false
     */
    fun isAggregateFunction(): Boolean = name.lowercase() in AGGREGATE_FUNCTIONS

    /**
     * 함수가 문자열 함수인지 확인합니다.
     *
     * @return 문자열 함수이면 true, 아니면 false
     */
    fun isStringFunction(): Boolean = name.lowercase() in STRING_FUNCTIONS

    /**
     * 함수가 사용자 정의 함수인지 확인합니다.
     *
     * @return 사용자 정의 함수이면 true, 아니면 false
     */
    fun isUserDefinedFunction(): Boolean = !isMathFunction() && !isAggregateFunction() && !isStringFunction()

    /**
     * 함수의 카테고리를 반환합니다.
     *
     * @return 함수 카테고리
     */
    fun getFunctionCategory(): FunctionCategory = when {
        isMathFunction() -> FunctionCategory.MATH
        isAggregateFunction() -> FunctionCategory.AGGREGATE
        isStringFunction() -> FunctionCategory.STRING
        else -> FunctionCategory.USER_DEFINED
    }

    /**
     * 특정 인수를 교체한 새로운 FunctionCallNode를 반환합니다.
     *
     * @param index 교체할 인수의 인덱스
     * @param newArgument 새로운 인수
     * @return 새로운 FunctionCallNode
     */
    fun withArgument(index: Int, newArgument: ASTNode): FunctionCallNode {
        if (index !in args.indices) {
            throw ASTException.indexOutOfRange()
        }
        val newArgs = args.toMutableList()
        newArgs[index] = newArgument
        return FunctionCallNode(name, newArgs)
    }

    /**
     * 인수를 추가한 새로운 FunctionCallNode를 반환합니다.
     *
     * @param newArgument 추가할 인수
     * @return 새로운 FunctionCallNode
     */
    fun withAddedArgument(newArgument: ASTNode): FunctionCallNode {
        if (args.size > MAX_ARGUMENTS) {
            throw ASTException.argumentCountExceeded()
        }
        return FunctionCallNode(name, args + newArgument)
    }

    /**
     * 인수 목록을 교체한 새로운 FunctionCallNode를 반환합니다.
     *
     * @param newArgs 새로운 인수 목록
     * @return 새로운 FunctionCallNode
     */
    fun withArguments(newArgs: List<ASTNode>): FunctionCallNode = FunctionCallNode(name, newArgs)

    /**
     * 함수명을 교체한 새로운 FunctionCallNode를 반환합니다.
     *
     * @param newName 새로운 함수명
     * @return 새로운 FunctionCallNode
     */
    fun withName(newName: String): FunctionCallNode = FunctionCallNode(newName, args)

    override fun toString(): String = toSimpleString()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return buildString {
            appendLine("${spaces}FunctionCallNode: $name")
            if (args.isNotEmpty()) {
                appendLine("${spaces}  arguments:")
                args.forEachIndexed { index, arg ->
                    appendLine("${spaces}    [$index]:")
                    if (index < args.size - 1) {
                        appendLine(arg.toTreeString(indent + 3))
                    } else {
                        append(arg.toTreeString(indent + 3))
                    }
                }
            } else {
                append("${spaces}  arguments: (none)")
            }
        }
    }

    /**
     * 함수 카테고리를 나타내는 열거형입니다.
     */
    enum class FunctionCategory {
        MATH, AGGREGATE, STRING, USER_DEFINED
    }

    companion object {
        /**
         * 최대 인수 개수입니다.
         */
        const val MAX_ARGUMENTS = 10

        /**
         * 수학 함수 목록입니다.
         */
        private val MATH_FUNCTIONS = setOf(
            "sin", "cos", "tan", "asin", "acos", "atan", "atan2",
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "exp", "log", "log10", "log2", "ln",
            "sqrt", "cbrt", "pow", "abs", "sign",
            "floor", "ceil", "round", "trunc",
            "min", "max", "clamp"
        )

        /**
         * 집계 함수 목록입니다.
         */
        private val AGGREGATE_FUNCTIONS = setOf(
            "sum", "avg", "mean", "median", "mode",
            "count", "distinct", "variance", "stddev"
        )

        /**
         * 문자열 함수 목록입니다.
         */
        private val STRING_FUNCTIONS = setOf(
            "length", "upper", "lower", "trim", "substring",
            "replace", "contains", "startswith", "endswith"
        )

        /**
         * 지원되는 모든 함수 목록을 반환합니다.
         *
         * @return 지원되는 함수 집합
         */
        fun getSupportedFunctions(): Set<String> = 
            MATH_FUNCTIONS + AGGREGATE_FUNCTIONS + STRING_FUNCTIONS

        /**
         * 특정 카테고리의 함수 목록을 반환합니다.
         *
         * @param category 함수 카테고리
         * @return 해당 카테고리의 함수 집합
         */
        fun getFunctionsByCategory(category: FunctionCategory): Set<String> = when (category) {
            FunctionCategory.MATH -> MATH_FUNCTIONS
            FunctionCategory.AGGREGATE -> AGGREGATE_FUNCTIONS
            FunctionCategory.STRING -> STRING_FUNCTIONS
            FunctionCategory.USER_DEFINED -> emptySet()
        }

        /**
         * 인수 없는 함수 호출을 생성합니다.
         *
         * @param name 함수명
         * @return FunctionCallNode 인스턴스
         */
        fun withoutArguments(name: String): FunctionCallNode = FunctionCallNode(name, emptyList())

        /**
         * 단일 인수 함수 호출을 생성합니다.
         *
         * @param name 함수명
         * @param argument 인수
         * @return FunctionCallNode 인스턴스
         */
        fun withSingleArgument(name: String, argument: ASTNode): FunctionCallNode = 
            FunctionCallNode(name, listOf(argument))

        /**
         * 다중 인수 함수 호출을 생성합니다.
         *
         * @param name 함수명
         * @param arguments 인수들
         * @return FunctionCallNode 인스턴스
         */
        fun withArguments(name: String, vararg arguments: ASTNode): FunctionCallNode = 
            FunctionCallNode(name, arguments.toList())
    }
}