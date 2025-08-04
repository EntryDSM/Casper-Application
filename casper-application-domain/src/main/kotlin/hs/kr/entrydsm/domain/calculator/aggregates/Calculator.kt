package hs.kr.entrydsm.domain.calculator.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.calculator.exceptions.CalculatorException
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.evaluator.aggregates.ExpressionEvaluator
import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.aggregates.LRParser
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import kotlin.system.measureTimeMillis

/**
 * 계산기 도메인의 핵심 집합 루트입니다.
 *
 * 수식 계산의 전체 프로세스를 관리하며, 렉싱, 파싱, 평가의 모든 단계를
 * 조율합니다. 다른 도메인들과의 협력을 통해 완전한 계산 기능을 제공하며,
 * 변수 바인딩, 다단계 계산, 오류 처리 등의 고급 기능을 지원합니다.
 *
 * @property lexer 토큰화를 담당하는 렉서
 * @property maxFormulaLength 최대 수식 길이
 * @property maxVariables 최대 변수 개수
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "calculator")
class Calculator(
    private val lexer: LexerAggregate = LexerAggregate(),
    private val parser: LRParser = LRParser.createDefault(),
    private val maxFormulaLength: Int = 5000,
    private val maxVariables: Int = 100
) {

    /**
     * 단일 수식을 계산합니다.
     *
     * @param request 계산 요청
     * @return 계산 결과
     * @throws CalculatorException 계산 중 오류 발생 시
     */
    fun calculate(request: CalculationRequest): CalculationResult {
        validateRequest(request)
        
        var result: Any? = null
        var ast: ASTNode? = null
        var tokens: List<Token>? = null
        
        val executionTime = measureTimeMillis {
            try {
                // 1. 토큰화
                tokens = tokenize(request.formula)
                
                // 2. 파싱 (간단한 파싱 로직 - 실제로는 LRParser 사용)
                ast = parseTokens(tokens!!)
                
                // 3. 평가
                val evaluator = ExpressionEvaluator.create(request.variables)
                result = evaluator.evaluate(ast!!)
                
            } catch (e: Exception) {
                throw CalculatorException.stepExecutionError(1, e)
            }
        }
        
        return CalculationResult(
            result = result,
            executionTimeMs = executionTime,
            formula = request.formula,
            variables = request.variables,
            steps = listOf("토큰화", "파싱", "평가"),
            ast = ast
        )
    }

    /**
     * 다단계 계산을 수행합니다.
     *
     * @param formulas 계산할 수식들
     * @param variables 변수 바인딩
     * @return 각 단계의 계산 결과
     * @throws CalculatorException 계산 중 오류 발생 시
     */
    fun calculateMultiStep(formulas: List<String>, variables: Map<String, Any> = emptyMap()): List<CalculationResult> {
        if (formulas.isEmpty()) {
            throw CalculatorException.emptySteps()
        }
        
        if (formulas.size > 50) { // 최대 단계 수 제한
            throw CalculatorException.tooManySteps(formulas.size, 50)
        }
        
        val results = mutableListOf<CalculationResult>()
        var currentVariables = variables.toMutableMap()
        
        formulas.forEachIndexed { index, formula ->
            try {
                val request = CalculationRequest(formula, currentVariables)
                val result = calculate(request)
                results.add(result)
                
                currentVariables["${STEP_VARIABLE_PREFIX}${index + 1}"] = result.result ?: 0.0
                
            } catch (e: Exception) {
                throw CalculatorException.stepExecutionError(index + 1, e)
            }
        }
        
        return results
    }

    /**
     * 수식을 토큰화합니다.
     *
     * @param formula 토큰화할 수식
     * @return 토큰 리스트
     * @throws CalculatorException 토큰화 중 오류 발생 시
     */
    private fun tokenize(formula: String): List<Token> {
        val result = lexer.tokenize(formula)
        return if (result.isSuccess) result.tokens else emptyList()
    }

    /**
     * 토큰들을 파싱하여 AST를 생성합니다.
     *
     * @param tokens 파싱할 토큰들
     * @return AST 노드
     * @throws CalculatorException 파싱 중 오류 발생 시
     */
    private fun parseTokens(tokens: List<Token>): ASTNode {
        return try {
            parser.parse(tokens)
        } catch (e: Exception) {
            throw CalculatorException.stepExecutionError(2, e)
        }
    }

    /**
     * 계산 요청의 유효성을 검증합니다.
     *
     * @param request 검증할 요청
     * @throws CalculatorException 유효하지 않은 요청인 경우
     */
    private fun validateRequest(request: CalculationRequest) {
        if (request.formula.isBlank()) {
            throw CalculatorException.emptyFormula()
        }
        
        if (request.formula.length > maxFormulaLength) {
            throw CalculatorException.formulaTooLong(request.formula, maxFormulaLength)
        }
        
        if (request.variables.size > maxVariables) {
            throw CalculatorException.tooManyVariables(request.variables.size, maxVariables)
        }
    }

    /**
     * 계산기 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxFormulaLength" to maxFormulaLength,
        "maxVariables" to maxVariables,
        "lexerConfiguration" to lexer.getConfiguration(),
        "supportedTokenTypes" to "ALL_CALCULATOR_TOKENS",
        "grammarStatistics" to Grammar.getGrammarStatistics()
    )

    /**
     * 수식의 문법 유효성을 검사합니다.
     *
     * @param formula 검사할 수식
     * @return 유효하면 true, 아니면 false
     * @throws CalculatorException 검증 중 오류 발생 시
     */
    fun isValidFormula(formula: String): Boolean = try {
        tokenize(formula)
        true
    } catch (e: Exception) {
        throw CalculatorException.formulaValidationError(formula, e)
    }

    /**
     * 수식에서 사용된 변수들을 추출합니다.
     *
     * @param formula 분석할 수식
     * @return 변수 이름 집합
     * @throws CalculatorException 변수 추출 중 오류 발생 시
     */
    fun extractVariables(formula: String): Set<String> = try {
        val tokens = tokenize(formula)
        tokens.filter { it.type == TokenType.VARIABLE || it.type == TokenType.IDENTIFIER }
            .map { it.value }
            .toSet()
    } catch (e: Exception) {
        throw CalculatorException.variableExtractionError(formula, e)
    }

    /**
     * 계산기 통계 정보를 반환합니다.
     *
     * @return 통계 정보
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "configuration" to getConfiguration(),
        "lexerStats" to lexer.getStatistics(),
        "grammarStats" to Grammar.getGrammarStatistics()
    )

    companion object {
        
        private const val STEP_VARIABLE_PREFIX = "__entry_calc_step_"
        /**
         * 기본 설정으로 계산기를 생성합니다.
         *
         * @return Calculator 인스턴스
         */
        fun createDefault(): Calculator = Calculator()

        /**
         * 사용자 정의 설정으로 계산기를 생성합니다.
         *
         * @param maxFormulaLength 최대 수식 길이
         * @param maxVariables 최대 변수 개수
         * @return Calculator 인스턴스
         */
        fun create(maxFormulaLength: Int = 5000, maxVariables: Int = 100): Calculator =
            Calculator(maxFormulaLength = maxFormulaLength, maxVariables = maxVariables)

        /**
         * 기본 계산기를 생성합니다.
         *
         * @return 기본 계산기 인스턴스
         */
        fun createBasic(): Calculator = Calculator(
            maxFormulaLength = 1000,
            maxVariables = 10
        )

        /**
         * 과학 계산기를 생성합니다.
         *
         * @return 과학 계산기 인스턴스
         */
        fun createScientific(): Calculator = Calculator(
            maxFormulaLength = 5000,
            maxVariables = 100
        )

        /**
         * 통계 계산기를 생성합니다.
         *
         * @return 통계 계산기 인스턴스
         */
        fun createStatistical(): Calculator = Calculator(
            maxFormulaLength = 10000,
            maxVariables = 500
        )

        /**
         * 공학 계산기를 생성합니다.
         *
         * @return 공학 계산기 인스턴스
         */
        fun createEngineering(): Calculator = Calculator(
            maxFormulaLength = 15000,
            maxVariables = 1000
        )

        /**
         * 설정과 함께 계산기를 생성합니다.
         *
         * @param settings 계산기 설정
         * @return 설정된 계산기 인스턴스
         */
        fun createWithSettings(settings: Map<String, Any>): Calculator {
            val maxFormula = (settings["maxFormulaLength"] as? Int) ?: 5000
            val maxVars = (settings["maxVariables"] as? Int) ?: 100
            return Calculator(maxFormulaLength = maxFormula, maxVariables = maxVars)
        }
    }
}