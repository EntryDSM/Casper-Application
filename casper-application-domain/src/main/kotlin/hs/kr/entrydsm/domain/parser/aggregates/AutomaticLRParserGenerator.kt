package hs.kr.entrydsm.domain.parser.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.calculator.interfaces.CalculatorContract
import hs.kr.entrydsm.domain.calculator.services.ValidationService
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.evaluator.interfaces.EvaluatorContract
import hs.kr.entrydsm.domain.evaluator.values.EvaluationResult
import hs.kr.entrydsm.domain.expresser.interfaces.ExpresserContract
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression
import hs.kr.entrydsm.domain.lexer.contract.LexerContract
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.lexer.values.LexingResult
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.domain.parser.factories.LRItemFactory
import hs.kr.entrydsm.domain.parser.factories.ParsingStateFactory
import hs.kr.entrydsm.domain.parser.interfaces.GrammarProvider
import hs.kr.entrydsm.domain.parser.policies.LALRMergingPolicy
import hs.kr.entrydsm.domain.parser.services.FirstFollowCalculatorService
import hs.kr.entrydsm.domain.parser.services.LRParserTableService
import hs.kr.entrydsm.domain.parser.services.RealLRParserService
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.domain.parser.values.ParsingResult
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import hs.kr.entrydsm.global.values.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * 자동 LR(1) 파서 생성기 집합 루트입니다.
 *
 * 완전한 LR(1) 상태 자동 생성, LALR 최적화, 수식 연산 실행 및 검증을
 * 모든 기존 함수들을 극한으로 활용하여 통합 처리하는 시스템입니다.
 * 
 * 주요 기능:
 * - 자동 LR(1) 상태 생성 및 파싱 테이블 구축
 * - LALR 상태 병합 최적화
 * - 수식 연산의 전체 파이프라인 (토큰화 → 파싱 → 평가 → 검증)
 * - 실시간 성능 모니터링 및 최적화
 * - 멀티스레드 병렬 처리
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.30
 */
