package hs.kr.entrydsm.domain.calculator.factories

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.entities.CalculationSession
import hs.kr.entrydsm.domain.calculator.exceptions.CalculatorException
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.global.annotation.specification.type.Priority
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.assertContains

/**
 * CalculatorFactory의 편의 메서드들을 테스트하는 클래스입니다.
 */
class CalculatorFactoryTest {

    private lateinit var factory: CalculatorFactory

    @BeforeEach
    fun setUp() {
        factory = CalculatorFactory()
    }

    @Test
    fun `createBasicCalculator가 기본 계산기를 생성하는지 테스트`() {
        val calculator = factory.createBasicCalculator()
        
        assertNotNull(calculator)
        assertTrue(calculator is Calculator)
    }

    @Test
    fun `createScientificCalculator가 과학 계산기를 생성하는지 테스트`() {
        val calculator = factory.createScientificCalculator()
        
        assertNotNull(calculator)
        assertTrue(calculator is Calculator)
    }

    @Test
    fun `createStatisticalCalculator가 통계 계산기를 생성하는지 테스트`() {
        val calculator = factory.createStatisticalCalculator()
        
        assertNotNull(calculator)
        assertTrue(calculator is Calculator)
    }

    @Test
    fun `createEngineeringCalculator가 공학용 계산기를 생성하는지 테스트`() {
        val calculator = factory.createEngineeringCalculator()
        
        assertNotNull(calculator)
        assertTrue(calculator is Calculator)
    }

    @Test
    fun `createSession이 계산 세션을 생성하는지 테스트`() {
        val session = factory.createSession("testUser")
        
        assertNotNull(session)
        assertTrue(session is CalculationSession)
    }

    @Test
    fun `createSession이 익명 세션을 생성하는지 테스트`() {
        val session = factory.createSession()
        
        assertNotNull(session)
        assertTrue(session is CalculationSession)
    }

    @Test
    fun `createRequest가 계산 요청을 생성하는지 테스트`() {
        val request = factory.createRequest("2 + 3")
        
        assertNotNull(request)
        assertTrue(request is CalculationRequest)
        assertEquals("2 + 3", request.formula)
    }

    @Test
    fun `createRequest가 변수가 포함된 계산 요청을 생성하는지 테스트`() {
        val variables = mapOf("x" to 10.0, "y" to 5.0)
        val request = factory.createRequest("x + y", variables)
        
        assertNotNull(request)
        assertTrue(request is CalculationRequest)
        assertEquals("x + y", request.formula)
        assertEquals(variables, request.variables)
    }

    @Test
    fun `Companion object의 getInstance가 싱글톤을 반환하는지 테스트`() {
        val instance1 = CalculatorFactory.getInstance()
        val instance2 = CalculatorFactory.getInstance()
        
        assertNotNull(instance1)
        assertNotNull(instance2)
        assertEquals(instance1, instance2) // 같은 인스턴스여야 함
    }

    @Test
    fun `Companion object의 편의 메서드들이 동작하는지 테스트`() {
        val basicCalculator = CalculatorFactory.quickCreateBasicCalculator()
        assertNotNull(basicCalculator)
        assertTrue(basicCalculator is Calculator)

        val scientificCalculator = CalculatorFactory.quickCreateScientificCalculator()
        assertNotNull(scientificCalculator)
        assertTrue(scientificCalculator is Calculator)

        val session = CalculatorFactory.quickCreateSession("quickUser")
        assertNotNull(session)
        assertTrue(session is CalculationSession)

        val request = CalculatorFactory.quickCreateRequest("1 + 1")
        assertNotNull(request)
        assertTrue(request is CalculationRequest)
        assertEquals("1 + 1", request.formula)
    }

    @Test
    fun `여러 계산기를 생성할 수 있는지 테스트`() {
        val basicCalc1 = factory.createBasicCalculator()
        val basicCalc2 = factory.createBasicCalculator()
        val scientificCalc = factory.createScientificCalculator()
        val engineeringCalc = factory.createEngineeringCalculator()
        val statisticalCalc = factory.createStatisticalCalculator()
        
        // 모든 계산기가 성공적으로 생성되었는지 확인
        assertNotNull(basicCalc1)
        assertNotNull(basicCalc2)
        assertNotNull(scientificCalc)
        assertNotNull(engineeringCalc)
        assertNotNull(statisticalCalc)
        
        // 각각 다른 인스턴스인지 확인 (Calculator의 equals 구현에 따라)
        assertTrue(basicCalc1 is Calculator)
        assertTrue(basicCalc2 is Calculator)
        assertTrue(scientificCalc is Calculator)
        assertTrue(engineeringCalc is Calculator)
        assertTrue(statisticalCalc is Calculator)
    }

