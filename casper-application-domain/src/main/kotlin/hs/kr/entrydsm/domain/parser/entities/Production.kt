package hs.kr.entrydsm.domain.parser.entities

import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.domain.ast.factory.ASTBuilders
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 문법의 생성 규칙을 나타내는 엔티티입니다.
 *
 * 계산기 언어의 BNF(Backus-Naur Form) 문법 규칙을 표현하며, 좌변(논터미널),
 * 우변(심볼 시퀀스), 그리고 해당 규칙을 적용할 때 AST 노드를 구축하는 빌더를
 * 포함합니다. LR(1) 파서에서 reduce 동작 시 사용됩니다.
 *
 * @property id 생성 규칙의 고유 식별자
 * @property left 생성 규칙의 좌변 (논터미널 심볼)
 * @property right 생성 규칙의 우변 (심볼 시퀀스)
 * @property astBuilder AST 노드를 구축하는 빌더
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(context = "parser", aggregateRoot = Production::class)
data class Production(
    val id: Int,
    val left: TokenType,
    val right: List<TokenType>,
    val astBuilder: ASTBuilderContract = ASTBuilders.Identity
) {
    
    init {
        if (id < -1) {
            throw ParserException.productionIdBelowMin(id)
        }

        if (!left.isNonTerminal()) {
            throw ParserException.productionLeftNotNonTerminal(left)
        }

        if (right.isEmpty() && !isEpsilonProduction()) {
            throw ParserException.productionRightEmpty()
        }
    }

    /**
     * 생성 규칙 우변의 길이를 반환합니다.
     *
     * @return 우변 심볼의 개수
     */
    val length: Int = right.size

    /**
     * 엡실론 생성 규칙인지 확인합니다.
     *
     * @return 엡실론 생성이면 true, 아니면 false
     */
    fun isEpsilonProduction(): Boolean = right.isEmpty()

    /**
     * 특정 위치의 심볼을 반환합니다.
     *
     * @param position 심볼 위치 (0-based)
     * @return 해당 위치의 심볼
     * @throws IndexOutOfBoundsException 위치가 범위를 벗어난 경우
     */
    fun getSymbolAt(position: Int): TokenType {
        if (position !in right.indices) {
            throw ParserException.productionPositionOutOfRange(position, right.size - 1)
        }

        return right[position]
    }

    /**
     * 특정 위치까지의 심볼들을 반환합니다.
     *
     * @param endPosition 끝 위치 (포함하지 않음)
     * @return 지정된 범위의 심볼 리스트
     */
    fun getSymbolsUntil(endPosition: Int): List<TokenType> {
        if (endPosition < 0) {
            throw ParserException.endPositionNegative(endPosition)
        }

        if (endPosition > right.size) {
            throw ParserException.endPositionExceeds(endPosition, right.size)
        }

        return right.take(endPosition)
    }

    /**
     * 특정 위치부터의 심볼들을 반환합니다.
     *
     * @param startPosition 시작 위치
     * @return 지정된 위치부터의 심볼 리스트
     */
    fun getSymbolsFrom(startPosition: Int): List<TokenType> {
        if (startPosition < 0) {
            throw ParserException.startPositionNegative(startPosition)
        }

            if (startPosition > right.size) {
            throw ParserException.startPositionExceeds(startPosition, right.size)
        }

        return right.drop(startPosition)
    }

    /**
     * 우변에서 터미널 심볼의 개수를 반환합니다.
     *
     * @return 터미널 심볼 개수
     */
    fun getTerminalCount(): Int = right.count { it.isTerminal }

    /**
     * 우변에서 논터미널 심볼의 개수를 반환합니다.
     *
     * @return 논터미널 심볼 개수
     */
    fun getNonTerminalCount(): Int = right.count { it.isNonTerminal() }

    /**
     * 우변에 특정 심볼이 포함되어 있는지 확인합니다.
     *
     * @param symbol 확인할 심볼
     * @return 포함되어 있으면 true, 아니면 false
     */
    fun containsSymbol(symbol: TokenType): Boolean = symbol in right

    /**
     * 우변에서 특정 심볼의 첫 번째 위치를 찾습니다.
     *
     * @param symbol 찾을 심볼
     * @return 첫 번째 위치 (없으면 -1)
     */
    fun findSymbolPosition(symbol: TokenType): Int = right.indexOf(symbol)

    /**
     * 우변에서 특정 심볼의 모든 위치를 찾습니다.
     *
     * @param symbol 찾을 심볼
     * @return 해당 심볼의 모든 위치 리스트
     */
    fun findAllSymbolPositions(symbol: TokenType): List<Int> = 
        right.mapIndexedNotNull { index, sym -> if (sym == symbol) index else null }

    /**
     * 우변이 터미널 심볼로만 구성되어 있는지 확인합니다.
     *
     * @return 모두 터미널이면 true, 아니면 false
     */
    fun isAllTerminals(): Boolean = right.all { it.isTerminal }

    /**
     * 우변이 논터미널 심볼로만 구성되어 있는지 확인합니다.
     *
     * @return 모두 논터미널이면 true, 아니면 false
     */
    fun isAllNonTerminals(): Boolean = right.all { it.isNonTerminal() }

    /**
     * 생성 규칙이 직접 좌재귀인지 확인합니다.
     *
     * @return 직접 좌재귀이면 true, 아니면 false
     */
    fun isDirectLeftRecursive(): Boolean = right.isNotEmpty() && right[0] == left

    /**
     * 생성 규칙이 직접 우재귀인지 확인합니다.
     *
     * @return 직접 우재귀이면 true, 아니면 false
     */
    fun isDirectRightRecursive(): Boolean = right.isNotEmpty() && right.last() == left

    /**
     * AST 빌더를 사용하여 AST 노드를 구축합니다.
     *
     * @param children 자식 심볼들
     * @return 구축된 AST 노드 또는 심볼
     * @throws IllegalArgumentException 자식 심볼의 개수나 타입이 올바르지 않은 경우
     */
    fun buildAST(children: List<Any>): Any {
        if (!astBuilder.validateChildren(children)) {
            throw ParserException.astBuilderValidationFailed(id, children.size)
        }
        return astBuilder.build(children)
    }


    /**
     * 생성 규칙을 BNF 형태의 문자열로 표현합니다.
     *
     * @return "좌변 → 우변" 형태의 문자열
     */
    fun toBNFString(): String = "$left → ${if (right.isEmpty()) "ε" else right.joinToString(" ")}"

    /**
     * 생성 규칙을 상세 정보가 포함된 문자열로 표현합니다.
     *
     * @return "ID: 좌변 → 우변 [빌더정보]" 형태의 문자열
     */
    fun toDetailString(): String = "$id: ${toBNFString()} [${astBuilder.getBuilderName()}]"

    /**
     * 생성 규칙의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "id" to id,
        "length" to length,
        "terminalCount" to getTerminalCount(),
        "nonTerminalCount" to getNonTerminalCount(),
        "isEpsilon" to isEpsilonProduction(),
        "isDirectLeftRecursive" to isDirectLeftRecursive(),
        "isDirectRightRecursive" to isDirectRightRecursive(),
        "builderType" to astBuilder.getBuilderName()
    )

    /**
     * 생성 규칙을 간단한 형태로 문자열 표현합니다.
     *
     * @return BNF 형태의 문자열
     */
    override fun toString(): String = toBNFString()

    companion object {
        /**
         * 엡실론 생성 규칙을 생성합니다.
         *
         * @param id 규칙 ID
         * @param left 좌변 심볼
         * @param astBuilder AST 빌더 (기본값: Identity)
         * @return 엡실론 생성 규칙
         */
        fun epsilon(id: Int, left: TokenType, astBuilder: ASTBuilderContract = ASTBuilders.Identity): Production =
            Production(id, left, emptyList(), astBuilder)

        /**
         * 단일 심볼 생성 규칙을 생성합니다.
         *
         * @param id 규칙 ID
         * @param left 좌변 심볼
         * @param right 우변 심볼
         * @param astBuilder AST 빌더 (기본값: Identity)
         * @return 단일 심볼 생성 규칙
         */
        fun single(id: Int, left: TokenType, right: TokenType, astBuilder: ASTBuilderContract = ASTBuilders.Identity): Production =
            Production(id, left, listOf(right), astBuilder)

        /**
         * 이항 연산자 생성 규칙을 생성합니다.
         *
         * @param id 규칙 ID
         * @param left 좌변 심볼
         * @param leftOperand 좌측 피연산자 심볼
         * @param operator 연산자 심볼
         * @param rightOperand 우측 피연산자 심볼
         * @param operatorString 연산자 문자열
         * @return 이항 연산자 생성 규칙
         */
        fun binaryOp(
            id: Int, 
            left: TokenType, 
            leftOperand: TokenType, 
            operator: TokenType, 
            rightOperand: TokenType,
            operatorString: String
        ): Production = Production(
            id, 
            left, 
            listOf(leftOperand, operator, rightOperand), 
            ASTBuilders.createBinaryOp(operatorString)
        )

        /**
         * 단항 연산자 생성 규칙을 생성합니다.
         *
         * @param id 규칙 ID
         * @param left 좌변 심볼
         * @param operator 연산자 심볼
         * @param operand 피연산자 심볼
         * @param operatorString 연산자 문자열
         * @return 단항 연산자 생성 규칙
         */
        fun unaryOp(
            id: Int, 
            left: TokenType, 
            operator: TokenType, 
            operand: TokenType,
            operatorString: String
        ): Production = Production(
            id, 
            left, 
            listOf(operator, operand), 
            ASTBuilders.createUnaryOp(operatorString)
        )

        /**
         * 생성 규칙 리스트의 유효성을 검증합니다.
         *
         * @param productions 검증할 생성 규칙 리스트
         * @return 유효하면 true, 아니면 false
         */
        fun validateProductions(productions: List<Production>): Boolean {
            if (productions.isEmpty()) return false
            
            // ID 중복 검사
            val ids = productions.map { it.id }
            if (ids.size != ids.toSet().size) return false
            
            // ID 연속성 검사 (0부터 시작하는 연속된 번호)
            val sortedIds = ids.sorted()
            if (sortedIds != (0 until productions.size).toList()) return false
            
            return true
        }
    }
}