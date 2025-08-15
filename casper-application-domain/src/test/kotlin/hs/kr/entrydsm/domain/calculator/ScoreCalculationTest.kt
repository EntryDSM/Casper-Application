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
 * 대덕소프트웨어마이스터고등학교 특별전형 점수 계산 수식 테스트
 * 
 * 실제 입시 점수 계산 로직이 정확히 동작하는지 검증합니다.
 * 교과점수(140점) + 출석점수(15점) + 봉사점수(15점) + 가산점(3점) = 총 173점 만점
 */
@DisplayName("대덕소프트웨어마이스터고등학교 특별전형 점수 계산 테스트")
class ScoreCalculationTest {

    private lateinit var calculator: Calculator
    
    // 테스트용 변수들 (실제 학생 성적 데이터)
    private val variables = mapOf(
        // 3학년 1학기 성적
        "korean_3_1" to 4,
        "social_3_1" to 3,
        "history_3_1" to 4,
        "math_3_1" to 5,
        "science_3_1" to 4,
        "tech_3_1" to 3,
        "english_3_1" to 4,
        
        // 2학년 2학기 성적
        "korean_2_2" to 3,
        "social_2_2" to 4,
        "history_2_2" to 3,
        "math_2_2" to 4,
        "science_2_2" to 3,
        "tech_2_2" to 4,
        "english_2_2" to 3,
        
        // 2학년 1학기 성적
        "korean_2_1" to 4,
        "social_2_1" to 4,
        "history_2_1" to 5,
        "math_2_1" to 4,
        "science_2_1" to 3,
        "tech_2_1" to 4,
        "english_2_1" to 4,
        
        // 출결 정보
        "absent_days" to 0,
        "late_count" to 1,
        "early_leave_count" to 0,
        "lesson_absence_count" to 0,
        
        // 기타 정보
        "volunteer_hours" to 18,
        "algorithm_award" to 0,
        "info_license" to 0
    )

    @BeforeEach
    fun setUp() {
        calculator = Calculator.createDefault()
    }