    @Test
    fun `여러 세션을 생성할 수 있는지 테스트`() {
        val session1 = factory.createSession("user1")
        val session2 = factory.createSession("user2")
        val anonymousSession = factory.createSession()
        
        assertNotNull(session1)
        assertNotNull(session2)
        assertNotNull(anonymousSession)
        
        assertTrue(session1 is CalculationSession)
        assertTrue(session2 is CalculationSession)
        assertTrue(anonymousSession is CalculationSession)
    }

    @Test
    fun `다양한 표현식으로 요청을 생성할 수 있는지 테스트`() {
        val simpleRequest = factory.createRequest("2 + 2")
        val complexRequest = factory.createRequest("sin(x) * cos(y) + sqrt(z)", mapOf("x" to 1.0, "y" to 2.0, "z" to 4.0))
        val functionRequest = factory.createRequest("max(a, b, c)", mapOf("a" to 10, "b" to 20, "c" to 15))
        
        assertNotNull(simpleRequest)
        assertNotNull(complexRequest)
        assertNotNull(functionRequest)
        
        assertEquals("2 + 2", simpleRequest.formula)
        assertEquals("sin(x) * cos(y) + sqrt(z)", complexRequest.formula)
        assertEquals("max(a, b, c)", functionRequest.formula)
        
        assertTrue(simpleRequest.variables.isEmpty())
        assertEquals(3, complexRequest.variables.size)
        assertEquals(3, functionRequest.variables.size)
    }

    @Test
    fun `팩토리가 올바르게 인스턴스화되는지 테스트`() {
        assertNotNull(factory)
    }

    @Test
    fun `createCustomCalculator가 사용자 정의 설정으로 계산기를 생성하는지 테스트`() {
        val customCalculator = factory.createCustomCalculator(
            precision = 15,
            enableCaching = true,
            enableOptimization = false
        )
        
        assertNotNull(customCalculator)
        assertTrue(customCalculator is Calculator)
    }

    @Test
    fun `createUserSession이 사용자 세션을 생성하는지 테스트`() {
        val userSession = factory.createUserSession("specificUser")
        
        assertNotNull(userSession)
        assertTrue(userSession is CalculationSession)
    }

    @Test
    fun `createTemporarySession이 임시 세션을 생성하는지 테스트`() {
        val tempSession = factory.createTemporarySession()
        
        assertNotNull(tempSession)
        assertTrue(tempSession is CalculationSession)
    }

    @Test
    fun `createCustomSession이 사용자 정의 세션을 생성하는지 테스트`() {
        val sessionId = "custom-session-123"
        val userId = "custom-user"
        val variables = mapOf("pi" to 3.14159, "e" to 2.71828)
        
        val customSession = factory.createCustomSession(
            sessionId = sessionId,
            userId = userId,
            variables = variables
        )
        
        assertNotNull(customSession)
        assertTrue(customSession is CalculationSession)
    }

    @Test
    fun `createPriorityRequest가 우선순위 요청을 생성하는지 테스트`() {
        val priorityRequest = factory.createPriorityRequest(
            "2 * 3 + 4", 
            Priority.HIGH
        )
        
        assertNotNull(priorityRequest)
        assertTrue(priorityRequest is CalculationRequest)
        assertEquals("2 * 3 + 4", priorityRequest.formula)
        assertTrue(priorityRequest.hasOption("priority"))
        assertEquals("HIGH", priorityRequest.getOption("priority"))
    }

    @Test
    fun `createBatchRequests가 일괄 요청들을 생성하는지 테스트`() {
        val expressions = listOf("1 + 1", "2 * 3", "10 / 2", "5 - 3")
        val variables = mapOf("x" to 5.0)
        
        val batchRequests = factory.createBatchRequests(expressions, variables)
        
        assertNotNull(batchRequests)
        assertEquals(4, batchRequests.size)
        
        batchRequests.forEachIndexed { index, request ->
            assertEquals(expressions[index], request.formula)
            assertEquals(variables, request.variables)
        }
    }

