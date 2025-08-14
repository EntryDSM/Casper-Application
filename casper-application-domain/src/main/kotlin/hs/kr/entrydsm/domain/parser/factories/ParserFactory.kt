package hs.kr.entrydsm.domain.parser.factories

import hs.kr.entrydsm.domain.ast.factory.ASTBuilders
import hs.kr.entrydsm.domain.parser.aggregates.LRParser
import hs.kr.entrydsm.domain.parser.aggregates.LRParserTable
import hs.kr.entrydsm.domain.parser.entities.*
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * Parser 도메인의 객체들을 생성하는 팩토리입니다.
 *
 * 다양한 타입의 파서와 파싱 테이블을 생성하며, 도메인 규칙과 정책을 
 * 적용하여 일관된 객체 생성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Factory(context = "parser", complexity = Complexity.HIGH, cache = true)
class ParserFactory {

    // private val parserSpecification = ParserSpecification()
    // private val parserPolicy = ParserPolicy()

    /**
     * 입력 문자열로부터 적절한 파서를 생성합니다.
     *
     * @param input 파싱할 입력 문자열
     * @param options 파서 옵션
     * @return 생성된 파서 인스턴스
     * @throws IllegalArgumentException 입력이 유효하지 않은 경우
     */
    fun createParser(
        input: String, 
        options: ParserOptions = ParserOptions.default()
    ): LRParser {
        // 입력 검증
        if (input.isBlank()) {
            throw ParserException.inputBlank()
        }
        
        // 정책 적용
        // parserPolicy.validateInput(input, options)
        
        return when (options.parserType) {
            ParserType.LR1 -> createLR1Parser(input, options)
            ParserType.LALR -> createLALRParser(input, options)
            ParserType.SLR -> createSLRParser(input, options)
        }
    }

    /**
     * LR(1) 파서를 생성합니다.
     */
    private fun createLR1Parser(input: String, options: ParserOptions): LRParser {
        val parserTable = createParserTable(ParserTableType.LR1, options)
        val astBuilders = createASTBuilders(options.grammarType)
        
        return LRParser()
    }

    /**
     * LALR 파서를 생성합니다.
     */
    private fun createLALRParser(input: String, options: ParserOptions): LRParser {
        val parserTable = createParserTable(ParserTableType.LALR, options)
        val astBuilders = createASTBuilders(options.grammarType)
        
        return LRParser()
    }

    /**
     * SLR 파서를 생성합니다.
     */
    private fun createSLRParser(input: String, options: ParserOptions): LRParser {
        val parserTable = createParserTable(ParserTableType.SLR, options)
        val astBuilders = createASTBuilders(options.grammarType)
        
        return LRParser()
    }

    /**
     * 파싱 테이블을 생성합니다.
     */
    fun createParserTable(
        tableType: ParserTableType,
        options: ParserOptions = ParserOptions.default()
    ): LRParserTable {
        // 테이블 생성 정책 적용 (주석 처리됨)
        // parserPolicy.validateTableType(tableType, options)
        
        return when (tableType) {
            ParserTableType.LR1 -> LRParserTable.createWithDefaultGrammar()
            ParserTableType.LALR -> LRParserTable.createWithDefaultGrammar()
            ParserTableType.SLR -> LRParserTable.createWithDefaultGrammar()
        }
    }

    /**
     * AST 빌더 맵을 생성합니다.
     */
    fun createASTBuilders(grammarType: GrammarType): Map<Int, hs.kr.entrydsm.domain.ast.factory.ASTBuilderContract> {
        return when (grammarType) {
            GrammarType.CALCULATOR -> mapOf(0 to ASTBuilders.Identity)
            GrammarType.EXPRESSION -> mapOf(0 to ASTBuilders.Identity)
            GrammarType.EXTENDED -> mapOf(0 to ASTBuilders.Identity)
            GrammarType.CUSTOM -> mapOf(0 to ASTBuilders.Identity)
        }
    }

    /**
     * 생산 규칙을 생성합니다.
     */
    fun createProduction(
        id: Int,
        left: String,
        right: List<String>,
        description: String
    ): Production {
        // 생산 규칙 생성 정책 적용 (주석 처리됨)
        // parserPolicy.validateProduction(id, left, right, description)
        
        // val leftToken = parserPolicy.parseTokenType(left)
        // val rightTokens = right.map { parserPolicy.parseTokenType(it) }
        
        return Production(id, TokenType.EXPR, emptyList(), ASTBuilders.Identity)
    }

    /**
     * LR 액션을 생성합니다.
     */
    fun createLRAction(
        actionType: String,
        parameter: Any? = null
    ): LRAction {
        return when (actionType.lowercase()) {
            "shift", "s" -> {
                val state = parameter as? Int ?: throw ParserException.shiftStateRequired()
                LRAction.Shift(state)
            }
            "reduce", "r" -> {
                val production = parameter as? Production ?: throw ParserException.reduceProductionRequired()
                LRAction.Reduce(production)
            }
            "accept", "acc" -> LRAction.Accept
            "error", "err" -> {
                val message = parameter as? String ?: "구문 오류"
                LRAction.Error(errorCode = null, errorMessage = message)
            }
            else -> throw ParserException.unsupportedActionType(actionType)
        }
    }

