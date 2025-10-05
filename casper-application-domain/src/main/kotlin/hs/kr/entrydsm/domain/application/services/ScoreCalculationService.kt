package hs.kr.entrydsm.domain.application.services

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 대덕소프트웨어마이스터고 입학전형 점수 계산 서비스
 * 
 * 2026학년도 입학전형 기준에 따라 교과성적, 출석점수, 봉사활동점수, 가산점을 계산합니다.
 */
class ScoreCalculationService {

    private val calculator = Calculator.createDefault()

    /**
     * 전형별 총점을 계산합니다.
     */
    fun calculateTotalScore(application: Application): BigDecimal {
        val subjectScore = calculateSubjectScore(application)
        val attendanceScore = calculateAttendanceScore(application)
        val volunteerScore = calculateVolunteerScore(application)
        val bonusScore = calculateBonusScore(application)

        return subjectScore.add(attendanceScore).add(volunteerScore).add(bonusScore)
    }

    /**
     * 교과 성적을 계산합니다 (전형별로 다름)
     */
    fun calculateSubjectScore(application: Application): BigDecimal {
        val baseScore = when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> calculateGraduatedSubjectScore(application)
            EducationalStatus.PROSPECTIVE_GRADUATE -> calculateProspectiveGraduatedSubjectScore(application)
            EducationalStatus.QUALIFICATION_EXAM -> calculateGedSubjectScore(application)
        }

