package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token

/**
 * 파싱 과정에서 스택에 저장되는 심볼의 타입 안전 표현을 제공하는 값 객체입니다.
 *
 * 토큰과 AST 노드를 구분하여 컴파일 타임 타입 검증을 제공하며,
 * 파싱 스택의 안전성과 정확성을 보장합니다.
 * POC 코드의 ParseSymbol을 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
sealed class ParseSymbol {

    /**
     * 파싱 스택에서 심볼의 타입을 확인합니다.
     *
     * @return 심볼 타입 문자열
     */
    abstract fun getSymbolType(): String

    /**
     * 심볼의 크기 (메모리 사용량 추정)를 반환합니다.
     *
     * @return 추정 메모리 사용량
     */
    abstract fun getSymbolSize(): Int

    /**
     * 심볼이 터미널인지 확인합니다.
     *
     * @return 터미널이면 true
     */
    abstract fun isTerminal(): Boolean

    /**
     * 심볼을 문자열로 표현합니다.
     *
     * @return 심볼의 문자열 표현
     */
    abstract fun getStringRepresentation(): String

    /**
     * 터미널 심볼 (토큰)을 나타내는 값 객체입니다.
     *
     * @property token 터미널 심볼의 토큰
     */
    data class TokenSymbol(val token: Token) : ParseSymbol() {

        init {
            require(token.value.isNotEmpty()) { "토큰의 값은 비어있을 수 없습니다" }
        }

        override fun getSymbolType(): String = "TOKEN"

        override fun getSymbolSize(): Int = token.value.length + 16 // 대략적인 크기

        override fun isTerminal(): Boolean = true

        override fun getStringRepresentation(): String = token.toString()

        /**
         * 토큰의 타입을 반환합니다.
         *
         * @return 토큰 타입
         */
        fun getTokenType() = token.type

        /**
         * 토큰의 값을 반환합니다.
         *
         * @return 토큰 값
         */
        fun getTokenValue(): String = token.value

        /**
         * 토큰의 위치 정보를 반환합니다.
         *
         * @return 토큰 위치
         */
        fun getTokenPosition(): Int = token.position.index

        /**
         * 토큰이 특정 타입인지 확인합니다.
         *
         * @param expectedType 확인할 토큰 타입
         * @return 해당 타입이면 true
         */
        fun isTokenType(expectedType: Any): Boolean = token.type == expectedType

        companion object {
            /**
             * 토큰으로부터 TokenSymbol을 생성합니다.
             *
             * @param token 생성할 토큰
             * @return TokenSymbol 인스턴스
             */
            fun of(token: Token): TokenSymbol = TokenSymbol(token)
        }

        override fun toString(): String = "TokenSymbol($token)"
    }

    /**
     * 논터미널 심볼 (AST 노드)을 나타내는 값 객체입니다.
     *
     * @property node 논터미널 심볼의 AST 노드
     */
    data class ASTSymbol(val node: ASTNode) : ParseSymbol() {

        override fun getSymbolType(): String = "AST"

        override fun getSymbolSize(): Int = estimateASTSize(node)

        override fun isTerminal(): Boolean = false

        override fun getStringRepresentation(): String = node.toString()

        /**
         * AST 노드의 타입을 반환합니다.
         *
         * @return AST 노드 타입
         */
        fun getNodeType(): String = node.javaClass.simpleName

        /**
         * AST 노드에 포함된 변수들을 반환합니다.
         *
         * @return 변수 집합
         */
        fun getVariables(): Set<String> = node.getVariables()

        /**
         * AST 노드가 특정 타입인지 확인합니다.
         *
         * @param expectedType 확인할 노드 타입
         * @return 해당 타입이면 true
         */
        inline fun <reified T : ASTNode> isNodeType(): Boolean = node is T

        /**
         * AST 노드를 특정 타입으로 캐스팅합니다.
         *
         * @return 캐스팅된 노드 또는 null
         */
        inline fun <reified T : ASTNode> asNodeType(): T? = node as? T

        /**
         * AST 노드의 깊이를 계산합니다.
         *
         * @return AST 깊이
         */
        fun getDepth(): Int = calculateDepth(node)

        private fun calculateDepth(node: ASTNode): Int {
            // 실제 구현에서는 노드 타입에 따라 자식 노드들을 검사해야 함
            // 여기서는 간단히 1을 반환
            return 1
        }

        companion object {
            /**
             * AST 노드로부터 ASTSymbol을 생성합니다.
             *
             * @param node 생성할 AST 노드
             * @return ASTSymbol 인스턴스
             */
            fun of(node: ASTNode): ASTSymbol = ASTSymbol(node)

            /**
             * AST 노드의 크기를 추정합니다.
             *
             * @param node 크기를 추정할 노드
             * @return 추정 크기
             */
            private fun estimateASTSize(node: ASTNode): Int {
                // 단순한 크기 추정 - 실제로는 더 정교한 계산이 필요
                return node.toString().length + 32
            }
        }

        override fun toString(): String = "ASTSymbol(${getNodeType()})"
    }

    /**
     * 함수 인수 목록을 나타내는 값 객체입니다.
     *
     * @property args 함수 인수 AST 노드 목록
     */
    data class ArgumentsSymbol(val args: List<ASTNode>) : ParseSymbol() {

        init {
            require(args.isNotEmpty()) { "인수 목록은 비어있을 수 없습니다" }
        }

        override fun getSymbolType(): String = "ARGUMENTS"

        override fun getSymbolSize(): Int = args.sumOf { it.toString().length } + 16

        override fun isTerminal(): Boolean = false

        override fun getStringRepresentation(): String = 
            "Args[${args.joinToString(", ") { it.toString() }}]"

        /**
         * 인수의 개수를 반환합니다.
         *
         * @return 인수 개수
         */
        fun getArgumentCount(): Int = args.size

        /**
         * 특정 인덱스의 인수를 반환합니다.
         *
         * @param index 인수 인덱스
         * @return 해당 인덱스의 AST 노드
         * @throws IndexOutOfBoundsException 인덱스가 범위를 벗어난 경우
         */
        fun getArgument(index: Int): ASTNode {
            require(index in args.indices) { "인수 인덱스가 범위를 벗어났습니다: $index" }
            return args[index]
        }

        /**
         * 첫 번째 인수를 반환합니다.
         *
         * @return 첫 번째 인수
         */
        fun getFirstArgument(): ASTNode = args.first()

        /**
         * 마지막 인수를 반환합니다.
         *
         * @return 마지막 인수
         */
        fun getLastArgument(): ASTNode = args.last()

        /**
         * 모든 인수에 포함된 변수들을 반환합니다.
         *
         * @return 변수 집합
         */
        fun getAllVariables(): Set<String> = 
            args.flatMap { it.getVariables() }.toSet()

        /**
         * 새로운 인수를 추가한 ArgumentsSymbol을 생성합니다.
         *
         * @param newArg 추가할 인수
         * @return 새로운 ArgumentsSymbol
         */
        fun addArgument(newArg: ASTNode): ArgumentsSymbol = 
            ArgumentsSymbol(args + newArg)

        /**
         * 인수 목록을 리스트로 반환합니다.
         *
         * @return 인수 리스트
         */
        fun toList(): List<ASTNode> = args.toList()

        companion object {
            /**
             * 단일 인수로부터 ArgumentsSymbol을 생성합니다.
             *
             * @param arg 단일 인수
             * @return ArgumentsSymbol 인스턴스
             */
            fun single(arg: ASTNode): ArgumentsSymbol = ArgumentsSymbol(listOf(arg))

            /**
             * 인수 리스트로부터 ArgumentsSymbol을 생성합니다.
             *
             * @param args 인수 리스트
             * @return ArgumentsSymbol 인스턴스
             */
            fun of(args: List<ASTNode>): ArgumentsSymbol = ArgumentsSymbol(args)

            /**
             * 빈 인수 목록으로 ArgumentsSymbol을 생성합니다.
             *
             * @return 빈 ArgumentsSymbol
             */
            fun empty(): ArgumentsSymbol = ArgumentsSymbol(emptyList())
        }

        override fun toString(): String = "ArgumentsSymbol(${args.size} args)"
    }

    companion object {
        /**
         * 토큰으로부터 ParseSymbol을 생성합니다.
         *
         * @param token 토큰
         * @return TokenSymbol
         */
        fun fromToken(token: Token): ParseSymbol = TokenSymbol.of(token)

        /**
         * AST 노드로부터 ParseSymbol을 생성합니다.
         *
         * @param node AST 노드
         * @return ASTSymbol
         */
        fun fromAST(node: ASTNode): ParseSymbol = ASTSymbol.of(node)

        /**
         * 인수 목록으로부터 ParseSymbol을 생성합니다.
         *
         * @param args 인수 목록
         * @return ArgumentsSymbol
         */
        fun fromArguments(args: List<ASTNode>): ParseSymbol = ArgumentsSymbol.of(args)

        /**
         * 임의의 객체로부터 적절한 ParseSymbol을 생성합니다.
         *
         * @param obj 변환할 객체
         * @return 적절한 ParseSymbol
         * @throws IllegalArgumentException 지원하지 않는 타입인 경우
         */
        fun from(obj: Any): ParseSymbol {
            return when (obj) {
                is Token -> fromToken(obj)
                is ASTNode -> fromAST(obj)
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    fromArguments(obj as List<ASTNode>)
                }
                else -> throw IllegalArgumentException(
                    "지원하지 않는 타입입니다: ${obj.javaClass.simpleName}"
                )
            }
        }

        /**
         * ParseSymbol 목록의 전체 크기를 계산합니다.
         *
         * @param symbols ParseSymbol 목록
         * @return 전체 크기
         */
        fun calculateTotalSize(symbols: List<ParseSymbol>): Int {
            return symbols.sumOf { it.getSymbolSize() }
        }

        /**
         * ParseSymbol 목록에서 터미널 심볼의 개수를 계산합니다.
         *
         * @param symbols ParseSymbol 목록
         * @return 터미널 심볼 개수
         */
        fun countTerminals(symbols: List<ParseSymbol>): Int {
            return symbols.count { it.isTerminal() }
        }

        /**
         * ParseSymbol 목록에서 논터미널 심볼의 개수를 계산합니다.
         *
         * @param symbols ParseSymbol 목록
         * @return 논터미널 심볼 개수
         */
        fun countNonTerminals(symbols: List<ParseSymbol>): Int {
            return symbols.count { !it.isTerminal() }
        }
    }
}