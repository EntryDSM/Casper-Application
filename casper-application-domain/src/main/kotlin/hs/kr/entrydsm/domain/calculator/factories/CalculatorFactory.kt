package hs.kr.entrydsm.domain.calculator.factories

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity
import hs.kr.entrydsm.global.annotation.specification.type.Priority
import java.time.Instant

/**
 * Calculator 도메인 객체들을 생성하는 팩토리입니다.
 *
 * DDD Factory 패턴을 적용하여 계산기와 관련된 객체들의 생성과 구성을 
 * 체계적으로 관리합니다. 다양한 유형의 계산기와 세션을 생성하고
 * 적절한 설정과 정책을 적용하여 일관된 객체 생성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Factory(
    context = "calculator",
    complexity = Complexity.HIGH,
    cache = true
)
class CalculatorFactory {

    companion object {
        private var createdCalculatorCount = 0L
        private var createdSessionCount = 0L
        private var createdRequestCount = 0L
        
        @Volatile
        private var instance: CalculatorFactory? = null
        
        fun getInstance(): CalculatorFactory {
            return instance ?: synchronized(this) {
                instance ?: CalculatorFactory().also { instance = it }
            }
        }
        
        // 편의 메서드들
        fun quickCreateBasicCalculator(): Calculator = getInstance().createBasicCalculator()
        fun quickCreateScientificCalculator(): Calculator = getInstance().createScientificCalculator()
        fun quickCreateSession(userId: String? = null): CalculationSession = getInstance().createSession(userId)
        fun quickCreateRequest(expression: String, variables: Map<String, Any> = emptyMap()): CalculationRequest = 
            getInstance().createRequest(expression, variables)
    }

    /**
     * 기본 계산기를 생성합니다.
     *
     * @return 기본 설정의 계산기
     */
    fun createBasicCalculator(): Calculator {
        createdCalculatorCount++
        return Calculator.createBasic()
    }

    /**
     * 과학 계산기를 생성합니다.
     *
     * @return 과학 계산 기능이 포함된 계산기
     */
    fun createScientificCalculator(): Calculator {
        createdCalculatorCount++
        return Calculator.createScientific()
    }

    /**
     * 통계 계산기를 생성합니다.
     *
     * @return 통계 함수가 포함된 계산기
     */
    fun createStatisticalCalculator(): Calculator {
        createdCalculatorCount++
        return Calculator.createStatistical()
    }

    /**
     * 공학용 계산기를 생성합니다.
     *
     * @return 공학 계산 기능이 포함된 계산기
     */
    fun createEngineeringCalculator(): Calculator {
        createdCalculatorCount++
        return Calculator.createEngineering()
    }

    /**
     * 사용자 정의 설정으로 계산기를 생성합니다.
     *
     * @param precision 정밀도
     * @param angleUnit 각도 단위
     * @param enableCaching 캐싱 활성화 여부
     * @param enableOptimization 최적화 활성화 여부
     * @return 사용자 정의 계산기
     */
    fun createCustomCalculator(
        precision: Int = 10,
        angleUnit: CalculationSession.CalculationSettings.AngleUnit = CalculationSession.CalculationSettings.AngleUnit.RADIANS,
        enableCaching: Boolean = true,
        enableOptimization: Boolean = true
    ): Calculator {
        createdCalculatorCount++
        
        val settingsMap = mapOf(
            "precision" to precision,
            "angleUnit" to angleUnit,
            "enableCaching" to enableCaching,
            "enableOptimization" to enableOptimization
        )
        
        return Calculator.createWithSettings(settingsMap)
    }

    /**
     * 새로운 계산 세션을 생성합니다.
     *
     * @param userId 사용자 ID (선택적)
     * @return 새로운 계산 세션
     */
    fun createSession(userId: String? = null): CalculationSession {
        createdSessionCount++
        return if (userId != null) {
            CalculationSession.createForUser(userId)
        } else {
            CalculationSession.createTemporary()
        }
    }

    /**
     * 기본 설정으로 사용자 세션을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 세션
     */
    fun createUserSession(userId: String): CalculationSession {
        require(userId.isNotBlank()) { "사용자 ID는 비어있을 수 없습니다" }
        createdSessionCount++
        return CalculationSession.createForUser(userId)
    }

    /**
     * 임시 세션을 생성합니다.
     *
     * @return 임시 세션
     */
    fun createTemporarySession(): CalculationSession {
        createdSessionCount++
        return CalculationSession.createTemporary()
    }

    /**
     * 사용자 정의 설정으로 세션을 생성합니다.
     *
     * @param sessionId 세션 ID
     * @param userId 사용자 ID
     * @param settings 계산 설정
     * @param variables 초기 변수들
     * @return 사용자 정의 세션
     */
    fun createCustomSession(
        sessionId: String,
        userId: String? = null,
        settings: CalculationSession.CalculationSettings = CalculationSession.CalculationSettings.default(),
        variables: Map<String, Any> = emptyMap()
    ): CalculationSession {
        createdSessionCount++
        return CalculationSession(
            sessionId = sessionId,
            userId = userId,
            variables = variables,
            settings = settings
        )
    }

    /**
     * 계산 요청을 생성합니다.
     *
     * @param formula 수식
     * @param variables 변수들 (선택적)
     * @return 계산 요청
     */
    fun createRequest(
        formula: String,
        variables: Map<String, Any> = emptyMap()
    ): CalculationRequest {
        require(formula.isNotBlank()) { "수식은 비어있을 수 없습니다" }
        createdRequestCount++
        
        return CalculationRequest(
            formula = formula,
            variables = variables
        )
    }

    /**
     * 우선순위가 있는 계산 요청을 생성합니다.
     *
     * @param formula 수식
     * @param priority 우선순위
     * @param variables 변수들
     * @return 우선순위 계산 요청
     */
    fun createPriorityRequest(
        formula: String,
        priority: Priority,
        variables: Map<String, Any> = emptyMap()
    ): CalculationRequest {
        createdRequestCount++
        val options = mapOf("priority" to priority.name)
        return CalculationRequest(
            formula = formula,
            variables = variables,
            options = options
        )
    }

    /**
     * 일괄 계산 요청들을 생성합니다.
     *
     * @param expressions 수식들
     * @param variables 공통 변수들
     * @return 계산 요청 목록
     */
    fun createBatchRequests(
        expressions: List<String>,
        variables: Map<String, Any> = emptyMap()
    ): List<CalculationRequest> {
        require(expressions.isNotEmpty()) { "수식 목록은 비어있을 수 없습니다" }
        
        return expressions.map { expression ->
            createRequest(expression, variables)
        }
    }

    /**
     * 성공한 계산 결과를 생성합니다.
     *
     * @param requestId 요청 ID
     * @param result 계산 결과 값
     * @param executionTime 실행 시간
     * @return 성공 계산 결과
     */
    fun createSuccessResult(
        formula: String,
        result: Any,
        executionTimeMs: Long = 0
    ): CalculationResult {
        return CalculationResult(
            result = result,
            executionTimeMs = executionTimeMs,
            formula = formula
        )
    }

    /**
     * 실패한 계산 결과를 생성합니다.
     *
     * @param requestId 요청 ID
     * @param error 오류 정보
     * @param executionTime 실행 시간
     * @return 실패 계산 결과
     */
    fun createFailureResult(
        formula: String,
        error: String,
        executionTimeMs: Long = 0
    ): CalculationResult {
        return CalculationResult(
            result = null,
            executionTimeMs = executionTimeMs,
            formula = formula,
            errors = listOf(error)
        )
    }

    /**
     * 예외로부터 실패 결과를 생성합니다.
     *
     * @param requestId 요청 ID
     * @param exception 예외
     * @param executionTime 실행 시간
     * @return 실패 계산 결과
     */
    fun createFailureFromException(
        formula: String,
        exception: Exception,
        executionTimeMs: Long = 0
    ): CalculationResult {
        return CalculationResult(
            result = null,
            executionTimeMs = executionTimeMs,
            formula = formula,
            errors = listOf("계산 오류: ${exception.message}")
        )
    }

    /**
     * 기본 변수들을 가진 환경을 생성합니다.
     *
     * @return 기본 변수 맵
     */
    fun createDefaultEnvironment(): Map<String, Any> {
        return mapOf(
            "PI" to kotlin.math.PI,
            "E" to kotlin.math.E,
            "TRUE" to true,
            "FALSE" to false,
            "INFINITY" to Double.POSITIVE_INFINITY,
            "NAN" to Double.NaN
        )
    }

    /**
     * 과학 계산용 환경을 생성합니다.
     *
     * @return 과학 상수가 포함된 변수 맵
     */
    fun createScientificEnvironment(): Map<String, Any> {
        val defaultEnv = createDefaultEnvironment().toMutableMap()
        
        // 물리 상수들
        defaultEnv["LIGHT_SPEED"] = 299792458.0 // m/s
        defaultEnv["PLANCK"] = 6.62607015e-34 // J⋅s
        defaultEnv["AVOGADRO"] = 6.02214076e23 // mol⁻¹
        defaultEnv["BOLTZMANN"] = 1.380649e-23 // J/K
        defaultEnv["GAS_CONSTANT"] = 8.314462618 // J/(mol⋅K)
        defaultEnv["ELECTRON_CHARGE"] = 1.602176634e-19 // C
        defaultEnv["ELECTRON_MASS"] = 9.1093837015e-31 // kg
        defaultEnv["PROTON_MASS"] = 1.67262192369e-27 // kg
        
        // 수학 상수들
        defaultEnv["GOLDEN_RATIO"] = (1 + kotlin.math.sqrt(5.0)) / 2
        defaultEnv["EULER_GAMMA"] = 0.5772156649015329
        
        return defaultEnv
    }

    /**
     * 공학용 환경을 생성합니다.
     *
     * @return 공학 상수가 포함된 변수 맵
     */
    fun createEngineeringEnvironment(): Map<String, Any> {
        val scientificEnv = createScientificEnvironment().toMutableMap()
        
        // 공학 상수들
        scientificEnv["GRAVITY"] = 9.80665 // m/s²
        scientificEnv["ATMOSPHERIC_PRESSURE"] = 101325.0 // Pa
        scientificEnv["ABSOLUTE_ZERO"] = -273.15 // °C
        scientificEnv["STEFAN_BOLTZMANN"] = 5.670374419e-8 // W⋅m⁻²⋅K⁻⁴
        
        return scientificEnv
    }

    /**
     * 통계용 환경을 생성합니다.
     *
     * @return 통계 상수가 포함된 변수 맵
     */
    fun createStatisticalEnvironment(): Map<String, Any> {
        val defaultEnv = createDefaultEnvironment().toMutableMap()
        
        // 통계 상수들
        defaultEnv["SQRT_2PI"] = kotlin.math.sqrt(2 * kotlin.math.PI)
        defaultEnv["LN_2"] = kotlin.math.ln(2.0)
        defaultEnv["LN_10"] = kotlin.math.ln(10.0)
        defaultEnv["LOG10_E"] = kotlin.math.log10(kotlin.math.E)
        
        return defaultEnv
    }

    /**
     * 고성능 계산기를 생성합니다.
     *
     * @param maxConcurrency 최대 동시 계산 수
     * @param cacheSize 캐시 크기
     * @return 고성능 계산기
     */
    fun createHighPerformanceCalculator(
        maxConcurrency: Int = 10,
        cacheSize: Int = 1000
    ): Calculator {
        createdCalculatorCount++
        
        val settingsMap = mapOf(
            "precision" to 15,
            "enableCaching" to true,
            "enableOptimization" to true,
            "maxHistorySize" to cacheSize
        )
        
        return Calculator.createWithSettings(settingsMap)
    }

    /**
     * 보안 강화 계산기를 생성합니다.
     *
     * @return 보안 설정이 강화된 계산기
     */
    fun createSecureCalculator(): Calculator {
        createdCalculatorCount++
        
        val settingsMap = mapOf(
            "precision" to 10,
            "strictMode" to true,
            "enableCaching" to false, // 보안을 위해 캐싱 비활성화
            "enableOptimization" to false, // 예측 가능한 동작을 위해 최적화 비활성화
            "maxHistorySize" to 10
        )
        
        return Calculator.createWithSettings(settingsMap)
    }

    /**
     * 요청 ID를 생성합니다.
     *
     * @return 고유한 요청 ID
     */
    private fun generateRequestId(): String {
        return "req_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
    }

    /**
     * 팩토리의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "factoryName" to "CalculatorFactory",
        "createdCalculators" to createdCalculatorCount,
        "createdSessions" to createdSessionCount,
        "createdRequests" to createdRequestCount,
        "supportedCalculatorTypes" to listOf("basic", "scientific", "statistical", "engineering", "custom"),
        "supportedEnvironments" to listOf("default", "scientific", "engineering", "statistical"),
        "cacheEnabled" to true,
        "complexityLevel" to Complexity.HIGH.name
    )

    /**
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "defaultPrecision" to 10,
        "defaultAngleUnit" to "RADIANS",
        "defaultCachingEnabled" to true,
        "defaultOptimizationEnabled" to true,
        "maxConcurrency" to 10,
        "defaultCacheSize" to 1000,
        "securityMode" to "standard"
    )

}