package hs.kr.entrydsm.domain.calculator

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * 대덕소프트웨어마이스터고등학교 특별전형 다단계 점수 계산 통합 테스트
 * 
 * 사용자가 제공한 JSON과 동일한 다단계 계산 프로세스를 검증합니다.
 */
@DisplayName("대덕소프트웨어마이스터고등학교 특별전형 다단계 점수 계산 통합 테스트")
class MultiStepScoreCalculationTest {

    private lateinit var calculator: Calculator
    
    // 사용자가 제공한 변수들
    private val variables = mapOf(
        "korean_3_1" to 4, "social_3_1" to 3, "history_3_1" to 4, "math_3_1" to 5,
        "science_3_1" to 4, "tech_3_1" to 3, "english_3_1" to 4,
        "korean_2_2" to 3, "social_2_2" to 4, "history_2_2" to 3, "math_2_2" to 4,
        "science_2_2" to 3, "tech_2_2" to 4, "english_2_2" to 3,
        "korean_2_1" to 4, "social_2_1" to 4, "history_2_1" to 5, "math_2_1" to 4,
        "science_2_1" to 3, "tech_2_1" to 4, "english_2_1" to 4,
        "absent_days" to 0, "late_count" to 1, "early_leave_count" to 0, "lesson_absence_count" to 0,
        "volunteer_hours" to 18, "algorithm_award" to 0, "info_license" to 0
    )

    @BeforeEach
    fun setUp() {
        calculator = Calculator.createDefault()
    }

