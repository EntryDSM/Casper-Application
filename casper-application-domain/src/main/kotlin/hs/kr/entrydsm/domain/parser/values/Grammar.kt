package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.ast.factory.ASTBuilders
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

/**
 * Grammar에서 사용하는 상수 모음
 */

/**
 * 계산기 언어의 문법 규칙을 정의하는 집합 루트입니다.
 *
 * 모든 생성 규칙(Production), 시작 심볼, 확장된 생성 규칙, 터미널 및 논터미널 심볼을 포함합니다.
 * LR(1) 파서 테이블 구축의 기반이 되는 완전한 BNF 문법을 제공하며,
 * POC 코드의 모든 연산자와 구문을 지원합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "parser")
object Grammar {

    /**
     * 모든 생성 규칙 목록 (AST 빌더 포함)
     */
    val productions: List<Production> = listOf(
        // 논리합
        Production(0, TokenType.EXPR, listOf(TokenType.EXPR, TokenType.OR, TokenType.AND_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_OR)),
        Production(1, TokenType.EXPR, listOf(TokenType.AND_EXPR), ASTBuilders.Identity),

        // 논리곱
        Production(2, TokenType.AND_EXPR, listOf(TokenType.AND_EXPR, TokenType.AND, TokenType.COMP_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_AND)),
        Production(3, TokenType.AND_EXPR, listOf(TokenType.COMP_EXPR), ASTBuilders.Identity),

        // 비교 연산
        Production(4, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.EQUAL, TokenType.ARITH_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_EQ)),
        Production(5, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.NOT_EQUAL, TokenType.ARITH_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_NEQ)),
        Production(6, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.LESS, TokenType.ARITH_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_LT)),
        Production(7, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.LESS_EQUAL, TokenType.ARITH_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_LTE)),
        Production(8, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.GREATER, TokenType.ARITH_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_GT)),
        Production(9, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.GREATER_EQUAL, TokenType.ARITH_EXPR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_GTE)),
        Production(10, TokenType.COMP_EXPR, listOf(TokenType.ARITH_EXPR), ASTBuilders.Identity),

        // 산술 연산
        Production(11, TokenType.ARITH_EXPR, listOf(TokenType.ARITH_EXPR, TokenType.PLUS, TokenType.TERM),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_ADD)),
        Production(12, TokenType.ARITH_EXPR, listOf(TokenType.ARITH_EXPR, TokenType.MINUS, TokenType.TERM),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_SUB)),
        Production(13, TokenType.ARITH_EXPR, listOf(TokenType.TERM), ASTBuilders.Identity),

        Production(14, TokenType.TERM, listOf(TokenType.TERM, TokenType.MULTIPLY, TokenType.FACTOR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_MUL)),
        Production(15, TokenType.TERM, listOf(TokenType.TERM, TokenType.DIVIDE, TokenType.FACTOR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_DIV)),
        Production(16, TokenType.TERM, listOf(TokenType.TERM, TokenType.MODULO, TokenType.FACTOR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_MOD)),
        Production(17, TokenType.TERM, listOf(TokenType.FACTOR), ASTBuilders.Identity),

        // 거듭제곱
        Production(18, TokenType.FACTOR, listOf(TokenType.PRIMARY, TokenType.POWER, TokenType.FACTOR),
            ASTBuilders.createBinaryOp(GrammarConsts.OP_POW)),
        Production(19, TokenType.FACTOR, listOf(TokenType.PRIMARY), ASTBuilders.Identity),

        // 단항 연산 / 기본 항
        Production(20, TokenType.PRIMARY, listOf(TokenType.LEFT_PAREN, TokenType.EXPR, TokenType.RIGHT_PAREN),
            ASTBuilders.Parenthesized),
        Production(21, TokenType.PRIMARY, listOf(TokenType.MINUS, TokenType.PRIMARY),
            ASTBuilders.createUnaryOp(GrammarConsts.OP_SUB)),
        Production(22, TokenType.PRIMARY, listOf(TokenType.PLUS, TokenType.PRIMARY),
            ASTBuilders.createUnaryOp(GrammarConsts.OP_ADD)),
        Production(23, TokenType.PRIMARY, listOf(TokenType.NOT, TokenType.PRIMARY),
            ASTBuilders.createUnaryOp(GrammarConsts.OP_NOT)),
        Production(24, TokenType.PRIMARY, listOf(TokenType.NUMBER), ASTBuilders.Number),
        Production(25, TokenType.PRIMARY, listOf(TokenType.VARIABLE), ASTBuilders.Variable),
        Production(26, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER), ASTBuilders.Variable),
        Production(27, TokenType.PRIMARY, listOf(TokenType.TRUE), ASTBuilders.BooleanTrue),
        Production(28, TokenType.PRIMARY, listOf(TokenType.FALSE), ASTBuilders.BooleanFalse),
        Production(29, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.ARGS, TokenType.RIGHT_PAREN),
            ASTBuilders.FunctionCall),
        Production(30, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN),
            ASTBuilders.FunctionCallEmpty),
        Production(31, TokenType.PRIMARY, listOf(
            TokenType.IF, TokenType.LEFT_PAREN, TokenType.EXPR, TokenType.COMMA,
            TokenType.EXPR, TokenType.COMMA, TokenType.EXPR, TokenType.RIGHT_PAREN
        ), ASTBuilders.If),
        Production(32, TokenType.ARGS, listOf(TokenType.EXPR), ASTBuilders.ArgsSingle),
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
     */
    fun getProduction(id: Int): Production {
        if (id !in productions.indices) {
            throw ParserException.productionIdOutOfRange(id = id, total = productions.size)
        }
        return productions[id]
    }

    /**
     * 주어진 규칙 문자열에 해당하는 생성 규칙을 반환합니다.
     */
    fun getProductionByRule(rule: String): Production {
        return productions.find { it.toString() == rule }
            ?: throw ParserException.productionNotFound(rule)
    }

    /**
     * 특정 좌변을 가진 모든 생성 규칙을 반환합니다.
     */
    fun getProductionsFor(leftSymbol: TokenType): List<Production> =
        productions.filter { it.left == leftSymbol }

    /**
     * 특정 심볼을 포함하는 모든 생성 규칙을 반환합니다.
     */
    fun getProductionsContaining(symbol: TokenType): List<Production> =
        productions.filter { it.containsSymbol(symbol) }

    /**
     * 좌재귀 생성 규칙들을 반환합니다.
     */
    fun getLeftRecursiveProductions(): List<Production> =
        productions.filter { it.isDirectLeftRecursive() }

    /**
     * 우재귀 생성 규칙들을 반환합니다.
     */
    fun getRightRecursiveProductions(): List<Production> =
        productions.filter { it.isDirectRightRecursive() }

    /**
     * 엡실론 생성 규칙들을 반환합니다.
     */
    fun getEpsilonProductions(): List<Production> =
        productions.filter { it.isEpsilonProduction() }

    /**
     * 특정 심볼이 터미널인지 확인합니다.
     */
    fun isTerminal(symbol: TokenType): Boolean = symbol in terminals

    /**
     * 특정 심볼이 논터미널인지 확인합니다.
     */
    fun isNonTerminal(symbol: TokenType): Boolean = symbol in nonTerminals

    /**
     * 문법의 통계 정보를 반환합니다.
     */
    fun getGrammarStatistics(): Map<String, Any> = mapOf(
        GrammarConsts.KEY_PRODUCTION_COUNT to productions.size,
        GrammarConsts.KEY_TERMINAL_COUNT to terminals.size,
        GrammarConsts.KEY_NON_TERMINAL_COUNT to nonTerminals.size,
        GrammarConsts.KEY_START_SYMBOL to startSymbol,
        GrammarConsts.KEY_LEFT_RECURSIVE_COUNT to getLeftRecursiveProductions().size,
        GrammarConsts.KEY_RIGHT_RECURSIVE_COUNT to getRightRecursiveProductions().size,
        GrammarConsts.KEY_EPSILON_COUNT to getEpsilonProductions().size,
        GrammarConsts.KEY_AVG_PRODUCTION_LEN to productions.map { it.length }.average(),
        GrammarConsts.KEY_MAX_PRODUCTION_LEN to (productions.maxOfOrNull { it.length } ?: 0),
        GrammarConsts.KEY_MIN_PRODUCTION_LEN to (productions.minOfOrNull { it.length } ?: 0)
    )

    /**
     * 문법의 유효성을 검증합니다.
     */
    fun isValid(): Boolean = try {
        Production.validateProductions(productions) &&
                isNonTerminal(startSymbol) &&
                getProductionsFor(startSymbol).isNotEmpty() &&
                (terminals intersect nonTerminals).isEmpty()
    } catch (e: Exception) {
        false
    }

    /**
     * 문법을 BNF 형태로 출력합니다.
     */
    fun toBNFString(): String = buildString {
        appendLine("${GrammarConsts.TITLE_GRAMMAR} (${productions.size} productions):")
        appendLine("${GrammarConsts.LABEL_START_SYMBOL}$startSymbol")
        appendLine("${GrammarConsts.LABEL_TERMINALS}${terminals.joinToString(", ")}")
        appendLine("${GrammarConsts.LABEL_NON_TERMINALS}${nonTerminals.joinToString(", ")}")
        appendLine()
        appendLine(GrammarConsts.LABEL_PRODUCTIONS)
        productions.forEach { appendLine("  ${it.toDetailString()}") }
        appendLine()
        appendLine(GrammarConsts.LABEL_AUGMENTED)
        appendLine("  ${augmentedProduction.toDetailString()}")
    }

    /**
     * 문법의 간단한 요약을 반환합니다.
     */
    fun getSummary(): String = buildString {
        val stats = getGrammarStatistics()
        appendLine(GrammarConsts.TITLE_SUMMARY)
        appendLine("${GrammarConsts.LABEL_PRODUCTIONS_SUM}${stats[GrammarConsts.KEY_PRODUCTION_COUNT]}")
        appendLine("${GrammarConsts.LABEL_TERMINALS_SUM}${stats[GrammarConsts.KEY_TERMINAL_COUNT]}")
        appendLine("${GrammarConsts.LABEL_NON_TERMINALS_SUM}${stats[GrammarConsts.KEY_NON_TERMINAL_COUNT]}")
        appendLine("${GrammarConsts.LABEL_START_SYMBOL_SUM}${stats[GrammarConsts.KEY_START_SYMBOL]}")
        appendLine("${GrammarConsts.LABEL_LEFT_REC_SUM}${stats[GrammarConsts.KEY_LEFT_RECURSIVE_COUNT]}")
        appendLine("${GrammarConsts.LABEL_RIGHT_REC_SUM}${stats[GrammarConsts.KEY_RIGHT_RECURSIVE_COUNT]}")
        appendLine("${GrammarConsts.LABEL_EPSILON_SUM}${stats[GrammarConsts.KEY_EPSILON_COUNT]}")
        appendLine("${GrammarConsts.LABEL_AVG_LEN_SUM}${"%.2f".format(stats[GrammarConsts.KEY_AVG_PRODUCTION_LEN])}")
        append("${GrammarConsts.LABEL_VALID_SUM}${isValid()}")
    }

    /**
     * 특정 논터미널의 생성 규칙들을 BNF 형태로 출력합니다.
     */
    fun getProductionsBNF(nonTerminal: TokenType): String {
        if (!isNonTerminal(nonTerminal)) throw ParserException.symbolNotNonTerminal(nonTerminal)

        val productionsForSymbol = getProductionsFor(nonTerminal)
        if (productionsForSymbol.isEmpty()) {
            return "$nonTerminal ${GrammarConsts.ARROW_UNICODE} ${GrammarConsts.NO_PRODUCTIONS}"
        }
        return buildString {
            append("$nonTerminal ${GrammarConsts.ARROW_UNICODE} ")
            productionsForSymbol.forEachIndexed { index, production ->
                if (index > 0) append(" | ")
                append(if (production.right.isEmpty()) GrammarConsts.EPSILON else production.right.joinToString(" "))
            }
        }
    }

    /**
     * 문자열 표현으로 생성 규칙을 찾습니다.
     */
    fun getProduction(productionString: String): Production {
        return productions.find { production ->
            val leftStr = production.left.toString()
            val rightStr = if (production.right.isEmpty()) GrammarConsts.EPSILON else production.right.joinToString(" ")
            val ascii = "$leftStr ${GrammarConsts.ARROW_ASCII} $rightStr"
            val unicode = "$leftStr ${GrammarConsts.ARROW_UNICODE} $rightStr"
            ascii == productionString || unicode == productionString
        } ?: throw ParserException.productionNotFound(productionString)
    }

    init {
        // 문법 유효성 검사
        if (!isValid()) throw ParserException.grammarInvalid()
    }

    object GrammarConsts {
        // 연산자 문자열
        const val OP_OR = "||"
        const val OP_AND = "&&"
        const val OP_EQ = "=="
        const val OP_NEQ = "!="
        const val OP_LT = "<"
        const val OP_LTE = "<="
        const val OP_GT = ">"
        const val OP_GTE = ">="
        const val OP_ADD = "+"
        const val OP_SUB = "-"
        const val OP_MUL = "*"
        const val OP_DIV = "/"
        const val OP_MOD = "%"
        const val OP_POW = "^"
        const val OP_NOT = "!"

        // BNF 출력용 심볼/문자열
        const val ARROW_ASCII = "->"
        const val ARROW_UNICODE = "→"
        const val EPSILON = "ε"
        const val NO_PRODUCTIONS = "(no productions)"

        // getGrammarStatistics 키
        const val KEY_PRODUCTION_COUNT = "productionCount"
        const val KEY_TERMINAL_COUNT = "terminalCount"
        const val KEY_NON_TERMINAL_COUNT = "nonTerminalCount"
        const val KEY_START_SYMBOL = "startSymbol"
        const val KEY_LEFT_RECURSIVE_COUNT = "leftRecursiveCount"
        const val KEY_RIGHT_RECURSIVE_COUNT = "rightRecursiveCount"
        const val KEY_EPSILON_COUNT = "epsilonCount"
        const val KEY_AVG_PRODUCTION_LEN = "avgProductionLength"
        const val KEY_MAX_PRODUCTION_LEN = "maxProductionLength"
        const val KEY_MIN_PRODUCTION_LEN = "minProductionLength"

        // BNF/요약 출력 문구
        const val TITLE_GRAMMAR = "Grammar"
        const val LABEL_START_SYMBOL = "Start Symbol: "
        const val LABEL_TERMINALS = "Terminals: "
        const val LABEL_NON_TERMINALS = "Non-terminals: "
        const val LABEL_PRODUCTIONS = "Productions:"
        const val LABEL_AUGMENTED = "Augmented Production:"
        const val TITLE_SUMMARY = "Grammar Summary:"
        const val LABEL_PRODUCTIONS_SUM = "  Productions: "
        const val LABEL_TERMINALS_SUM = "  Terminals: "
        const val LABEL_NON_TERMINALS_SUM = "  Non-terminals: "
        const val LABEL_START_SYMBOL_SUM = "  Start Symbol: "
        const val LABEL_LEFT_REC_SUM = "  Left Recursive: "
        const val LABEL_RIGHT_REC_SUM = "  Right Recursive: "
        const val LABEL_EPSILON_SUM = "  Epsilon Productions: "
        const val LABEL_AVG_LEN_SUM = "  Avg Production Length: "
        const val LABEL_VALID_SUM = "  Valid: "
    }
}