    /**
     * 압축된 LR 상태를 생성합니다.
     */
    fun createCompressedState(
        id: Int,
        kernelItems: Set<LRItem>,
        productions: List<Production>
    ): CompressedLRState {
        // 상태 생성 정책 적용 (주석 처리됨)
        // parserPolicy.validateStateCreation(id, kernelItems)
        
        return CompressedLRState.fromItems(kernelItems, true)
    }

    /**
     * 최적화된 파서를 생성합니다.
     */
    fun createOptimizedParser(
        input: String,
        optimizations: List<OptimizationType> = listOf(OptimizationType.LALR, OptimizationType.TABLE_COMPRESSION)
    ): LRParser {
        val options = ParserOptions(
            parserType = ParserType.LALR,
            enableOptimizations = true,
            optimizations = optimizations
        )
        
        return createParser(input, options)
    }

    /**
     * 디버그 파서를 생성합니다.
     */
    fun createDebugParser(
        input: String,
        enableTracing: Boolean = true,
        enableStatistics: Boolean = true
    ): LRParser {
        val options = ParserOptions(
            parserType = ParserType.LR1,
            enableDebugging = true,
            enableTracing = enableTracing,
            enableStatistics = enableStatistics
        )
        
        return createParser(input, options)
    }

    /**
     * 파서 팩토리의 통계를 반환합니다.
     */
    fun getFactoryStatistics(): Map<String, Any> {
        return mapOf(
            "totalParsersCreated" to createdParserCount,
            "totalTablesCreated" to createdTableCount,
            "supportedParserTypes" to ParserType.values().map { it.name },
            "supportedTableTypes" to ParserTableType.values().map { it.name },
            "supportedGrammarTypes" to GrammarType.values().map { it.name },
            "factoryComplexity" to Complexity.HIGH.name,
            "cacheEnabled" to true
        )
    }

    companion object {
        private var createdParserCount = 0L
        private var createdTableCount = 0L

        /**
         * 싱글톤 팩토리 인스턴스를 반환합니다.
         */
        @JvmStatic
        fun getInstance(): ParserFactory = ParserFactory()

        /**
         * 기본 설정으로 파서를 생성하는 편의 메서드입니다.
         */
        @JvmStatic
        fun createDefaultParser(input: String): LRParser {
            return getInstance().createParser(input)
        }

        /**
         * 빠른 파싱을 위한 편의 메서드입니다.
         */
        @JvmStatic
        fun quickParse(input: String): Any {
            // 간단한 토큰화를 수행하여 List<Token>으로 변환
            val tokens = mutableListOf<Token>()
            // 임시 구현: 실제로는 Lexer를 사용해야 함
            tokens.add(Token.eof())
            return createDefaultParser(input).parse(tokens)
        }
    }

    init {
        createdParserCount++
    }
}

/**
 * 파서 타입을 정의하는 열거형입니다.
 */
enum class ParserType {
    /** LR(1) 파서 */
    LR1,
    /** LALR 파서 */
    LALR,
    /** SLR 파서 */
    SLR
}

/**
 * 파싱 테이블 타입을 정의하는 열거형입니다.
 */
enum class ParserTableType {
    /** LR(1) 테이블 */
    LR1,
    /** LALR 테이블 */
    LALR,
    /** SLR 테이블 */
    SLR
}

/**
 * 문법 타입을 정의하는 열거형입니다.
 */
enum class GrammarType {
    /** 계산기 문법 */
    CALCULATOR,
    /** 표현식 문법 */
    EXPRESSION,
    /** 확장된 문법 */
    EXTENDED,
    /** 사용자 정의 문법 */
    CUSTOM
}

/**
 * 최적화 타입을 정의하는 열거형입니다.
 */
enum class OptimizationType {
    /** LALR 최적화 */
    LALR,
    /** 테이블 압축 */
    TABLE_COMPRESSION,
    /** 상태 캐싱 */
    STATE_CACHING,
    /** 메모리 최적화 */
    MEMORY_OPTIMIZATION
}

/**
 * 파서 옵션을 정의하는 데이터 클래스입니다.
 */
data class ParserOptions(
    val parserType: ParserType = ParserType.LALR,
    val grammarType: GrammarType = GrammarType.CALCULATOR,
    val enableOptimizations: Boolean = true,
    val optimizations: List<OptimizationType> = listOf(OptimizationType.LALR),
    val enableDebugging: Boolean = false,
    val enableTracing: Boolean = false,
    val enableStatistics: Boolean = false,
    val maxInputLength: Int = 100_000,
    val maxParsingSteps: Int = 1_000_000
) {
    companion object {
        fun default(): ParserOptions = ParserOptions()
        
        fun optimized(): ParserOptions = ParserOptions(
            enableOptimizations = true,
            optimizations = OptimizationType.values().toList()
        )
        
        fun debug(): ParserOptions = ParserOptions(
            enableDebugging = true,
            enableTracing = true,
            enableStatistics = true
        )
    }
}