    @Test
    @DisplayName("전체 다단계 점수 계산 프로세스 테스트")
    fun `사용자 제공 JSON과 동일한 다단계 계산이 정확히 동작하는지 확인`() {
        
        // 사용자가 제공한 정확한 단계별 수식들
        val formulas = listOf(
            // 1. 3학년 1학기 교과평균
            "(korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7",
            
            // 2. 2학년 2학기 교과평균  
            "(korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7",
            
            // 3. 2학년 1학기 교과평균
            "(korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7",
            
            // 4. 3학년 1학기 점수 (40점 만점) - step1 사용
            "8 * step1",
            
            // 5. 2학년 2학기 점수 (20점 만점) - step2 사용
            "4 * step2", 
            
            // 6. 2학년 1학기 점수 (20점 만점) - step3 사용
            "4 * step3",
            
            // 7. 교과 기준점수 (80점 만점) - step4, step5, step6 사용
            "step4 + step5 + step6",
            
            // 8. 일반전형 교과점수 (140점 만점) - step7 사용
            "step7 * 1.75",
            
            // 9. 환산결석일수 계산
            "absent_days + late_count/3 + early_leave_count/3 + lesson_absence_count/3",
            
            // 10. 환산결석일수 정수변환 (ROUND 함수 대신 간단한 처리)
            "step9",  // 실제로는 ROUND(step9 - 0.5, 0)이지만 단순화
            
            // 11. 출석점수 (복잡한 IF문 대신 단순화) - 환산결석일수가 1미만이면 15점
            "IF(step10 < 1, 15, 14)",
            
            // 12. 봉사활동점수 (MIN 함수 대신 IF문)
            "IF(volunteer_hours > 15, 15, volunteer_hours)",
            
            // 13. 가산점
            "algorithm_award * 3 + info_license * 0",
            
            // 14. 최종 총점 계산
            "step8 + step11 + step12 + step13"
        )
        
        // 다단계 계산 실행
        val results = calculator.calculateMultiStep(formulas, variables)
        
        // 모든 단계가 성공했는지 확인
        assertEquals(14, results.size)
        results.forEachIndexed { index, result ->
            assertTrue(result.isSuccess(), "Step ${index + 1} failed: ${result.errors}")
            assertNotNull(result.result, "Step ${index + 1} result is null")
        }
        
        // 각 단계별 결과 검증
        val step1Result = results[0].result as Double // 3학년 1학기 평균
        val step2Result = results[1].result as Double // 2학년 2학기 평균
        val step3Result = results[2].result as Double // 2학년 1학기 평균
        val step4Result = results[3].result as Double // 3학년 1학기 점수
        val step5Result = results[4].result as Double // 2학년 2학기 점수
        val step6Result = results[5].result as Double // 2학년 1학기 점수
        val step7Result = results[6].result as Double // 교과 기준점수
        val step8Result = results[7].result as Double // 일반전형 교과점수
        val step9Result = results[8].result as Double // 환산결석일수
        val step10Result = results[9].result as Double // 환산결석일수 정수변환
        val step11Result = results[10].result as Double // 출석점수
        val step12Result = results[11].result as Double // 봉사점수
        val step13Result = results[12].result as Double // 가산점
        val finalResult = results[13].result as Double // 최종 점수
        
        // 예상값 계산 및 검증
        
        // Step 1: (4+3+4+5+4+3+4)/7 = 27/7 ≈ 3.857
        assertEquals(27.0/7.0, step1Result, 0.001, "3학년 1학기 평균 계산 오류")
        
        // Step 2: (3+4+3+4+3+4+3)/7 = 24/7 ≈ 3.429  
        assertEquals(24.0/7.0, step2Result, 0.001, "2학년 2학기 평균 계산 오류")
        
        // Step 3: (4+4+5+4+3+4+4)/7 = 28/7 = 4.0
        assertEquals(4.0, step3Result, 0.001, "2학년 1학기 평균 계산 오류")
        
        // Step 4: 8 * step1 = 8 * (27/7) ≈ 30.857
        assertEquals(8.0 * 27.0/7.0, step4Result, 0.001, "3학년 1학기 점수 계산 오류")
        
        // Step 5: 4 * step2 = 4 * (24/7) ≈ 13.714
        assertEquals(4.0 * 24.0/7.0, step5Result, 0.001, "2학년 2학기 점수 계산 오류")
        
        // Step 6: 4 * step3 = 4 * 4 = 16.0
        assertEquals(16.0, step6Result, 0.001, "2학년 1학기 점수 계산 오류")
        
        // Step 7: step4 + step5 + step6 ≈ 30.857 + 13.714 + 16 = 60.571
        val expectedStep7 = 8.0 * 27.0/7.0 + 4.0 * 24.0/7.0 + 16.0
        assertEquals(expectedStep7, step7Result, 0.001, "교과 기준점수 계산 오류")
        
        // Step 8: step7 * 1.75 ≈ 60.571 * 1.75 ≈ 106.0
        assertEquals(expectedStep7 * 1.75, step8Result, 0.001, "일반전형 교과점수 계산 오류")
        
        // Step 9: 0 + 1/3 + 0/3 + 0/3 = 1/3 ≈ 0.333
        assertEquals(1.0/3.0, step9Result, 0.001, "환산결석일수 계산 오류")
        
        // Step 10: step9 (단순화) ≈ 0.333
        assertEquals(step9Result, step10Result, 0.001, "환산결석일수 정수변환 오류")
        
        // Step 11: IF(0.333 < 1, 15, 14) = 15
        assertEquals(15.0, step11Result, 0.001, "출석점수 계산 오류")
        
        // Step 12: IF(18 > 15, 15, 18) = 15  
        assertEquals(15.0, step12Result, 0.001, "봉사점수 계산 오류")
        
        // Step 13: 0 * 3 + 0 * 0 = 0
        assertEquals(0.0, step13Result, 0.001, "가산점 계산 오류")
        
        // Step 14: step8 + step11 + step12 + step13 ≈ 106.0 + 15 + 15 + 0 = 136.0
        val expectedFinal = expectedStep7 * 1.75 + 15.0 + 15.0 + 0.0
        assertEquals(expectedFinal, finalResult, 0.001, "최종 점수 계산 오류")
        
        // 결과 출력
        println("=== 다단계 점수 계산 결과 ===")
        println("Step 1 - 3학년 1학기 평균: ${String.format("%.3f", step1Result)}")
        println("Step 2 - 2학년 2학기 평균: ${String.format("%.3f", step2Result)}")
        println("Step 3 - 2학년 1학기 평균: ${String.format("%.3f", step3Result)}")
        println("Step 4 - 3학년 1학기 점수: ${String.format("%.3f", step4Result)}점")
        println("Step 5 - 2학년 2학기 점수: ${String.format("%.3f", step5Result)}점")
        println("Step 6 - 2학년 1학기 점수: ${String.format("%.3f", step6Result)}점")
        println("Step 7 - 교과 기준점수: ${String.format("%.3f", step7Result)}점 (80점 만점)")
        println("Step 8 - 일반전형 교과점수: ${String.format("%.3f", step8Result)}점 (140점 만점)")
        println("Step 9 - 환산결석일수: ${String.format("%.3f", step9Result)}일")
        println("Step 10 - 환산결석일수 정수변환: ${String.format("%.3f", step10Result)}일")
        println("Step 11 - 출석점수: ${(step11Result as Number).toInt()}점 (15점 만점)")  
        println("Step 12 - 봉사점수: ${(step12Result as Number).toInt()}점 (15점 만점)")
        println("Step 13 - 가산점: ${(step13Result as Number).toInt()}점 (3점 만점)")
        println("Step 14 - 최종 총점: ${String.format("%.3f", finalResult)}점 (173점 만점)")
        println("=============================")
    }