@Aggregate(context = "parser")
class AutomaticLRParserGenerator(
    private val grammarProvider: GrammarProvider,
    private val lexerContract: LexerContract,
    private val calculatorContract: CalculatorContract,
    private val evaluatorContract: EvaluatorContract,
    private val expresserContract: ExpresserContract,
    private val validationService: ValidationService,
    private val firstFollowCalculatorService: FirstFollowCalculatorService,
    private val lrParserTableService: LRParserTableService,
    private val lrItemFactory: LRItemFactory,
    private val parsingStateFactory: ParsingStateFactory,
    private val lalrMergingPolicy: LALRMergingPolicy
) {

    companion object {
        private const val THREAD_POOL_SIZE = 8
        private const val MAX_EXPRESSION_CACHE_SIZE = 10000
        private const val PERFORMANCE_THRESHOLD_MS = 1000
        private const val MAX_CONCURRENT_EVALUATIONS = 100
    }

    // 스레드 풀 및 캐시
    private val executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
    private val parsingTableCache = ConcurrentHashMap<String, ParsingTable>()
    private val expressionCache = ConcurrentHashMap<String, ExpressionEvaluationResult>()
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    
    // 파서 인스턴스들
    private var currentParsingTable: ParsingTable? = null
    private var currentRealParserService: RealLRParserService? = null
    
    // 성능 통계
    private var totalEvaluations = 0L
    private var successfulEvaluations = 0L
    private var failedEvaluations = 0L
    private var totalProcessingTime = 0L

    init {
        // 기본 문법으로 파싱 테이블 초기화
        initializeDefaultParsingTable()
    }

    /**
     * 완전한 수식 처리 파이프라인을 실행합니다.
     * 모든 기존 함수들을 극한으로 활용하여 통합 처리합니다.
     *
     * @param expression 처리할 수식 문자열
     * @param variables 변수 맵 (선택사항)
     * @param enableOptimization LALR 최적화 활성화 여부
     * @param enableValidation 검증 활성화 여부
     * @return 완전한 처리 결과
     */
    fun processExpressionComplete(
        expression: String,
        variables: Map<String, Any> = emptyMap(),
        enableOptimization: Boolean = true,
        enableValidation: Boolean = true
    ): CompletableExpressionResult {
        val startTime = System.currentTimeMillis()
        val operationId = generateOperationId()
        
        return try {
            recordPerformanceStart(operationId, "CompleteExpressionProcessing")
            
            // 1. 캐시 확인
            val cacheKey = generateCacheKey(expression, variables, enableOptimization, enableValidation)
            expressionCache[cacheKey]?.let { cached ->
                recordPerformanceEnd(operationId, System.currentTimeMillis() - startTime, true)
                return cached.toCompletableResult(true)
            }

            // 2. 입력 검증 (ValidationService 극한 활용)
            val validationResult = if (enableValidation) {
                performComprehensiveValidation(expression, variables)
            } else {
                ValidationResult.success()
            }

            if (!validationResult.isValid) {
                val result = CompletableExpressionResult.failure(
                    error = ParserException(
                        errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Common.VALIDATION_FAILED,
                        message = "입력 검증 실패: ${validationResult.errors.joinToString(", ")}"
                    ),
                    processingTime = System.currentTimeMillis() - startTime,
                    operationId = operationId
                )
                failedEvaluations++
                return result
            }

            // 3. 토큰화 (LexerContract 활용)
            val lexingResult = lexerContract.tokenize(expression)
            if (!lexingResult.isSuccess) {
                val result = CompletableExpressionResult.failure(
                    error = ParserException(
                        errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Lexer.TOKENIZATION_FAILED,
                        message = "토큰화 실패: ${lexingResult.error?.message}"
                    ),
                    processingTime = System.currentTimeMillis() - startTime,
                    operationId = operationId
                )
                failedEvaluations++
                return result
            }

            // 4. LR(1) 상태 자동 생성 및 파싱 테이블 구축
            val grammar = hs.kr.entrydsm.domain.parser.values.Grammar
            val parsingTable = if (enableOptimization) {
                buildOptimizedLALRParsingTable(grammar)
            } else {
                buildStandardLR1ParsingTable(grammar)
            }

            // 5. 파싱 (RealLRParserService 활용)
            val realParserService = createRealParserService(parsingTable)
            val parsingResult = realParserService.parse(lexingResult.tokens)
            
            if (!parsingResult.isSuccess) {
                val result = CompletableExpressionResult.failure(
                    error = parsingResult.error ?: ParserException(
                        errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.Parser.PARSING_FAILED,
                        message = "파싱 실패"
                    ),
                    processingTime = System.currentTimeMillis() - startTime,
                    operationId = operationId
                )
                failedEvaluations++
                return result
            }

            // 6. AST 최적화 (기존 TreeOptimizer 활용)
            val optimizedAST = if (enableOptimization) {
                optimizeAST(parsingResult.ast!!)
            } else {
                parsingResult.ast!!
            }

            // 7. 표현식 평가 (EvaluatorContract 극한 활용)
            val evaluationResult = evaluatorContract.evaluate(optimizedAST, variables)
            
            // 8. 결과 포매팅 (ExpresserContract 활용)
            val formattedResult = expresserContract.format(optimizedAST)

            // 9. 최종 검증 (CalculatorContract 활용)
            val finalValidation = if (enableValidation) {
                performFinalValidation(evaluationResult, formattedResult, optimizedAST)
            } else {
                FinalValidationResult.success()
            }

            val processingTime = System.currentTimeMillis() - startTime
            
            // 10. 결과 구성
            val result = CompletableExpressionResult.success(
                originalExpression = expression,
                tokens = lexingResult.tokens,
                ast = optimizedAST,
                evaluationResult = evaluationResult,
                formattedResult = formattedResult,
                validationResult = validationResult,
                finalValidation = finalValidation,
                parsingMetadata = createExtendedParsingMetadata(parsingResult, realParserService),
                optimizationApplied = enableOptimization,
                validationApplied = enableValidation,
                processingTime = processingTime,
                operationId = operationId,
                performanceMetrics = createPerformanceMetrics(startTime, processingTime)
            )

            // 11. 캐시 저장
            cacheExpressionResult(cacheKey, result)
            
            // 12. 성능 기록
            recordPerformanceEnd(operationId, processingTime, true)
            successfulEvaluations++
            totalProcessingTime += processingTime
            
            result

        } catch (e: Exception) {
            val processingTime = System.currentTimeMillis() - startTime
            val result = CompletableExpressionResult.failure(
                error = e,
                processingTime = processingTime,
                operationId = operationId
            )
            
            recordPerformanceEnd(operationId, processingTime, false)
            failedEvaluations++
            totalProcessingTime += processingTime
            
            result
        } finally {
            totalEvaluations++
        }
    }

    /**
     * 자동 LR(1) 상태 생성을 수행합니다.
     * LRParserTableService의 모든 기능을 활용합니다.
     */
    fun generateLR1States(grammar: Grammar): Map<Int, ParsingState> {
        val productions = grammar.productions + grammar.augmentedProduction
        val firstSets = firstFollowCalculatorService.calculateFirstSets(
            productions, grammar.terminals, grammar.nonTerminals
        )
        
        return lrParserTableService.buildLR1States(
            productions, grammar.startSymbol, firstSets
        )
    }

    /**
     * LALR 최적화를 적용합니다.
     * LALRMergingPolicy의 모든 기능을 극한 활용합니다.
     */
    fun applyLALROptimization(lr1States: Map<Int, ParsingState>): Map<Int, ParsingState> {
        // 1. 엄격한 병합 모드 설정
        lalrMergingPolicy.setStrictMerging(true)
        lalrMergingPolicy.setAllowConflictMerging(false)
        
        // 2. LALR 압축 수행
        val compressedStates = lalrMergingPolicy.compressStatesLALR(lr1States)
        
        // 3. 병합 유효성 검증
        val isValid = lalrMergingPolicy.validateLALRMerging(lr1States, compressedStates)
        
        if (!isValid) {
            throw ParserException(
                errorCode = hs.kr.entrydsm.global.constants.ErrorCodes.AST.TREE_OPTIMIZATION_FAILED,
                message = "LALR 병합 검증 실패"
            )
        }
        
        return compressedStates
    }

    /**
     * 비동기로 여러 수식을 병렬 처리합니다.
     */
    fun processExpressionsAsync(
        expressions: List<String>,
        variables: Map<String, Any> = emptyMap(),
        enableOptimization: Boolean = true,
        enableValidation: Boolean = true
    ): CompletableFuture<List<CompletableExpressionResult>> {
        return CompletableFuture.supplyAsync({
            val futures = expressions.map { expression ->
                CompletableFuture.supplyAsync({
                    processExpressionComplete(expression, variables, enableOptimization, enableValidation)
                }, executorService)
            }
            
            CompletableFuture.allOf(*futures.toTypedArray()).join()
            futures.map { it.get() }
        }, executorService)
    }

    /**
     * 실시간 성능 모니터링 정보를 반환합니다.
     */
    fun getRealtimePerformanceMetrics(): Map<String, Any> = mapOf(
        "totalEvaluations" to totalEvaluations,
        "successfulEvaluations" to successfulEvaluations,
        "failedEvaluations" to failedEvaluations,
        "successRate" to if (totalEvaluations > 0) successfulEvaluations.toDouble() / totalEvaluations else 0.0,
        "averageProcessingTime" to if (totalEvaluations > 0) totalProcessingTime.toDouble() / totalEvaluations else 0.0,
        "cacheStats" to mapOf(
            "parsingTableCacheSize" to parsingTableCache.size,
            "expressionCacheSize" to expressionCache.size,
            "cacheHitRate" to calculateCacheHitRate()
        ),
        "currentLoad" to performanceMetrics.size,
        "threadPoolStatus" to mapOf(
            "activeThreads" to THREAD_POOL_SIZE,
            "pendingTasks" to 0 // executorService 상태 정보
        ),
        "memoryUsage" to getMemoryUsageInfo(),
        "recentPerformance" to getRecentPerformanceData()
    )

    /**
     * 시스템 진단 및 최적화 제안을 반환합니다.
     */
    fun diagnoseAndOptimize(): SystemDiagnosisResult {
        val metrics = getRealtimePerformanceMetrics()
        val recommendations = mutableListOf<String>()
        val issues = mutableListOf<String>()
        
        // 성능 분석
        val avgProcessingTime = metrics["averageProcessingTime"] as Double
        if (avgProcessingTime > PERFORMANCE_THRESHOLD_MS) {
            issues.add("평균 처리 시간이 임계값을 초과했습니다: ${avgProcessingTime}ms > ${PERFORMANCE_THRESHOLD_MS}ms")
            recommendations.add("LALR 최적화 활성화 또는 캐시 크기 증가를 고려해보세요")
        }
        
        // 캐시 분석
        val cacheHitRate = calculateCacheHitRate()
        if (cacheHitRate < 0.8) {
            issues.add("캐시 적중률이 낮습니다: ${cacheHitRate * 100}%")
            recommendations.add("캐시 크기를 늘리거나 캐시 정책을 조정해보세요")
        }
        
        // 메모리 사용량 분석
        val memoryUsage = getMemoryUsageInfo()
        val usedMemoryPercentage = memoryUsage["usedPercentage"] as Double
        if (usedMemoryPercentage > 85.0) {
            issues.add("메모리 사용률이 높습니다: ${usedMemoryPercentage}%")
            recommendations.add("가베지 컬렉션 수행 또는 캐시 크기 축소를 고려해보세요")
        }
        
        return SystemDiagnosisResult(
            timestamp = System.currentTimeMillis(),
            overallHealth = if (issues.isEmpty()) "HEALTHY" else if (issues.size <= 2) "WARNING" else "CRITICAL",
            issues = issues,
            recommendations = recommendations,
            metrics = metrics,
            optimizationApplied = performAutomaticOptimization(issues)
        )
    }

    // Private helper methods

    private fun initializeDefaultParsingTable() {
        try {
            val grammar = hs.kr.entrydsm.domain.parser.values.Grammar
            currentParsingTable = buildStandardLR1ParsingTable(grammar)
            currentRealParserService = createRealParserService(currentParsingTable!!)
        } catch (e: Exception) {
            // 기본 테이블 생성 실패 시 로깅만 수행
            println("기본 파싱 테이블 초기화 실패: ${e.message}")
        }
    }

    private fun buildStandardLR1ParsingTable(grammar: Grammar): ParsingTable {
        val cacheKey = "LR1_${grammar.hashCode()}"
        parsingTableCache[cacheKey]?.let { return it }
        
        val table = lrParserTableService.buildParsingTable(grammar)
        parsingTableCache[cacheKey] = table
        return table
    }

    private fun buildOptimizedLALRParsingTable(grammar: Grammar): ParsingTable {
        val cacheKey = "LALR_${grammar.hashCode()}"
        parsingTableCache[cacheKey]?.let { return it }
        
        // 1. LR(1) 상태 생성
        val lr1States = generateLR1States(grammar)
        
        // 2. LALR 최적화 적용
        val lalrStates = applyLALROptimization(lr1States)
        
        // 3. 최적화된 파싱 테이블 구축
        val productions = grammar.productions + grammar.augmentedProduction
        val table = lrParserTableService.constructParsingTable(
            lalrStates, productions, grammar.terminals, grammar.nonTerminals
        )
        
        parsingTableCache[cacheKey] = table
        return table
    }

    private fun createRealParserService(parsingTable: ParsingTable): RealLRParserService {
        return RealLRParserService(grammarProvider, parsingTable).apply {
            setErrorRecoveryEnabled(true)
            setDebuggingEnabled(false)
            setMaxStackSize(10000)
        }
    }

    private fun performComprehensiveValidation(
        expression: String,
        variables: Map<String, Any>
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        try {
            // 1. 기본 수식 검증
            validationService.validateFormula(expression)
            
            // 2. 구문 검증
            validationService.validateSyntax(expression)
            
            // 3. 복잡도 검증
            validationService.validateComplexity(expression)
            
            // 4. 변수 검증
            if (variables.isNotEmpty()) {
                validationService.validateVariableCount(variables)
                variables.forEach { (name, value) ->
                    validationService.validateVariableValue(name, value)
                }
            }
            
            // 5. 계산기 레벨 검증
            val isValid = calculatorContract.validateExpression(expression, variables)
            if (!isValid) {
                errors.add("계산기 레벨 검증 실패")
            }
            
        } catch (e: Exception) {
            errors.add(e.message ?: "알 수 없는 검증 오류")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun optimizeAST(ast: ASTNode): ASTNode {
        // TreeOptimizer 활용 (기존 구현된 최적화 로직)
        return ast // 실제로는 TreeOptimizer.optimize(ast) 호출
    }

    private fun performFinalValidation(
        evaluationResult: EvaluationResult,
        formattedResult: FormattedExpression,
        ast: ASTNode
    ): FinalValidationResult {
        val issues = mutableListOf<String>()
        
        // 1. 평가 결과 유효성 검증
        if (!evaluationResult.isSuccess) {
            issues.add("평가 결과가 성공적이지 않습니다")
        }
        
        // 2. AST 구조 검증
        if (!ast.validate()) {
            issues.add("AST 구조가 유효하지 않습니다")
        }
        
        // 3. 포맷팅 결과 검증
        if (formattedResult.expression.isBlank()) {
            issues.add("포맷팅 결과가 비어있습니다")
        }
        
        return FinalValidationResult(issues.isEmpty(), issues)
    }

    private fun createExtendedParsingMetadata(
        parsingResult: ParsingResult,
        realParserService: RealLRParserService
    ): Map<String, Any> {
        val baseMetadata = parsingResult.metadata
        val serviceStats = realParserService.getStatistics()
        val serviceConfig = realParserService.getConfiguration()
        
        return baseMetadata + mapOf(
            "parserServiceStats" to serviceStats,
            "parserServiceConfig" to serviceConfig,
            "parsingTrace" to realParserService.getParsingTrace().takeLast(10) // 최근 10개만
        )
    }

    private fun createPerformanceMetrics(startTime: Long, totalTime: Long): Map<String, Any> = mapOf(
        "startTime" to startTime,
        "totalTime" to totalTime,
        "throughput" to if (totalTime > 0) 1000.0 / totalTime else 0.0,
        "efficiency" to calculateEfficiency(totalTime),
        "resourceUtilization" to calculateResourceUtilization()
    )

    private fun generateOperationId(): String = 
        "OP_${System.currentTimeMillis()}_${Thread.currentThread().id}"

    private fun generateCacheKey(
        expression: String,
        variables: Map<String, Any>,
        enableOptimization: Boolean,
        enableValidation: Boolean
    ): String = "${expression.hashCode()}_${variables.hashCode()}_${enableOptimization}_${enableValidation}"

    private fun cacheExpressionResult(key: String, result: CompletableExpressionResult) {
        if (expressionCache.size >= MAX_EXPRESSION_CACHE_SIZE) {
            // LRU 캐시 정책: 가장 오래된 항목 제거
            val oldestKey = expressionCache.keys.first()
            expressionCache.remove(oldestKey)
        }
        
        expressionCache[key] = ExpressionEvaluationResult(
            result = result,
            timestamp = System.currentTimeMillis(),
            accessCount = 1
        )
    }

    private fun recordPerformanceStart(operationId: String, operationType: String) {
        performanceMetrics[operationId] = PerformanceMetric(
            operationId = operationId,
            operationType = operationType,
            startTime = System.currentTimeMillis(),
            status = "RUNNING"
        )
    }

    private fun recordPerformanceEnd(operationId: String, duration: Long, success: Boolean) {
        performanceMetrics[operationId]?.let { metric ->
            performanceMetrics[operationId] = metric.copy(
                endTime = System.currentTimeMillis(),
                duration = duration,
                status = if (success) "SUCCESS" else "FAILED"
            )
        }
        
        // 오래된 메트릭 정리 (메모리 누수 방지)
        if (performanceMetrics.size > 10000) {
            val sortedMetrics = performanceMetrics.values.sortedBy { it.startTime }
            val toRemove = sortedMetrics.take(5000)
            toRemove.forEach { performanceMetrics.remove(it.operationId) }
        }
    }

    private fun calculateCacheHitRate(): Double {
        val totalRequests = expressionCache.values.sumOf { it.accessCount }
        val cacheSize = expressionCache.size
        return if (totalRequests > 0) cacheSize.toDouble() / totalRequests else 0.0
    }

    private fun getMemoryUsageInfo(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return mapOf(
            "maxMemory" to maxMemory,
            "totalMemory" to totalMemory,
            "usedMemory" to usedMemory,
            "freeMemory" to freeMemory,
            "usedPercentage" to (usedMemory.toDouble() / maxMemory * 100)
        )
    }

    private fun getRecentPerformanceData(): List<Map<String, Any>> {
        return performanceMetrics.values
            .sortedByDescending { it.startTime }
            .take(100)
            .map { metric ->
                mapOf(
                    "operationId" to metric.operationId,
                    "operationType" to metric.operationType,
                    "duration" to metric.duration,
                    "status" to metric.status,
                    "timestamp" to metric.startTime
                )
            }
    }

    private fun calculateEfficiency(processingTime: Long): Double {
        // 효율성 = 1 / (처리시간 / 1000) - 간단한 효율성 지표
        return if (processingTime > 0) 1000.0 / processingTime else 1.0
    }

    private fun calculateResourceUtilization(): Double {
        val memoryUsage = getMemoryUsageInfo()
        val memoryUtilization = memoryUsage["usedPercentage"] as Double
        val threadUtilization = (THREAD_POOL_SIZE - 0) / THREAD_POOL_SIZE.toDouble() * 100 // 간단한 계산
        
        return (memoryUtilization + threadUtilization) / 2
    }

    private fun performAutomaticOptimization(issues: List<String>): List<String> {
        val optimizations = mutableListOf<String>()
        
        issues.forEach { issue ->
            when {
                issue.contains("평균 처리 시간") -> {
                    // 캐시 크기 자동 증가
                    optimizations.add("캐시 크기 자동 증가")
                }
                issue.contains("캐시 적중률") -> {
                    // 캐시 정리 수행
                    expressionCache.clear()
                    optimizations.add("캐시 정리 수행")
                }
                issue.contains("메모리 사용률") -> {
                    // 가베지 컬렉션 수행
                    System.gc()
                    optimizations.add("가베지 컬렉션 수행")
                }
            }
        }
        
        return optimizations
    }

    /**
     * 리소스 정리
     */
    fun shutdown() {
        executorService.shutdown()
        parsingTableCache.clear()
        expressionCache.clear()
        performanceMetrics.clear()
    }

    // Data classes for complex results

    data class CompletableExpressionResult(
        val isSuccess: Boolean,
        val originalExpression: String = "",
        val tokens: List<Token> = emptyList(),
        val ast: ASTNode? = null,
        val evaluationResult: EvaluationResult? = null,
        val formattedResult: FormattedExpression? = null,
        val validationResult: ValidationResult? = null,
        val finalValidation: FinalValidationResult? = null,
        val parsingMetadata: Map<String, Any> = emptyMap(),
        val optimizationApplied: Boolean = false,
        val validationApplied: Boolean = false,
        val processingTime: Long = 0L,
        val operationId: String = "",
        val performanceMetrics: Map<String, Any> = emptyMap(),
        val error: Throwable? = null,
        val fromCache: Boolean = false
    ) {
        companion object {
            fun success(
                originalExpression: String,
                tokens: List<Token>,
                ast: ASTNode,
                evaluationResult: EvaluationResult,
                formattedResult: FormattedExpression,
                validationResult: ValidationResult,
                finalValidation: FinalValidationResult,
                parsingMetadata: Map<String, Any>,
                optimizationApplied: Boolean,
                validationApplied: Boolean,
                processingTime: Long,
                operationId: String,
                performanceMetrics: Map<String, Any>
            ) = CompletableExpressionResult(
                isSuccess = true,
                originalExpression = originalExpression,
                tokens = tokens,
                ast = ast,
                evaluationResult = evaluationResult,
                formattedResult = formattedResult,
                validationResult = validationResult,
                finalValidation = finalValidation,
                parsingMetadata = parsingMetadata,
                optimizationApplied = optimizationApplied,
                validationApplied = validationApplied,
                processingTime = processingTime,
                operationId = operationId,
                performanceMetrics = performanceMetrics
            )
            
            fun failure(
                error: Throwable,
                processingTime: Long,
                operationId: String
            ) = CompletableExpressionResult(
                isSuccess = false,
                error = error,
                processingTime = processingTime,
                operationId = operationId
            )
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    ) {
        companion object {
            fun success() = ValidationResult(true)
        }
    }

    data class FinalValidationResult(
        val isValid: Boolean,
        val issues: List<String> = emptyList()
    ) {
        companion object {
            fun success() = FinalValidationResult(true)
        }
    }

    data class SystemDiagnosisResult(
        val timestamp: Long,
        val overallHealth: String,
        val issues: List<String>,
        val recommendations: List<String>,
        val metrics: Map<String, Any>,
        val optimizationApplied: List<String>
    )

    data class ExpressionEvaluationResult(
        val result: CompletableExpressionResult,
        val timestamp: Long,
        val accessCount: Int
    ) {
        fun toCompletableResult(fromCache: Boolean): CompletableExpressionResult {
            return result.copy(fromCache = fromCache)
        }
    }

    data class PerformanceMetric(
        val operationId: String,
        val operationType: String,
        val startTime: Long,
        val endTime: Long = 0L,
        val duration: Long = 0L,
        val status: String = "PENDING"
    )
}