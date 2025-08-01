package hs.kr.entrydsm.domain.parser.factories

import hs.kr.entrydsm.domain.ast.factory.ASTBuilders
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * Production(생산 규칙) 생성을 담당하는 팩토리 클래스입니다.
 *
 * DDD Factory 패턴을 적용하여 생산 규칙의 복잡한 생성 로직을 캡슐화하고,
 * 다양한 유형의 생산 규칙 생성 방법을 제공합니다. BNF 문법 규칙을
 * Production 객체로 변환하는 과정을 관리합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Factory(
    context = "parser",
    complexity = Complexity.NORMAL,
    cache = true
)
class ProductionFactory {

    companion object {
        private const val MAX_PRODUCTION_LENGTH = 50
        private const val MAX_PRODUCTION_COUNT = 1000
        private var nextProductionId = 0
    }

    /**
     * 기본 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param right 우변 심볼들
     * @param builder AST 빌더
     * @return 생성된 생산 규칙
     */
    fun createProduction(
        id: Int,
        left: TokenType,
        right: List<TokenType>,
        builder: ASTBuilderContract = ASTBuilders.Identity
    ): Production {
        validateProductionData(id, left, right)
        
        return Production(
            id = id,
            left = left,
            right = right,
            astBuilder = builder
        )
    }

    /**
     * 자동 ID 할당으로 생산 규칙을 생성합니다.
     *
     * @param left 좌변 심볼
     * @param right 우변 심볼들
     * @param builder AST 빌더
     * @return 생성된 생산 규칙
     */
    fun createProduction(
        left: TokenType,
        right: List<TokenType>,
        builder: ASTBuilderContract = ASTBuilders.Identity
    ): Production {
        val id = generateNextId()
        return createProduction(id, left, right, builder)
    }

    /**
     * 단일 심볼 우변을 가진 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param right 단일 우변 심볼
     * @param builder AST 빌더
     * @return 생성된 생산 규칙
     */
    fun createSingleSymbolProduction(
        id: Int,
        left: TokenType,
        right: TokenType,
        builder: ASTBuilderContract = ASTBuilders.Identity
    ): Production {
        return createProduction(id, left, listOf(right), builder)
    }

    /**
     * 엡실론 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param builder AST 빌더
     * @return 엡실론 생산 규칙
     */
    fun createEpsilonProduction(
        id: Int,
        left: TokenType,
        builder: ASTBuilderContract = ASTBuilders.Identity
    ): Production {
        return createProduction(id, left, emptyList(), builder)
    }

    /**
     * 이항 연산자 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param leftOperand 좌측 피연산자 심볼
     * @param operator 연산자 심볼
     * @param rightOperand 우측 피연산자 심볼
     * @return 이항 연산자 생산 규칙
     */
    fun createBinaryOperatorProduction(
        id: Int,
        left: TokenType,
        leftOperand: TokenType,
        operator: TokenType,
        rightOperand: TokenType
    ): Production {
        require(operator.isOperator) { "연산자 심볼이 아닙니다: $operator" }
        
        val operatorSymbol = when (operator) {
            TokenType.PLUS -> "+"
            TokenType.MINUS -> "-"
            TokenType.MULTIPLY -> "*"
            TokenType.DIVIDE -> "/"
            TokenType.MODULO -> "%"
            TokenType.POWER -> "^"
            TokenType.AND -> "&&"
            TokenType.OR -> "||"
            TokenType.EQUAL -> "=="
            TokenType.NOT_EQUAL -> "!="
            TokenType.LESS -> "<"
            TokenType.LESS_EQUAL -> "<="
            TokenType.GREATER -> ">"
            TokenType.GREATER_EQUAL -> ">="
            else -> operator.name
        }
        
        return createProduction(
            id = id,
            left = left,
            right = listOf(leftOperand, operator, rightOperand),
            builder = ASTBuilders.createBinaryOp(operatorSymbol)
        )
    }

    /**
     * 단항 연산자 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param operator 연산자 심볼
     * @param operand 피연산자 심볼
     * @return 단항 연산자 생산 규칙
     */
    fun createUnaryOperatorProduction(
        id: Int,
        left: TokenType,
        operator: TokenType,
        operand: TokenType
    ): Production {
        require(operator.isOperator) { "연산자 심볼이 아닙니다: $operator" }
        
        val operatorSymbol = when (operator) {
            TokenType.MINUS -> "-"
            TokenType.PLUS -> "+"
            TokenType.NOT -> "!"
            else -> operator.name
        }
        
        return createProduction(
            id = id,
            left = left,
            right = listOf(operator, operand),
            builder = ASTBuilders.createUnaryOp(operatorSymbol)
        )
    }