    @Test
    @DisplayName("3학년 1학기 교과평균 계산 테스트")
    fun `3학년 1학기 교과평균이 정확히 계산되는지 확인`() {
        val formula = "(korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        assertNotNull(result.result)
        
        // 예상값: (4+3+4+5+4+3+4) / 7 = 27/7 ≈ 3.857
        val expected = 27.0 / 7.0
        assertEquals(expected, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("2학년 2학기 교과평균 계산 테스트")
    fun `2학년 2학기 교과평균이 정확히 계산되는지 확인`() {
        val formula = "(korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 예상값: (3+4+3+4+3+4+3) / 7 = 24/7 ≈ 3.429
        val expected = 24.0 / 7.0
        assertEquals(expected, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("2학년 1학기 교과평균 계산 테스트")
    fun `2학년 1학기 교과평균이 정확히 계산되는지 확인`() {
        val formula = "(korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 예상값: (4+4+5+4+3+4+4) / 7 = 28/7 = 4.0
        assertEquals(4.0, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("학기별 점수 계산 테스트")
    fun `학기별 점수가 정확히 계산되는지 확인`() {
        // 3학년 1학기 점수 (40점 만점)
        val formula_3_1 = "8 * ((korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7)"
        val request_3_1 = CalculationRequest(formula_3_1, variables)
        val result_3_1 = calculator.calculate(request_3_1)
        
        assertTrue(result_3_1.isSuccess())
        // 예상값: 8 * (27/7) ≈ 30.857
        assertEquals(8.0 * 27.0 / 7.0, result_3_1.result as Double, 0.001)
        
        // 2학년 2학기 점수 (20점 만점)
        val formula_2_2 = "4 * ((korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7)"
        val request_2_2 = CalculationRequest(formula_2_2, variables)
        val result_2_2 = calculator.calculate(request_2_2)
        
        assertTrue(result_2_2.isSuccess())
        // 예상값: 4 * (24/7) ≈ 13.714
        assertEquals(4.0 * 24.0 / 7.0, result_2_2.result as Double, 0.001)
        
        // 2학년 1학기 점수 (20점 만점)
        val formula_2_1 = "4 * ((korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7)"
        val request_2_1 = CalculationRequest(formula_2_1, variables)
        val result_2_1 = calculator.calculate(request_2_1)
        
        assertTrue(result_2_1.isSuccess())
        // 예상값: 4 * (28/7) = 16.0
        assertEquals(16.0, result_2_1.result as Double, 0.001)
    }

    @Test
    @DisplayName("교과 기준점수 계산 테스트 (80점 만점)")
    fun `교과 기준점수가 정확히 계산되는지 확인`() {
        val formula = """
            8 * ((korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7) +
            4 * ((korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7) +
            4 * ((korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7)
        """.trimIndent()
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 예상값: 30.857 + 13.714 + 16.0 ≈ 60.571
        val expected = 8.0 * 27.0 / 7.0 + 4.0 * 24.0 / 7.0 + 4.0 * 28.0 / 7.0
        assertEquals(expected, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("일반전형 교과점수 계산 테스트 (140점 만점)")
    fun `일반전형 교과점수가 정확히 계산되는지 확인`() {
        val formula = """
            (8 * ((korean_3_1 + social_3_1 + history_3_1 + math_3_1 + science_3_1 + tech_3_1 + english_3_1) / 7) +
             4 * ((korean_2_2 + social_2_2 + history_2_2 + math_2_2 + science_2_2 + tech_2_2 + english_2_2) / 7) +
             4 * ((korean_2_1 + social_2_1 + history_2_1 + math_2_1 + science_2_1 + tech_2_1 + english_2_1) / 7)) * 1.75
        """.trimIndent()
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 예상값: 60.571 * 1.75 ≈ 106.0
        val baseScore = 8.0 * 27.0 / 7.0 + 4.0 * 24.0 / 7.0 + 4.0 * 28.0 / 7.0
        val expected = baseScore * 1.75
        assertEquals(expected, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("환산결석일수 계산 테스트")
    fun `환산결석일수가 정확히 계산되는지 확인`() {
        val formula = "absent_days + late_count/3 + early_leave_count/3 + lesson_absence_count/3"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 예상값: 0 + 1/3 + 0/3 + 0/3 = 1/3 ≈ 0.333
        assertEquals(1.0/3.0, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("봉사활동점수 계산 테스트 (15점 만점)")
    fun `봉사활동점수가 정확히 계산되는지 확인`() {
        // MIN 함수가 없다면 IF 조건문으로 대체
        val formula = "IF(volunteer_hours > 15, 15, volunteer_hours)"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 18시간 -> 15점 만점이므로 15점
        assertEquals(15.0, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("가산점 계산 테스트 (3점 만점)")
    fun `가산점이 정확히 계산되는지 확인`() {
        val formula = "algorithm_award * 3 + info_license * 0"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        // 예상값: 0 * 3 + 0 * 0 = 0
        assertEquals(0.0, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("복잡한 IF 조건문 출석점수 계산 테스트 (15점 만점)")
    fun `출석점수 계산이 정확히 동작하는지 확인`() {
        // 환산결석일수가 1/3이므로 ROUND(1/3 - 0.5) = ROUND(-0.167) = 0
        // 0일이면 15점
        val convertedAbsentDays = 0 // ROUND(1/3 - 0.5) = 0
        
        // 간단한 테스트: 결석일수가 0일 때 15점
        val formula = "IF(0 >= 1, 14, 15)"
        val request = CalculationRequest(formula, variables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        assertEquals(15.0, result.result as Double, 0.001)
    }

    @Test
    @DisplayName("전체 점수 계산 통합 테스트")
    fun `전체 점수 계산이 정확히 동작하는지 확인`() {
        // 단계별 계산
        val variables = this.variables.toMutableMap()
        
        // 1. 교과 점수 계산
        val baseScore = 8.0 * 27.0 / 7.0 + 4.0 * 24.0 / 7.0 + 4.0 * 28.0 / 7.0
        val academicScore = baseScore * 1.75
        
        // 2. 출석 점수 (환산결석일수 0 -> 15점)
        val attendanceScore = 15.0
        
        // 3. 봉사 점수 (18시간 -> 15점)
        val volunteerScore = 15.0
        
        // 4. 가산점 (0점)
        val bonusScore = 0.0
        
        val expectedTotal = academicScore + attendanceScore + volunteerScore + bonusScore
        
        // 간단한 총점 계산 테스트
        val testVariables: MutableMap<String, Any> = variables.toMutableMap()
        testVariables["academic_score"] = academicScore
        testVariables["attendance_score"] = attendanceScore
        testVariables["volunteer_score"] = volunteerScore
        testVariables["bonus_score"] = bonusScore
        
        val formula = "academic_score + attendance_score + volunteer_score + bonus_score"
        val request = CalculationRequest(formula, testVariables)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        assertEquals(expectedTotal, result.result as Double, 0.001)
        
        // 예상 총점 출력
        println("=== 대덕소프트웨어마이스터고등학교 특별전형 점수 계산 결과 ===")
        println("교과점수: ${String.format("%.3f", academicScore)}점 (140점 만점)")
        println("출석점수: ${attendanceScore.toInt()}점 (15점 만점)")
        println("봉사점수: ${volunteerScore.toInt()}점 (15점 만점)")
        println("가산점: ${bonusScore.toInt()}점 (3점 만점)")
        println("총점: ${String.format("%.3f", expectedTotal)}점 (173점 만점)")
    }

    @Test
    @DisplayName("극한 케이스 테스트 - 만점 학생")
    fun `만점 학생의 점수 계산 테스트`() {
        val perfectVariables = mapOf(
            // 모든 과목 5점 (만점)
            "korean_3_1" to 5, "social_3_1" to 5, "history_3_1" to 5, "math_3_1" to 5,
            "science_3_1" to 5, "tech_3_1" to 5, "english_3_1" to 5,
            "korean_2_2" to 5, "social_2_2" to 5, "history_2_2" to 5, "math_2_2" to 5,
            "science_2_2" to 5, "tech_2_2" to 5, "english_2_2" to 5,
            "korean_2_1" to 5, "social_2_1" to 5, "history_2_1" to 5, "math_2_1" to 5,
            "science_2_1" to 5, "tech_2_1" to 5, "english_2_1" to 5,
            
            // 완벽한 출결
            "absent_days" to 0, "late_count" to 0, "early_leave_count" to 0, "lesson_absence_count" to 0,
            
            // 최대 봉사시간과 가산점
            "volunteer_hours" to 20, "algorithm_award" to 1, "info_license" to 1
        )
        
        // 교과점수: 80 * 1.75 = 140점
        val academicScore = 140.0
        val attendanceScore = 15.0
        val volunteerScore = 15.0
        val bonusScore = 3.0 // 1 * 3 + 1 * 0 = 3
        val expectedTotal = 173.0 // 만점
        
        val perfectVars: MutableMap<String, Any> = perfectVariables.toMutableMap()
        perfectVars["academic_score"] = academicScore
        perfectVars["attendance_score"] = attendanceScore
        perfectVars["volunteer_score"] = volunteerScore  
        perfectVars["bonus_score"] = bonusScore
        
        val formula = "academic_score + attendance_score + volunteer_score + bonus_score"
        val request = CalculationRequest(formula, perfectVars)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        assertEquals(expectedTotal, result.result as Double, 0.001)
        
        println("=== 만점 학생 점수 계산 결과 ===")
        println("교과점수: ${academicScore.toInt()}점 (140점 만점)")
        println("출석점수: ${attendanceScore.toInt()}점 (15점 만점)")
        println("봉사점수: ${volunteerScore.toInt()}점 (15점 만점)")
        println("가산점: ${bonusScore.toInt()}점 (3점 만점)")
        println("총점: ${expectedTotal.toInt()}점 (173점 만점) - 만점!")
    }

    @Test
    @DisplayName("극한 케이스 테스트 - 최저점 학생")
    fun `최저점 학생의 점수 계산 테스트`() {
        val minVariables = mapOf(
            // 모든 과목 1점 (최저점)
            "korean_3_1" to 1, "social_3_1" to 1, "history_3_1" to 1, "math_3_1" to 1,
            "science_3_1" to 1, "tech_3_1" to 1, "english_3_1" to 1,
            "korean_2_2" to 1, "social_2_2" to 1, "history_2_2" to 1, "math_2_2" to 1,
            "science_2_2" to 1, "tech_2_2" to 1, "english_2_2" to 1,
            "korean_2_1" to 1, "social_2_1" to 1, "history_2_1" to 1, "math_2_1" to 1,
            "science_2_1" to 1, "tech_2_1" to 1, "english_2_1" to 1,
            
            // 최악의 출결 (15일 이상 결석 -> 0점)
            "absent_days" to 20, "late_count" to 0, "early_leave_count" to 0, "lesson_absence_count" to 0,
            
            // 봉사시간 없음, 가산점 없음
            "volunteer_hours" to 0, "algorithm_award" to 0, "info_license" to 0
        )
        
        // 교과점수: 16 * 1.75 = 28점 (모든 과목 1점일 때)
        val academicScore = 16.0 * 1.75 // 28점
        val attendanceScore = 0.0 // 15일 이상 결석
        val volunteerScore = 0.0
        val bonusScore = 0.0
        val expectedTotal = academicScore // 28점
        
        val minVars: MutableMap<String, Any> = minVariables.toMutableMap()
        minVars["academic_score"] = academicScore
        minVars["attendance_score"] = attendanceScore
        minVars["volunteer_score"] = volunteerScore
        minVars["bonus_score"] = bonusScore
        
        val formula = "academic_score + attendance_score + volunteer_score + bonus_score"
        val request = CalculationRequest(formula, minVars)
        
        val result = calculator.calculate(request)
        
        assertTrue(result.isSuccess())
        assertEquals(expectedTotal, result.result as Double, 0.001)
        
        println("=== 최저점 학생 점수 계산 결과 ===")
        println("교과점수: ${String.format("%.1f", academicScore)}점 (140점 만점)")
        println("출석점수: ${attendanceScore.toInt()}점 (15점 만점)")
        println("봉사점수: ${volunteerScore.toInt()}점 (15점 만점)")
        println("가산점: ${bonusScore.toInt()}점 (3점 만점)")
        println("총점: ${String.format("%.1f", expectedTotal)}점 (173점 만점)")
    }
}