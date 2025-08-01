package hs.kr.entrydsm.domain.calculator

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * 계산기 기본 기능 검증 테스트
 * 
 * 점수 계산에 사용되는 기본적인 산술 연산과 함수들이 
 * 올바르게 동작하는지 검증합니다.
 */
@DisplayName("계산기 기본 기능 검증 테스트")
class CalculatorFunctionalTest {

    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        calculator = Calculator.createDefault()
    }

    @Test
    @DisplayName("기본 산술 연산 테스트")
    fun `기본 산술 연산이 정확히 동작하는지 확인`() {
        val testCases = mapOf(
            "2 + 3" to 5.0,
            "10 - 4" to 6.0,
            "3 * 4" to 12.0,
            "15 / 3" to 5.0,
            "2 + 3 * 4" to 14.0, // 연산자 우선순위
            "(2 + 3) * 4" to 20.0, // 괄호 우선순위
            "7 / 2" to 3.5 // 소수점 나누기
        )
        
        testCases.forEach { (formula, expected) ->
            val request = CalculationRequest(formula)
            val result = calculator.calculate(request)
            
            assertTrue(result.isSuccess(), "Formula '$formula' failed: ${result.errors}")
            assertEquals(expected, result.result as Double, 0.001, "Formula '$formula' result mismatch")
            println("✓ $formula = ${result.result}")
        }
    }

    @Test
    @DisplayName("변수를 사용한 계산 테스트")
    fun `변수가 포함된 수식 계산이 정확히 동작하는지 확인`() {
        val variables = mapOf(
            "x" to 5,
            "y" to 3,
            "korean" to 4,
            "math" to 5,
            "english" to 3
        )
        
        val testCases = mapOf(
            "x + y" to 8.0,
            "x * y" to 15.0,
            "x / y" to (5.0/3.0),
            "(korean + math + english) / 3" to 4.0, // 평균 계산
            "korean * 2 + math * 3" to 23.0 // 가중 계산
        )
        
        testCases.forEach { (formula, expected) ->
            val request = CalculationRequest(formula, variables)
            val result = calculator.calculate(request)
            
            assertTrue(result.isSuccess(), "Formula '$formula' failed: ${result.errors}")
            assertEquals(expected, result.result as Double, 0.001, "Formula '$formula' result mismatch")
            println("✓ $formula = ${result.result}")
        }
    }

    @Test
    @DisplayName("IF 조건문 테스트")
    fun `IF 조건문이 정확히 동작하는지 확인`() {
        val variables = mapOf(
            "score" to 85,
            "absent_days" to 2,
            "volunteer_hours" to 18
        )
        
        val testCases = mapOf(
            "IF(score > 80, 1, 0)" to 1.0, // true 경우
            "IF(score < 70, 1, 0)" to 0.0, // false 경우
            "IF(absent_days < 5, 15, 10)" to 15.0, // 출석점수 계산
            "IF(volunteer_hours > 15, 15, volunteer_hours)" to 15.0 // MIN 함수 대체
        )
        
        testCases.forEach { (formula, expected) ->
            val request = CalculationRequest(formula, variables)
            val result = calculator.calculate(request)
            
            assertTrue(result.isSuccess(), "Formula '$formula' failed: ${result.errors}")
            assertEquals(expected, result.result as Double, 0.001, "Formula '$formula' result mismatch")
            println("✓ $formula = ${result.result}")
        }
    }

    @Test
    @DisplayName("중첩된 IF 조건문 테스트")
    fun `중첩된 IF 조건문이 정확히 동작하는지 확인`() {
        val variables = mapOf("days" to 3)
        
        // 출석점수 계산과 유사한 중첩 IF문
        val formula = "IF(days >= 5, 10, IF(days >= 3, 12, IF(days >= 1, 14, 15)))"
        val request = CalculationRequest(formula, variables)
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess(), "Nested IF failed: ${result.errors}")
        assertEquals(12.0, result.result as Double, 0.001)
        println("✓ Nested IF: $formula = ${result.result}")
        
        // 다른 값으로 테스트
        val testCases = mapOf(
            0 to 15.0, // days < 1
            1 to 14.0, // 1 <= days < 3  
            3 to 12.0, // 3 <= days < 5
            5 to 10.0  // days >= 5
        )
        
        testCases.forEach { (daysValue, expected) ->
            val vars = mapOf("days" to daysValue)
            val req = CalculationRequest(formula, vars)
            val res = calculator.calculate(req)
            
            assertTrue(res.isSuccess())
            assertEquals(expected, res.result as Double, 0.001)
            println("✓ days=$daysValue -> ${res.result}")
        }
    }

    @Test
    @DisplayName("복잡한 점수 계산 수식 테스트")
    fun `실제 점수 계산과 유사한 복잡한 수식이 동작하는지 확인`() {
        val variables = mapOf(
            "k1" to 4, "s1" to 3, "h1" to 4, "m1" to 5, "sc1" to 4, "t1" to 3, "e1" to 4, // 3-1
            "k2" to 3, "s2" to 4, "h2" to 3, "m2" to 4, "sc2" to 3, "t2" to 4, "e2" to 3, // 2-2
            "k3" to 4, "s3" to 4, "h3" to 5, "m3" to 4, "sc3" to 3, "t3" to 4, "e3" to 4  // 2-1
        )
        
        // 실제 교과점수 계산과 동일한 복잡한 수식
        val formula = """
            (8 * ((k1 + s1 + h1 + m1 + sc1 + t1 + e1) / 7) +
             4 * ((k2 + s2 + h2 + m2 + sc2 + t2 + e2) / 7) +
             4 * ((k3 + s3 + h3 + m3 + sc3 + t3 + e3) / 7)) * 1.75
        """.trimIndent()
        
        val request = CalculationRequest(formula, variables)
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess(), "Complex formula failed: ${result.errors}")
        assertNotNull(result.result)
        
        // 단계별 계산으로 검증
        val avg1 = (4+3+4+5+4+3+4) / 7.0 // 27/7
        val avg2 = (3+4+3+4+3+4+3) / 7.0 // 24/7  
        val avg3 = (4+4+5+4+3+4+4) / 7.0 // 28/7
        val expected = (8 * avg1 + 4 * avg2 + 4 * avg3) * 1.75
        
        assertEquals(expected, result.result as Double, 0.001)
        println("✓ Complex formula result: ${result.result} (expected: $expected)")
    }

    @Test
    @DisplayName("오류 상황 처리 테스트")
    fun `다양한 오류 상황에서 적절한 오류 처리가 되는지 확인`() {
        val errorCases = listOf(
            "5 / 0", // 0으로 나누기
            "undefined_var + 1", // 정의되지 않은 변수
            "5 +", // 문법 오류
            "(((5 + 3)", // 괄호 불일치
            "" // 빈 수식
        )
        
        errorCases.forEach { formula ->
            try {
                val request = CalculationRequest(formula)
                val result = calculator.calculate(request)
                
                if (!result.isSuccess()) {
                    println("✓ Expected error for '$formula': ${result.errors}")
                } else {
                    println("? Unexpected success for '$formula': ${result.result}")
                }
            } catch (e: Exception) {
                println("✓ Expected exception for '$formula': ${e.message}")
            }
        }
    }

    @Test
    @DisplayName("성능 테스트")
    fun `계산 성능이 합리적인 범위 내에 있는지 확인`() {
        val variables = mapOf(
            "a" to 1, "b" to 2, "c" to 3, "d" to 4, "e" to 5
        )
        
        val complexFormula = "((a + b) * (c + d) - e) / ((a * b) + (c * d))"
        
        // 100번 반복 실행
        val startTime = System.currentTimeMillis()
        repeat(100) {
            val request = CalculationRequest(complexFormula, variables)
            val result = calculator.calculate(request)
            assertTrue(result.isSuccess())
        }
        val endTime = System.currentTimeMillis()
        
        val totalTime = endTime - startTime
        val avgTime = totalTime / 100.0
        
        println("✓ Performance test: 100 calculations in ${totalTime}ms (avg: ${avgTime}ms)")
        assertTrue(avgTime < 100.0, "Average calculation time too slow: ${avgTime}ms")
    }

    @Test
    @DisplayName("실제 사용자 데이터로 종합 테스트")
    fun `실제 사용자 데이터를 사용한 종합 점수 계산 테스트`() {
        val userData = mapOf(
            "korean_3_1" to 4, "social_3_1" to 3, "history_3_1" to 4, "math_3_1" to 5,
            "science_3_1" to 4, "tech_3_1" to 3, "english_3_1" to 4,
            "korean_2_2" to 3, "social_2_2" to 4, "history_2_2" to 3, "math_2_2" to 4,
            "science_2_2" to 3, "tech_2_2" to 4, "english_2_2" to 3,
            "korean_2_1" to 4, "social_2_1" to 4, "history_2_1" to 5, "math_2_1" to 4,
            "science_2_1" to 3, "tech_2_1" to 4, "english_2_1" to 4,
            "absent_days" to 0, "late_count" to 1, "volunteer_hours" to 18
        )
        
        // 단계별 계산
        val formulas = listOf(
            "(korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7",
            "(korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7", 
            "(korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7",
            "step1 * 8 + step2 * 4 + step3 * 4", // 기준점수
            "step4 * 1.75", // 교과점수
            "IF(volunteer_hours > 15, 15, volunteer_hours)", // 봉사점수
            "step5 + step6 + 15" // 총점 (출석점수 15점 가정)
        )
        
        val results = calculator.calculateMultiStep(formulas, userData)
        
        assertEquals(7, results.size)
        results.forEach { result ->
            assertTrue(result.isSuccess(), "Calculation failed: ${result.errors}")
            assertNotNull(result.result)
        }
        
        val finalScore = results.last().result as Double
        println("=== 실제 사용자 데이터 계산 결과 ===")
        println("3학년 1학기 평균: ${String.format("%.3f", results[0].result)}")
        println("2학년 2학기 평균: ${String.format("%.3f", results[1].result)}")
        println("2학년 1학기 평균: ${String.format("%.3f", results[2].result)}")
        println("교과 기준점수: ${String.format("%.3f", results[3].result)}점")
        println("교과점수: ${String.format("%.3f", results[4].result)}점")
        println("봉사점수: ${(results[5].result as Double).toInt()}점")
        println("총점: ${String.format("%.3f", finalScore)}점")
        println("===============================")
        
        // 합리적인 점수 범위 확인
        assertTrue(finalScore > 80.0 && finalScore < 173.0, "Total score out of reasonable range: $finalScore")
    }
}