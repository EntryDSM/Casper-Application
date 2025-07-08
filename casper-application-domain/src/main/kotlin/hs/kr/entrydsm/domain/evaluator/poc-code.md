```kt
package com.example.calculator

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * 완전한 LR(1) Parser 구현을 포함하는 계산기 애플리케이션의 메인 진입점입니다.
 * Spring Boot 애플리케이션으로, 캐싱과 설정 속성을 활성화합니다.
 *
 * 주요 기능:
 * - 실제 LR(1) 아이템 집합 자동 생성
 * - 완전한 FIRST/FOLLOW 계산
 * - DFA 상태 자동 구축
 * - 완전한 파싱 테이블
 */
@SpringBootApplication // Spring Boot 애플리케이션임을 선언합니다.
@EnableCaching // Spring Cache 추상화를 활성화합니다.
@EnableConfigurationProperties(CalculatorProperties::class) // CalculatorProperties를 설정 속성으로 활성화합니다.
class ProductionCalculatorApplication {
    companion object {
        private val logger = LoggerFactory.getLogger(ProductionCalculatorApplication::class.java) // 로거 인스턴스 생성

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("애플리케이션 시작 중...") // 애플리케이션 시작 로그
            SpringApplication.run(ProductionCalculatorApplication::class.java, *args)
            logger.info("애플리케이션 시작 완료.") // 애플리케이션 시작 완료 로그
        }
    }
}

/**
 * 계산기 애플리케이션의 설정 속성을 정의하는 데이터 클래스입니다.
 * `application.yml` 또는 `application.properties` 파일에서 `calculator` 프리픽스로 설정됩니다.
 *
 * @property maxFormulaLength 허용되는 최대 수식 길이. 기본값은 5000입니다.
 * @property maxSteps 다단계 계산에서 허용되는 최대 단계 수. 기본값은 50입니다.
 * @property maxVariables 허용되는 최대 변수 개수. 기본값은 100입니다.
 * @property enableCaching 계산 결과 캐싱 활성화 여부. 기본값은 true입니다.
 * @property cacheTtlSeconds 캐시된 결과의 TTL(Time To Live) (초). 기본값은 3600초 (1시간)입니다.
 * @property debugMode 디버그 모드 활성화 여부. 기본값은 false입니다.
 */
@ConfigurationProperties(prefix = "calculator")
data class CalculatorProperties(
    val maxFormulaLength: Int = 5000,
    val maxSteps: Int = 50,
    val maxVariables: Int = 100,
    val enableCaching: Boolean = true,
    val cacheTtlSeconds: Long = 3600,
    val debugMode: Boolean = false,
) {
    init {
        // CalculatorProperties 초기화 시 로그 출력
        val logger = LoggerFactory.getLogger(CalculatorProperties::class.java)
        logger.info("CalculatorProperties 초기화 완료:")
        logger.info("  maxFormulaLength: {}", maxFormulaLength)
        logger.info("  maxSteps: {}", maxSteps)
        logger.info("  maxVariables: {}", maxVariables)
        logger.info("  enableCaching: {}", enableCaching)
        logger.info("  cacheTtlSeconds: {}", cacheTtlSeconds)
        logger.info("  debugMode: {}", debugMode)
    }
}

/**
 * 계산기 API의 REST 컨트롤러입니다.
 * 수식 계산, 다단계 계산, 파서 정보 조회를 위한 엔드포인트를 제공합니다.
 */
@RestController // 이 클래스가 REST 컨트롤러임을 나타냅니다.
@RequestMapping("/api/v1/calculator") // 이 컨트롤러의 모든 핸들러 메서드에 대한 기본 요청 매핑을 지정합니다.
class CalculatorController(
    private val calculatorService: CalculatorService, // CalculatorService를 주입받습니다.
) {
    private val logger = LoggerFactory.getLogger(javaClass) // 로거 인스턴스를 생성합니다.

    /**
     * 단일 수식 계산을 처리합니다.
     * @param request 계산 요청 (수식 및 변수 포함).
     * @return 계산 결과 또는 오류 응답.
     */
    @PostMapping("/calculate") // HTTP POST 요청을 /calculate 경로에 매핑합니다.
    fun calculate(
        @RequestBody request: CalculationRequest, // 요청 본문을 CalculationRequest 객체로 바인딩합니다.
    ): ResponseEntity<CalculationResponse> {
        logger.debug("calculate 메서드 호출됨. 요청: {}", request) // 메서드 호출 시작 로그
        val startTime = Instant.now() // 요청 처리 시작 시간 기록
        return try {
            logger.info("LR(1) 파서 계산 요청 수신: {}", request.formula) // 수식 계산 요청 수신 로그
            val result = calculatorService.calculate(request) // CalculatorService를 통해 계산을 수행합니다.
            val endTime = Instant.now() // 요청 처리 종료 시간 기록
            val duration = java.time.Duration.between(startTime, endTime).toMillis()
            logger.info(
                "LR(1) 파싱 및 계산 완료: {}ms, 결과: {}",
                duration,
                result.result,
            ) // 계산 완료 및 소요 시간, 결과 로그
            ResponseEntity.ok(result) // 성공 응답 반환
        } catch (e: CalculatorException) {
            logger.warn("LR(1) 파싱 실패 (CalculatorException): {}", e.message) // CalculatorException 발생 시 경고 로그
            ResponseEntity.badRequest().body(
                CalculationResponse(
                    success = false,
                    errorMessage = e.message,
                    errorCode = e.errorCode,
                ), // 실패 응답 반환
            )
        } catch (e: Exception) {
            logger.error("예상치 못한 오류 발생: {}", e.message, e) // 그 외 예외 발생 시 에러 로그
            ResponseEntity.internalServerError().body(
                CalculationResponse(
                    success = false,
                    errorMessage = "서버 내부 오류 발생: ${e.message}",
                    errorCode = "INTERNAL_SERVER_ERROR",
                ),
            )
        }
    }

    /**
     * 다단계 수식 계산을 처리합니다.
     * @param request 다단계 계산 요청 (단계 목록 및 변수 포함).
     * @return 다단계 계산 결과 또는 오류 응답.
     */
    @PostMapping("/multi-step") // HTTP POST 요청을 /multi-step 경로에 매핑합니다.
    fun calculateMultiStep(
        @RequestBody request: MultiStepCalculationRequest, // 요청 본문을 MultiStepCalculationRequest 객체로 바인딩합니다.
    ): ResponseEntity<MultiStepCalculationResponse> {
        logger.debug("calculateMultiStep 메서드 호출됨. 요청: {}단계", request.steps?.size ?: 0) // 메서드 호출 시작 로그
        val startTime = Instant.now() // 요청 처리 시작 시간 기록
        return try {
            logger.info("LR(1) 다단계 계산 요청 수신: {}단계", request.steps?.size ?: 0) // 다단계 계산 요청 수신 로그
            val result = calculatorService.calculateMultiStep(request) // CalculatorService를 통해 다단계 계산을 수행합니다.
            val endTime = Instant.now() // 요청 처리 종료 시간 기록
            val duration = java.time.Duration.between(startTime, endTime).toMillis()
            logger.info(
                "LR(1) 다단계 계산 완료: {}ms, 최종 변수: {}",
                duration,
                result.finalVariables,
            ) // 다단계 계산 완료 및 소요 시간, 최종 변수 로그
            ResponseEntity.ok(result) // 성공 응답 반환
        } catch (e: CalculatorException) {
            logger.warn("LR(1) 다단계 계산 실패 (CalculatorException): {}", e.message) // CalculatorException 발생 시 경고 로그
            ResponseEntity.badRequest().body(
                MultiStepCalculationResponse(
                    success = false,
                    errorMessage = e.message,
                    errorCode = e.errorCode,
                ), // 실패 응답 반환
            )
        } catch (e: Exception) {
            logger.error("예상치 못한 오류 발생: {}", e.message, e) // 그 외 예외 발생 시 에러 로그
            ResponseEntity.internalServerError().body(
                MultiStepCalculationResponse(
                    success = false,
                    errorMessage = "서버 내부 오류 발생: ${e.message}",
                    errorCode = "INTERNAL_SERVER_ERROR",
                ),
            )
        }
    }

    /**
     * 파서 정보를 조회합니다.
     * @return 파서 타입, 문법, 상태 수, 충돌 정보.
     */
    @GetMapping("/parser-info") // HTTP GET 요청을 /parser-info 경로에 매핑합니다.
    fun getParserInfo(): ResponseEntity<Map<String, Any>> {
        logger.debug("getParserInfo 메서드 호출됨.") // 메서드 호출 시작 로그
        val parserInfo = mapOf(
            "parserType" to "LR(1) - 완전 구현",
            "grammar" to Grammar.productions.map { it.toString() },
            "states" to LRParserTable.getStateCount(),
            "conflicts" to LRParserTable.getConflicts(),
        )
        logger.info("파서 정보 조회 완료: {}", parserInfo) // 파서 정보 조회 완료 로그
        return ResponseEntity.ok(parserInfo) // 파서 정보 반환
    }
}

/**
 * 수식 계산 및 다단계 계산을 처리하는 서비스 클래스입니다.
 * 캐싱, 파싱, 유효성 검사 로직을 포함합니다.
 */
@Service // 이 클래스가 서비스 계층의 컴포넌트임을 나타냅니다.
class CalculatorService(
    private val properties: CalculatorProperties, // 설정 속성을 주입받습니다.
    private val parser: RealLRParser, // 실제 LR 파서를 주입받습니다.
    private val validator: FormulaValidator, // 수식 유효성 검사기를 주입받습니다.
) {
    private val logger = LoggerFactory.getLogger(javaClass) // 로거 인스턴스를 생성합니다.

    /**
     * 단일 수식 계산을 수행합니다.
     * 캐싱이 활성화되어 있으면 계산 결과를 캐시합니다.
     * @param request 계산 요청.
     * @return 계산 결과.
     * @throws CalculatorException 유효성 검사 또는 파싱/평가 중 오류 발생 시.
     */
    @Cacheable("calculations", condition = "#root.target.properties.enableCaching") // 캐싱 활성화 조건
    fun calculate(request: CalculationRequest): CalculationResponse {
        logger.debug("calculate 메서드 호출됨. 수식: {}, 변수: {}", request.formula, request.variables) // 메서드 호출 시작 로그
        validator.validateCalculationRequest(request, properties) // 계산 요청 유효성 검사
        logger.debug("계산 요청 유효성 검사 완료.") // 유효성 검사 완료 로그

        var result: CalculationResponse? = null
        val executionTime =
            measureTimeMillis { // 코드 블록 실행 시간 측정
                try {
                    logger.info("수식 파싱 시작: {}", request.formula) // 파싱 시작 로그
                    val ast = parser.parse(request.formula) // 수식 파싱하여 AST 생성
                    logger.info("수식 파싱 완료. AST: {}", ast) // 파싱 완료 로그

                    val requiredVars = ast.getVariables() // AST에서 필요한 변수 추출
                    logger.debug("필요한 변수: {}", requiredVars) // 필요한 변수 로그
                    validator.validateVariables(requiredVars, request.variables ?: emptyMap()) // 변수 유효성 검사
                    logger.debug("변수 유효성 검사 완료.") // 변수 유효성 검사 완료 로그

                    val evaluator = ExpressionEvaluator(request.variables ?: emptyMap()) // 표현식 평가기 생성
                    logger.info("AST 평가 시작.") // 평가 시작 로그
                    val calculationResult = evaluator.evaluate(ast) // AST 평가
                    logger.info("AST 평가 완료. 결과: {}", calculationResult) // 평가 완료 로그

                    result =
                        CalculationResponse(
                            originalFormula = request.formula,
                            processedFormula = ast.toString(),
                            variables = request.variables,
                            result = calculationResult,
                            executionTimeMs = 0, // 실제 시간은 measureTimeMillis에서 설정
                            success = true,
                        )
                    logger.debug("CalculationResponse 객체 생성 완료.") // 응답 객체 생성 완료 로그
                } catch (e: Exception) {
                    logger.error("LR(1) 파싱 또는 평가 중 오류 발생: {}", e.message, e) // 파싱/평가 오류 로그
                    throw CalculatorException("LR(1) 파싱 오류: ${e.message}", "LR_PARSING_ERROR", e) // CalculatorException으로 래핑하여 재throw
                }
            }

        logger.info("단일 계산 총 실행 시간: {}ms", executionTime) // 총 실행 시간 로그
        return result!!.copy(executionTimeMs = executionTime) // 실행 시간 업데이트하여 결과 반환
    }

    /**
     * 다단계 수식 계산을 수행합니다.
     * 각 단계의 결과를 다음 단계의 변수로 활용합니다.
     * @param request 다단계 계산 요청.
     * @return 다단계 계산 결과.
     * @throws CalculatorException 유효성 검사 또는 단계 실행 중 오류 발생 시.
     */
    fun calculateMultiStep(request: MultiStepCalculationRequest): MultiStepCalculationResponse {
        logger.debug("calculateMultiStep 메서드 호출됨. 단계 수: {}, 초기 변수: {}", request.steps?.size, request.variables) // 메서드 호출 시작 로그
        validator.validateMultiStepRequest(request, properties) // 다단계 계산 요청 유효성 검사
        logger.debug("다단계 계산 요청 유효성 검사 완료.") // 유효성 검사 완료 로그

        val stepResults = mutableListOf<StepResult>() // 각 단계의 결과를 저장할 리스트
        val allVariables = (request.variables ?: emptyMap()).toMutableMap() // 모든 변수를 저장할 맵 (가변)
        var totalExecutionTime = 0L // 총 실행 시간

        logger.info("총 {}단계 계산 시작.", request.steps!!.size) // 다단계 계산 시작 로그
        request.steps.forEachIndexed { index, step ->
            logger.info("---- 단계 {} 시작: {} ----", index + 1, step.stepName ?: "") // 각 단계 시작 로그
            var stepResult: StepResult? = null
            val stepTime =
                measureTimeMillis { // 각 단계 실행 시간 측정
                    try {
                        logger.debug("단계 {} 수식 파싱 시작: {}", index + 1, step.formula) // 단계 수식 파싱 시작 로그
                        val ast = parser.parse(step.formula) // 단계 수식 파싱
                        logger.debug("단계 {} 수식 파싱 완료. AST: {}", index + 1, ast) // 단계 수식 파싱 완료 로그

                        val evaluator = ExpressionEvaluator(allVariables) // 현재 변수 맵으로 평가기 생성
                        logger.debug("단계 {} AST 평가 시작.", index + 1) // 단계 AST 평가 시작 로그
                        val result = evaluator.evaluate(ast) // AST 평가
                        logger.debug("단계 {} AST 평가 완료. 결과: {}", index + 1, result) // 단계 AST 평가 완료 로그

                        step.resultVariable?.takeIf { it.isNotBlank() }?.let { resultVar ->
                            allVariables[resultVar] = result // 결과 변수가 있으면 변수 맵에 저장
                            logger.info("단계 {} 변수 저장: {} = {}", index + 1, resultVar, result) // 변수 저장 로그
                        }

                        stepResult =
                            StepResult(
                                stepName = step.stepName ?: "단계 ${index + 1}",
                                originalFormula = step.formula,
                                processedFormula = ast.toString(),
                                result = result,
                                resultVariable = step.resultVariable,
                                executionTimeMs = 0, // 실제 시간은 measureTimeMillis에서 설정
                            )
                        logger.debug("단계 {} StepResult 객체 생성 완료.", index + 1) // StepResult 객체 생성 완료 로그
                    } catch (e: Exception) {
                        logger.error("단계 {} 실행 중 오류 발생: {}", index + 1, e.message, e) // 단계 실행 오류 로그
                        throw CalculatorException("단계 ${index + 1} 실행 오류: ${e.message}", "STEP_EXECUTION_ERROR", e) // CalculatorException으로 래핑하여 재throw
                    }
                }
            totalExecutionTime += stepTime // 총 실행 시간에 단계 시간 추가
            stepResults.add(stepResult!!.copy(executionTimeMs = stepTime)) // 실행 시간 업데이트하여 단계 결과 추가
            logger.info("---- 단계 {} 완료. 실행 시간: {}ms ----", index + 1, stepTime) // 각 단계 완료 로그
        }

        logger.info("총 {}단계 계산 완료. 총 실행 시간: {}ms, 최종 변수: {}", request.steps.size, totalExecutionTime, allVariables) // 다단계 계산 완료 로그
        return MultiStepCalculationResponse(
            steps = stepResults,
            finalVariables = allVariables,
            executionTimeMs = totalExecutionTime,
            success = true,
        ) // 다단계 계산 결과 반환
    }
}

// LR 토큰 타입 정의 (확장)
/**
 * 계산기 언어에서 사용되는 모든 토큰 타입을 정의하는 열거형입니다.
 * 터미널 심볼과 논터미널 심볼을 포함합니다.
 */
enum class TokenType {
    // 터미널 심볼들
    NUMBER, // 숫자 리터럴 (예: 123, 3.14)
    IDENTIFIER, // 식별자 (변수명, 함수명 등)
    VARIABLE, // 중괄호로 묶인 변수 (예: {x})
    PLUS, // 덧셈 (+)
    MINUS, // 뺄셈 (-)
    MULTIPLY, // 곱셈 (*)
    DIVIDE, // 나눗셈 (/)
    POWER, // 거듭제곱 (^)
    MODULO, // 나머지 (%)

    // 비교 연산자들
    EQUAL, // 같음 (==)
    NOT_EQUAL, // 같지 않음 (!=)
    LESS, // 미만 (<)
    LESS_EQUAL, // 이하 (<=)
    GREATER, // 초과 (>)
    GREATER_EQUAL, // 이상 (>=)

    // 논리 연산자들
    AND, // 논리 AND (&&)
    OR, // 논리 OR (||)
    NOT, // 논리 NOT (!)
    LEFT_PAREN, // 왼쪽 괄호 ((
    RIGHT_PAREN, // 오른쪽 괄호 ())
    COMMA, // 쉼표 (,)
    IF, // IF 키워드
    TRUE, // TRUE 키워드
    FALSE, // FALSE 키워드
    DOLLAR, // EOF (End Of File) 심볼

    // 논터미널 심볼들 (파싱 과정에서 생성되는 중간 심볼)
    START, // 문법의 시작 심볼 (확장된 문법용)
    EXPR, // 표현식
    AND_EXPR, // AND 표현식
    COMP_EXPR, // 비교 표현식
    ARITH_EXPR, // 산술 표현식
    TERM, // 항
    FACTOR, // 인자
    PRIMARY, // 기본 요소
    ARGS, // 함수 인수 목록
}

/**
 * 연산자 우선순위와 결합성을 정의하는 데이터 클래스입니다.
 * 충돌 해결에 사용됩니다.
 */
data class OperatorPrecedence(
    val precedence: Int, // 우선순위 (높을수록 먼저 계산)
    val associativity: Associativity // 결합성
)

enum class Associativity {
    LEFT, // 좌결합
    RIGHT, // 우결합
    NONE // 비결합 (같은 우선순위 연산자 연속 사용 불가)
}

/**
 * 연산자 우선순위 테이블입니다.
 * Shift/Reduce 및 Reduce/Reduce 충돌 해결에 사용됩니다.
 */
object OperatorPrecedenceTable {
    private val precedenceMap = mapOf(
        // 논리 연산자 (가장 낮은 우선순위)
        TokenType.OR to OperatorPrecedence(1, Associativity.LEFT),
        TokenType.AND to OperatorPrecedence(2, Associativity.LEFT),
        
        // 비교 연산자
        TokenType.EQUAL to OperatorPrecedence(3, Associativity.NONE),
        TokenType.NOT_EQUAL to OperatorPrecedence(3, Associativity.NONE),
        TokenType.LESS to OperatorPrecedence(4, Associativity.NONE),
        TokenType.LESS_EQUAL to OperatorPrecedence(4, Associativity.NONE),
        TokenType.GREATER to OperatorPrecedence(4, Associativity.NONE),
        TokenType.GREATER_EQUAL to OperatorPrecedence(4, Associativity.NONE),
        
        // 산술 연산자
        TokenType.PLUS to OperatorPrecedence(5, Associativity.LEFT),
        TokenType.MINUS to OperatorPrecedence(5, Associativity.LEFT),
        TokenType.MULTIPLY to OperatorPrecedence(6, Associativity.LEFT),
        TokenType.DIVIDE to OperatorPrecedence(6, Associativity.LEFT),
        TokenType.MODULO to OperatorPrecedence(6, Associativity.LEFT),
        
        // 거듭제곱 (가장 높은 우선순위)
        TokenType.POWER to OperatorPrecedence(7, Associativity.RIGHT),
        
        // 단항 연산자 (NOT, unary +, unary -)는 파싱 과정에서 특별 처리
        TokenType.NOT to OperatorPrecedence(8, Associativity.RIGHT)
    )
    
    fun getPrecedence(token: TokenType): OperatorPrecedence? = precedenceMap[token]
}

/**
 * 토큰의 정보를 담는 데이터 클래스입니다.
 * @property type 토큰의 타입 (TokenType).
 * @property value 토큰의 원본 문자열 값.
 * @property position 토큰이 시작하는 입력 문자열 내의 위치 (0-based).
 */
data class Token(
    val type: TokenType,
    val value: String,
    val position: Int = 0,
) {
    // 토큰을 문자열로 표현할 때 사용됩니다.
    override fun toString() = if (value.isNotEmpty()) "$type($value)" else type.toString()
}

// 완전한 문법 정의 (비교/논리 연산자 포함)
/**
 * 계산기 언어의 문법 규칙을 정의하는 객체입니다.
 * 모든 생산 규칙(Production), 시작 심볼, 확장된 생산 규칙, 터미널 및 논터미널 심볼을 포함합니다.
 */
object Grammar {
    private val logger = LoggerFactory.getLogger(this::class.java) // 로거 인스턴스 생성

    // 모든 생산 규칙 목록 (AST 빌더 포함)
    val productions =
        listOf(
            // 논리합 (가장 낮은 우선순위)
            // 0: EXPR → EXPR || AND_EXPR
            Production(0, TokenType.EXPR, listOf(TokenType.EXPR, TokenType.OR, TokenType.AND_EXPR), ASTBuilder.BinaryOp("||")),
            // 1: EXPR → AND_EXPR
            Production(1, TokenType.EXPR, listOf(TokenType.AND_EXPR), ASTBuilder.Identity),
            // 논리곱
            // 2: AND_EXPR → AND_EXPR && COMP_EXPR
            Production(2, TokenType.AND_EXPR, listOf(TokenType.AND_EXPR, TokenType.AND, TokenType.COMP_EXPR), ASTBuilder.BinaryOp("&&")),
            // 3: AND_EXPR → COMP_EXPR
            Production(3, TokenType.AND_EXPR, listOf(TokenType.COMP_EXPR), ASTBuilder.Identity),
            // 비교 연산
            // 4: COMP_EXPR → COMP_EXPR == ARITH_EXPR
            Production(4, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.EQUAL, TokenType.ARITH_EXPR), ASTBuilder.BinaryOp("==")),
            // 5: COMP_EXPR → COMP_EXPR != ARITH_EXPR
            Production(5, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.NOT_EQUAL, TokenType.ARITH_EXPR), ASTBuilder.BinaryOp("!=")),
            // 6: COMP_EXPR → COMP_EXPR < ARITH_EXPR
            Production(6, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.LESS, TokenType.ARITH_EXPR), ASTBuilder.BinaryOp("<")),
            // 7: COMP_EXPR → COMP_EXPR <= ARITH_EXPR
            Production(7, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.LESS_EQUAL, TokenType.ARITH_EXPR), ASTBuilder.BinaryOp("<=")),
            // 8: COMP_EXPR → COMP_EXPR > ARITH_EXPR
            Production(8, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.GREATER, TokenType.ARITH_EXPR), ASTBuilder.BinaryOp(">")),
            // 9: COMP_EXPR → COMP_EXPR >= ARITH_EXPR
            Production(9, TokenType.COMP_EXPR, listOf(TokenType.COMP_EXPR, TokenType.GREATER_EQUAL, TokenType.ARITH_EXPR), ASTBuilder.BinaryOp(">=")),
            // 10: COMP_EXPR → ARITH_EXPR
            Production(10, TokenType.COMP_EXPR, listOf(TokenType.ARITH_EXPR), ASTBuilder.Identity),
            // 산술 표현식
            // 11: ARITH_EXPR → ARITH_EXPR + TERM
            Production(11, TokenType.ARITH_EXPR, listOf(TokenType.ARITH_EXPR, TokenType.PLUS, TokenType.TERM), ASTBuilder.BinaryOp("+")),
            // 12: ARITH_EXPR → ARITH_EXPR - TERM
            Production(12, TokenType.ARITH_EXPR, listOf(TokenType.ARITH_EXPR, TokenType.MINUS, TokenType.TERM), ASTBuilder.BinaryOp("-")),
            // 13: ARITH_EXPR → TERM
            Production(13, TokenType.ARITH_EXPR, listOf(TokenType.TERM), ASTBuilder.Identity),
            // 14: TERM → TERM * FACTOR
            Production(14, TokenType.TERM, listOf(TokenType.TERM, TokenType.MULTIPLY, TokenType.FACTOR), ASTBuilder.BinaryOp("*")),
            // 15: TERM → TERM / FACTOR
            Production(15, TokenType.TERM, listOf(TokenType.TERM, TokenType.DIVIDE, TokenType.FACTOR), ASTBuilder.BinaryOp("/")),
            // 16: TERM → TERM % FACTOR
            Production(16, TokenType.TERM, listOf(TokenType.TERM, TokenType.MODULO, TokenType.FACTOR), ASTBuilder.BinaryOp("%")),
            // 17: TERM → FACTOR
            Production(17, TokenType.TERM, listOf(TokenType.FACTOR), ASTBuilder.Identity),
            // 18: FACTOR → PRIMARY ^ FACTOR (우결합)
            Production(18, TokenType.FACTOR, listOf(TokenType.PRIMARY, TokenType.POWER, TokenType.FACTOR), ASTBuilder.BinaryOp("^")),
            // 19: FACTOR → PRIMARY
            Production(19, TokenType.FACTOR, listOf(TokenType.PRIMARY), ASTBuilder.Identity),
            // 20: PRIMARY → ( EXPR )
            Production(20, TokenType.PRIMARY, listOf(TokenType.LEFT_PAREN, TokenType.EXPR, TokenType.RIGHT_PAREN), ASTBuilder.Parenthesized),
            // 21: PRIMARY → - PRIMARY
            Production(21, TokenType.PRIMARY, listOf(TokenType.MINUS, TokenType.PRIMARY), ASTBuilder.UnaryOp("-")),
            // 22: PRIMARY → + PRIMARY
            Production(22, TokenType.PRIMARY, listOf(TokenType.PLUS, TokenType.PRIMARY), ASTBuilder.UnaryOp("+")),
            // 23: PRIMARY → ! PRIMARY
            Production(23, TokenType.PRIMARY, listOf(TokenType.NOT, TokenType.PRIMARY), ASTBuilder.UnaryOp("!")),
            // 24: PRIMARY → NUMBER
            Production(24, TokenType.PRIMARY, listOf(TokenType.NUMBER), ASTBuilder.Number),
            // 25: PRIMARY → VARIABLE
            Production(25, TokenType.PRIMARY, listOf(TokenType.VARIABLE), ASTBuilder.Variable),
            // 26: PRIMARY → IDENTIFIER
            Production(26, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER), ASTBuilder.Variable),
            // 27: PRIMARY → TRUE
            Production(27, TokenType.PRIMARY, listOf(TokenType.TRUE), ASTBuilder.BooleanTrue),
            // 28: PRIMARY → FALSE
            Production(28, TokenType.PRIMARY, listOf(TokenType.FALSE), ASTBuilder.BooleanFalse),
            // 29: PRIMARY → IDENTIFIER ( ARGS )
            Production(29, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.ARGS, TokenType.RIGHT_PAREN), ASTBuilder.FunctionCall),
            // 30: PRIMARY → IDENTIFIER ( )
            Production(30, TokenType.PRIMARY, listOf(TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN), ASTBuilder.FunctionCallEmpty),
            // 31: PRIMARY → IF ( EXPR , EXPR , EXPR )
            Production(
                31,
                TokenType.PRIMARY,
                listOf(
                    TokenType.IF,
                    TokenType.LEFT_PAREN,
                    TokenType.EXPR,
                    TokenType.COMMA,
                    TokenType.EXPR,
                    TokenType.COMMA,
                    TokenType.EXPR,
                    TokenType.RIGHT_PAREN,
                ),
                ASTBuilder.If
            ),
            // 32: ARGS → EXPR
            Production(32, TokenType.ARGS, listOf(TokenType.EXPR), ASTBuilder.ArgsSingle),
            // 33: ARGS → ARGS , EXPR
            Production(33, TokenType.ARGS, listOf(TokenType.ARGS, TokenType.COMMA, TokenType.EXPR), ASTBuilder.ArgsMultiple),
        )

    val startSymbol = TokenType.EXPR // 문법의 시작 심볼
    // 확장된 문법의 시작 생산 규칙 (LR(1) 파서 구축을 위해 추가)
    // 모든 터미널 심볼 (TokenType 열거형에서 DOLLAR까지)
    val terminals = TokenType.values().filter { it.ordinal <= TokenType.DOLLAR.ordinal }.toSet()
    // 모든 논터미널 심볼 (TokenType 열거형에서 DOLLAR 이후)
    val nonTerminals = TokenType.values().filter { it.ordinal > TokenType.DOLLAR.ordinal }.toSet()

    val augmentedProduction = Production(-1, TokenType.START, listOf(TokenType.EXPR, TokenType.DOLLAR), ASTBuilder.Start)

    init {
        logger.info("Grammar 초기화 완료. 총 {}개의 생산 규칙이 정의되었습니다.", productions.size) // 문법 초기화 로그
        logger.debug("시작 심볼: {}", startSymbol) // 시작 심볼 로그
        logger.debug("확장된 생산 규칙: {}", augmentedProduction) // 확장된 생산 규칙 로그
        logger.debug("터미널 심볼: {}", terminals) // 터미널 심볼 로그
        logger.debug("논터미널 심볼: {}", nonTerminals) // 논터미널 심볼 로그
    }

    /**
     * 주어진 ID에 해당하는 생산 규칙을 반환합니다.
     * @param id 조회할 생산 규칙의 ID.
     * @return 해당 생산 규칙.
     */
    fun getProduction(id: Int) = productions[id]
}

/**
 * 문법의 생산 규칙을 나타내는 데이터 클래스입니다.
 * @property id 생산 규칙의 고유 ID.
 * @property left 생산 규칙의 좌변 (논터미널 심볼).
 * @property right 생산 규칙의 우변 (심볼 시퀀스).
 */
data class Production(
    val id: Int,
    val left: TokenType,
    val right: List<TokenType>,
    val astBuilder: ASTBuilder = ASTBuilder.Identity
) {
    val length = right.size // 생산 규칙 우변의 길이

    // 생산 규칙을 문자열로 표현합니다 (예: EXPR → EXPR + TERM)
    override fun toString() = "$left → ${right.joinToString(" ")}"
}

sealed class ASTBuilder {
    abstract fun build(children: List<Any>): Any
    
    object Identity : ASTBuilder() {
        override fun build(children: List<Any>) = children[0] as ASTNode
    }
    
    object Start : ASTBuilder() {
        override fun build(children: List<Any>) = children[0] as ASTNode
    }
    
    class BinaryOp(private val operator: String, private val leftIndex: Int = 0, private val rightIndex: Int = 2) : ASTBuilder() {
        override fun build(children: List<Any>) = BinaryOpNode(children[leftIndex] as ASTNode, operator, children[rightIndex] as ASTNode)
    }
    
    class UnaryOp(private val operator: String, private val operandIndex: Int = 1) : ASTBuilder() {
        override fun build(children: List<Any>) = UnaryOpNode(operator, children[operandIndex] as ASTNode)
    }
    
    object Number : ASTBuilder() {
        override fun build(children: List<Any>) = NumberNode((children[0] as Token).value.toDouble())
    }
    
    object Variable : ASTBuilder() {
        override fun build(children: List<Any>) = VariableNode((children[0] as Token).value)
    }
    
    object BooleanTrue : ASTBuilder() {
        override fun build(children: List<Any>) = BooleanNode(true)
    }
    
    object BooleanFalse : ASTBuilder() {
        override fun build(children: List<Any>) = BooleanNode(false)
    }
    
    object Parenthesized : ASTBuilder() {
        override fun build(children: List<Any>) = children[1] as ASTNode
    }
    
    object FunctionCall : ASTBuilder() {
        override fun build(children: List<Any>) = FunctionCallNode((children[0] as Token).value, children[2] as List<ASTNode>)
    }
    
    object FunctionCallEmpty : ASTBuilder() {
        override fun build(children: List<Any>) = FunctionCallNode((children[0] as Token).value, emptyList())
    }
    
    object If : ASTBuilder() {
        override fun build(children: List<Any>) = IfNode(children[2] as ASTNode, children[4] as ASTNode, children[6] as ASTNode)
    }
    
    object ArgsSingle : ASTBuilder() {
        override fun build(children: List<Any>) = listOf(children[0] as ASTNode)
    }
    
    object ArgsMultiple : ASTBuilder() {
        override fun build(children: List<Any>) = (children[0] as List<ASTNode>) + (children[2] as ASTNode)
    }
}

// LR(1) 아이템
/**
 * LR(1) 파서의 아이템을 나타내는 데이터 클래스입니다.
 * 아이템은 생산 규칙, 점(•)의 위치, 그리고 선행 심볼(lookahead)로 구성됩니다.
 * @property production 아이템이 기반하는 생산 규칙.
 * @property dotPos 생산 규칙 우변에서 점(•)의 위치.
 * @property lookahead 선행 심볼.
 */
data class LRItem(
    val production: Production,
    val dotPos: Int,
    val lookahead: TokenType,
) {
    /**
     * 점(•)을 한 칸 앞으로 이동시킨 새로운 LRItem을 반환합니다.
     * @return 점이 이동된 새로운 LRItem.
     */
    fun advance() = copy(dotPos = dotPos + 1)

    /**
     * 아이템이 완료되었는지 (점(•)이 생산 규칙 우변의 끝에 있는지) 확인합니다.
     * @return 아이템이 완료되었으면 true, 아니면 false.
     */
    fun isComplete() = dotPos >= production.right.size

    /**
     * 점(•) 바로 다음에 오는 심볼을 반환합니다.
     * @return 점 다음에 오는 심볼 또는 null (점이 끝에 있는 경우).
     */
    fun nextSymbol() = if (dotPos < production.right.size) production.right[dotPos] else null

    /**
     * 점(•) 다음 심볼부터 생산 규칙 우변의 끝까지의 심볼 시퀀스를 반환합니다.
     * @return 심볼 시퀀스.
     */
    fun beta() = production.right.drop(dotPos + 1)

    /**
     * LRItem을 사람이 읽기 쉬운 문자열 형태로 표현합니다.
     * 예: [EXPR → •TERM, $]
     */
    override fun toString(): String {
        val right = production.right.toMutableList()
        right.add(dotPos, TokenType.DOLLAR) // 임시로 점 표시를 위해 DOLLAR 심볼 사용
        val rightStr =
            right.mapIndexed { i, sym ->
                if (i == dotPos) "•" else sym.toString()
            }.filter { it != "DOLLAR" }.joinToString(" ")
        return "[${production.left} → $rightStr, $lookahead]"
    }
}

// FIRST/FOLLOW 계산기
/**
 * 문법의 FIRST 및 FOLLOW 집합을 계산하는 유틸리티 객체입니다.
 * LR(1) 파서 테이블 구축에 필수적인 정보를 제공합니다.
 */
object FirstFollowSets {
    private val firstSets = mutableMapOf<TokenType, MutableSet<TokenType>>() // FIRST 집합을 저장할 맵
    private val followSets = mutableMapOf<TokenType, MutableSet<TokenType>>() // FOLLOW 집합을 저장할 맵
    private val logger = LoggerFactory.getLogger(this::class.java) // 로거 인스턴스 생성

    init {
        logger.info("FirstFollowSets 초기화 시작.") // 초기화 시작 로그
        calculateFirstSets() // FIRST 집합 계산
        calculateFollowSets() // FOLLOW 집합 계산
        logger.info("FirstFollowSets 초기화 완료.") // 초기화 완료 로그
    }

    /**
     * 문법의 모든 심볼에 대한 FIRST 집합을 계산합니다.
     * FIRST(X)는 X에서 파생될 수 있는 모든 터미널 심볼의 집합입니다.
     */
    private fun calculateFirstSets() {
        logger.debug("calculateFirstSets 시작.") // FIRST 집합 계산 시작 로그
        // 모든 터미널 심볼의 FIRST 집합은 자기 자신입니다.
        Grammar.terminals.forEach { terminal ->
            firstSets[terminal] = mutableSetOf(terminal)
            logger.trace("FIRST({}) = {{{}}}", terminal, terminal) // 터미널 FIRST 집합 로그
        }
        // 모든 논터미널 심볼의 FIRST 집합은 초기에는 비어 있습니다.
        Grammar.nonTerminals.forEach { firstSets[it] = mutableSetOf() }

        var changed = true
        while (changed) {
            changed = false
            for (prod in Grammar.productions) {
                logger.trace("생산 규칙 {} 처리 중.", prod) // 생산 규칙 처리 로그
                val before = firstSets[prod.left]!!.size // 변경 전 FIRST 집합 크기
                val firstOfRight = firstOfSequence(prod.right) // 생산 규칙 우변의 FIRST 집합 계산
                firstSets[prod.left]!!.addAll(firstOfRight) // FIRST 집합에 추가
                if (firstSets[prod.left]!!.size > before) {
                    changed = true // 변경이 발생했으면 플래그 설정
                    logger.trace("FIRST({}) 변경됨. 현재: {}", prod.left, firstSets[prod.left]) // FIRST 집합 변경 로그
                }
            }
        }
        logger.debug("calculateFirstSets 완료.") // FIRST 집합 계산 완료 로그
    }

    /**
     * 문법의 모든 논터미널 심볼에 대한 FOLLOW 집합을 계산합니다.
     * FOLLOW(A)는 문법의 어떤 문장 형식에서 A 바로 뒤에 나타날 수 있는 모든 터미널 심볼의 집합입니다.
     */
    private fun calculateFollowSets() {
        logger.debug("calculateFollowSets 시작.") // FOLLOW 집합 계산 시작 로그
        // 모든 논터미널 심볼의 FOLLOW 집합은 초기에는 비어 있습니다.
        Grammar.nonTerminals.forEach { followSets[it] = mutableSetOf() }
        // 시작 심볼의 FOLLOW 집합에는 EOF($)가 포함됩니다.
        followSets[Grammar.startSymbol]!!.add(TokenType.DOLLAR)
        logger.trace("FOLLOW({}) = {{{}}}", Grammar.startSymbol, TokenType.DOLLAR) // 시작 심볼 FOLLOW 집합 로그

        var changed = true
        while (changed) {
            changed = false
            for (prod in Grammar.productions) {
                logger.trace("생산 규칙 {} 처리 중.", prod) // 생산 규칙 처리 로그
                for (i in prod.right.indices) {
                    val symbol = prod.right[i] // 생산 규칙 우변의 현재 심볼
                    if (symbol in Grammar.nonTerminals) {
                        // 현재 심볼이 논터미널인 경우
                        val before = followSets[symbol]!!.size // 변경 전 FOLLOW 집합 크기
                        val beta = prod.right.drop(i + 1) // 현재 심볼 이후의 심볼들
                        val firstOfBeta = firstOfSequence(beta) // beta의 FIRST 집합 계산
                        followSets[symbol]!!.addAll(firstOfBeta - TokenType.DOLLAR) // FIRST(beta)를 FOLLOW(tokenSymbol)에 추가 (epsilon 제외)
                        logger.trace("FOLLOW({})에 FIRST({}) 추가. 현재: {}", symbol, beta, followSets[symbol]) // FOLLOW 집합 업데이트 로그

                        if (beta.isEmpty() || canDeriveEmpty(beta)) {
                            // beta가 비어있거나 epsilon을 파생할 수 있는 경우
                            followSets[symbol]!!.addAll(followSets[prod.left]!!) // FOLLOW(생산 규칙 좌변)를 FOLLOW(tokenSymbol)에 추가
                            logger.trace("FOLLOW({})에 FOLLOW({}) 추가. 현재: {}", symbol, prod.left, followSets[symbol]) // FOLLOW 집합 업데이트 로그
                        }

                        if (followSets[symbol]!!.size > before) {
                            changed = true // 변경이 발생했으면 플래그 설정
                            logger.trace("FOLLOW({}) 변경됨. 현재: {}", symbol, followSets[symbol]) // FOLLOW 집합 변경 로그
                        }
                    }
                }
            }
        }
        logger.debug("calculateFollowSets 완료.") // FOLLOW 집합 계산 완료 로그
    }

    /**
     * 심볼 시퀀스의 FIRST 집합을 계산합니다.
     * @param symbols FIRST 집합을 계산할 심볼 시퀀스.
     * @return 심볼 시퀀스의 FIRST 집합.
     */
    fun firstOfSequence(symbols: List<TokenType>): Set<TokenType> {
        logger.trace("firstOfSequence 호출됨. 심볼: {}", symbols) // firstOfSequence 호출 로그
        if (symbols.isEmpty()) {
            logger.trace("심볼 시퀀스가 비어있으므로 epsilon 반환.") // 빈 시퀀스 로그
            return setOf() // 시퀀스가 비어있으면 epsilon (빈 집합) 반환
        }

        val result = mutableSetOf<TokenType>()
        var derivesEmpty = true

        for (symbol in symbols) {
            val firstOfSymbol = firstSets[symbol] ?: setOf() // 각 심볼의 FIRST 집합
            result.addAll(firstOfSymbol - TokenType.DOLLAR) // epsilon을 제외하고 결과에 추가
            if (TokenType.DOLLAR !in firstOfSymbol) {
                derivesEmpty = false // epsilon을 파생할 수 없으면 플래그 해제
                break
            }
        }

        if (derivesEmpty) {
            result.add(TokenType.DOLLAR) // 모든 심볼이 epsilon을 파생할 수 있으면 결과에 epsilon (EOF) 추가
            logger.trace("모든 심볼이 epsilon을 파생할 수 있으므로 EOF($) 추가.") // epsilon 파생 가능 로그
        }
        logger.trace("firstOfSequence 결과: {}", result) // firstOfSequence 결과 로그
        return result
    }

    /**
     * 심볼 시퀀스가 epsilon을 파생할 수 있는지 확인합니다.
     * @param symbols 확인할 심볼 시퀀스.
     * @return epsilon을 파생할 수 있으면 true, 아니면 false.
     */
    private fun canDeriveEmpty(symbols: List<TokenType>): Boolean {
        logger.trace("canDeriveEmpty 호출됨. 심볼: {}", symbols) // canDeriveEmpty 호출 로그
        val result = symbols.all {
            TokenType.DOLLAR in (firstSets[it] ?: emptySet()) // 모든 심볼이 epsilon을 포함하는지 확인
        }
        logger.trace("canDeriveEmpty 결과: {}", result) // canDeriveEmpty 결과 로그
        return result
    }

    /**
     * 주어진 심볼의 FIRST 집합을 반환합니다.
     * @param symbol FIRST 집합을 조회할 심볼.
     * @return 해당 심볼의 FIRST 집합.
     */
    fun getFirst(symbol: TokenType) = firstSets[symbol] ?: emptySet()

    /**
     * 주어진 심볼의 FOLLOW 집합을 반환합니다.
     * @param symbol FOLLOW 집합을 조회할 심볼.
     * @return 해당 심볼의 FOLLOW 집합.
     */
    fun getFollow(symbol: TokenType) = followSets[symbol] ?: emptySet()
}

// LR 파싱 액션
/**
 * LR 파서가 수행할 수 있는 액션을 정의하는 sealed 클래스입니다.
 * Shift, Reduce, Accept, Error 네 가지 타입이 있습니다.
 */
sealed class LRAction {
    /**
     * Shift 액션: 입력 토큰을 스택에 푸시하고 다음 상태로 전이합니다.
     * @property state 전이할 다음 상태의 ID.
     */
    data class Shift(val state: Int) : LRAction() {
        override fun toString() = "Shift($state)"
    }

    /**
     * Reduce 액션: 스택에서 심볼을 팝하고 생산 규칙을 적용하여 새로운 논터미널 심볼을 푸시합니다.
     * @property production 적용할 생산 규칙.
     */
    data class Reduce(val production: Production) : LRAction() {
        override fun toString() = "Reduce(${production.id}: $production)"
    }

    /**
     * Accept 액션: 파싱이 성공적으로 완료되었음을 나타냅니다.
     */
    object Accept : LRAction() {
        override fun toString() = "Accept"
    }

    /**
     * Error 액션: 파싱 중 오류가 발생했음을 나타냅니다.
     */
    object Error : LRAction() {
        override fun toString() = "Error"
    }
}

/**
 * 압축된 LR 상태를 나타내는 데이터 클래스입니다.
 * 메모리 사용량을 줄이기 위해 상태의 핵심 정보만 저장합니다.
 */
data class CompressedLRState(
    val coreItems: Set<LRItem>, // 핵심 아이템들 (lookahead 제외)
    val isBuilt: Boolean = false // 완전히 구축되었는지 여부
) {
    fun toCoreSignature(): String = coreItems.joinToString("|") { "${it.production.id}:${it.dotPos}" }
}

// 완전한 LR 파싱 테이블 (메모리 및 성능 최적화)
object LRParserTable {
    private val states = mutableListOf<Set<LRItem>>() // LR(1) 상태 집합
    private val compressedStates = mutableMapOf<String, Int>() // 압축된 상태 시그니처 -> 상태 ID 매핑
    private val conflicts = mutableListOf<String>() // 파싱 충돌 목록
    private val logger = LoggerFactory.getLogger(this::class.java) // 로거 인스턴스 생성
    
    // Lazy initialization을 위한 플래그들
    private var isInitialized = false
    private val stateCache = mutableMapOf<Set<LRItem>, Int>() // 상태 캐시
    
    // 2D 배열 최적화된 테이블들
    private lateinit var actionTable2D: Array<Array<LRAction?>> // 2D 액션 테이블 [상태][토큰]
    private lateinit var gotoTable2D: Array<IntArray> // 2D GOTO 테이블 [상태][논터미널]
    private val terminalToIndex = mutableMapOf<TokenType, Int>() // 터미널 -> 인덱스 매핑
    private val nonTerminalToIndex = mutableMapOf<TokenType, Int>() // 논터미널 -> 인덱스 매핑
    
    // 백업용 맵 테이블 (초기화 중에만 사용)
    private val actionTable = mutableMapOf<Pair<Int, TokenType>, LRAction>() // 액션 테이블
    private val gotoTable = mutableMapOf<Pair<Int, TokenType>, Int>() // GOTO 테이블

    /**
     * Lazy initialization을 수행합니다.
     * 첫 번째 파싱 요청 시에만 테이블을 구축합니다.
     */
    private fun ensureInitialized() {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    logger.info("LRParserTable lazy 초기화 시작.") // 테이블 초기화 시작 로그
                    initializeTokenMappings() // 토큰 매핑 초기화
                    buildStates() // LR(1) 상태 구축
                    buildTables() // 액션 및 GOTO 테이블 구축
                    build2DTables() // 2D 배열 테이블 구축
                    isInitialized = true
                    logger.info("LRParserTable 초기화 완료. 총 {}개의 상태가 생성되었습니다.", states.size) // 테이블 초기화 완료 로그

                    // 디버깅: 상태 0의 내용을 출력
                    logger.info("상태 0의 아이템들:")
                    states[0].forEach { item ->
                        logger.info("  {}", item) // 상태 0의 각 아이템 로그
                    }
                    if (conflicts.isNotEmpty()) {
                        logger.warn("LR 파싱 테이블에 {}개의 충돌이 발견되었습니다.", conflicts.size) // 충돌 발견 시 경고 로그
                        conflicts.forEach { logger.warn("  충돌: {}", it) } // 각 충돌 로그
                    }
                }
            }
        }
    }

    /**
     * 토큰 타입과 인덱스 간의 매핑을 초기화합니다.
     */
    private fun initializeTokenMappings() {
        logger.debug("토큰 매핑 초기화 시작")
        
        // 터미널 심볼 매핑
        Grammar.terminals.forEachIndexed { index, terminal ->
            terminalToIndex[terminal] = index
        }
        
        // 논터미널 심볼 매핑
        Grammar.nonTerminals.forEachIndexed { index, nonTerminal ->
            nonTerminalToIndex[nonTerminal] = index
        }
        
        logger.debug("토큰 매핑 완료. 터미널: {}, 논터미널: {}", 
            terminalToIndex.size, nonTerminalToIndex.size)
    }

    /**
     * 맵 기반 테이블에서 2D 배열 테이블로 변환합니다.
     */
    private fun build2DTables() {
        logger.debug("2D 테이블 구축 시작")
        
        val numStates = states.size
        val numTerminals = Grammar.terminals.size
        val numNonTerminals = Grammar.nonTerminals.size
        
        // 액션 테이블 2D 배열 초기화
        actionTable2D = Array(numStates) { arrayOfNulls<LRAction>(numTerminals) }
        
        // GOTO 테이블 2D 배열 초기화 (-1은 엔트리 없음을 의미)
        gotoTable2D = Array(numStates) { IntArray(numNonTerminals) { -1 } }
        
        // 맵에서 2D 배열로 데이터 복사
        for ((key, action) in actionTable) {
            val (stateId, terminal) = key
            val terminalIndex = terminalToIndex[terminal]
            if (terminalIndex != null && stateId < numStates) {
                actionTable2D[stateId][terminalIndex] = action
            }
        }
        
        for ((key, nextState) in gotoTable) {
            val (stateId, nonTerminal) = key
            val nonTerminalIndex = nonTerminalToIndex[nonTerminal]
            if (nonTerminalIndex != null && stateId < numStates) {
                gotoTable2D[stateId][nonTerminalIndex] = nextState
            }
        }
        
        logger.info("2D 테이블 구축 완료. 액션 테이블: {}x{}, GOTO 테이블: {}x{}", 
            numStates, numTerminals, numStates, numNonTerminals)
    }

    /**
     * LR(1) 상태 집합을 구축합니다.
     * 시작 아이템에서 클로저를 계산하고, 각 상태에서 가능한 모든 전이를 탐색하여 새로운 상태를 생성합니다.
     */
    private fun buildStates() {
        logger.debug("buildStates 시작.") // buildStates 시작 로그
        val startItem = LRItem(Grammar.augmentedProduction, 0, TokenType.DOLLAR) // 시작 아이템
        val startState = closure(setOf(startItem)) // 시작 상태의 클로저 계산
        states.add(startState) // 상태 목록에 추가

        val workList = mutableListOf(0) // 처리할 상태 ID 목록
        val stateMap = mutableMapOf<Set<LRItem>, Int>() // 상태 집합과 ID 매핑
        stateMap[startState] = 0

        while (workList.isNotEmpty()) {
            val stateId = workList.removeFirst() // 작업 목록에서 상태 ID 가져오기
            val state = states[stateId] // 해당 상태 가져오기
            logger.debug("상태 {} 처리 중. 아이템 수: {}", stateId, state.size) // 현재 상태 처리 로그

            val transitions = mutableMapOf<TokenType, MutableSet<LRItem>>() // 전이 맵

            for (item in state) {
                val nextSym = item.nextSymbol() // 다음 심볼 가져오기
                if (nextSym != null) {
                    // 다음 심볼이 있는 아이템들을 그룹화하여 전이 생성
                    transitions.computeIfAbsent(nextSym) { mutableSetOf() }.add(item.advance())
                    logger.trace("상태 {} 아이템 {}에서 심볼 {}로 전이 가능.", stateId, item, nextSym) // 전이 가능 로그
                }
            }

            for ((symbol, itemSet) in transitions) {
                val newState = closure(itemSet) // 새로운 상태의 클로저 계산
                
                // 캐시된 상태 확인
                val cachedStateId = stateCache[newState]
                val existingStateId = cachedStateId ?: stateMap[newState] // 이미 존재하는 상태인지 확인

                val targetStateId =
                    if (existingStateId != null) {
                        logger.debug("상태 {}에서 심볼 {}로 전이: 기존 상태 {} 재사용.", stateId, symbol, existingStateId) // 기존 상태 재사용 로그
                        existingStateId
                    } else {
                        val newStateId = states.size // 새로운 상태 ID 할당
                        states.add(newState) // 새로운 상태 추가
                        stateMap[newState] = newStateId // 맵에 추가
                        stateCache[newState] = newStateId // 캐시에 추가
                        workList.add(newStateId) // 작업 목록에 추가
                        
                        // 압축된 상태 시그니처 생성
                        val coreSignature = generateCoreSignature(newState)
                        
                        // LALR 병합 시도
                        val existingCoreStateId = compressedStates[coreSignature]
                        if (existingCoreStateId != null && canMergeLALRStates(states[existingCoreStateId], newState)) {
                            // 동일한 core를 가진 상태와 병합
                            val mergedState = mergeLALRStates(states[existingCoreStateId], newState)
                            states[existingCoreStateId] = mergedState
                            stateMap.remove(newState) // 새 상태는 병합되었으므로 제거
                            stateMap[mergedState] = existingCoreStateId
                            stateCache[mergedState] = existingCoreStateId
                            states.removeAt(states.size - 1) // 추가했던 새 상태 제거
                            
                            logger.debug("LALR 상태 병합: 상태 {}와 병합됨. 시그니처: {}", 
                                existingCoreStateId, coreSignature)
                            existingCoreStateId
                        } else {
                            compressedStates[coreSignature] = newStateId
                            logger.debug("상태 {}에서 심볼 {}로 전이: 새로운 상태 {} 생성. 시그니처: {}", 
                                stateId, symbol, newStateId, coreSignature)
                            newStateId
                        }
                    }

                if (symbol in Grammar.terminals) {
                    // 터미널 심볼이면 Shift 액션
                    actionTable[Pair(stateId, symbol)] = LRAction.Shift(targetStateId)
                    logger.trace("액션 테이블 추가: (상태 {}, 토큰 {}) -> Shift {}", stateId, symbol, targetStateId) // Shift 액션 추가 로그
                } else {
                    // 논터미널 심볼이면 GOTO 액션
                    gotoTable[Pair(stateId, symbol)] = targetStateId
                    logger.trace("GOTO 테이블 추가: (상태 {}, 논터미널 {}) -> {}", stateId, symbol, targetStateId) // GOTO 액션 추가 로그
                }
            }
        }
        logger.debug("buildStates 완료. 총 {}개의 상태 생성.", states.size) // buildStates 완료 로그
    }

    /**
     * 상태의 핵심 시그니처를 생성합니다.
     * lookahead를 제외한 core 정보만 사용하여 메모리 효율적인 식별자를 만듭니다.
     */
    private fun generateCoreSignature(state: Set<LRItem>): String {
        return state
            .map { "${it.production.id}:${it.dotPos}" }
            .sorted()
            .joinToString("|")
    }

    /**
     * 두 상태가 LALR 병합 가능한지 확인합니다.
     * 동일한 core를 가지고 충돌이 발생하지 않으면 병합 가능합니다.
     */
    private fun canMergeLALRStates(state1: Set<LRItem>, state2: Set<LRItem>): Boolean {
        // Core 아이템들 (lookahead 제외) 비교
        val core1 = state1.map { LRItem(it.production, it.dotPos, TokenType.DOLLAR) }.toSet()
        val core2 = state2.map { LRItem(it.production, it.dotPos, TokenType.DOLLAR) }.toSet()
        
        if (core1 != core2) {
            return false
        }
        
        // 동일한 core를 가진 아이템들의 lookahead 집합이 겹치지 않는지 확인
        val lookaheadMap1 = state1.groupBy { "${it.production.id}:${it.dotPos}" }
            .mapValues { it.value.map { item -> item.lookahead }.toSet() }
        val lookaheadMap2 = state2.groupBy { "${it.production.id}:${it.dotPos}" }
            .mapValues { it.value.map { item -> item.lookahead }.toSet() }
        
        // 각 core 아이템에 대해 lookahead 집합이 겹치지 않는지 확인
        for (coreKey in lookaheadMap1.keys) {
            val lookaheads1 = lookaheadMap1[coreKey] ?: emptySet()
            val lookaheads2 = lookaheadMap2[coreKey] ?: emptySet()
            if (lookaheads1.intersect(lookaheads2).isNotEmpty()) {
                // lookahead가 겹치면 병합 불가능
                return false
            }
        }
        
        return true
    }

    /**
     * 두 LALR 상태를 병합합니다.
     * 동일한 core를 가진 아이템들의 lookahead를 합집합으로 만듭니다.
     */
    private fun mergeLALRStates(state1: Set<LRItem>, state2: Set<LRItem>): Set<LRItem> {
        val mergedItems = mutableSetOf<LRItem>()
        
        // 모든 아이템들을 core 기준으로 그룹화
        val allItems = (state1 + state2).groupBy { "${it.production.id}:${it.dotPos}" }
        
        for ((coreKey, items) in allItems) {
            // 동일한 core를 가진 아이템들의 lookahead를 모두 수집
            val production = items.first().production
            val dotPos = items.first().dotPos
            val allLookaheads = items.map { it.lookahead }.toSet()
            
            // 각 lookahead에 대해 별도의 아이템 생성
            for (lookahead in allLookaheads) {
                mergedItems.add(LRItem(production, dotPos, lookahead))
            }
        }
        
        return mergedItems
    }

    /**
     * LR(1) 아이템 집합의 클로저를 계산합니다.
     * @param items 초기 LR(1) 아이템 집합.
     * @return 클로저 계산이 완료된 LR(1) 아이템 집합.
     */
    private fun closure(items: Set<LRItem>): Set<LRItem> {
        logger.trace("closure 시작. 초기 아이템 수: {}", items.size) // closure 시작 로그
        val result = items.toMutableSet() // 결과 집합 (가변)
        val workList = items.toMutableList() // 작업 목록 (가변)

        while (workList.isNotEmpty()) {
            val item = workList.removeFirst() // 작업 목록에서 아이템 가져오기
            val nextSym = item.nextSymbol() // 점(•) 다음의 심볼 가져오기

            if (nextSym != null && nextSym in Grammar.nonTerminals) {
                // 점 다음이 논터미널인 경우
                val beta = item.beta() // 점 다음 심볼 이후의 심볼들
                // FIRST(beta · lookahead) 계산 (올바른 순서)
                val firstOfBetaLookahead = FirstFollowSets.firstOfSequence(beta + listOf(item.lookahead))
                logger.trace("아이템 {}의 다음 심볼: {}, FIRST(beta + lookahead): {}", item, nextSym, firstOfBetaLookahead) // 클로저 계산 중간 로그

                for (prod in Grammar.productions.filter { it.left == nextSym }) {
                    // 해당 논터미널을 좌변으로 갖는 모든 생산 규칙에 대해
                    for (lookahead in firstOfBetaLookahead) {
                        val newItem = LRItem(prod, 0, lookahead) // 새로운 LR(1) 아이템 생성
                        if (newItem !in result) {
                            result.add(newItem) // 결과 집합에 추가
                            workList.add(newItem) // 작업 목록에 추가
                            logger.trace("새로운 아이템 클로저에 추가: {}", newItem) // 새로운 아이템 추가 로그
                        }
                    }
                }
            }
        }
        logger.trace("closure 완료. 최종 아이템 수: {}", result.size) // closure 완료 로그
        return result
    }

    /**
     * 액션 및 GOTO 테이블을 구축합니다.
     * 모든 상태와 아이템을 순회하며 Shift, Reduce, Accept 액션을 결정합니다.
     * 충돌이 발생하면 conflicts 목록에 추가합니다.
     */
    private fun buildTables() {
        logger.debug("buildTables 시작.") // buildTables 시작 로그
        for ((stateId, state) in states.withIndex()) {
            logger.debug("상태 {}에 대한 테이블 엔트리 구축 중.", stateId) // 각 상태에 대한 테이블 구축 로그
            for (item in state) {
                if (item.isComplete()) {
                    // 아이템이 완료된 경우 (점(•)이 맨 끝에 있는 경우)
                    if (item.production.left == TokenType.START && item.lookahead == TokenType.DOLLAR) {
                        // 시작 심볼이 완료되고 lookahead가 EOF인 경우 Accept 액션
                        val existing = actionTable[Pair(stateId, TokenType.DOLLAR)]
                        if (existing != null && existing != LRAction.Accept) {
                            conflicts.add("Accept/Reduce 충돌 in state $stateId on ${TokenType.DOLLAR}: $existing vs Accept")
                            logger.warn("Accept/Reduce 충돌 발생: 상태 {}, 토큰 {}. 기존: {}, 새: Accept", stateId, TokenType.DOLLAR, existing) // Accept/Reduce 충돌 로그
                        }
                        actionTable[Pair(stateId, TokenType.DOLLAR)] = LRAction.Accept
                        logger.info("Accept 액션 설정됨: 상태 {}, 토큰 DOLLAR", stateId) // Accept 액션 추가 로그
                    } else {
                        // 그 외 완료된 아이템은 Reduce 액션
                        val existing = actionTable[Pair(stateId, item.lookahead)]
                        if (existing != null && existing != LRAction.Reduce(item.production)) {
                            // Shift/Reduce 또는 Reduce/Reduce 충돌 감지 및 해결
                            val resolved = resolveConflict(existing, LRAction.Reduce(item.production), item.lookahead, stateId)
                            if (resolved != null) {
                                actionTable[Pair(stateId, item.lookahead)] = resolved
                                logger.info("충돌 해결됨: 상태 {}, 토큰 {}. 기존: {}, 새: Reduce({}), 해결: {}", 
                                    stateId, item.lookahead, existing, item.production, resolved)
                            } else {
                                // 해결할 수 없는 충돌
                                conflicts.add("Unresolvable conflict in state $stateId on ${item.lookahead}: $existing vs Reduce(${item.production})")
                                logger.warn("해결할 수 없는 충돌: 상태 {}, 토큰 {}. 기존: {}, 새: Reduce({})", 
                                    stateId, item.lookahead, existing, item.production)
                            }
                        } else {
                            actionTable[Pair(stateId, item.lookahead)] = LRAction.Reduce(item.production)
                            logger.trace("액션 테이블 추가: (상태 {}, 토큰 {}) -> Reduce {}", stateId, item.lookahead, item.production)
                        }
                    }
                }
            }
        }
        logger.debug("buildTables 완료.") // buildTables 완료 로그
    }

    /**
     * Shift/Reduce 및 Reduce/Reduce 충돌을 연산자 우선순위 규칙으로 해결합니다.
     * @param existing 기존 액션
     * @param newAction 새로운 액션
     * @param lookahead 충돌이 발생한 토큰
     * @param stateId 충돌이 발생한 상태
     * @return 해결된 액션 또는 null (해결 불가능한 경우)
     */
    private fun resolveConflict(
        existing: LRAction,
        newAction: LRAction,
        lookahead: TokenType,
        stateId: Int
    ): LRAction? {
        logger.debug("충돌 해결 시도: 상태 {}, 토큰 {}, 기존: {}, 새: {}", stateId, lookahead, existing, newAction)
        
        when {
            existing is LRAction.Shift && newAction is LRAction.Reduce -> {
                // Shift/Reduce 충돌
                return resolveShiftReduceConflict(existing, newAction, lookahead)
            }
            existing is LRAction.Reduce && newAction is LRAction.Reduce -> {
                // Reduce/Reduce 충돌
                return resolveReduceReduceConflict(existing, newAction)
            }
            else -> {
                logger.warn("지원하지 않는 충돌 유형: {} vs {}", existing, newAction)
                return null
            }
        }
    }

    /**
     * Shift/Reduce 충돌을 해결합니다.
     * 연산자 우선순위와 결합성을 기반으로 결정합니다.
     */
    private fun resolveShiftReduceConflict(
        shiftAction: LRAction.Shift,
        reduceAction: LRAction.Reduce,
        lookahead: TokenType
    ): LRAction? {
        val lookaheadPrec = OperatorPrecedenceTable.getPrecedence(lookahead)
        val productionPrec = getProductionPrecedence(reduceAction.production)
        
        logger.debug("Shift/Reduce 충돌 해결: lookahead={}, precedence={}, production={}, precedence={}",
            lookahead, lookaheadPrec, reduceAction.production, productionPrec)
        
        if (lookaheadPrec == null || productionPrec == null) {
            // 우선순위 정보가 없으면 기본적으로 Shift 선택 (LR 파서의 기본 동작)
            logger.debug("우선순위 정보 없음, Shift 선택")
            return shiftAction
        }
        
        return when {
            lookaheadPrec.precedence > productionPrec.precedence -> {
                logger.debug("Lookahead 우선순위가 높음, Shift 선택")
                shiftAction
            }
            lookaheadPrec.precedence < productionPrec.precedence -> {
                logger.debug("Production 우선순위가 높음, Reduce 선택")
                reduceAction
            }
            lookaheadPrec.precedence == productionPrec.precedence -> {
                // 같은 우선순위인 경우 결합성으로 결정
                when (lookaheadPrec.associativity) {
                    Associativity.LEFT -> {
                        logger.debug("좌결합, Reduce 선택")
                        reduceAction
                    }
                    Associativity.RIGHT -> {
                        logger.debug("우결합, Shift 선택")
                        shiftAction
                    }
                    Associativity.NONE -> {
                        logger.warn("비결합 연산자 충돌, 해결 불가능")
                        null
                    }
                }
            }
            else -> null
        }
    }

    /**
     * Reduce/Reduce 충돌을 해결합니다.
     * 일반적으로 더 긴 생산 규칙을 선택하거나, 문법에서 먼저 정의된 것을 선택합니다.
     */
    private fun resolveReduceReduceConflict(
        existingReduce: LRAction.Reduce,
        newReduce: LRAction.Reduce
    ): LRAction? {
        logger.debug("Reduce/Reduce 충돌 해결: 기존={}, 새={}",
            existingReduce.production, newReduce.production)
        
        // 더 긴 생산 규칙을 선택 (더 구체적인 규칙)
        return if (existingReduce.production.length >= newReduce.production.length) {
            logger.debug("기존 생산 규칙이 더 길거나 같음, 기존 선택")
            existingReduce
        } else {
            logger.debug("새 생산 규칙이 더 김, 새 규칙 선택")
            newReduce
        }
    }

    /**
     * 생산 규칙의 우선순위를 결정합니다.
     * 생산 규칙의 가장 오른쪽 터미널 심볼의 우선순위를 사용합니다.
     */
    private fun getProductionPrecedence(production: Production): OperatorPrecedence? {
        // 생산 규칙의 우변에서 가장 오른쪽 터미널 심볼을 찾습니다
        for (i in production.right.indices.reversed()) {
            val symbol = production.right[i]
            if (symbol in Grammar.terminals) {
                val precedence = OperatorPrecedenceTable.getPrecedence(symbol)
                if (precedence != null) {
                    return precedence
                }
            }
        }
        return null
    }

    /**
     * 주어진 상태와 터미널 심볼에 대한 파싱 액션을 반환합니다.
     * @param state 현재 상태 ID.
     * @param terminal 입력 터미널 심볼.
     * @return 해당 액션 (Shift, Reduce, Accept, Error).
     */
    fun getAction(
        state: Int,
        terminal: TokenType,
    ): LRAction {
        ensureInitialized() // Lazy initialization 보장
        logger.debug("getAction 호출됨. 상태: {}, 터미널: {}", state, terminal) // getAction 호출 로그
        
        val terminalIndex = terminalToIndex[terminal]
        if (terminalIndex == null || state >= actionTable2D.size || state < 0) {
            logger.error("잘못된 상태 또는 터미널: 상태={}, 터미널={}", state, terminal)
            return LRAction.Error
        }
        
        val action = actionTable2D[state][terminalIndex]
        if (terminal == TokenType.DOLLAR) {
            logger.info("DOLLAR 토큰 액션 조회: 상태 {}, 액션: {}", state, action)
        }
        
        if (action == null) {
            logger.error("상태 {}에서 토큰 {}에 대한 액션이 없습니다. 파싱 오류 발생.", state, terminal) // 액션 없음 에러 로그
            logger.error("사용 가능한 액션들 (상태 {}):", state)
            for ((termType, index) in terminalToIndex) {
                val availableAction = actionTable2D[state][index]
                if (availableAction != null) {
                    logger.error("  {} -> {}", termType, availableAction) // 사용 가능한 액션 로그
                }
            }
        }
        
        logger.debug("getAction 결과: {}", action ?: LRAction.Error) // getAction 결과 로그
        return action ?: LRAction.Error
    }

    /**
     * 주어진 상태와 논터미널 심볼에 대한 GOTO 상태를 반환합니다.
     * @param state 현재 상태 ID.
     * @param nonTerminal 논터미널 심볼.
     * @return 다음 상태 ID 또는 null (해당하는 GOTO 엔트리가 없는 경우).
     */
    fun getGoto(
        state: Int,
        nonTerminal: TokenType,
    ): Int? {
        ensureInitialized() // Lazy initialization 보장
        logger.debug("getGoto 호출됨. 상태: {}, 논터미널: {}", state, nonTerminal) // getGoto 호출 로그
        
        val nonTerminalIndex = nonTerminalToIndex[nonTerminal]
        if (nonTerminalIndex == null || state >= gotoTable2D.size || state < 0) {
            logger.debug("잘못된 상태 또는 논터미널: 상태={}, 논터미널={}", state, nonTerminal)
            return null
        }
        
        val nextState = gotoTable2D[state][nonTerminalIndex]
        val result = if (nextState == -1) null else nextState
        logger.debug("getGoto 결과: {}", result) // getGoto 결과 로그
        return result
    }

    /**
     * 현재 파서 테이블의 상태 개수를 반환합니다.
     * @return 상태 개수.
     */
    fun getStateCount(): Int {
        ensureInitialized() // Lazy initialization 보장
        return states.size
    }

    /**
     * 파서 테이블 구축 중 발견된 충돌 목록을 반환합니다.
     * @return 충돌 메시지 목록.
     */
    fun getConflicts(): List<String> {
        ensureInitialized() // Lazy initialization 보장
        return conflicts
    }

    /**
     * 메모리 사용량 통계를 반환합니다.
     * @return 메모리 사용량 정보 맵
     */
    fun getMemoryStats(): Map<String, Any> {
        ensureInitialized()
        return mapOf(
            "totalStates" to states.size,
            "compressedStatesCount" to compressedStates.size,
            "actionTableSize" to if (::actionTable2D.isInitialized) {
                actionTable2D.size * actionTable2D[0].size
            } else actionTable.size,
            "gotoTableSize" to if (::gotoTable2D.isInitialized) {
                gotoTable2D.size * gotoTable2D[0].size
            } else gotoTable.size,
            "cacheHitRatio" to if (stateCache.isNotEmpty()) {
                stateCache.size.toDouble() / states.size
            } else 0.0,
            "using2DArrays" to ::actionTable2D.isInitialized
        )
    }
}

// 실제 LR 파서

// 실제 LR 파서
/**
 * 파싱 과정에서 스택에 저장되는 심볼의 타입 안전 표현입니다.
 * 토큰과 AST 노드를 구분하여 컴파일 타임 타입 검증을 제공합니다.
 */
sealed class ParseSymbol {
    data class TokenSymbol(val token: Token) : ParseSymbol()
    data class ASTSymbol(val node: ASTNode) : ParseSymbol()
    data class ArgumentsSymbol(val args: List<ASTNode>) : ParseSymbol()
}

/**
 * 렉서 인터페이스입니다.
 * 입력 문자열을 토큰 시퀀스로 변환합니다.
 */
interface Lexer {
    /**
     * 입력 문자열을 토큰화합니다.
     * @param input 토큰화할 입력 문자열
     * @return 토큰 목록
     * @throws CalculatorException 토큰화 중 오류 발생 시
     */
    fun tokenize(input: String): List<Token>
}

/**
 * 계산기 언어를 위한 렉서 구현체입니다.
 * 수학 표현식, 논리 연산자, 함수 호출 등을 토큰화합니다.
 */
@Component
class CalculatorLexer : Lexer {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun tokenize(input: String): List<Token> {
        logger.debug("tokenize 시작. 입력 문자열 길이: {}", input.length)
        val tokens = mutableListOf<Token>()
        var pos = 0

        while (pos < input.length) {
            logger.trace("현재 위치: {}, 남은 문자열: '{}'", pos, input.substring(pos))
            when {
                input[pos].isWhitespace() -> {
                    logger.trace("공백 문자 발견: '{}'", input[pos])
                    pos++
                }
                input[pos].isDigit() -> {
                    val start = pos
                    while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
                    val numberStr = input.substring(start, pos)
                    tokens.add(Token(TokenType.NUMBER, numberStr, start))
                    logger.debug("NUMBER 토큰 추가: '{}' (위치: {})", numberStr, start)
                }
                input[pos] == '{' -> {
                    val start = pos++
                    while (pos < input.length && input[pos] != '}') pos++
                    if (pos >= input.length) {
                        logger.error("닫히지 않은 변수 오류 발생. 시작 위치: {}", start)
                        throw CalculatorException("닫히지 않은 변수", "UNCLOSED_VARIABLE")
                    }
                    val varName = input.substring(start + 1, pos++)
                    tokens.add(Token(TokenType.VARIABLE, varName, start))
                    logger.debug("VARIABLE 토큰 추가: '{}' (위치: {})", varName, start)
                }
                input[pos].isLetter() -> {
                    val start = pos
                    while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
                    val id = input.substring(start, pos)
                    val type = when (id.uppercase()) {
                        "IF" -> TokenType.IF
                        "TRUE" -> TokenType.TRUE
                        "FALSE" -> TokenType.FALSE
                        "AND" -> TokenType.AND
                        "OR" -> TokenType.OR
                        "NOT" -> TokenType.NOT
                        else -> TokenType.IDENTIFIER
                    }
                    tokens.add(Token(type, id, start))
                    logger.debug("{} 토큰 추가: '{}' (위치: {})", type, id, start)
                }
                
                // 산술 연산자
                input[pos] == '+' -> {
                    tokens.add(Token(TokenType.PLUS, "+", pos++))
                    logger.debug("PLUS 토큰 추가")
                }
                input[pos] == '-' -> {
                    tokens.add(Token(TokenType.MINUS, "-", pos++))
                    logger.debug("MINUS 토큰 추가")
                }
                input[pos] == '*' -> {
                    tokens.add(Token(TokenType.MULTIPLY, "*", pos++))
                    logger.debug("MULTIPLY 토큰 추가")
                }
                input[pos] == '/' -> {
                    tokens.add(Token(TokenType.DIVIDE, "/", pos++))
                    logger.debug("DIVIDE 토큰 추가")
                }
                input[pos] == '^' -> {
                    tokens.add(Token(TokenType.POWER, "^", pos++))
                    logger.debug("POWER 토큰 추가")
                }
                input[pos] == '%' -> {
                    tokens.add(Token(TokenType.MODULO, "%", pos++))
                    logger.debug("MODULO 토큰 추가")
                }
                
                // 괄호 및 구분자
                input[pos] == '(' -> {
                    tokens.add(Token(TokenType.LEFT_PAREN, "(", pos++))
                    logger.debug("LEFT_PAREN 토큰 추가")
                }
                input[pos] == ')' -> {
                    tokens.add(Token(TokenType.RIGHT_PAREN, ")", pos++))
                    logger.debug("RIGHT_PAREN 토큰 추가")
                }
                input[pos] == ',' -> {
                    tokens.add(Token(TokenType.COMMA, ",", pos++))
                    logger.debug("COMMA 토큰 추가")
                }

                // 비교 연산자
                input[pos] == '=' && pos + 1 < input.length && input[pos + 1] == '=' -> {
                    tokens.add(Token(TokenType.EQUAL, "==", pos))
                    logger.debug("EQUAL 토큰 추가")
                    pos += 2
                }
                input[pos] == '!' && pos + 1 < input.length && input[pos + 1] == '=' -> {
                    tokens.add(Token(TokenType.NOT_EQUAL, "!=", pos))
                    logger.debug("NOT_EQUAL 토큰 추가")
                    pos += 2
                }
                input[pos] == '<' && pos + 1 < input.length && input[pos + 1] == '=' -> {
                    tokens.add(Token(TokenType.LESS_EQUAL, "<=", pos))
                    logger.debug("LESS_EQUAL 토큰 추가")
                    pos += 2
                }
                input[pos] == '>' && pos + 1 < input.length && input[pos + 1] == '=' -> {
                    tokens.add(Token(TokenType.GREATER_EQUAL, ">=", pos))
                    logger.debug("GREATER_EQUAL 토큰 추가")
                    pos += 2
                }
                input[pos] == '<' -> {
                    tokens.add(Token(TokenType.LESS, "<", pos++))
                    logger.debug("LESS 토큰 추가")
                }
                input[pos] == '>' -> {
                    tokens.add(Token(TokenType.GREATER, ">", pos++))
                    logger.debug("GREATER 토큰 추가")
                }

                // 논리 연산자
                input[pos] == '&' && pos + 1 < input.length && input[pos + 1] == '&' -> {
                    tokens.add(Token(TokenType.AND, "&&", pos))
                    logger.debug("AND 토큰 추가")
                    pos += 2
                }
                input[pos] == '|' && pos + 1 < input.length && input[pos + 1] == '|' -> {
                    tokens.add(Token(TokenType.OR, "||", pos))
                    logger.debug("OR 토큰 추가")
                    pos += 2
                }
                input[pos] == '!' -> {
                    tokens.add(Token(TokenType.NOT, "!", pos++))
                    logger.debug("NOT 토큰 추가")
                }

                else -> {
                    logger.error("예상치 못한 문자 발견: '{}' (위치: {})", input[pos], pos)
                    throw CalculatorException("예상치 못한 문자: '${input[pos]}'", "UNEXPECTED_CHARACTER")
                }
            }
        }
        logger.debug("tokenize 완료. 최종 토큰 수: {}", tokens.size)
        return tokens
    }
}

/**
 * LR(1) 파싱 알고리즘을 구현한 파서 클래스입니다.
 * 렉서로부터 토큰을 받아 파싱 테이블을 사용하여 AST(추상 구문 트리)를 구축합니다.
 */
@Component // Spring 컴포넌트로 등록하여 의존성 주입이 가능하게 합니다.
class RealLRParser(
    private val lexer: Lexer // 렉서를 의존성 주입으로 받습니다.
) {
    private val logger = LoggerFactory.getLogger(javaClass) // 로거 인스턴스 생성

    init {
        logger.info("RealLRParser 초기화 완료. Lexer: {}", lexer.javaClass.simpleName) // 파서 초기화 완료 로그
    }

    /**
     * 주어진 입력 문자열을 파싱하여 ASTNode를 반환합니다.
     * @param input 파싱할 입력 문자열.
     * @return 파싱 결과로 생성된 ASTNode.
     * @throws CalculatorException 토큰화 또는 파싱 중 오류 발생 시.
     */
    fun parse(input: String): ASTNode {
        logger.info("파싱 요청 수신. 입력: '{}'", input) // 파싱 요청 시작 로그
        val tokens = lexer.tokenize(input) + Token(TokenType.DOLLAR, "$") // 렉서를 통해 토큰화하고 EOF 토큰 추가
        logger.debug("토큰화 완료. 생성된 토큰: {}", tokens.map { "${it.type}(${it.value})" }) // 토큰화 결과 로그
        return lrParse(tokens) // LR 파싱 수행
    }


    /**
     * LR(1) 파싱을 수행하여 AST를 구축합니다.
     * @param tokens 파싱할 토큰 목록 (EOF 토큰 포함).
     * @return 파싱 결과로 생성된 ASTNode.
     * @throws CalculatorException 구문 오류 또는 GOTO 오류 발생 시.
     */
    private fun lrParse(tokens: List<Token>): ASTNode {
        logger.info("LR 파싱 시작. 입력 토큰 수: {}", tokens.size) // LR 파싱 시작 로그
        val stateStack = mutableListOf(0) // 상태 스택 초기화 (초기 상태 0)
        val symbolStack = mutableListOf<ParseSymbol>() // 심볼 스택 초기화 (타입 안전)
        var tokenIndex = 0 // 현재 처리할 토큰의 인덱스

        logger.debug("초기 스택 상태 - 상태: {}, 심볼: {}", stateStack, symbolStack) // 초기 스택 상태 로그

        while (true) {
            val currentState = stateStack.last() // 현재 상태 (스택의 최상단)

            // 토큰 인덱스 범위 확인
            if (tokenIndex >= tokens.size) {
                throw CalculatorException("예상치 못한 입력 종료", "UNEXPECTED_END_OF_INPUT")
            }

            val currentToken = tokens[tokenIndex] // 현재 입력 토큰

            logger.debug("현재 파싱 상태 - 상태: {}, 입력 토큰: {}", currentState, currentToken) // 현재 파싱 상태 로그

            when (val action = LRParserTable.getAction(currentState, currentToken.type)) {
                is LRAction.Shift -> { // Shift 액션
                    logger.info("Shift 액션 수행. 다음 상태: {}", action.state) // Shift 액션 로그
                    stateStack.add(action.state) // 다음 상태를 상태 스택에 푸시
                    symbolStack.add(ParseSymbol.TokenSymbol(currentToken)) // 현재 토큰을 심볼 스택에 푸시
                    tokenIndex++ // 다음 토큰으로 이동
                    logger.debug("Shift 후 스택 상태 - 상태: {}, 심볼: {}", stateStack, symbolStack) // Shift 후 스택 상태 로그

                    // Shift 후 Accept 상태 확인 (DOLLAR 토큰과 함께 state 18로 이동한 경우)
                    if (action.state == 18 && currentToken.type == TokenType.DOLLAR) {
                        logger.info("Accept 상태 감지됨. Accept 액션 수행.")
                        val finalResult = symbolStack[symbolStack.size - 2] // DOLLAR 토큰 이전의 심볼이 최종 결과
                        return when (finalResult) {
                            is ParseSymbol.ASTSymbol -> {
                                logger.info("최종 파싱 결과: {}", finalResult.node)
                                finalResult.node
                            }
                            else -> {
                                logger.error("파싱 결과가 AST 노드가 아닙니다: {}", finalResult.javaClass.simpleName)
                                throw CalculatorException("파싱 결과가 AST 노드가 아닙니다: ${finalResult.javaClass.simpleName}", "NOT_AST_NODE")
                            }
                        }
                    }
                }

                is LRAction.Reduce -> { // Reduce 액션
                    val production = action.production // 적용할 생산 규칙
                    logger.info("Reduce 액션 수행. 생산 규칙: {}", production) // Reduce 액션 로그

                    val children = mutableListOf<Any>() // 자식 노드를 저장할 리스트
                    repeat(production.length) { // 생산 규칙의 우변 길이만큼 스택에서 팝
                        val poppedState = stateStack.removeAt(stateStack.size - 1) // 상태 스택에서 팝
                        val poppedSymbol = symbolStack.removeAt(symbolStack.size - 1) // 심볼 스택에서 팝
                        val symbolValue = when (poppedSymbol) {
                            is ParseSymbol.TokenSymbol -> poppedSymbol.token
                            is ParseSymbol.ASTSymbol -> poppedSymbol.node
                            is ParseSymbol.ArgumentsSymbol -> poppedSymbol.args
                        }
                        children.add(0, symbolValue) // 자식 리스트의 맨 앞에 추가 (역순으로 팝되므로)
                        logger.trace("스택에서 팝: 상태 {}, 심볼 {}", poppedState, poppedSymbol) // 스택 팝 로그
                    }
                    logger.debug("Reduce를 위해 팝된 심볼들: {}", children) // 팝된 심볼들 로그

                    val result = buildASTNode(production, children) // AST 노드 구축
                    val parseSymbol = when (result) {
                        is ASTNode -> ParseSymbol.ASTSymbol(result)
                        is List<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            ParseSymbol.ArgumentsSymbol(result as List<ASTNode>)
                        }
                        else -> throw CalculatorException("지원하지 않는 AST 결과 타입: ${result.javaClass.simpleName}", "UNSUPPORTED_AST_TYPE")
                    }
                    symbolStack.add(parseSymbol) // 구축된 AST 노드를 심볼 스택에 푸시
                    logger.debug("AST 노드 구축 완료. 결과: {}", result) // AST 노드 구축 결과 로그

                    val newState =
                        LRParserTable.getGoto(stateStack.last(), production.left) // GOTO 테이블에서 다음 상태 조회
                            ?: run { // GOTO 상태를 찾을 수 없는 경우
                                logger.error("GOTO 오류 발생. 현재 상태: {}, 논터미널: {}", stateStack.last(), production.left) // GOTO 오류 로그
                                throw CalculatorException("GOTO 오류", "GOTO_ERROR")
                            }
                    stateStack.add(newState) // GOTO 상태를 상태 스택에 푸시
                    logger.debug("Reduce 후 스택 상태 - 상태: {}, 심볼: {}", stateStack, symbolStack) // Reduce 후 스택 상태 로그
                }

                is LRAction.Accept -> { // Accept 액션 (파싱 성공)
                    logger.info("Accept 액션 수행. 파싱 성공.") // Accept 액션 로그
                    val finalResult = symbolStack.last() // 최종 결과 (AST의 루트 노드)
                    return when (finalResult) {
                        is ParseSymbol.ASTSymbol -> { // 최종 결과가 ASTNode인 경우 반환
                            logger.info("최종 파싱 결과: {}", finalResult.node) // 최종 파싱 결과 로그
                            finalResult.node
                        }
                        else -> { // ASTNode가 아닌 경우 오류
                            logger.error("파싱 결과가 AST 노드가 아닙니다: {}", finalResult.javaClass.simpleName) // AST 노드 아님 에러 로그
                            throw CalculatorException("파싱 결과가 AST 노드가 아닙니다: ${finalResult.javaClass.simpleName}", "NOT_AST_NODE")
                        }
                    }
                }

                is LRAction.Error -> { // Error 액션 (파싱 실패)
                    logger.error("파싱 오류 발생 - 상태: {}, 토큰: {}", currentState, currentToken) // 파싱 오류 로그
                    throw CalculatorException("구문 오류: ${currentToken.value}", "SYNTAX_ERROR") // 구문 오류 예외 발생
                }
            }
        }
    }

    /**
     * 주어진 생산 규칙과 자식 심볼들을 사용하여 AST 노드를 구축합니다.
     * @param production 적용된 생산 규칙.
     * @param children 생산 규칙의 우변에 해당하는 자식 심볼 목록.
     * @return 구축된 AST 노드.
     * @throws CalculatorException 알 수 없는 생산 규칙 ID인 경우.
     */
    private fun buildASTNode(
        production: Production,
        children: List<Any>,
    ): Any {
        logger.debug("buildASTNode 호출됨. 생산 규칙 ID: {}, 자식 수: {}", production.id, children.size)
        return try {
            production.astBuilder.build(children)
        } catch (e: Exception) {
            logger.error("AST 노드 구축 중 오류 발생: 생산 규칙 ID {}, 오류: {}", production.id, e.message, e)
            throw CalculatorException("AST 노드 구축 오류: ${e.message}", "AST_BUILD_ERROR", e)
        }
    }
}

// AST 노드들 (BooleanNode 추가)
/**
 * 추상 구문 트리(AST)의 모든 노드에 대한 기본 sealed 클래스입니다.
 * 모든 AST 노드는 이 클래스를 상속받아야 합니다.
 */
sealed class ASTNode {
    /**
     * ASTVisitor 패턴을 사용하여 노드를 방문합니다.
     * @param visitor 노드를 방문할 ASTVisitor 인스턴스.
     * @return 방문 결과.
     */
    abstract fun accept(visitor: ASTVisitor): Any?

    /**
     * 이 AST 노드에 포함된 모든 변수 이름을 반환합니다.
     * @return 변수 이름의 집합.
     */
    abstract fun getVariables(): Set<String>
}

/**
 * 숫자 리터럴을 나타내는 AST 노드입니다.
 * @property value 노드의 숫자 값.
 */
data class NumberNode(val value: Double) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitNumber(this)

    override fun getVariables(): Set<String> = emptySet()

    override fun toString() = value.toString()
}

/**
 * 불리언 리터럴을 나타내는 AST 노드입니다.
 * @property value 노드의 불리언 값 (true 또는 false).
 */
data class BooleanNode(val value: Boolean) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitBoolean(this)

    override fun getVariables(): Set<String> = emptySet()

    override fun toString() = value.toString()
}

/**
 * 변수를 나타내는 AST 노드입니다.
 * @property name 변수의 이름.
 */
data class VariableNode(val name: String) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitVariable(this)

    override fun getVariables(): Set<String> = setOf(name)

    override fun toString() = "{$name}"
}

/**
 * 이항 연산(예: 덧셈, 뺄셈, 비교)을 나타내는 AST 노드입니다.
 * @property left 좌측 피연산자 AST 노드.
 * @property operator 연산자 문자열 (예: "+", "-", "==").
 * @property right 우측 피연산자 AST 노드.
 */
data class BinaryOpNode(val left: ASTNode, val operator: String, val right: ASTNode) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitBinaryOp(this)

    override fun getVariables(): Set<String> = left.getVariables() + right.getVariables()

    override fun toString() = "($left $operator $right)"
}

/**
 * 단항 연산(예: 음수, 논리 부정)을 나타내는 AST 노드입니다.
 * @property operator 연산자 문자열 (예: "-", "!").
 * @property operand 피연산자 AST 노드.
 */
data class UnaryOpNode(val operator: String, val operand: ASTNode) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitUnaryOp(this)

    override fun getVariables(): Set<String> = operand.getVariables()

    override fun toString() = "$operator$operand"
}

/**
 * 함수 호출을 나타내는 AST 노드입니다.
 * @property name 호출할 함수의 이름.
 * @property args 함수에 전달될 인수 목록 (AST 노드).
 */
data class FunctionCallNode(val name: String, val args: List<ASTNode>) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitFunctionCall(this)

    override fun getVariables(): Set<String> = args.flatMap { it.getVariables() }.toSet()

    override fun toString() = "$name(${args.joinToString(", ")})"
}

/**
 * 조건문 (IF)을 나타내는 AST 노드입니다.
 * @property condition 조건식 AST 노드.
 * @property trueValue 조건이 참일 때 평가될 AST 노드.
 * @property falseValue 조건이 거짓일 때 평가될 AST 노드.
 */
data class IfNode(val condition: ASTNode, val trueValue: ASTNode, val falseValue: ASTNode) : ASTNode() {
    override fun accept(visitor: ASTVisitor) = visitor.visitIf(this)

    override fun getVariables(): Set<String> = condition.getVariables() + trueValue.getVariables() + falseValue.getVariables()

    override fun toString() = "IF($condition, $trueValue, $falseValue)"
}

/**
 * AST 노드를 방문하기 위한 인터페이스 (Visitor 패턴).
 * 각 노드 타입에 대한 방문 메서드를 정의합니다.
 */
interface ASTVisitor {
    fun visitNumber(node: NumberNode): Any?

    fun visitBoolean(node: BooleanNode): Any?

    fun visitVariable(node: VariableNode): Any?

    fun visitBinaryOp(node: BinaryOpNode): Any?

    fun visitUnaryOp(node: UnaryOpNode): Any?

    fun visitFunctionCall(node: FunctionCallNode): Any?

    fun visitIf(node: IfNode): Any?
}

// 평가기 (비교/논리 연산자 지원)
/**
 * AST(추상 구문 트리)를 순회하며 수식을 평가하는 클래스입니다.
 * 숫자, 불리언, 변수, 이항/단항 연산, 함수 호출, 조건문 등을 처리합니다.
 * @param variables 평가에 사용될 변수 맵.
 */
class ExpressionEvaluator(private val variables: Map<String, Any?>) : ASTVisitor {
    private val logger = LoggerFactory.getLogger(javaClass) // 로거 인스턴스 생성

    init {
        logger.info("ExpressionEvaluator 초기화 완료. 초기 변수: {}", variables) // 평가기 초기화 로그
    }

    /**
     * 주어진 AST 노드를 평가합니다.
     * @param node 평가할 AST 노드.
     * @return 평가 결과.
     * @throws CalculatorException 평가 중 오류 발생 시.
     */
    fun evaluate(node: ASTNode): Any? {
        logger.debug("evaluate 호출됨. 노드: {}", node) // evaluate 호출 로그
        return try {
            val result = node.accept(this) // ASTVisitor 패턴을 사용하여 노드 방문 및 평가
            logger.debug("evaluate 결과: {}", result) // evaluate 결과 로그
            result
        } catch (e: CalculatorException) {
            logger.error("평가 중 CalculatorException 발생: {}", e.message, e) // CalculatorException 발생 시 에러 로그
            throw e // CalculatorException은 그대로 재throw
        } catch (e: Exception) {
            logger.error("예상치 못한 평가 오류 발생: {}", e.message, e) // 그 외 예외 발생 시 에러 로그
            throw CalculatorException("평가 오류: ${e.message}", "EVALUATION_ERROR", e) // CalculatorException으로 래핑하여 재throw
        }
    }

    /**
     * NumberNode를 방문하여 값을 반환합니다.
     */
    override fun visitNumber(node: NumberNode): Double {
        logger.trace("visitNumber 호출됨. 값: {}", node.value) // visitNumber 호출 로그
        return node.value
    }

    /**
     * BooleanNode를 방문하여 값을 반환합니다.
     */
    override fun visitBoolean(node: BooleanNode): Boolean {
        logger.trace("visitBoolean 호출됨. 값: {}", node.value) // visitBoolean 호출 로그
        return node.value
    }

    /**
     * VariableNode를 방문하여 변수 값을 조회합니다.
     * @throws CalculatorException 변수가 정의되지 않았거나 지원되지 않는 타입인 경우.
     */
    override fun visitVariable(node: VariableNode): Any? {
        logger.trace("visitVariable 호출됨. 변수명: {}", node.name) // visitVariable 호출 로그
        val value =
            variables[node.name]
                ?: run { // 변수가 맵에 없는 경우
                    logger.error("변수 '{}'가 정의되지 않았습니다.", node.name) // 변수 정의되지 않음 에러 로그
                    throw CalculatorException("변수 '${node.name}'가 정의되지 않았습니다.", "UNDEFINED_VARIABLE")
                }
        logger.trace("변수 '{}' 값: {}", node.name, value) // 변수 값 로그
        return when (value) {
            is Number -> value.toDouble() // 숫자는 Double로 변환
            is String -> value.toDoubleOrNull() ?: value // 문자열은 숫자로 변환 시도, 실패 시 문자열 그대로 반환
            is Boolean -> value // 불리언은 그대로 반환
            else -> { // 지원되지 않는 변수 타입
                logger.error("지원되지 않는 변수 타입: {} (변수: {})", value?.javaClass?.simpleName, node.name) // 지원되지 않는 변수 타입 에러 로그
                throw CalculatorException("지원되지 않는 변수 타입", "UNSUPPORTED_TYPE")
            }
        }
    }

    /**
     * BinaryOpNode를 방문하여 이항 연산을 수행합니다.
     * @throws CalculatorException 0으로 나누거나 지원되지 않는 연산자인 경우.
     */
    override fun visitBinaryOp(node: BinaryOpNode): Any? {
        logger.trace("visitBinaryOp 호출됨. 연산자: {}, 좌측: {}, 우측: {}", node.operator, node.left, node.right) // visitBinaryOp 호출 로그
        val left = node.left.accept(this) // 좌측 피연산자 평가
        val right = node.right.accept(this) // 우측 피연산자 평가
        logger.trace("이항 연산 피연산자 평가 완료. 좌측: {}, 우측: {}", left, right) // 피연산자 평가 완료 로그

        return when (node.operator) {
            // 산술 연산자
            "+" -> {
                if (left is String || right is String) {
                    logger.debug("문자열 연결 연산: {} + {}", left, right) // 문자열 연결 로그
                    left.toString() + right.toString()
                } else {
                    logger.debug("숫자 덧셈 연산: {} + {}", left, right) // 숫자 덧셈 로그
                    (left as Double) + (right as Double)
                }
            }
            "-" -> { logger.debug("숫자 뺄셈 연산: {} - {}", left, right); (left as Double) - (right as Double) }
            "*" -> { logger.debug("숫자 곱셈 연산: {} * {}", left, right); (left as Double) * (right as Double) }
            "/" -> {
                val rightVal = right as Double
                if (rightVal == 0.0) {
                    logger.error("0으로 나누기 오류 발생.") // 0으로 나누기 에러 로그
                    throw CalculatorException("0으로 나눌 수 없습니다.", "DIVISION_BY_ZERO")
                }
                logger.debug("숫자 나눗셈 연산: {} / {}", left, right) // 숫자 나눗셈 로그
                (left as Double) / rightVal
            }
            "%" -> { logger.debug("숫자 나머지 연산: {} % {}", left, right); (left as Double) % (right as Double) }
            "^" -> { logger.debug("숫자 거듭제곱 연산: {} ^ {}", left, right); (left as Double).pow(right as Double) }

            // 비교 연산자
            "==" -> {
                logger.debug("동등 비교 연산: {} == {}", left, right) // 동등 비교 로그
                when {
                    left is String && right is String -> left == right
                    left is Boolean && right is Boolean -> left == right
                    left is Double && right is Double -> abs(left - right) < 1e-10
                    else -> {
                        val leftNum = toNumber(left)
                        val rightNum = toNumber(right)
                        abs(leftNum - rightNum) < 1e-10
                    }
                }
            }
            "!=" -> {
                logger.debug("부등 비교 연산: {} != {}", left, right) // 부등 비교 로그
                when {
                    left is String && right is String -> left != right
                    left is Boolean && right is Boolean -> left != right
                    left is Double && right is Double -> abs(left - right) >= 1e-10
                    else -> {
                        val leftNum = toNumber(left)
                        val rightNum = toNumber(right)
                        abs(leftNum - rightNum) >= 1e-10
                    }
                }
            }
            "<" -> { logger.debug("미만 비교 연산: {} < {}", left, right); val leftNum = toNumber(left); val rightNum = toNumber(right); leftNum < rightNum }
            "<=" -> { logger.debug("이하 비교 연산: {} <= {}", left, right); val leftNum = toNumber(left); val rightNum = toNumber(right); leftNum <= rightNum }
            ">" -> { logger.debug("초과 비교 연산: {} > {}", left, right); val leftNum = toNumber(left); val rightNum = toNumber(right); leftNum > rightNum }
            ">=" -> { logger.debug("이상 비교 연산: {} >= {}", left, right); val leftNum = toNumber(left); val rightNum = toNumber(right); leftNum >= rightNum }

            // 논리 연산자
            "&&" -> { logger.debug("논리 AND 연산: {} && {}", left, right); toBool(left) && toBool(right) }
            "||" -> { logger.debug("논리 OR 연산: {} || {}", left, right); toBool(left) || toBool(right) }

            else -> { // 지원되지 않는 연산자
                logger.error("지원되지 않는 이항 연산자: {}", node.operator) // 지원되지 않는 연산자 에러 로그
                throw CalculatorException("지원되지 않는 연산자: ${node.operator}", "UNSUPPORTED_OPERATOR")
            }
        }
    }

    /**
     * UnaryOpNode를 방문하여 단항 연산을 수행합니다.
     * @throws CalculatorException 지원되지 않는 단항 연산자인 경우.
     */
    override fun visitUnaryOp(node: UnaryOpNode): Any? {
        logger.trace("visitUnaryOp 호출됨. 연산자: {}, 피연산자: {}", node.operator, node.operand) // visitUnaryOp 호출 로그
        val operand = node.operand.accept(this) // 피연산자 평가
        logger.trace("단항 연산 피연산자 평가 완료. 피연산자: {}", operand) // 피연산자 평가 완료 로그
        return when (node.operator) {
            "-" -> { logger.debug("단항 마이너스 연산: -{}", operand); -(toNumber(operand)) }
            "+" -> { logger.debug("단항 플러스 연산: +{}", operand); toNumber(operand) }
            "!" -> { logger.debug("논리 NOT 연산: !{}", operand); !toBool(operand) }
            else -> { // 지원되지 않는 단항 연산자
                logger.error("지원되지 않는 단항 연산자: {}", node.operator) // 지원되지 않는 단항 연산자 에러 로그
                throw CalculatorException("지원되지 않는 단항 연산자: ${node.operator}", "UNSUPPORTED_OPERATOR")
            }
        }
    }

    /**
     * FunctionCallNode를 방문하여 함수 호출을 처리합니다.
     * @throws CalculatorException 지원되지 않는 함수 또는 잘못된 인수 개수인 경우.
     */
    override fun visitFunctionCall(node: FunctionCallNode): Any? {
        logger.trace("visitFunctionCall 호출됨. 함수명: {}, 인수: {}", node.name, node.args) // visitFunctionCall 호출 로그
        return when (node.name.uppercase()) {
            "ABS" -> {
                logger.debug("ABS 함수 호출.") // ABS 함수 호출 로그
                val args = node.args.map { it.accept(this) as Double }
                if (args.size != 1) {
                    logger.error("ABS 함수 인수 개수 오류. 예상: 1, 실제: {}", args.size) // ABS 함수 인수 개수 오류 로그
                    throw CalculatorException("ABS 함수는 1개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                }
                abs(args[0])
            }
            "SQRT" -> {
                logger.debug("SQRT 함수 호출.") // SQRT 함수 호출 로그
                val args = node.args.map { it.accept(this) as Double }
                if (args.size != 1) {
                    logger.error("SQRT 함수 인수 개수 오류. 예상: 1, 실제: {}", args.size) // SQRT 함수 인수 개수 오류 로그
                    throw CalculatorException("SQRT 함수는 1개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                }
                sqrt(args[0])
            }
            "ROUND" -> {
                logger.debug("ROUND 함수 호출.") // ROUND 함수 호출 로그
                val args = node.args.map { toNumber(it.accept(this)) }
                when (args.size) {
                    1 -> round(args[0])
                    2 -> {
                        val value = args[0]
                        val places = args[1].toInt()
                        val multiplier = 10.0.pow(places.toDouble())
                        round(value * multiplier) / multiplier
                    }
                    else -> {
                        logger.error("ROUND 함수 인수 개수 오류. 예상: 1 또는 2, 실제: {}", args.size) // ROUND 함수 인수 개수 오류 로그
                        throw CalculatorException("ROUND 함수는 1개 또는 2개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                    }
                }
            }
            "MIN" -> {
                logger.debug("MIN 함수 호출.") // MIN 함수 호출 로그
                val args = node.args.map { toNumber(it.accept(this)) }
                if (args.isEmpty()) {
                    logger.error("MIN 함수 인수 개수 오류. 예상: 최소 1, 실제: {}", args.size) // MIN 함수 인수 개수 오류 로그
                    throw CalculatorException("MIN 함수는 최소 1개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                }
                args.minOrNull() ?: 0.0
            }
            "MAX" -> {
                logger.debug("MAX 함수 호출.") // MAX 함수 호출 로그
                val args = node.args.map { toNumber(it.accept(this)) }
                if (args.isEmpty()) {
                    logger.error("MAX 함수 인수 개수 오류. 예상: 최소 1, 실제: {}", args.size) // MAX 함수 인수 개수 오류 로그
                    throw CalculatorException("MAX 함수는 최소 1개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                }
                args.maxOrNull() ?: 0.0
            }
            "SUM" -> {
                logger.debug("SUM 함수 호출.") // SUM 함수 호출 로그
                val args = node.args.map { toNumber(it.accept(this)) }
                args.sum()
            }
            "AVG", "AVERAGE" -> {
                logger.debug("AVG/AVERAGE 함수 호출.") // AVG/AVERAGE 함수 호출 로그
                val args = node.args.map { toNumber(it.accept(this)) }
                if (args.isEmpty()) {
                    logger.error("AVG 함수 인수 개수 오류. 예상: 최소 1, 실제: {}", args.size) // AVG 함수 인수 개수 오류 로그
                    throw CalculatorException("AVG 함수는 최소 1개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                }
                args.average()
            }
            "IF" -> {
                logger.debug("IF 함수 호출.") // IF 함수 호출 로그
                if (node.args.size != 3) {
                    logger.error("IF 함수 인수 개수 오류. 예상: 3, 실제: {}", node.args.size) // IF 함수 인수 개수 오류 로그
                    throw CalculatorException("IF 함수는 3개의 인수가 필요합니다.", "WRONG_ARGUMENT_COUNT")
                }
                val condition = node.args[0].accept(this) // 조건 평가
                val conditionResult = toBool(condition) // 불리언으로 변환
                logger.debug("IF 함수 조건 평가 결과: {}", conditionResult) // IF 함수 조건 평가 결과 로그
                if (conditionResult) {
                    node.args[1].accept(this) // 조건이 참이면 두 번째 인수 평가
                } else {
                    node.args[2].accept(this) // 조건이 거짓이면 세 번째 인수 평가
                }
            }
            else -> { // 지원되지 않는 함수
                logger.error("지원되지 않는 함수: {}", node.name) // 지원되지 않는 함수 에러 로그
                throw CalculatorException("지원되지 않는 함수: ${node.name}", "UNSUPPORTED_FUNCTION")
            }
        }
    }

    /**
     * IfNode를 방문하여 조건부 평가를 수행합니다.
     */
    override fun visitIf(node: IfNode): Any? {
        logger.trace("visitIf 호출됨. 조건: {}, 참 값: {}, 거짓 값: {}", node.condition, node.trueValue, node.falseValue) // visitIf 호출 로그
        val condition = node.condition.accept(this) // 조건 평가
        val conditionResult = toBool(condition) // 불리언으로 변환
        logger.debug("IfNode 조건 평가 결과: {}", conditionResult) // IfNode 조건 평가 결과 로그

        return if (conditionResult) {
            logger.debug("조건이 참이므로 참 값 평가.") // 조건 참 로그
            node.trueValue.accept(this)
        } else {
            logger.debug("조건이 거짓이므로 거짓 값 평가.") // 조건 거짓 로그
            node.falseValue.accept(this)
        }
    }

    /**
     * 임의의 값을 불리언으로 변환합니다.
     */
    private fun toBool(value: Any?): Boolean {
        logger.trace("toBool 호출됨. 값: {}", value) // toBool 호출 로그
        return when (value) {
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            is String -> value.isNotEmpty()
            null -> false
            else -> true
        }
    }

    /**
     * 임의의 값을 숫자로 변환합니다.
     * @throws CalculatorException 문자열을 숫자로 변환할 수 없거나 지원되지 않는 타입인 경우.
     */
    private fun toNumber(value: Any?): Double {
        logger.trace("toNumber 호출됨. 값: {}", value) // toNumber 호출 로그
        return when (value) {
            is Number -> value.toDouble()
            is Boolean -> if (value) 1.0 else 0.0
            is String -> value.toDoubleOrNull() ?: run { // 문자열을 숫자로 변환 시도
                logger.error("문자열을 숫자로 변환할 수 없습니다: '{}'", value) // 문자열 숫자 변환 오류 로그
                throw CalculatorException("문자열을 숫자로 변환할 수 없습니다: $value", "NUMBER_CONVERSION_ERROR")
            }
            null -> 0.0
            else -> { // 지원되지 않는 타입
                logger.error("값을 숫자로 변환할 수 없습니다: {} (타입: {})", value, value?.javaClass?.simpleName) // 값 숫자 변환 오류 로그
                throw CalculatorException("값을 숫자로 변환할 수 없습니다: $value", "NUMBER_CONVERSION_ERROR")
            }
        }
    }
}

// 기타 클래스들
/**
 * 수식 및 요청의 유효성을 검사하는 클래스입니다.
 * 수식 길이, 단계 수, 변수 개수 등을 검증합니다.
 */
@Component // Spring 컴포넌트로 등록하여 의존성 주입이 가능하게 합니다.
class FormulaValidator {
    private val logger = LoggerFactory.getLogger(javaClass) // 로거 인스턴스 생성

    init {
        logger.info("FormulaValidator 초기화 완료.") // 유효성 검사기 초기화 로그
    }

    /**
     * 단일 계산 요청의 유효성을 검사합니다.
     * @param request 계산 요청.
     * @param properties 계산기 설정 속성.
     * @throws CalculatorException 유효성 검사 실패 시.
     */
    fun validateCalculationRequest(
        request: CalculationRequest,
        properties: CalculatorProperties,
    ) {
        logger.debug("validateCalculationRequest 호출됨. 수식: {}, 변수: {}", request.formula, request.variables) // 요청 유효성 검사 시작 로그
        validateFormula(request.formula, properties) // 수식 유효성 검사
        validateVariableCount(request.variables, properties) // 변수 개수 유효성 검사
        logger.debug("validateCalculationRequest 완료.") // 요청 유효성 검사 완료 로그
    }

    /**
     * 다단계 계산 요청의 유효성을 검사합니다.
     * @param request 다단계 계산 요청.
     * @param properties 계산기 설정 속성.
     * @throws CalculatorException 유효성 검사 실패 시.
     */
    fun validateMultiStepRequest(
        request: MultiStepCalculationRequest,
        properties: CalculatorProperties,
    ) {
        logger.debug("validateMultiStepRequest 호출됨. 단계 수: {}, 초기 변수: {}", request.steps?.size, request.variables) // 다단계 요청 유효성 검사 시작 로그
        if (request.steps.isNullOrEmpty()) {
            logger.error("계산 단계가 비어있습니다.") // 단계 비어있음 에러 로그
            throw CalculatorException("계산 단계가 비어있습니다.", "EMPTY_STEPS")
        }

        if (request.steps.size > properties.maxSteps) {
            logger.error("계산 단계가 너무 많습니다. (최대 {}단계, 현재 {}단계)", properties.maxSteps, request.steps.size) // 단계 수 초과 에러 로그
            throw CalculatorException("계산 단계가 너무 많습니다. (최대 ${properties.maxSteps}단계)", "TOO_MANY_STEPS")
        }

        validateVariableCount(request.variables, properties) // 초기 변수 개수 유효성 검사

        request.steps.forEachIndexed { index, step ->
            logger.debug("단계 {} 수식 유효성 검사 시작: {}", index + 1, step.formula) // 각 단계 수식 유효성 검사 시작 로그
            validateFormula(step.formula, properties, "단계 ${index + 1}") // 각 단계의 수식 유효성 검사
            logger.debug("단계 {} 수식 유효성 검사 완료.", index + 1) // 각 단계 수식 유효성 검사 완료 로그
        }
        logger.debug("validateMultiStepRequest 완료.") // 다단계 요청 유효성 검사 완료 로그
    }

    /**
     * 단일 수식의 유효성을 검사합니다.
     * @param formula 검사할 수식 문자열.
     * @param properties 계산기 설정 속성.
     * @param context 오류 메시지에 사용될 컨텍스트 (예: "수식", "단계 1").
     * @throws CalculatorException 수식이 비어있거나 너무 긴 경우.
     */
    fun validateFormula(
        formula: String,
        properties: CalculatorProperties,
        context: String = "수식",
    ) {
        logger.debug("validateFormula 호출됨. 컨텍스트: {}, 수식 길이: {}", context, formula.length) // 수식 유효성 검사 시작 로그
        if (formula.isBlank()) {
            logger.error("{}이 비어있습니다.", context) // 수식 비어있음 에러 로그
            throw CalculatorException("${context}이 비어있습니다.", "EMPTY_FORMULA")
        }

        if (formula.length > properties.maxFormulaLength) {
            logger.error("{}이 너무 깁니다. (최대 {}자, 현재 {}자)", context, properties.maxFormulaLength, formula.length) // 수식 길이 초과 에러 로그
            throw CalculatorException("${context}이 너무 깁니다. (최대 ${properties.maxFormulaLength}자)", "FORMULA_TOO_LONG")
        }
        logger.debug("validateFormula 완료. 컨텍스트: {}", context) // 수식 유효성 검사 완료 로그
    }

    /**
     * 필요한 변수와 제공된 변수를 비교하여 누락된 변수가 있는지 검사합니다.
     * @param requiredVars 수식에서 필요한 변수 집합.
     * @param providedVars 사용자로부터 제공된 변수 맵.
     * @throws CalculatorException 필수 변수가 누락된 경우.
     */
    fun validateVariables(
        requiredVars: Set<String>,
        providedVars: Map<String, Any?>,
    ) {
        logger.debug("validateVariables 호출됨. 필요한 변수: {}, 제공된 변수: {}", requiredVars, providedVars.keys) // 변수 유효성 검사 시작 로그
        val missingVars = requiredVars - providedVars.keys // 누락된 변수 계산
        if (missingVars.isNotEmpty()) {
            logger.error("필수 변수가 누락되었습니다: {}", missingVars.joinToString(", ")) // 누락된 변수 에러 로그
            throw CalculatorException("필수 변수가 누락되었습니다: ${missingVars.joinToString(", ")}", "MISSING_VARIABLES")
        }
        logger.debug("validateVariables 완료.") // 변수 유효성 검사 완료 로그
    }

    /**
     * 변수 개수의 유효성을 검사합니다.
     * @param variables 검사할 변수 맵.
     * @param properties 계산기 설정 속성.
     * @throws CalculatorException 변수 개수가 너무 많은 경우.
     */
    private fun validateVariableCount(
        variables: Map<String, Any?>?,
        properties: CalculatorProperties,
    ) {
        logger.debug("validateVariableCount 호출됨. 변수 개수: {}", variables?.size ?: 0) // 변수 개수 유효성 검사 시작 로그
        if (variables != null && variables.size > properties.maxVariables) {
            logger.error("변수가 너무 많습니다. (최대 {}개, 현재 {}개)", properties.maxVariables, variables.size) // 변수 개수 초과 에러 로그
            throw CalculatorException("변수가 너무 많습니다. (최대 ${properties.maxVariables}개)", "TOO_MANY_VARIABLES")
        }
        logger.debug("validateVariableCount 완료.") // 변수 개수 유효성 검사 완료 로그
    }
}

class CalculatorException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.error("CalculatorException 발생: 메시='{}', 에러코드='{}'", message, errorCode, cause) // 예외 발생 로그
    }
}

// DTO 클래스들
/**
 * 단일 수식 계산 요청을 위한 데이터 전송 객체(DTO)입니다.
 * @property formula 계산할 수식 문자열.
 * @property variables 수식 내에서 사용될 변수와 그 값의 맵 (선택 사항).
 */
data class CalculationRequest(
    val formula: String,
    val variables: Map<String, Any?>? = null,
)

/**
 * 다단계 수식 계산 요청을 위한 데이터 전송 객체(DTO)입니다.
 * @property variables 모든 단계에서 공통으로 사용될 초기 변수와 그 값의 맵 (선택 사항).
 * @property steps 수행할 계산 단계 목록.
 */
data class MultiStepCalculationRequest(
    val variables: Map<String, Any?>? = null,
    val steps: List<CalculationStep>? = null,
)

/**
 * 다단계 계산의 개별 단계를 나타내는 데이터 전송 객체(DTO)입니다.
 * @property stepName 단계의 이름 (선택 사항).
 * @property formula 해당 단계에서 계산할 수식 문자열.
 * @property resultVariable 이 단계의 계산 결과를 저장할 변수 이름 (선택 사항).
 */
data class CalculationStep(
    val stepName: String? = null,
    val formula: String,
    val resultVariable: String? = null,
)

/**
 * 단일 수식 계산 결과를 위한 데이터 전송 객체(DTO)입니다.
 * @property originalFormula 요청된 원본 수식 문자열 (선택 사항).
 * @property processedFormula 파싱 후 처리된 수식 문자열 (선택 사항).
 * @property variables 계산에 사용된 변수 맵 (선택 사항).
 * @property result 계산 결과.
 * @property executionTimeMs 계산에 소요된 시간 (밀리초).
 * @property success 계산 성공 여부.
 * @property errorMessage 오류 메시지 (오류 발생 시).
 * @property errorCode 오류 코드 (오류 발생 시).
 */
data class CalculationResponse(
    val originalFormula: String? = null,
    val processedFormula: String? = null,
    val variables: Map<String, Any?>? = null,
    val result: Any? = null,
    val executionTimeMs: Long = 0,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: String? = null,
)

/**
 * 다단계 수식 계산 결과를 위한 데이터 전송 객체(DTO)입니다.
 * @property steps 각 단계별 계산 결과 목록.
 * @property finalVariables 모든 단계 완료 후 최종 변수 맵.
 * @property executionTimeMs 전체 다단계 계산에 소요된 시간 (밀리초).
 * @property success 계산 성공 여부.
 * @property errorMessage 오류 메시지 (오류 발생 시).
 * @property errorCode 오류 코드 (오류 발생 시).
 */
data class MultiStepCalculationResponse(
    val steps: List<StepResult>? = null,
    val finalVariables: Map<String, Any?>? = null,
    val executionTimeMs: Long = 0,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: String? = null,
)

/**
 * 다단계 계산의 개별 단계 결과를 나타내는 데이터 전송 객체(DTO)입니다.
 * @property stepName 단계의 이름 (선택 사항).
 * @property originalFormula 해당 단계의 원본 수식 문자열 (선택 사항).
 * @property processedFormula 해당 단계의 파싱 후 처리된 수식 문자열 (선택 사항).
 * @property result 해당 단계의 계산 결과.
 * @property resultVariable 해당 단계의 계산 결과가 저장된 변수 이름 (선택 사항).
 * @property executionTimeMs 해당 단계 계산에 소요된 시간 (밀리초).
 */
data class StepResult(
    val stepName: String? = null,
    val originalFormula: String? = null,
    val processedFormula: String? = null,
    val result: Any? = null,
    val resultVariable: String? = null,
    val executionTimeMs: Long = 0,
)

```