    /**
     * 함수 호출 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param hasArguments 인수가 있는지 여부
     * @return 함수 호출 생산 규칙
     */
    fun createFunctionCallProduction(
        id: Int,
        left: TokenType,
        hasArguments: Boolean = true
    ): Production {
        return if (hasArguments) {
            createProduction(
                id = id,
                left = left,
                right = listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.ARGS, TokenType.RIGHT_PAREN),
                builder = ASTBuilders.FunctionCall
            )
        } else {
            createProduction(
                id = id,
                left = left,
                right = listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN),
                builder = ASTBuilders.FunctionCallEmpty
            )
        }
    }

    /**
     * 조건부 표현식 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @return 조건부 표현식 생산 규칙
     */
    fun createConditionalProduction(
        id: Int,
        left: TokenType
    ): Production {
        return createProduction(
            id = id,
            left = left,
            right = listOf(
                TokenType.IF, TokenType.LEFT_PAREN, TokenType.EXPR, TokenType.COMMA,
                TokenType.EXPR, TokenType.COMMA, TokenType.EXPR, TokenType.RIGHT_PAREN
            ),
            builder = ASTBuilders.If
        )
    }

    /**
     * 괄호 표현식 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param inner 내부 표현식 심볼
     * @return 괄호 표현식 생산 규칙
     */
    fun createParenthesizedProduction(
        id: Int,
        left: TokenType,
        inner: TokenType
    ): Production {
        return createProduction(
            id = id,
            left = left,
            right = listOf(TokenType.LEFT_PAREN, inner, TokenType.RIGHT_PAREN),
            builder = ASTBuilders.Parenthesized
        )
    }

    /**
     * 터미널 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param terminal 터미널 심볼
     * @return 터미널 생산 규칙
     */
    fun createTerminalProduction(
        id: Int,
        left: TokenType,
        terminal: TokenType
    ): Production {
        require(terminal.isTerminal) { "터미널 심볼이 아닙니다: $terminal" }
        
        val builder = when (terminal) {
            TokenType.NUMBER -> ASTBuilders.Number
            TokenType.IDENTIFIER, TokenType.VARIABLE -> ASTBuilders.Variable
            TokenType.TRUE -> ASTBuilders.BooleanTrue
            TokenType.FALSE -> ASTBuilders.BooleanFalse
            else -> ASTBuilders.Identity
        }
        
        return createProduction(id, left, listOf(terminal), builder)
    }

    /**
     * 인수 목록 생산 규칙을 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param isSingle 단일 인수인지 여부
     * @return 인수 목록 생산 규칙
     */
    fun createArgumentListProduction(
        id: Int,
        left: TokenType,
        isSingle: Boolean = true
    ): Production {
        return if (isSingle) {
            createProduction(
                id = id,
                left = left,
                right = listOf(TokenType.EXPR),
                builder = ASTBuilders.ArgsSingle
            )
        } else {
            createProduction(
                id = id,
                left = left,
                right = listOf(left, TokenType.COMMA, TokenType.EXPR),
                builder = ASTBuilders.ArgsMultiple
            )
        }
    }

    /**
     * 확장 생산 규칙을 생성합니다 (LR 파서용).
     *
     * @param startSymbol 시작 심볼
     * @param endSymbol 끝 심볼
     * @return 확장 생산 규칙
     */
    fun createAugmentedProduction(
        startSymbol: TokenType,
        endSymbol: TokenType = TokenType.DOLLAR
    ): Production {
        return createProduction(
            id = -1,
            left = TokenType.START,
            right = listOf(startSymbol, endSymbol),
            builder = ASTBuilders.Start
        )
    }

    /**
     * BNF 문자열로부터 생산 규칙을 파싱하여 생성합니다.
     *
     * @param id 생산 규칙 ID
     * @param bnfRule BNF 형태의 규칙 문자열 (예: "EXPR -> EXPR + TERM")
     * @return 파싱된 생산 규칙
     */
    fun createFromBNF(id: Int, bnfRule: String): Production {
        val parts = bnfRule.split("->").map { it.trim() }
        require(parts.size == 2) { "잘못된 BNF 형식: $bnfRule" }
        
        val leftSymbol = parseTokenType(parts[0])
        val rightSymbols = if (parts[1].trim() == "ε" || parts[1].trim().isEmpty()) {
            emptyList()
        } else {
            parts[1].split("\\s+".toRegex()).map { parseTokenType(it) }
        }
        
        return createProduction(id, leftSymbol, rightSymbols)
    }

    /**
     * 여러 생산 규칙을 한 번에 생성합니다.
     *
     * @param productions 생성할 생산 규칙들의 정의
     * @return 생성된 생산 규칙들
     */
    fun createMultipleProductions(
        productions: List<ProductionDefinition>
    ): List<Production> {
        require(productions.size <= MAX_PRODUCTION_COUNT) {
            "생산 규칙 개수가 최대값을 초과했습니다: ${productions.size} > $MAX_PRODUCTION_COUNT"
        }
        
        return productions.map { definition ->
            createProduction(
                id = definition.id,
                left = definition.left,
                right = definition.right,
                builder = definition.builder
            )
        }
    }

    /**
     * 기존 생산 규칙을 복사하여 새로운 ID로 생성합니다.
     *
     * @param original 원본 생산 규칙
     * @param newId 새로운 ID
     * @return 복사된 생산 규칙
     */
    fun copyProduction(original: Production, newId: Int): Production {
        return createProduction(
            id = newId,
            left = original.left,
            right = original.right,
            builder = original.astBuilder
        )
    }

    /**
     * 생산 규칙의 우변을 수정한 새로운 규칙을 생성합니다.
     *
     * @param original 원본 생산 규칙
     * @param newRight 새로운 우변
     * @param newId 새로운 ID (null이면 자동 생성)
     * @return 수정된 생산 규칙
     */
    fun modifyProductionRight(
        original: Production,
        newRight: List<TokenType>,
        newId: Int? = null
    ): Production {
        val id = newId ?: generateNextId()
        return createProduction(id, original.left, newRight, original.astBuilder)
    }

    /**
     * 다음 생산 규칙 ID를 생성합니다.
     *
     * @return 다음 ID
     */
    private fun generateNextId(): Int {
        return nextProductionId++
    }

    /**
     * 생산 규칙 데이터의 유효성을 검증합니다.
     *
     * @param id 생산 규칙 ID
     * @param left 좌변 심볼
     * @param right 우변 심볼들
     */
    private fun validateProductionData(id: Int, left: TokenType, right: List<TokenType>) {
        require(left.isNonTerminal()) { "좌변은 논터미널이어야 합니다: $left" }
        require(right.size <= MAX_PRODUCTION_LENGTH) {
            "우변이 최대 길이를 초과했습니다: ${right.size} > $MAX_PRODUCTION_LENGTH"
        }
    }

    /**
     * 문자열을 TokenType으로 파싱합니다.
     *
     * @param tokenString 토큰 문자열
     * @return 파싱된 TokenType
     */
    private fun parseTokenType(tokenString: String): TokenType {
        return try {
            TokenType.valueOf(tokenString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("알 수 없는 토큰 타입: $tokenString")
        }
    }

    /**
     * 생산 규칙 정의를 나타내는 데이터 클래스입니다.
     */
    data class ProductionDefinition(
        val id: Int,
        val left: TokenType,
        val right: List<TokenType>,
        val builder: ASTBuilderContract = ASTBuilders.Identity
    )

    /**
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxProductionLength" to MAX_PRODUCTION_LENGTH,
        "maxProductionCount" to MAX_PRODUCTION_COUNT,
        "nextProductionId" to nextProductionId,
        "supportedOperations" to listOf(
            "createProduction", "createBinaryOperatorProduction", "createUnaryOperatorProduction",
            "createFunctionCallProduction", "createConditionalProduction", "createTerminalProduction",
            "createFromBNF", "createMultipleProductions", "copyProduction"
        )
    )

    /**
     * 팩토리 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "factoryName" to "ProductionFactory",
        "creationMethods" to 15,
        "currentNextId" to nextProductionId,
        "specializedBuilders" to 8
    )

    /**
     * 다음 ID 카운터를 재설정합니다.
     *
     * @param startId 시작 ID
     */
    fun resetIdCounter(startId: Int = 0) {
        nextProductionId = startId
    }
}