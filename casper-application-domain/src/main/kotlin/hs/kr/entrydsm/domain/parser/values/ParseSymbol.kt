package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.parser.exceptions.ParserException

/**
 * 파싱 과정에서 스택에 저장되는 심볼의 타입 안전 표현을 제공하는 값 객체입니다.
 *
 * 토큰과 AST 노드를 구분하여 컴파일 타임 타입 검증을 제공하며,
 * 파싱 스택의 안전성과 정확성을 보장합니다.
 * POC 코드의 ParseSymbol을 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
sealed class ParseSymbol {

    /**
     * 심볼 타입, 크기 등 고정 값 상수를 모아둔 객체
     */
    object Constants {
        const val TOKEN_TYPE = "TOKEN"
        const val AST_TYPE = "AST"
        const val ARGUMENTS_TYPE = "ARGUMENTS"

        const val TOKEN_SIZE_OFFSET = 16
        const val AST_SIZE_OFFSET = 32
        const val ARGUMENTS_SIZE_OFFSET = 16
    }

    abstract fun getSymbolType(): String
    abstract fun getSymbolSize(): Int
    abstract fun isTerminal(): Boolean
    abstract fun getStringRepresentation(): String

    /**
     * 터미널 심볼 (토큰)을 나타내는 값 객체
     */
    data class TokenSymbol(val token: Token) : ParseSymbol() {
        init {
            require(token.value.isNotEmpty()) {
                throw ParserException.tokenValueEmpty(token)
            }
        }

        override fun getSymbolType(): String = Constants.TOKEN_TYPE
        override fun getSymbolSize(): Int = token.value.length + Constants.TOKEN_SIZE_OFFSET
        override fun isTerminal(): Boolean = true
        override fun getStringRepresentation(): String = token.toString()

        fun getTokenType() = token.type
        fun getTokenValue(): String = token.value
        fun getTokenPosition(): Int = token.position.index
        fun isTokenType(expectedType: Any): Boolean = token.type == expectedType

        companion object {
            fun of(token: Token): TokenSymbol = TokenSymbol(token)
        }
    }

    /**
     * 논터미널 심볼 (AST 노드)을 나타내는 값 객체
     */
    data class ASTSymbol(val node: ASTNode) : ParseSymbol() {
        override fun getSymbolType(): String = Constants.AST_TYPE
        override fun getSymbolSize(): Int = estimateASTSize(node)
        override fun isTerminal(): Boolean = false
        override fun getStringRepresentation(): String = node.toString()

        fun getNodeType(): String = node.javaClass.simpleName
        fun getVariables(): Set<String> = node.getVariables()
        inline fun <reified T : ASTNode> isNodeType(): Boolean = node is T
        inline fun <reified T : ASTNode> asNodeType(): T? = node as? T
        fun getDepth(): Int = 1 // 추후 실제 구현 시 수정 가능

        companion object {
            fun of(node: ASTNode): ASTSymbol = ASTSymbol(node)
            private fun estimateASTSize(node: ASTNode): Int =
                node.toString().length + Constants.AST_SIZE_OFFSET
        }
    }

    /**
     * 함수 인수 목록을 나타내는 값 객체
     */
    data class ArgumentsSymbol(val args: List<ASTNode>) : ParseSymbol() {
        init {
            require(args.isNotEmpty()) {
                throw ParserException.argumentsEmpty()
            }
        }

        override fun getSymbolType(): String = Constants.ARGUMENTS_TYPE
        override fun getSymbolSize(): Int =
            args.sumOf { it.toString().length } + Constants.ARGUMENTS_SIZE_OFFSET
        override fun isTerminal(): Boolean = false
        override fun getStringRepresentation(): String =
            "Args[${args.joinToString(", ") { it.toString() }}]"

        fun getArgumentCount(): Int = args.size
        fun getArgument(index: Int): ASTNode {
            if (index !in args.indices) {
                throw ParserException.argumentIndexOutOfRange(index, args.size)
            }
            return args[index]
        }

        fun getFirstArgument(): ASTNode = args.first()
        fun getLastArgument(): ASTNode = args.last()
        fun getAllVariables(): Set<String> =
            args.flatMap { it.getVariables() }.toSet()
        fun addArgument(newArg: ASTNode): ArgumentsSymbol = ArgumentsSymbol(args + newArg)
        fun toList(): List<ASTNode> = args.toList()

        companion object {
            fun single(arg: ASTNode): ArgumentsSymbol = ArgumentsSymbol(listOf(arg))
            fun of(args: List<ASTNode>): ArgumentsSymbol = ArgumentsSymbol(args)
            fun empty(): ArgumentsSymbol = ArgumentsSymbol(emptyList())
        }
    }

    companion object {
        fun fromToken(token: Token): ParseSymbol = TokenSymbol.of(token)
        fun fromAST(node: ASTNode): ParseSymbol = ASTSymbol.of(node)
        fun fromArguments(args: List<ASTNode>): ParseSymbol = ArgumentsSymbol.of(args)

        fun from(obj: Any): ParseSymbol = when (obj) {
            is Token -> fromToken(obj)
            is ASTNode -> fromAST(obj)
            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                fromArguments(obj as List<ASTNode>)
            }
            else -> throw ParserException.unsupportedObjectType(obj.javaClass.simpleName)
        }

        fun calculateTotalSize(symbols: List<ParseSymbol>): Int =
            symbols.sumOf { it.getSymbolSize() }

        fun countTerminals(symbols: List<ParseSymbol>): Int =
            symbols.count { it.isTerminal() }

        fun countNonTerminals(symbols: List<ParseSymbol>): Int =
            symbols.count { !it.isTerminal() }
    }
}