    @Test
    fun `createSuccessResult가 성공 결과를 생성하는지 테스트`() {
        val successResult = factory.createSuccessResult(
            formula = "2 + 3",
            result = 5.0,
            executionTimeMs = 100L
        )
        
        assertNotNull(successResult)
        assertTrue(successResult is CalculationResult)
        assertEquals(5.0, successResult.result)
        assertEquals(100L, successResult.executionTimeMs)
        assertEquals("2 + 3", successResult.formula)
        assertTrue(successResult.isSuccess())
    }

    @Test
    fun `createFailureResult가 실패 결과를 생성하는지 테스트`() {
        val failureResult = factory.createFailureResult(
            formula = "1 / 0",
            error = "Division by zero",
            executionTimeMs = 50L
        )
        
        assertNotNull(failureResult)
        assertTrue(failureResult is CalculationResult)
        assertEquals(null, failureResult.result)
        assertEquals(50L, failureResult.executionTimeMs)
        assertEquals("1 / 0", failureResult.formula)
        assertFalse(failureResult.isSuccess())
        assertTrue(failureResult.errors.contains("Division by zero"))
    }

    @Test
    fun `createFailureFromException이 예외로부터 실패 결과를 생성하는지 테스트`() {
        val exception = IllegalArgumentException("Invalid argument")
        val failureResult = factory.createFailureFromException(
            formula = "invalid_function(x)",
            exception = exception,
            executionTimeMs = 25L
        )
        
        assertNotNull(failureResult)
        assertTrue(failureResult is CalculationResult)
        assertEquals(null, failureResult.result)
        assertEquals(25L, failureResult.executionTimeMs)
        assertEquals("invalid_function(x)", failureResult.formula)
        assertFalse(failureResult.isSuccess())
        assertContains(failureResult.errors.first(), "Invalid argument")
    }

    @Test
    fun `createDefaultEnvironment가 기본 환경을 생성하는지 테스트`() {
        val defaultEnv = factory.createDefaultEnvironment()
        
        assertNotNull(defaultEnv)
        assertTrue(defaultEnv.containsKey("PI"))
        assertTrue(defaultEnv.containsKey("E"))
        assertTrue(defaultEnv.containsKey("TRUE"))
        assertTrue(defaultEnv.containsKey("FALSE"))
        assertEquals(kotlin.math.PI, defaultEnv["PI"])
        assertEquals(kotlin.math.E, defaultEnv["E"])
    }

    @Test
    fun `createScientificEnvironment가 과학 환경을 생성하는지 테스트`() {
        val scientificEnv = factory.createScientificEnvironment()
        
        assertNotNull(scientificEnv)
        assertTrue(scientificEnv.containsKey("PI"))
        assertTrue(scientificEnv.containsKey("E"))
        assertTrue(scientificEnv.containsKey("LIGHT_SPEED"))
        assertTrue(scientificEnv.containsKey("PLANCK"))
        assertTrue(scientificEnv.containsKey("AVOGADRO"))
        assertEquals(299792458.0, scientificEnv["LIGHT_SPEED"])
    }

    @Test
    fun `createEngineeringEnvironment가 공학 환경을 생성하는지 테스트`() {
        val engineeringEnv = factory.createEngineeringEnvironment()
        
        assertNotNull(engineeringEnv)
        assertTrue(engineeringEnv.containsKey("GRAVITY"))
        assertTrue(engineeringEnv.containsKey("ATMOSPHERIC_PRESSURE"))
        assertTrue(engineeringEnv.containsKey("ABSOLUTE_ZERO"))
        assertEquals(9.80665, engineeringEnv["GRAVITY"])
        assertEquals(101325.0, engineeringEnv["ATMOSPHERIC_PRESSURE"])
    }

    @Test
    fun `createStatisticalEnvironment가 통계 환경을 생성하는지 테스트`() {
        val statisticalEnv = factory.createStatisticalEnvironment()
        
        assertNotNull(statisticalEnv)
        assertTrue(statisticalEnv.containsKey("SQRT_2PI"))
        assertTrue(statisticalEnv.containsKey("LN_2"))
        assertTrue(statisticalEnv.containsKey("LN_10"))
        assertEquals(kotlin.math.sqrt(2 * kotlin.math.PI), statisticalEnv["SQRT_2PI"])
    }