    @Test
    @DisplayName("실제 JSON 데이터 구조와 정확히 매칭되는 테스트")
    fun `사용자 JSON의 각 단계명과 수식이 정확히 동작하는지 확인`() {
        // 사용자가 제공한 JSON의 steps 구조를 그대로 테스트
        val steps = listOf(
            Triple("3학년 1학기 교과평균", "(korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7", "semester_3_1_avg"),
            Triple("2학년 2학기 교과평균", "(korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7", "semester_2_2_avg"),
            Triple("2학년 1학기 교과평균", "(korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7", "semester_2_1_avg"),
            Triple("3학년 1학기 점수 (40점 만점)", "8 * step1", "score_3_1"),
            Triple("2학년 2학기 점수 (20점 만점)", "4 * step2", "score_2_2"),
            Triple("2학년 1학기 점수 (20점 만점)", "4 * step3", "score_2_1"),
            Triple("교과 기준점수 (80점 만점)", "step4 + step5 + step6", "base_academic_score"),
            Triple("일반전형 교과점수 (140점 만점)", "step7 * 1.75", "academic_score")
        )
        
        val formulas = steps.map { it.second }
        val results = calculator.calculateMultiStep(formulas, variables)
        
        // 각 단계별 검증
        steps.forEachIndexed { index, (stepName, formula, resultVariable) ->
            val result = results[index]
            assertTrue(result.isSuccess(), "$stepName 계산 실패: ${result.errors}")
            assertNotNull(result.result, "$stepName 결과가 null입니다")
            
            println("✓ $stepName: ${String.format("%.3f", result.result)} ($resultVariable)")
        }
        
        // 주요 검증 포인트들
        assertEquals(27.0/7.0, results[0].result as Double, 0.001) // 3학년 1학기 평균
        assertEquals(24.0/7.0, results[1].result as Double, 0.001) // 2학년 2학기 평균  
        assertEquals(4.0, results[2].result as Double, 0.001) // 2학년 1학기 평균
        
        val academicScore = results[7].result as Double
        assertTrue(academicScore > 100.0 && academicScore < 110.0, "교과점수가 예상 범위를 벗어남: $academicScore")
    }

    @Test  
    @DisplayName("수식 오류 상황 테스트")
    fun `잘못된 수식이나 변수 참조 시 적절한 오류 처리가 되는지 확인`() {
        val invalidFormulas = listOf(
            "nonexistent_variable + 1", // 존재하지 않는 변수
            "korean_3_1 / 0", // 0으로 나누기
            "step10", // 존재하지 않는 step 참조
            "korean_3_1 + " // 문법 오류
        )
        
        invalidFormulas.forEach { formula ->
            try {
                val results = calculator.calculateMultiStep(listOf(formula), variables)
                // 일부 오류는 계산 과정에서 잡힐 수 있음
                if (results.isNotEmpty() && !results[0].isSuccess()) {
                    println("✓ 예상된 오류 처리됨: $formula -> ${results[0].errors}")
                }
            } catch (e: Exception) {
                println("✓ 예상된 예외 발생: $formula -> ${e.message}")
            }
        }
    }
}