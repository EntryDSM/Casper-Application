package hs.kr.entrydsm.domain.parser.factories

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.domain.ast.factory.ASTBuilders
import hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract

/**
 * AST Builder 생성을 담당하는 팩토리 클래스입니다.
 *
 * DDD Factory 패턴을 적용하여 다양한 AST 빌더의 생성 로직을 캡슐화하고,
 * 파싱 과정에서 필요한 AST 구축 전략을 제공합니다. 각 생산 규칙에 적합한
 * AST 빌더를 동적으로 생성하고 관리합니다.
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
class ASTBuilderContractFactory {

    companion object {
        private val builderCache = mutableMapOf<String, ASTBuilderContract>()
        private const val MAX_CACHE_SIZE = 1000
    }

    /**
     * 이항 연산자 AST 빌더를 생성합니다.
     *
     * @param operator 연산자 문자열
     * @param precedence 우선순위 (옵션)
     * @param isLeftAssoc 좌결합 여부 (옵션)
     * @return 이항 연산자 AST 빌더
     */
    fun createBinaryOperatorBuilder(
        operator: String,
        precedence: Int? = null,
        isLeftAssoc: Boolean? = null
    ): ASTBuilderContract {
        val cacheKey = "binary:$operator:$precedence:$isLeftAssoc"
        
        return builderCache.getOrPut(cacheKey) {
            validateOperator(operator)
            ASTBuilders.createBinaryOp(operator)
        }
    }

    /**
     * 단항 연산자 AST 빌더를 생성합니다.
     *
     * @param operator 연산자 문자열
     * @param isPrefix 전위 연산자 여부
     * @return 단항 연산자 AST 빌더
     */
    fun createUnaryOperatorBuilder(
        operator: String,
        isPrefix: Boolean = true
    ): ASTBuilderContract {
        val cacheKey = "unary:$operator:$isPrefix"
        
        return builderCache.getOrPut(cacheKey) {
            validateOperator(operator)
            ASTBuilders.createUnaryOp(operator)
        }
    }

    /**
     * 산술 연산자 AST 빌더를 생성합니다.
     *
     * @param tokenType 연산자 토큰 타입
     * @return 산술 연산자 AST 빌더
     */
    fun createArithmeticBuilder(tokenType: TokenType): ASTBuilderContract {
        require(tokenType.isArithmeticOperator()) { "산술 연산자가 아닙니다: $tokenType" }
        
        val operator = when (tokenType) {
            TokenType.PLUS -> "+"
            TokenType.MINUS -> "-"
            TokenType.MULTIPLY -> "*"
            TokenType.DIVIDE -> "/"
            TokenType.MODULO -> "%"
            TokenType.POWER -> "^"
            else -> throw IllegalArgumentException("지원하지 않는 산술 연산자: $tokenType")
        }
        
        return createBinaryOperatorBuilder(operator)
    }

    /**
     * 논리 연산자 AST 빌더를 생성합니다.
     *
     * @param tokenType 연산자 토큰 타입
     * @return 논리 연산자 AST 빌더
     */
    fun createLogicalBuilder(tokenType: TokenType): ASTBuilderContract {
        require(tokenType.isLogicalOperator()) { "논리 연산자가 아닙니다: $tokenType" }
        
        val operator = when (tokenType) {
            TokenType.AND -> "&&"
            TokenType.OR -> "||"
            TokenType.NOT -> "!"
            else -> throw IllegalArgumentException("지원하지 않는 논리 연산자: $tokenType")
        }
        
        return if (tokenType == TokenType.NOT) {
            createUnaryOperatorBuilder(operator)
        } else {
            createBinaryOperatorBuilder(operator)
        }
    }

    /**
     * 비교 연산자 AST 빌더를 생성합니다.
     *
     * @param tokenType 연산자 토큰 타입
     * @return 비교 연산자 AST 빌더
     */
    fun createComparisonBuilder(tokenType: TokenType): ASTBuilderContract {
        require(tokenType.isComparisonOperator()) { "비교 연산자가 아닙니다: $tokenType" }
        
        val operator = when (tokenType) {
            TokenType.EQUAL -> "=="
            TokenType.NOT_EQUAL -> "!="
            TokenType.LESS -> "<"
            TokenType.LESS_EQUAL -> "<="
            TokenType.GREATER -> ">"
            TokenType.GREATER_EQUAL -> ">="
            else -> throw IllegalArgumentException("지원하지 않는 비교 연산자: $tokenType")
        }
        
        return createBinaryOperatorBuilder(operator)
    }

    /**
     * 함수 호출 AST 빌더를 생성합니다.
     *
     * @param hasArguments 인수가 있는지 여부
     * @param minArgs 최소 인수 개수
     * @param maxArgs 최대 인수 개수 (null이면 제한 없음)
     * @return 함수 호출 AST 빌더
     */
    fun createFunctionCallBuilder(
        hasArguments: Boolean = true,
        minArgs: Int = 0,
        maxArgs: Int? = null
    ): ASTBuilderContract {
        val cacheKey = "function:$hasArguments:$minArgs:$maxArgs"
        
        return builderCache.getOrPut(cacheKey) {
            if (hasArguments) {
                ASTBuilders.FunctionCall
            } else {
                ASTBuilders.FunctionCallEmpty
            }
        }
    }

    /**
     * 조건부 표현식 AST 빌더를 생성합니다.
     *
     * @param isShortCircuit 단축 평가 여부
     * @return 조건부 표현식 AST 빌더
     */
    fun createConditionalBuilder(isShortCircuit: Boolean = true): ASTBuilderContract {
        val cacheKey = "conditional:$isShortCircuit"
        
        return builderCache.getOrPut(cacheKey) {
            ASTBuilders.If
        }
    }

    /**
     * 리터럴 값 AST 빌더를 생성합니다.
     *
     * @param tokenType 리터럴 토큰 타입
     * @return 리터럴 AST 빌더
     */
    fun createLiteralBuilder(tokenType: TokenType): ASTBuilderContract {
        require(tokenType.isLiteral) { "리터럴 토큰이 아닙니다: $tokenType" }
        
        val cacheKey = "literal:$tokenType"
        
        return builderCache.getOrPut(cacheKey) {
            when (tokenType) {
                TokenType.NUMBER -> ASTBuilders.Number
                TokenType.TRUE -> ASTBuilders.BooleanTrue
                TokenType.FALSE -> ASTBuilders.BooleanFalse
                TokenType.IDENTIFIER, TokenType.VARIABLE -> ASTBuilders.Variable
                else -> throw IllegalArgumentException("지원하지 않는 리터럴 타입: $tokenType")
            }
        }
    }

    /**
     * 인수 목록 AST 빌더를 생성합니다.
     *
     * @param isSingleArg 단일 인수인지 여부
     * @return 인수 목록 AST 빌더
     */
    fun createArgumentListBuilder(isSingleArg: Boolean): ASTBuilderContract {
        val cacheKey = "args:$isSingleArg"
        
        return builderCache.getOrPut(cacheKey) {
            if (isSingleArg) {
                ASTBuilders.ArgsSingle
            } else {
                ASTBuilders.ArgsMultiple
            }
        }
    }

    /**
     * 괄호 표현식 AST 빌더를 생성합니다.
     *
     * @param preserveParentheses 괄호 정보 보존 여부
     * @return 괄호 표현식 AST 빌더
     */
    fun createParenthesizedBuilder(preserveParentheses: Boolean = false): ASTBuilderContract {
        val cacheKey = "paren:$preserveParentheses"
        
        return builderCache.getOrPut(cacheKey) {
            ASTBuilders.Parenthesized
        }
    }

    /**
     * 식별자(Identity) AST 빌더를 생성합니다.
     *
     * @return 식별자 AST 빌더
     */
    fun createIdentityBuilder(): ASTBuilderContract {
        return ASTBuilders.Identity
    }

    /**
     * 엡실론 AST 빌더를 생성합니다.
     *
     * @return 엡실론 AST 빌더
     */
    fun createEpsilonBuilder(): ASTBuilderContract {
        return ASTBuilders.Identity
    }

    /**
     * 시작 심볼 AST 빌더를 생성합니다.
     *
     * @return 시작 심볼 AST 빌더
     */
    fun createStartBuilder(): ASTBuilderContract {
        return ASTBuilders.Start
    }

    /**
     * 사용자 정의 AST 빌더를 생성합니다.
     *
     * @param name 빌더 이름
     * @param buildFunction 빌드 함수
     * @return 사용자 정의 AST 빌더
     */
    fun createCustomBuilder(
        name: String,
        buildFunction: (List<Any?>) -> Any?
    ): ASTBuilderContract {
        require(name.isNotBlank()) { "빌더 이름은 비어있을 수 없습니다" }
        
        val cacheKey = "custom:$name"
        
        return builderCache.getOrPut(cacheKey) {
            object : ASTBuilderContract {
                override fun build(children: List<Any>): Any {
                    return buildFunction(children) ?: ""
                }
                
                override fun toString(): String = "CustomBuilder($name)"
            }
        }
    }

    /**
     * 토큰 타입으로부터 적절한 AST 빌더를 자동 선택합니다.
     *
     * @param tokenType 토큰 타입
     * @param context 추가 컨텍스트 정보
     * @return 선택된 AST 빌더
     */
    fun selectBuilderForToken(
        tokenType: TokenType,
        context: Map<String, Any> = emptyMap()
    ): ASTBuilderContract {
        return when {
            tokenType.isArithmeticOperator() -> createArithmeticBuilder(tokenType)
            tokenType.isLogicalOperator() -> createLogicalBuilder(tokenType)
            tokenType.isComparisonOperator() -> createComparisonBuilder(tokenType)
            tokenType.isLiteral -> createLiteralBuilder(tokenType)
            tokenType == TokenType.LEFT_PAREN -> createParenthesizedBuilder()
            tokenType == TokenType.IF -> createConditionalBuilder()
            else -> createIdentityBuilder()
        }
    }

    /**
     * 생산 규칙의 패턴으로부터 적절한 AST 빌더를 선택합니다.
     *
     * @param leftSymbol 좌변 심볼
     * @param rightSymbols 우변 심볼들
     * @return 선택된 AST 빌더
     */
    fun selectBuilderForProduction(
        leftSymbol: TokenType,
        rightSymbols: List<TokenType>
    ): ASTBuilderContract {
        return when (rightSymbols.size) {
            0 -> createEpsilonBuilder()
            1 -> {
                val symbol = rightSymbols[0]
                if (symbol.isLiteral) {
                    createLiteralBuilder(symbol)
                } else {
                    createIdentityBuilder()
                }
            }
            2 -> {
                // 단항 연산자 패턴 검사
                val (first, second) = rightSymbols
                if (first.isOperator) {
                    createUnaryOperatorBuilder(getOperatorString(first))
                } else {
                    createIdentityBuilder()
                }
            }
            3 -> {
                // 이항 연산자 또는 괄호 패턴 검사
                val (first, second, third) = rightSymbols
                when {
                    first == TokenType.LEFT_PAREN && third == TokenType.RIGHT_PAREN -> 
                        createParenthesizedBuilder()
                    second.isOperator -> 
                        createBinaryOperatorBuilder(getOperatorString(second))
                    else -> 
                        createIdentityBuilder()
                }
            }
            4 -> {
                // 함수 호출 패턴 검사 (identifier ( args ))
                if (rightSymbols[0] == TokenType.IDENTIFIER && 
                    rightSymbols[1] == TokenType.LEFT_PAREN &&
                    rightSymbols[3] == TokenType.RIGHT_PAREN) {
                    createFunctionCallBuilder(hasArguments = true)
                } else {
                    createIdentityBuilder()
                }
            }
            8 -> {
                // 조건부 표현식 패턴 검사 (if ( expr , expr , expr ))
                if (rightSymbols[0] == TokenType.IF &&
                    rightSymbols[1] == TokenType.LEFT_PAREN &&
                    rightSymbols[7] == TokenType.RIGHT_PAREN) {
                    createConditionalBuilder()
                } else {
                    createIdentityBuilder()
                }
            }
            else -> createIdentityBuilder()
        }
    }

    /**
     * 빌더 캐시를 정리합니다.
     *
     * @param force 강제 정리 여부
     */
    fun clearCache(force: Boolean = false) {
        if (force || builderCache.size > MAX_CACHE_SIZE) {
            builderCache.clear()
        }
    }

    /**
     * 캐시 통계 정보를 반환합니다.
     *
     * @return 캐시 통계 맵
     */
    fun getCacheStatistics(): Map<String, Any> = mapOf(
        "cacheSize" to builderCache.size,
        "maxCacheSize" to MAX_CACHE_SIZE,
        "cacheUtilization" to (builderCache.size.toDouble() / MAX_CACHE_SIZE),
        "cachedBuilderTypes" to builderCache.keys.map { it.split(":")[0] }.distinct()
    )

    /**
     * 연산자 문자열의 유효성을 검증합니다.
     *
     * @param operator 연산자 문자열
     */
    private fun validateOperator(operator: String) {
        require(operator.isNotBlank()) { "연산자는 비어있을 수 없습니다" }
        require(operator.length <= 3) { "연산자 길이가 너무 깁니다: $operator" }
    }

    /**
     * 토큰 타입으로부터 연산자 문자열을 반환합니다.
     *
     * @param tokenType 토큰 타입
     * @return 연산자 문자열
     */
    private fun getOperatorString(tokenType: TokenType): String {
        return when (tokenType) {
            TokenType.PLUS -> "+"
            TokenType.MINUS -> "-"
            TokenType.MULTIPLY -> "*"
            TokenType.DIVIDE -> "/"
            TokenType.MODULO -> "%"
            TokenType.POWER -> "^"
            TokenType.AND -> "&&"
            TokenType.OR -> "||"
            TokenType.NOT -> "!"
            TokenType.EQUAL -> "=="
            TokenType.NOT_EQUAL -> "!="
            TokenType.LESS -> "<"
            TokenType.LESS_EQUAL -> "<="
            TokenType.GREATER -> ">"
            TokenType.GREATER_EQUAL -> ">="
            else -> tokenType.name
        }
    }

    /**
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxCacheSize" to MAX_CACHE_SIZE,
        "supportedBuilderTypes" to listOf(
            "binary", "unary", "arithmetic", "logical", "comparison",
            "functionCall", "conditional", "literal", "arguments",
            "parenthesized", "identity", "epsilon", "start", "custom"
        ),
        "cacheEnabled" to true,
        "autoSelection" to true
    )

    /**
     * 팩토리 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "factoryName" to "ASTBuilderContractFactory",
        "builderCreationMethods" to 13,
        "autoSelectionMethods" to 2,
        "cacheStatistics" to getCacheStatistics()
    )
}