    @Test
    fun `createHighPerformanceCalculator가 고성능 계산기를 생성하는지 테스트`() {
        val highPerfCalculator = factory.createHighPerformanceCalculator(
            maxConcurrency = 20,
            cacheSize = 2000
        )
        
        assertNotNull(highPerfCalculator)
        assertTrue(highPerfCalculator is Calculator)
    }

    @Test
    fun `createSecureCalculator가 보안 계산기를 생성하는지 테스트`() {
        val secureCalculator = factory.createSecureCalculator()
        
        assertNotNull(secureCalculator)
        assertTrue(secureCalculator is Calculator)
    }

    @Test
    fun `getStatistics가 팩토리 통계를 반환하는지 테스트`() {
        // 몇 개의 객체를 생성하여 통계를 누적
        factory.createBasicCalculator()
        factory.createSession("user1")
        factory.createRequest("1 + 1")
        
        val statistics = factory.getStatistics()
        
        assertNotNull(statistics)
        assertTrue(statistics.containsKey("factoryName"))
        assertTrue(statistics.containsKey("createdCalculators"))
        assertTrue(statistics.containsKey("createdSessions"))
        assertTrue(statistics.containsKey("createdRequests"))
        assertEquals("CalculatorFactory", statistics["factoryName"])
    }

    @Test
    fun `getConfiguration이 팩토리 설정을 반환하는지 테스트`() {
        val configuration = factory.getConfiguration()
        
        assertNotNull(configuration)
        assertTrue(configuration.containsKey("defaultPrecision"))
        assertTrue(configuration.containsKey("defaultAngleUnit"))
        assertTrue(configuration.containsKey("defaultCachingEnabled"))
        assertEquals(10, configuration["defaultPrecision"])
        assertEquals("RADIANS", configuration["defaultAngleUnit"])
        assertEquals(true, configuration["defaultCachingEnabled"])
    }

    @Test
    fun `createUserSession이 빈 사용자 ID로 예외를 발생시키는지 테스트`() {
        assertThrows<CalculatorException> {
            factory.createUserSession("")
        }
        
        assertThrows<CalculatorException> {
            factory.createUserSession("   ")
        }
    }

    @Test
    fun `createRequest가 빈 수식으로 예외를 발생시키는지 테스트`() {
        assertThrows<CalculatorException> {
            factory.createRequest("")
        }
        
        assertThrows<CalculatorException> {
            factory.createRequest("   ")
        }
    }

    @Test
    fun `createBatchRequests가 빈 표현식 목록으로 예외를 발생시키는지 테스트`() {
        assertThrows<CalculatorException> {
            factory.createBatchRequests(emptyList())
        }
    }

    @Test
    fun `팩토리가 DDD Factory 패턴을 올바르게 구현하는지 테스트`() {
        // Factory 패턴 검증: 일관된 객체 생성
        val calculator1 = factory.createBasicCalculator()
        val calculator2 = factory.createBasicCalculator()
        
        // 각각 독립적인 인스턴스여야 함
        assertTrue(calculator1 !== calculator2)
        
        // 하지만 같은 타입이어야 함
        assertEquals(calculator1::class, calculator2::class)
    }

    @Test
    fun `팩토리 통계가 객체 생성을 정확히 추적하는지 테스트`() {
        val initialStats = factory.getStatistics()
        val initialCalculators = initialStats["createdCalculators"] as Long
        val initialSessions = initialStats["createdSessions"] as Long
        val initialRequests = initialStats["createdRequests"] as Long
        
        // 새 객체들 생성
        factory.createBasicCalculator()
        factory.createScientificCalculator()
        factory.createSession("test")
        factory.createRequest("test")
        
        val updatedStats = factory.getStatistics()
        val updatedCalculators = updatedStats["createdCalculators"] as Long
        val updatedSessions = updatedStats["createdSessions"] as Long
        val updatedRequests = updatedStats["createdRequests"] as Long
        
        assertEquals(initialCalculators + 2, updatedCalculators)
        assertEquals(initialSessions + 1, updatedSessions)
        assertEquals(initialRequests + 1, updatedRequests)
    }
}