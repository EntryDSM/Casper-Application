package hs.kr.entrydsm.domain.calculator.services

import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.domain.calculator.policies.CalculationPolicy
import hs.kr.entrydsm.domain.calculator.specifications.CalculationValiditySpec
import hs.kr.entrydsm.domain.evaluator.aggregates.ExpressionEvaluator
import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.domain.parser.aggregates.LRParser
import hs.kr.entrydsm.domain.ast.services.TreeOptimizer
import hs.kr.entrydsm.domain.calculator.exceptions.CalculatorException
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.global.configuration.CalculatorConfiguration
import hs.kr.entrydsm.global.configuration.interfaces.ConfigurationProvider
import java.security.MessageDigest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 계산기의 핵심 비즈니스 로직을 처리하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 계산 요청의 처리와 결과 생성을
 * 담당합니다. 렉싱, 파싱, 평가의 전체 파이프라인을 조율하며
 * 정책과 명세를 적용하여 안전하고 정확한 계산을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "CalculatorService",
    type = hs.kr.entrydsm.global.annotation.service.type.ServiceType.DOMAIN_SERVICE
)
class CalculatorService(
    private val lexer: LexerAggregate,
    private val parser: LRParser,
    private val evaluator: ExpressionEvaluator,
    private val calculationPolicy: CalculationPolicy,
    private val validitySpec: CalculationValiditySpec,
    private val treeOptimizer: TreeOptimizer,
    private val configurationProvider: ConfigurationProvider
) {

    companion object {
        private const val CALCULATION_SERVICE = "CalculationService"
        private const val ANONYMOUS = "anonymous"
        private const val UNKNOWN_ERROR = "Unknown error"
        private const val CALCULATOR_SERVICE = "CalculatorService"
    }

    // 설정은 ConfigurationProvider를 통해 동적으로 접근
    private val config: CalculatorConfiguration
        get() = configurationProvider.getCalculatorConfiguration()

    private val calculationCache = ConcurrentHashMap<String, CachedResult>()
    private val performanceMetrics = PerformanceMetrics()
    private val requestCounter = AtomicLong(0)
    
    // 코루틴 스코프 및 디스패처 설정
    private val calculationScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob() + CoroutineName(CALCULATION_SERVICE)
    )
    private val calculationDispatcher: CoroutineDispatcher
        get() = Dispatchers.Default.limitedParallelism(config.concurrency)

    /**
     * 계산 요청을 처리합니다.
     *
     * @param request 계산 요청
     * @param session 계산 세션 (선택적)
     * @return 계산 결과
     */
    fun calculate(request: CalculationRequest, session: CalculationSession? = null): CalculationResult {
        val startTime = System.currentTimeMillis()
        
        try {
            performanceMetrics.incrementTotalRequests()
            
            // 주기적 캐시 정리 (100번 요청마다)
            if (requestCounter.incrementAndGet() % 100 == 0L) {
                manageCaches()
            }
            
            // 1. 요청 유효성 검증
            validateRequest(request, session)
            
            // 2. 정책 검증
            checkPolicy(request, session)
            
            // 3. 캐시 확인
            val cachedResult = retrieveFromCache(request, session)
            if (cachedResult != null) {
                return cachedResult
            }
            
            // 4. 계산 실행
            val result = performCalculation(request, session)
            
            // 5. 결과 캐싱
            cacheResultIfNeeded(request, session, result)
            
            // 6. 메트릭 업데이트
            val executionTime = System.currentTimeMillis() - startTime
            updateMetrics(request, session, executionTime)
            
            return result
            
        } catch (e: DomainException) {
            // 도메인 예외는 이미 적절한 컨텍스트를 포함하고 있음
            performanceMetrics.incrementFailures()
            return createFailureResult(request, e.message ?: "도메인 오류", startTime)
            
        } catch (e: IllegalArgumentException) {
            // 잘못된 인수 예외
            performanceMetrics.incrementFailures()
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "잘못된 계산 인수: ${e.message}",
                cause = e,
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to (session?.sessionId ?: ANONYMOUS)
                )
            )
            
        } catch (e: ArithmeticException) {
            // 산술 연산 예외
            performanceMetrics.incrementFailures()
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "수학 연산 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to (session?.sessionId ?: ANONYMOUS)
                )
            )
            
        } catch (e: Exception) {
            // 예상치 못한 시스템 예외
            performanceMetrics.incrementFailures()
            throw DomainException(
                errorCode = ErrorCode.UNEXPECTED_ERROR,
                message = "계산 실행 중 예상치 못한 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to (session?.sessionId ?: ANONYMOUS),
                    "exceptionType" to e.javaClass.simpleName
                )
            )
        }
    }

    /**
     * 일괄 계산을 수행합니다.
     *
     * @param requests 계산 요청들
     * @param session 계산 세션
     * @return 계산 결과들
     */
    fun calculateBatch(requests: List<CalculationRequest>, session: CalculationSession? = null): List<CalculationResult> {
        if (requests.isEmpty()) {
            throw CalculatorException.requestListEmpty()
        }

        return requests.map { request ->
            calculate(request, session)
        }
    }

    /**
     * 병렬 계산을 수행합니다.
     * Kotlin 코루틴을 사용하여 효율적인 비동기 병렬 처리를 제공합니다.
     *
     * @param requests 계산 요청들
     * @param session 계산 세션
     * @param concurrency 동시 실행할 최대 작업 수 (기본값: 10)
     * @return 계산 결과들
     */
    fun calculateParallel(
        requests: List<CalculationRequest>, 
        session: CalculationSession? = null,
        concurrency: Int = DEFAULT_CONCURRENCY
    ): List<CalculationResult> {
        if (requests.isEmpty()) {
            throw CalculatorException.requestListEmpty()
        }

        if (concurrency <= 0) {
            throw CalculatorException.invalidConcurrencyLevel(concurrency)
        }
        
        return runBlocking(calculationDispatcher.limitedParallelism(concurrency)) {
            requests.map { request ->
                async {
                    try {
                        calculate(request, session)
                    } catch (e: Exception) {
                        // 개별 계산 실패가 전체 배치를 중단시키지 않도록 처리
                        createFailureResult(request, "병렬 계산 오류: ${e.message}", System.currentTimeMillis())
                    }
                }
            }.awaitAll()
        }
    }

    /**
     * Flow 기반 스트리밍 병렬 계산을 수행합니다.
     * 대용량 배치 처리에 적합하며 메모리 효율적이고 백프레셔를 지원합니다.
     *
     * @param requests 계산 요청들
     * @param session 계산 세션
     * @param concurrency 동시 실행할 최대 작업 수 (기본값: 10)
     * @param bufferSize 버퍼 크기 (기본값: 50)
     * @return 계산 결과 Flow
     */
    fun calculateParallelFlow(
        requests: List<CalculationRequest>,
        session: CalculationSession? = null,
        concurrency: Int = DEFAULT_CONCURRENCY,
        bufferSize: Int = 50
    ): Flow<CalculationResult> {
        if (requests.isEmpty()) {
            throw CalculatorException.requestListEmpty()
        }

        if (concurrency <= 0) {
            throw CalculatorException.invalidConcurrencyLevel(concurrency)
        }

        if (bufferSize <= 0) {
            throw CalculatorException.invalidBufferSize(bufferSize)
        }

        return requests.asFlow()
            .buffer(capacity = bufferSize)
            .map { request ->
                try {
                    calculate(request, session)
                } catch (e: Exception) {
                    createFailureResult(request, "스트리밍 계산 오류: ${e.message}", System.currentTimeMillis())
                }
            }
            .flowOn(calculationDispatcher.limitedParallelism(concurrency))
    }

    /**
     * 표현식의 유효성을 검증합니다.
     *
     * @param expression 검증할 표현식
     * @param variables 변수들
     * @return 유효하면 true
     */
    fun validateExpression(expression: String, variables: Map<String, Any> = emptyMap()): Boolean {
        val request = CalculationRequest(
            formula = expression,
            variables = variables
        )
        return validitySpec.isSatisfiedBy(request)
    }

    /**
     * 표현식을 분석합니다.
     *
     * @param expression 분석할 표현식
     * @return 분석 결과
     */
    fun analyzeExpression(expression: String): Map<String, Any> {
        try {
            val lexingResult = lexer.tokenize(expression)
            val tokens = if (lexingResult.isSuccess) lexingResult.tokens else emptyList()
            val ast = parser.parse(tokens)
            
            return mapOf(
                "tokenCount" to tokens.size,
                "parseTree" to ast.toString(),
                "variables" to extractVariables(expression),
                "functions" to extractFunctions(expression),
                "complexity" to calculationPolicy.calculateTimeout(expression) / 1000,
                "isValid" to validateExpression(expression),
                "riskScore" to validitySpec.calculateRiskScore(expression)
            )
        } catch (e: Exception) {
            return mapOf(
                "error" to (e.message ?: UNKNOWN_ERROR),
                "isValid" to false
            )
        }
    }

    /**
     * 계산 성능을 최적화합니다.
     *
     * @param expression 최적화할 표현식
     * @return 최적화된 표현식
     */
    fun optimizeExpression(expression: String): String {
        // 간단한 최적화 예시들
        var optimized = expression.trim()
        
        // 중복 공백 제거
        optimized = optimized.replace(Regex("\\s+"), " ")
        
        // 불필요한 괄호 제거 (간단한 경우만)
        optimized = optimized.replace(Regex("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)"), "$1")
        
        // 상수 폴딩은 AST 최적화 단계에서 TreeOptimizer에 위임됨
        // 문자열 기반 최적화 대신 AST 기반 상수 폴딩 사용
        
        return optimized
    }

    /**
     * 캐시를 관리합니다.
     * ConcurrentHashMap을 사용하여 스레드 안전성을 보장합니다.
     *
     * @param maxSize 최대 캐시 크기
     * @param maxAge 최대 캐시 유지 시간 (밀리초)
     */
    fun manageCaches(maxSize: Int = 1000, maxAge: Long = 3600000) {
        val currentTime = System.currentTimeMillis()
        
        val expiredKeys = calculationCache.entries
            .filter { (_, cached) -> currentTime - cached.timestamp > maxAge }
            .map { it.key }
        
        expiredKeys.forEach { key ->
            calculationCache.remove(key)
        }
        
        if (calculationCache.size > maxSize) {
            val entriesToRemove = calculationCache.entries
                .sortedBy { it.value.timestamp }
                .take(calculationCache.size - maxSize)
                .map { it.key }
            
            entriesToRemove.forEach { key ->
                calculationCache.remove(key)
            }
        }
    }

    private suspend fun executeCalculation(request: CalculationRequest, session: CalculationSession?): CalculationResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. 렉싱
            val lexingResult = lexer.tokenize(request.formula)
            val tokens = if (lexingResult.isSuccess) lexingResult.tokens else emptyList()
            
            // 2. 파싱
            val ast = parser.parse(tokens)
            
            // 3. AST 최적화 (상수 폴딩 포함)
            val optimizedAst = treeOptimizer.optimize(ast)
            
            // 4. 변수 결합
            val allVariables = mutableMapOf<String, Any>()
            session?.variables?.let { allVariables.putAll(it) }
            allVariables.putAll(request.variables)
            
            // 5. 평가 (최적화된 AST 사용)
            val evaluationResult = evaluateWithRetry(optimizedAst, allVariables)
            
            val executionTime = System.currentTimeMillis() - startTime
            
            return CalculationResult(
                result = evaluationResult,
                executionTimeMs = executionTime,
                formula = request.formula,
                metadata = mapOf(
                    "tokenCount" to tokens.size,
                    "variableCount" to allVariables.size,
                    "astDepth" to calculateASTDepth(optimizedAst),
                    "optimized" to true
                )
            )
            
        } catch (e: Exception) {
            return createFailureResult(request, "계산 오류: ${e.message}", startTime)
        }
    }

    private suspend fun evaluateWithRetry(ast: Any, variables: Map<String, Any>, retries: Int = config.maxRetries): Any? {
        repeat(retries) { attempt ->
            try {
                // AST를 실제 ASTNode로 변환하여 평가
                return evaluateAST(ast, variables)
            } catch (e: Exception) {
                if (attempt == retries - 1) {
                    throw e
                }
                delay(100)
            }
        }
        throw CalculatorException.maxRetryExceeded()
    }

    private fun evaluateAST(ast: Any, variables: Map<String, Any>): Any? {
        return try {
            val astNode = ast as? hs.kr.entrydsm.domain.ast.entities.ASTNode
                ?: throw CalculatorException.invalidAstNodeType(ast.javaClass.simpleName)

            val evaluatorWithVariables = if (variables.isNotEmpty()) {
                evaluator.withVariables(variables)
            } else {
                evaluator
            }
            
            val result = evaluatorWithVariables.evaluate(astNode)
            
            validateEvaluationResult(result, astNode)
            
            result
            
        } catch (e: IllegalArgumentException) {
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "AST 평가 실패: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "variableCount" to variables.size,
                    "variables" to variables.keys.joinToString(", ")
                )
            )
        } catch (e: ArithmeticException) {
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "수학 연산 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "variableCount" to variables.size,
                    "variables" to variables.keys.joinToString(", ")
                )
            )
        } catch (e: Exception) {
            throw DomainException(
                errorCode = ErrorCode.UNEXPECTED_ERROR,
                message = "AST 평가 중 예상치 못한 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "variableCount" to variables.size,
                    "variables" to variables.keys.joinToString(", "),
                    "exceptionType" to e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * 평가 결과의 유효성을 검증합니다.
     */
    private fun validateEvaluationResult(result: Any?, astNode: hs.kr.entrydsm.domain.ast.entities.ASTNode) {
        when (result) {
            is Double -> {
                if (result.isNaN()) {
                    throw DomainException(
                        errorCode = ErrorCode.MATH_ERROR,
                        message = "계산 결과가 NaN입니다",
                        context = mapOf(
                            "astType" to astNode.javaClass.simpleName,
                            "result" to "NaN"
                        )
                    )
                }
                if (result.isInfinite()) {
                    throw DomainException(
                        errorCode = ErrorCode.MATH_ERROR,
                        message = "계산 결과가 무한대입니다",
                        context = mapOf(
                            "astType" to astNode.javaClass.simpleName,
                            "result" to if (result > 0) "+Infinity" else "-Infinity"
                        )
                    )
                }
            }
            is Float -> {
                if (result.isNaN()) {
                    throw DomainException(
                        errorCode = ErrorCode.MATH_ERROR,
                        message = "계산 결과가 NaN입니다",
                        context = mapOf(
                            "astType" to astNode.javaClass.simpleName,
                            "result" to "NaN"
                        )
                    )
                }
                if (result.isInfinite()) {
                    throw DomainException(
                        errorCode = ErrorCode.MATH_ERROR,
                        message = "계산 결과가 무한대입니다",
                        context = mapOf(
                            "astType" to astNode.javaClass.simpleName,
                            "result" to if (result > 0) "+Infinity" else "-Infinity"
                        )
                    )
                }
            }
        }
    }

    private fun createFailureResult(request: CalculationRequest, error: String, startTime: Long): CalculationResult {
        val executionTime = System.currentTimeMillis() - startTime
        return CalculationResult(
            result = null,
            executionTimeMs = executionTime,
            formula = request.formula,
            errors = listOf(error)
        )
    }

    private fun generateCacheKey(request: CalculationRequest, session: CalculationSession?): String {
        val variables = (session?.variables ?: emptyMap()) + request.variables
        
        // 변수들을 키로 정렬하여 일관된 문자열 생성
        val sortedVariables = variables.entries.sortedBy { it.key }
            .joinToString(",") { "${it.key}=${it.value}" }
        
        // 수식과 변수를 결합한 문자열
        val combinedString = "${request.formula}|$sortedVariables"
        
        // SHA-256 해시로 안전한 캐시 키 생성
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(combinedString.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // SHA-256을 사용할 수 없는 경우 fallback으로 안전한 문자열 기반 키 사용
            "formula_${request.formula.replace("[^a-zA-Z0-9]".toRegex(), "_")}_vars_${sortedVariables.replace("[^a-zA-Z0-9,=]".toRegex(), "_")}"
                .take(200) // 키 길이 제한
        }
    }

    private fun getCachedResult(key: String): CachedResult? {
        val cached = calculationCache[key]
        return if (cached != null && System.currentTimeMillis() - cached.timestamp < 3600000) {
            cached
        } else {
            if (cached != null) {
                calculationCache.remove(key)
            }
            null
        }
    }

    private fun cacheResult(key: String, result: CalculationResult) {
        if (result.isSuccess()) {

            if (calculationCache.size >= 950) {
                manageCaches(900)
            }
            
            if (calculationCache.size < 1000) {
                calculationCache[key] = CachedResult(
                    result = result.result,
                    executionTime = result.executionTimeMs,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }

    private fun extractVariables(expression: String): Set<String> {
        val pattern = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
        return pattern.findAll(expression)
            .map { it.value }
            .filter { it.uppercase() !in setOf("PI", "E", "TRUE", "FALSE", "SIN", "COS", "TAN", "LOG", "ABS") }
            .toSet()
    }

    private fun extractFunctions(expression: String): Set<String> {
        val pattern = Regex("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(")
        return pattern.findAll(expression)
            .map { it.groupValues[1] }
            .toSet()
    }


    /**
     * AST의 실제 구조적 깊이를 계산합니다.
     * 
     * @param ast 깊이를 계산할 AST 객체
     * @return AST의 최대 깊이 (루트에서 가장 깊은 리프 노드까지의 거리)
     */
    private fun calculateASTDepth(ast: Any): Int {
        return try {
            val astNode = ast as? hs.kr.entrydsm.domain.ast.entities.ASTNode
                ?: throw CalculatorException.invalidAstNodeType(ast.javaClass.simpleName)

            calculateNodeDepth(astNode)
            
        } catch (e: IllegalArgumentException) {
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "AST 깊이 계산 실패: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "operation" to "calculateASTDepth"
                )
            )
        } catch (e: StackOverflowError) {
            throw DomainException(
                errorCode = ErrorCode.AST_DEPTH_EXCEEDED,
                message = "AST 깊이 계산 중 스택 오버플로우: AST가 너무 깊습니다",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "operation" to "calculateASTDepth",
                    "error" to "StackOverflow"
                )
            )
        } catch (e: Exception) {
            throw DomainException(
                errorCode = ErrorCode.UNEXPECTED_ERROR,
                message = "AST 깊이 계산 중 예상치 못한 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "astType" to ast.javaClass.simpleName,
                    "operation" to "calculateASTDepth",
                    "exceptionType" to e.javaClass.simpleName
                )
            )
        }
    }
    
    /**
     * AST 노드의 실제 깊이를 재귀적으로 계산합니다.
     * 
     * @param node 깊이를 계산할 AST 노드
     * @return 해당 노드를 루트로 하는 서브트리의 최대 깊이
     */
    private fun calculateNodeDepth(node: hs.kr.entrydsm.domain.ast.entities.ASTNode): Int {
        return when (node) {
            // 리프 노드들은 깊이 1
            is hs.kr.entrydsm.domain.ast.entities.NumberNode,
            is hs.kr.entrydsm.domain.ast.entities.BooleanNode,
            is hs.kr.entrydsm.domain.ast.entities.VariableNode -> 1
            
            // 단항 연산자 노드: 1 + 피연산자의 깊이
            is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode -> {
                1 + calculateNodeDepth(node.operand)
            }
            
            // 이항 연산자 노드: 1 + max(왼쪽 자식, 오른쪽 자식)
            is hs.kr.entrydsm.domain.ast.entities.BinaryOpNode -> {
                1 + maxOf(
                    calculateNodeDepth(node.left),
                    calculateNodeDepth(node.right)
                )
            }
            
            // 함수 호출 노드: 1 + 인수들 중 최대 깊이
            is hs.kr.entrydsm.domain.ast.entities.FunctionCallNode -> {
                if (node.args.isEmpty()) {
                    1
                } else {
                    1 + node.args.maxOf { calculateNodeDepth(it) }
                }
            }
            
            // 조건문 노드 (IF): 1 + max(조건, 참값, 거짓값)
            is hs.kr.entrydsm.domain.ast.entities.IfNode -> {
                1 + maxOf(
                    calculateNodeDepth(node.condition),
                    calculateNodeDepth(node.trueValue),
                    calculateNodeDepth(node.falseValue)
                )
            }
            
            // 인수 목록 노드: 1 + 인수들 중 최대 깊이
            is hs.kr.entrydsm.domain.ast.entities.ArgumentsNode -> {
                if (node.arguments.isEmpty()) {
                    1
                } else {
                    1 + node.arguments.maxOf { calculateNodeDepth(it) }
                }
            }
            
            // 알 수 없는 노드 타입: getChildren() 메서드 활용
            else -> {
                try {
                    val children = node.getChildren()
                    if (children.isEmpty()) {
                        1 // 자식이 없으면 리프 노드
                    } else {
                        1 + children.maxOf { calculateNodeDepth(it) }
                    }
                } catch (e: Exception) {
                    throw DomainException(
                        errorCode = ErrorCode.AST_TRAVERSAL_ERROR,
                        message = "알 수 없는 AST 노드 타입의 깊이 계산 실패: ${e.message}",
                        cause = e,
                        context = mapOf(
                            "nodeType" to node.javaClass.simpleName,
                            "operation" to "calculateNodeDepth",
                            "exceptionType" to e.javaClass.simpleName
                        )
                    )
                }
            }
        }
    }

    private fun estimateMemoryUsage(expression: String): Int {
        // 간단한 메모리 사용량 추정
        return (expression.length * 0.001).toInt() + 1 // MB
    }

    /**
     * 캐시된 결과를 나타내는 데이터 클래스입니다.
     */
    private data class CachedResult(
        val result: Any?,
        val executionTime: Long,
        val timestamp: Long
    ) {
        fun toCalculationResult(formula: String, startTime: Long): CalculationResult {
            return CalculationResult(
                result = result,
                executionTimeMs = System.currentTimeMillis() - startTime,
                formula = formula,
                metadata = mapOf("fromCache" to true)
            )
        }
    }

    /**
     * 성능 메트릭을 관리하는 클래스입니다.
     */
    private class PerformanceMetrics {
        private var totalRequests = 0L
        private var totalFailures = 0L
        private var totalCacheHits = 0L
        private var totalExecutionTime = 0L
        private var requestCount = 0L

        fun incrementTotalRequests() = synchronized(this) { totalRequests++ }
        fun incrementFailures() = synchronized(this) { totalFailures++ }
        fun incrementCacheHits() = synchronized(this) { totalCacheHits++ }
        
        fun updateExecutionTime(time: Long) = synchronized(this) {
            totalExecutionTime += time
            requestCount++
        }

        fun getMetrics(): Map<String, Any> = synchronized(this) {
            mapOf(
                "totalRequests" to totalRequests,
                "totalFailures" to totalFailures,
                "totalCacheHits" to totalCacheHits,
                "averageExecutionTime" to if (requestCount > 0) totalExecutionTime.toDouble() / requestCount else 0.0,
                "successRate" to if (totalRequests > 0) ((totalRequests - totalFailures).toDouble() / totalRequests) else 0.0,
                "cacheHitRate" to if (totalRequests > 0) (totalCacheHits.toDouble() / totalRequests) else 0.0
            )
        }
    }

    /**
     * 서비스의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "serviceName" to CALCULATOR_SERVICE,
        "defaultTimeoutMs" to config.defaultTimeoutMs,
        "maxRetries" to config.maxRetries,
        "cacheEnabled" to true,
        "maxCacheSize" to 1000,
        "cacheExpirationMs" to 3600000,
        "parallelProcessingEnabled" to true
    )

    /**
     * 서비스의 성능 통계를 반환합니다.
     *
     * @return 성능 통계 맵
     */
    fun getStatistics(): Map<String, Any> {
        val metrics = performanceMetrics.getMetrics()
        return metrics + mapOf(
            "currentCacheSize" to calculationCache.size,
            "maxCacheSize" to 1000
        )
    }

    /**
     * 서비스 상태를 확인합니다.
     *
     * @return 상태 정보 맵
     */
    fun getStatus(): Map<String, Any> = mapOf(
        "status" to "active",
        "healthCheck" to checkHealth(),
        "lastActivity" to System.currentTimeMillis(),
        "cacheStatus" to mapOf(
            "size" to calculationCache.size,
            "enabled" to true
        )
    )

    /**
     * 요청 유효성을 검증합니다.
     */
    private fun validateRequest(request: CalculationRequest, session: CalculationSession?) {
        if (!validitySpec.isSatisfiedBy(request, session)) {
            val errors = validitySpec.getValidationErrors(request, session)
            throw DomainException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                message = "유효성 검증 실패: ${errors.joinToString(", ") { it.message }}",
                context = mapOf(
                    "formula" to request.formula,
                    "errorCount" to errors.size,
                    "errors" to errors.map { it.message }
                )
            )
        }
    }

    /**
     * 계산 정책을 확인합니다.
     */
    private fun checkPolicy(request: CalculationRequest, session: CalculationSession?) {
        if (session != null && !calculationPolicy.isCalculationAllowed(request, session)) {
            throw DomainException(
                errorCode = ErrorCode.BUSINESS_RULE_VIOLATION,
                message = "계산 정책 위반",
                context = mapOf(
                    "formula" to request.formula,
                    "sessionId" to session.sessionId
                )
            )
        }
    }

    /**
     * 캐시에서 결과를 조회합니다.
     */
    private fun retrieveFromCache(request: CalculationRequest, session: CalculationSession?): CalculationResult? {
        if (session?.settings?.enableCaching != true) return null
        
        val cacheKey = generateCacheKey(request, session)
        val cachedResult = getCachedResult(cacheKey)
        
        return if (cachedResult != null) {
            performanceMetrics.incrementCacheHits()
            cachedResult.toCalculationResult(request.formula, System.currentTimeMillis())
        } else null
    }

    /**
     * 실제 계산을 수행합니다.
     */
    private fun performCalculation(request: CalculationRequest, session: CalculationSession?): CalculationResult {
        return runBlocking {
            executeCalculation(request, session)
        }
    }

    /**
     * 필요한 경우 결과를 캐시에 저장합니다.
     */
    private fun cacheResultIfNeeded(request: CalculationRequest, session: CalculationSession?, result: CalculationResult) {
        if (session?.settings?.enableCaching == true && result.isSuccess()) {
            val cacheKey = generateCacheKey(request, session)
            cacheResult(cacheKey, result)
        }
    }

    /**
     * 메트릭을 업데이트합니다.
     */
    private fun updateMetrics(request: CalculationRequest, session: CalculationSession?, executionTime: Long) {
        calculationPolicy.updateSessionMetrics(
            session?.sessionId ?: ANONYMOUS,
            executionTime,
            estimateMemoryUsage(request.formula)
        )
        performanceMetrics.updateExecutionTime(executionTime)
    }

    /**
     * 서비스 건강 상태를 확인합니다.
     *
     * @return 건강하면 true
     */
    private fun checkHealth(): Boolean {
        return try {
            // 간단한 계산으로 건강 상태 확인
            val testRequest = CalculationRequest("1+1", emptyMap())
            val result = calculate(testRequest)
            result.isSuccess()
        } catch (e: DomainException) {
            // 도메인 예외는 이미 적절한 컨텍스트를 포함하고 있음
            throw DomainException(
                errorCode = ErrorCode.HEALTH_CHECK_FAILED,
                message = "서비스 건강 상태 확인 실패: ${e.message}",
                cause = e,
                context = mapOf(
                    "healthCheckFormula" to "1+1",
                    "originalErrorCode" to e.getCode(),
                    "originalDomain" to e.getDomain(),
                    "healthCheckFailed" to true
                )
            )
        } catch (e: Exception) {
            // 예상치 못한 시스템 예외
            throw DomainException(
                errorCode = ErrorCode.HEALTH_CHECK_FAILED,
                message = "서비스 건강 상태 확인 중 예상치 못한 오류: ${e.message}",
                cause = e,
                context = mapOf(
                    "healthCheckFormula" to "1+1",
                    "exceptionType" to e.javaClass.simpleName,
                    "healthCheckFailed" to true
                )
            )
        }
    }
}