        // 전형별 환산 비율 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> baseScore.multiply(BigDecimal("1.75")) // 140점 (80점 × 175%)
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> baseScore // 80점 (80점 × 100%)
        }
    }

    /**
     * 졸업자 교과 성적 계산 (최근 4개 학기)
     */
    private fun calculateGraduatedSubjectScore(application: Application): BigDecimal {
        val formula = """
            학기1_평점 = (국어_3_2 + 사회_3_2 + 역사_3_2 + 수학_3_2 + 과학_3_2 + 기술가정_3_2 + 영어_3_2) / 7
            학기2_평점 = (국어_3_1 + 사회_3_1 + 역사_3_1 + 수학_3_1 + 과학_3_1 + 기술가정_3_1 + 영어_3_1) / 7
            학기3_평점 = (국어_2_2 + 사회_2_2 + 역사_2_2 + 수학_2_2 + 과학_2_2 + 기술가정_2_2 + 영어_2_2) / 7
            학기4_평점 = (국어_2_1 + 사회_2_1 + 역사_2_1 + 수학_2_1 + 과학_2_1 + 기술가정_2_1 + 영어_2_1) / 7
            
            학기1_점수 = 4 * 학기1_평점
            학기2_점수 = 4 * 학기2_평점
            학기3_점수 = 4 * 학기3_평점
            학기4_점수 = 4 * 학기4_평점
            
            교과성적 = 학기1_점수 + 학기2_점수 + 학기3_점수 + 학기4_점수
        """

        val variables = mapOf(
            "국어_3_2" to (application.korean_3_2 ?: 0),
            "사회_3_2" to (application.social_3_2 ?: 0),
            "역사_3_2" to (application.history_3_2 ?: 0),
            "수학_3_2" to (application.math_3_2 ?: 0),
            "과학_3_2" to (application.science_3_2 ?: 0),
            "기술가정_3_2" to (application.tech_3_2 ?: 0),
            "영어_3_2" to (application.english_3_2 ?: 0),
            
            "국어_3_1" to (application.korean_3_1 ?: 0),
            "사회_3_1" to (application.social_3_1 ?: 0),
            "역사_3_1" to (application.history_3_1 ?: 0),
            "수학_3_1" to (application.math_3_1 ?: 0),
            "과학_3_1" to (application.science_3_1 ?: 0),
            "기술가정_3_1" to (application.tech_3_1 ?: 0),
            "영어_3_1" to (application.english_3_1 ?: 0),
            
            "국어_2_2" to (application.korean_2_2 ?: 0),
            "사회_2_2" to (application.social_2_2 ?: 0),
            "역사_2_2" to (application.history_2_2 ?: 0),
            "수학_2_2" to (application.math_2_2 ?: 0),
            "과학_2_2" to (application.science_2_2 ?: 0),
            "기술가정_2_2" to (application.tech_2_2 ?: 0),
            "영어_2_2" to (application.english_2_2 ?: 0),
            
            "국어_2_1" to (application.korean_2_1 ?: 0),
            "사회_2_1" to (application.social_2_1 ?: 0),
            "역사_2_1" to (application.history_2_1 ?: 0),
            "수학_2_1" to (application.math_2_1 ?: 0),
            "과학_2_1" to (application.science_2_1 ?: 0),
            "기술가정_2_1" to (application.tech_2_1 ?: 0),
            "영어_2_1" to (application.english_2_1 ?: 0)
        )

        val result = calculator.calculate(CalculationRequest(formula, variables))
        return BigDecimal.valueOf(result.asDouble() ?: 0.0).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 졸업예정자 교과 성적 계산 (최근 3개 학기)
     */
    private fun calculateProspectiveGraduatedSubjectScore(application: Application): BigDecimal {
        val formula = """
            학기1_평점 = (국어_3_1 + 사회_3_1 + 역사_3_1 + 수학_3_1 + 과학_3_1 + 기술가정_3_1 + 영어_3_1) / 7
            학기2_평점 = (국어_2_2 + 사회_2_2 + 역사_2_2 + 수학_2_2 + 과학_2_2 + 기술가정_2_2 + 영어_2_2) / 7
            학기3_평점 = (국어_2_1 + 사회_2_1 + 역사_2_1 + 수학_2_1 + 과학_2_1 + 기술가정_2_1 + 영어_2_1) / 7
            
            학기1_점수 = 8 * 학기1_평점
            학기2_점수 = 4 * 학기2_평점
            학기3_점수 = 4 * 학기3_평점
            
            교과성적 = 학기1_점수 + 학기2_점수 + 학기3_점수
        """

        val variables = mapOf(
            "국어_3_1" to (application.korean_3_1 ?: 0),
            "사회_3_1" to (application.social_3_1 ?: 0),
            "역사_3_1" to (application.history_3_1 ?: 0),
            "수학_3_1" to (application.math_3_1 ?: 0),
            "과학_3_1" to (application.science_3_1 ?: 0),
            "기술가정_3_1" to (application.tech_3_1 ?: 0),
            "영어_3_1" to (application.english_3_1 ?: 0),
            
            "국어_2_2" to (application.korean_2_2 ?: 0),
            "사회_2_2" to (application.social_2_2 ?: 0),
            "역사_2_2" to (application.history_2_2 ?: 0),
            "수학_2_2" to (application.math_2_2 ?: 0),
            "과학_2_2" to (application.science_2_2 ?: 0),
            "기술가정_2_2" to (application.tech_2_2 ?: 0),
            "영어_2_2" to (application.english_2_2 ?: 0),
            
            "국어_2_1" to (application.korean_2_1 ?: 0),
            "사회_2_1" to (application.social_2_1 ?: 0),
            "역사_2_1" to (application.history_2_1 ?: 0),
            "수학_2_1" to (application.math_2_1 ?: 0),
            "과학_2_1" to (application.science_2_1 ?: 0),
            "기술가정_2_1" to (application.tech_2_1 ?: 0),
            "영어_2_1" to (application.english_2_1 ?: 0)
        )

        val result = calculator.calculate(CalculationRequest(formula, variables))
        return BigDecimal.valueOf(result.asDouble() ?: 0.0).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 검정고시 교과 성적 계산
     */
    private fun calculateGedSubjectScore(application: Application): BigDecimal {
        val formula = """
            평점 = (국어 + 사회 + 역사 + 수학 + 과학 + 기술가정 + 영어) / 7
            교과성적 = 16 * 평점
        """

        val variables = mapOf(
            "국어" to (application.gedKorean ?: 0),
            "사회" to (application.gedSocial ?: 0),
            "역사" to (application.gedHistory ?: 0),
            "수학" to (application.gedMath ?: 0),
            "과학" to (application.gedScience ?: 0),
            "기술가정" to (application.gedTech ?: 0),
            "영어" to (application.gedEnglish ?: 0)
        )

        val result = calculator.calculate(CalculationRequest(formula, variables))
        return BigDecimal.valueOf(result.asDouble() ?: 0.0).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 출석 점수 계산 (15점 만점)
     */
    fun calculateAttendanceScore(application: Application): BigDecimal {
        val formula = """
            환산결석 = 결석일수 + (지각횟수/3) + (조퇴횟수/3) + (결과횟수/3)
            출석점수 = max(0, min(15, 15 - 환산결석))
        """

        val variables = mapOf(
            "결석일수" to (application.absence ?: 0),
            "지각횟수" to (application.tardiness ?: 0),
            "조퇴횟수" to (application.earlyLeave ?: 0),
            "결과횟수" to (application.classExit ?: 0)
        )

        val result = calculator.calculate(CalculationRequest(formula, variables))
        return BigDecimal.valueOf(result.asDouble() ?: 0.0).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 봉사활동 점수 계산 (15점 만점)
     */
    fun calculateVolunteerScore(application: Application): BigDecimal {
        val formula = """
            봉사점수 = min(15, max(0, 봉사시간))
        """

        val variables = mapOf(
            "봉사시간" to (application.volunteer ?: 0)
        )

        val result = calculator.calculate(CalculationRequest(formula, variables))
        return BigDecimal.valueOf(result.asDouble() ?: 0.0).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 가산점 계산
     */
    fun calculateBonusScore(application: Application): BigDecimal {
        val formula = when (application.applicationType) {
            ApplicationType.COMMON -> """
                알고리즘대회점수 = 알고리즘수상 * 3
                가산점 = 알고리즘대회점수
            """
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> """
                알고리즘대회점수 = 알고리즘수상 * 3
                정보처리기능사점수 = 정보처리기능사 * 6
                가산점 = 알고리즘대회점수 + 정보처리기능사점수
            """
        }

        val variables = mapOf(
            "알고리즘수상" to if (application.algorithmAward == true) 1 else 0,
            "정보처리기능사" to if (application.infoProcessingCert == true) 1 else 0
        )

        val result = calculator.calculate(CalculationRequest(formula, variables))
        return BigDecimal.valueOf(result.asDouble() ?: 0.0).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 전형별 최대 점수 반환
     */
    fun getMaxScore(applicationType: ApplicationType): BigDecimal {
        return when (applicationType) {
            ApplicationType.COMMON -> BigDecimal("173") // 140 + 15 + 15 + 3
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> BigDecimal("119") // 80 + 15 + 15 + 9
        }
    }

    /**
     * 점수 상세 정보 반환
     */
    fun getScoreDetails(application: Application): Map<String, BigDecimal> {
        return mapOf(
            "교과성적" to calculateSubjectScore(application),
            "출석점수" to calculateAttendanceScore(application),
            "봉사활동점수" to calculateVolunteerScore(application),
            "가산점" to calculateBonusScore(application),
            "총점" to calculateTotalScore(application),
            "최대점수" to getMaxScore(application.applicationType)
        )
    }
}