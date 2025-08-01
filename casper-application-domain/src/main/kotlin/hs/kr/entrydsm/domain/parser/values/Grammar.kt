package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.ast.factory.ASTBuilders
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

/**
 * 계산기 언어의 문법 규칙을 정의하는 집합 루트입니다.
 *
 * 모든 생성 규칙(Production), 시작 심볼, 확장된 생성 규칙, 터미널 및 논터미널 심볼을 포함합니다.
 * LR(1) 파서 테이블 구축의 기반이 되는 완전한 BNF 문법을 제공하며,
 * POC 코드의 모든 연산자와 구문을 지원합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "parser")
object Grammar {

    /**
     * 모든 생성 규칙 목록 (AST 빌더 포함)
     */
    val productions: List<Production> = listOf(
        // 논리합 (가장 낮은 우선순위)
        // 0: EXPR → EXPR || AND_EXPR
        Production(0, TokenType.EXPR, listOf(TokenType.EXPR, TokenType.OR, TokenType.AND_EXPR), 
            ASTBuilders.createBinaryOp("||")),
        // 1: EXPR → AND_EXPR
        Production(1, TokenType.EXPR, listOf(TokenType.AND_EXPR), ASTBuilders.Identity),
        
        // 논리곱
        // 2: AND_EXPR → AND_EXPR && COMP_EXPR
        Production(2, TokenType.AND_EXPR, listOf(TokenType.AND_EXPR, TokenType.AND, TokenType.COMP_EXPR), 
            ASTBuilders.createBinaryOp("&&")),
        // 3: AND_EXPR → COMP_EXPR
        Production(3, TokenType.AND_EXPR, listOf(TokenType.COMP_EXPR), ASTBuilders.Identity),
        
        // 비교 연산
        // 4: COMP_EXPR → COMP_EXPR == ARITH_EXPR
        Production(4, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.EQUAL, TokenType.ARITH_EXPR), 
            ASTBuilders.createBinaryOp("==")),
        // 5: COMP_EXPR → COMP_EXPR != ARITH_EXPR
        Production(5, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.NOT_EQUAL, TokenType.ARITH_EXPR), 
            ASTBuilders.createBinaryOp("!=")),
        // 6: COMP_EXPR → COMP_EXPR < ARITH_EXPR
        Production(6, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.LESS, TokenType.ARITH_EXPR), 
            ASTBuilders.createBinaryOp("<")),
        // 7: COMP_EXPR → COMP_EXPR <= ARITH_EXPR
        Production(7, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.LESS_EQUAL, TokenType.ARITH_EXPR), 
            ASTBuilders.createBinaryOp("<=")),
        // 8: COMP_EXPR → COMP_EXPR > ARITH_EXPR
        Production(8, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.GREATER, TokenType.ARITH_EXPR), 
            ASTBuilders.createBinaryOp(">")),
        // 9: COMP_EXPR → COMP_EXPR >= ARITH_EXPR
        Production(9, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.GREATER_EQUAL, TokenType.ARITH_EXPR), 
            ASTBuilders.createBinaryOp(">=")),
        // 10: COMP_EXPR → ARITH_EXPR
        Production(10, TokenType.COMP_EXPR, listOf(TokenType.ARITH_EXPR), ASTBuilders.Identity),
        
        // 산술 표현식
        // 11: ARITH_EXPR → ARITH_EXPR + TERM
        Production(11, TokenType.ARITH_EXPR, listOf(TokenType.ARITH_EXPR, TokenType.PLUS, TokenType.TERM), 
            ASTBuilders.createBinaryOp("+")),
        // 12: ARITH_EXPR → ARITH_EXPR - TERM
        Production(12, TokenType.ARITH_EXPR, listOf(TokenType.ARITH_EXPR, TokenType.MINUS, TokenType.TERM), 
            ASTBuilders.createBinaryOp("-")),
        // 13: ARITH_EXPR → TERM
        Production(13, TokenType.ARITH_EXPR, listOf(TokenType.TERM), ASTBuilders.Identity),
        
        // 14: TERM → TERM * FACTOR
        Production(14, TokenType.TERM, listOf(TokenType.TERM, TokenType.MULTIPLY, TokenType.FACTOR), 
            ASTBuilders.createBinaryOp("*")),
        // 15: TERM → TERM / FACTOR
        Production(15, TokenType.TERM, listOf(TokenType.TERM, TokenType.DIVIDE, TokenType.FACTOR), 
            ASTBuilders.createBinaryOp("/")),
        // 16: TERM → TERM % FACTOR
        Production(16, TokenType.TERM, listOf(TokenType.TERM, TokenType.MODULO, TokenType.FACTOR), 
            ASTBuilders.createBinaryOp("%")),
        // 17: TERM → FACTOR
        Production(17, TokenType.TERM, listOf(TokenType.FACTOR), ASTBuilders.Identity),
        
        // 18: FACTOR → PRIMARY ^ FACTOR (우결합)
        Production(18, TokenType.FACTOR, listOf(TokenType.PRIMARY, TokenType.POWER, TokenType.FACTOR), 
            ASTBuilders.createBinaryOp("^")),
        // 19: FACTOR → PRIMARY
        Production(19, TokenType.FACTOR, listOf(TokenType.PRIMARY), ASTBuilders.Identity),
        
        // 20: PRIMARY → ( EXPR )
        Production(20, TokenType.PRIMARY, listOf(TokenType.LEFT_PAREN, TokenType.EXPR, TokenType.RIGHT_PAREN), 
            ASTBuilders.Parenthesized),
        // 21: PRIMARY → - PRIMARY
        Production(21, TokenType.PRIMARY, listOf(TokenType.MINUS, TokenType.PRIMARY), 
            ASTBuilders.createUnaryOp("-")),
        // 22: PRIMARY → + PRIMARY
        Production(22, TokenType.PRIMARY, listOf(TokenType.PLUS, TokenType.PRIMARY), 
            ASTBuilders.createUnaryOp("+")),
        // 23: PRIMARY → ! PRIMARY
        Production(23, TokenType.PRIMARY, listOf(TokenType.NOT, TokenType.PRIMARY), 
            ASTBuilders.createUnaryOp("!")),
        // 24: PRIMARY → NUMBER
        Production(24, TokenType.PRIMARY, listOf(TokenType.NUMBER), ASTBuilders.Number),
        // 25: PRIMARY → VARIABLE
        Production(25, TokenType.PRIMARY, listOf(TokenType.VARIABLE), ASTBuilders.Variable),
        // 26: PRIMARY → IDENTIFIER
        Production(26, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER), ASTBuilders.Variable),
        // 27: PRIMARY → TRUE
        Production(27, TokenType.PRIMARY, listOf(TokenType.TRUE), ASTBuilders.BooleanTrue),
        // 28: PRIMARY → FALSE
        Production(28, TokenType.PRIMARY, listOf(TokenType.FALSE), ASTBuilders.BooleanFalse),
        // 29: PRIMARY → IDENTIFIER ( ARGS )
        Production(29, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.ARGS, TokenType.RIGHT_PAREN), 
            ASTBuilders.FunctionCall),
        // 30: PRIMARY → IDENTIFIER ( )
        Production(30, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN), 
            ASTBuilders.FunctionCallEmpty),
        // 31: PRIMARY → IF ( EXPR , EXPR , EXPR )
        Production(31, TokenType.PRIMARY, listOf(
            TokenType.IF, TokenType.LEFT_PAREN, TokenType.EXPR, TokenType.COMMA, 
            TokenType.EXPR, TokenType.COMMA, TokenType.EXPR, TokenType.RIGHT_PAREN
        ), ASTBuilders.If),
        // 32: ARGS → EXPR
        Production(32, TokenType.ARGS, listOf(TokenType.EXPR), ASTBuilders.ArgsSingle),
        // 33: ARGS → ARGS , EXPR
        Production(33, TokenType.ARGS, listOf(TokenType.ARGS, TokenType.COMMA, TokenType.EXPR), 
            ASTBuilders.ArgsMultiple)
    )

    /**
     * 문법의 시작 심볼입니다.
     */
    val startSymbol: TokenType = TokenType.EXPR

    /**
     * 확장된 문법의 시작 생성 규칙입니다.
     * LR(1) 파서 구축을 위해 추가되는 규칙입니다.
     */
    val augmentedProduction: Production = Production(
        -1, 
        TokenType.START, 
        listOf(TokenType.EXPR, TokenType.DOLLAR), 
        ASTBuilders.Start
    )

    /**
     * 모든 터미널 심볼 집합입니다.
     */
    val terminals: Set<TokenType> = TokenType.getTerminals().toSet()

    /**
     * 모든 논터미널 심볼 집합입니다.
     */
    val nonTerminals: Set<TokenType> = TokenType.getNonTerminals().toSet()

    /**
     * 주어진 ID에 해당하는 생성 규칙을 반환합니다.
     *
     * @param id 조회할 생성 규칙의 ID
     * @return 해당 생성 규칙
     * @throws IndexOutOfBoundsException ID가 범위를 벗어난 경우
     */
    fun getProduction(id: Int): Production {
        require(id in productions.indices) { "생성 규칙 ID가 범위를 벗어났습니다: $id, 범위: 0-${productions.size - 1}" }
        return productions[id]
    }

    /**
     * 주어진 규칙 문자열에 해당하는 생성 규칙을 반환합니다.
     *
     * @param rule 조회할 생성 규칙 문자열 (예: "EXPR -> OR_EXPR")
     * @return 해당 생성 규칙
     * @throws IllegalArgumentException 규칙을 찾을 수 없는 경우
     */
    fun getProductionByRule(rule: String): Production {
        val production = productions.find { it.toString() == rule }
            ?: throw IllegalArgumentException("생성 규칙을 찾을 수 없습니다: $rule")
        return production
    }

    /**
     * 특정 좌변을 가진 모든 생성 규칙을 반환합니다.
     *
     * @param leftSymbol 좌변 심볼
     * @return 해당 좌변을 가진 생성 규칙들
     */
    fun getProductionsFor(leftSymbol: TokenType): List<Production> = 
        productions.filter { it.left == leftSymbol }

    /**
     * 특정 심볼을 포함하는 모든 생성 규칙을 반환합니다.
     *
     * @param symbol 포함할 심볼
     * @return 해당 심볼을 포함하는 생성 규칙들
     */
    fun getProductionsContaining(symbol: TokenType): List<Production> = 
        productions.filter { it.containsSymbol(symbol) }

    /**
     * 좌재귀 생성 규칙들을 반환합니다.
     *
     * @return 좌재귀 생성 규칙들
     */
    fun getLeftRecursiveProductions(): List<Production> = 
        productions.filter { it.isDirectLeftRecursive() }

    /**
     * 우재귀 생성 규칙들을 반환합니다.
     *
     * @return 우재귀 생성 규칙들
     */
    fun getRightRecursiveProductions(): List<Production> = 
        productions.filter { it.isDirectRightRecursive() }

    /**
     * 엡실론 생성 규칙들을 반환합니다.
     *
     * @return 엡실론 생성 규칙들
     */
    fun getEpsilonProductions(): List<Production> = 
        productions.filter { it.isEpsilonProduction() }

    /**
     * 특정 심볼이 터미널인지 확인합니다.
     *
     * @param symbol 확인할 심볼
     * @return 터미널이면 true, 아니면 false
     */
    fun isTerminal(symbol: TokenType): Boolean = symbol in terminals

    /**
     * 특정 심볼이 논터미널인지 확인합니다.
     *
     * @param symbol 확인할 심볼
     * @return 논터미널이면 true, 아니면 false
     */
    fun isNonTerminal(symbol: TokenType): Boolean = symbol in nonTerminals

    /**
     * 문법의 통계 정보를 반환합니다.
     *
     * @return 문법 통계 정보
     */
    fun getGrammarStatistics(): Map<String, Any> = mapOf(
        "productionCount" to productions.size,
        "terminalCount" to terminals.size,
        "nonTerminalCount" to nonTerminals.size,
        "startSymbol" to startSymbol,
        "leftRecursiveCount" to getLeftRecursiveProductions().size,
        "rightRecursiveCount" to getRightRecursiveProductions().size,
        "epsilonCount" to getEpsilonProductions().size,
        "avgProductionLength" to productions.map { it.length }.average(),
        "maxProductionLength" to (productions.maxOfOrNull { it.length } ?: 0),
        "minProductionLength" to (productions.minOfOrNull { it.length } ?: 0)
    )

    /**
     * 문법의 유효성을 검증합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean = try {
        // 생성 규칙 유효성 검사
        Production.validateProductions(productions) &&
        // 시작 심볼이 논터미널인지 확인
        isNonTerminal(startSymbol) &&
        // 시작 심볼을 좌변으로 하는 규칙이 있는지 확인
        getProductionsFor(startSymbol).isNotEmpty() &&
        // 터미널과 논터미널의 교집합이 없는지 확인
        (terminals intersect nonTerminals).isEmpty()
    } catch (e: Exception) {
        false
    }

    /**
     * 문법을 BNF 형태로 출력합니다.
     *
     * @return BNF 형태의 문법 문자열
     */
    fun toBNFString(): String = buildString {
        appendLine("Grammar (${productions.size} productions):")
        appendLine("Start Symbol: $startSymbol")
        appendLine("Terminals: ${terminals.joinToString(", ")}")
        appendLine("Non-terminals: ${nonTerminals.joinToString(", ")}")
        appendLine()
        appendLine("Productions:")
        productions.forEach { production ->
            appendLine("  ${production.toDetailString()}")
        }
        appendLine()
        appendLine("Augmented Production:")
        appendLine("  ${augmentedProduction.toDetailString()}")
    }

    /**
     * 문법의 간단한 요약을 반환합니다.
     *
     * @return 문법 요약 문자열
     */
    fun getSummary(): String = buildString {
        val stats = getGrammarStatistics()
        appendLine("Grammar Summary:")
        appendLine("  Productions: ${stats["productionCount"]}")
        appendLine("  Terminals: ${stats["terminalCount"]}")
        appendLine("  Non-terminals: ${stats["nonTerminalCount"]}")
        appendLine("  Start Symbol: ${stats["startSymbol"]}")
        appendLine("  Left Recursive: ${stats["leftRecursiveCount"]}")
        appendLine("  Right Recursive: ${stats["rightRecursiveCount"]}")
        appendLine("  Epsilon Productions: ${stats["epsilonCount"]}")
        appendLine("  Avg Production Length: ${"%.2f".format(stats["avgProductionLength"])}")
        append("  Valid: ${isValid()}")
    }

    /**
     * 특정 논터미널의 생성 규칙들을 BNF 형태로 출력합니다.
     *
     * @param nonTerminal 출력할 논터미널
     * @return BNF 형태의 생성 규칙 문자열
     */
    fun getProductionsBNF(nonTerminal: TokenType): String {
        require(isNonTerminal(nonTerminal)) { "논터미널이 아닙니다: $nonTerminal" }
        
        val productionsForSymbol = getProductionsFor(nonTerminal)
        if (productionsForSymbol.isEmpty()) {
            return "$nonTerminal → (no productions)"
        }
        
        return buildString {
            append("$nonTerminal → ")
            productionsForSymbol.forEachIndexed { index, production ->
                if (index > 0) append(" | ")
                append(if (production.right.isEmpty()) "ε" else production.right.joinToString(" "))
            }
        }
    }

    /**
     * 문자열 표현으로 생성 규칙을 찾습니다.
     *
     * @param productionString 생성 규칙의 문자열 표현 (예: "EXPR -> OR_EXPR")
     * @return 해당하는 Production 객체
     * @throws IllegalArgumentException 해당하는 생성 규칙을 찾을 수 없는 경우
     */
    fun getProduction(productionString: String): Production {
        return productions.find { production ->
            val leftStr = production.left.toString()
            val rightStr = if (production.right.isEmpty()) "ε" else production.right.joinToString(" ")
            val fullStr = "$leftStr -> $rightStr"
            fullStr == productionString || fullStr.replace("->", "→") == productionString
        } ?: throw IllegalArgumentException("Production not found: $productionString")
    }

    init {
        // 문법 유효성 검사
        require(isValid()) { "문법이 유효하지 않습니다" }
    }
}