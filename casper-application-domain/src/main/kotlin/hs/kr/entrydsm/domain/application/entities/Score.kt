package hs.kr.entrydsm.domain.application.entities

import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.EntityBase
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 성적 엔티티 (equus-application 구조 기반)
 */
@Entity(aggregateRoot = Application::class, context = "application")
data class Score(
    private val receiptCode: ReceiptCode,
    
    // 기본 점수
    val attendanceScore: Int? = null,           // 출석점수
    val volunteerScore: BigDecimal? = null,     // 봉사점수
    
    // 졸업생 성적 - 기존 (3학년 2학기 기본)
    val koreanGrade: String? = null,            // 국어 (예: "AABX")
    val socialGrade: String? = null,            // 사회
    val historyGrade: String? = null,           // 역사  
    val mathGrade: String? = null,              // 수학
    val scienceGrade: String? = null,           // 과학
    val englishGrade: String? = null,           // 영어
    val techAndHomeGrade: String? = null,       // 기술가정
    
    // 학기별 과목별 성적 - 3학년 1학기
    val korean_3_1: String? = null,
    val social_3_1: String? = null,
    val history_3_1: String? = null,
    val math_3_1: String? = null,
    val science_3_1: String? = null,
    val english_3_1: String? = null,
    val techAndHome_3_1: String? = null,
    
    // 학기별 과목별 성적 - 2학년 2학기
    val korean_2_2: String? = null,
    val social_2_2: String? = null,
    val history_2_2: String? = null,
    val math_2_2: String? = null,
    val science_2_2: String? = null,
    val english_2_2: String? = null,
    val techAndHome_2_2: String? = null,
    
    // 학기별 과목별 성적 - 2학년 1학기
    val korean_2_1: String? = null,
    val social_2_1: String? = null,
    val history_2_1: String? = null,
    val math_2_1: String? = null,
    val science_2_1: String? = null,
    val english_2_1: String? = null,
    val techAndHome_2_1: String? = null,
    
    // 검정고시 성적 (0~100점)
    val qualificationKorean: BigDecimal? = null,    // 검정고시 국어
    val qualificationSocial: BigDecimal? = null,    // 검정고시 사회
    val qualificationMath: BigDecimal? = null,      // 검정고시 수학
    val qualificationScience: BigDecimal? = null,   // 검정고시 과학
    val qualificationEnglish: BigDecimal? = null,   // 검정고시 영어
    val qualificationOpt: BigDecimal? = null,       // 검정고시 선택과목
    
    // 학기별 성적 합계
    val thirdBeforeBeforeScore: BigDecimal? = null, // 2학년 1학기 점수
    val thirdBeforeScore: BigDecimal? = null,       // 2학년 2학기 점수  
    val thirdGradeScore: BigDecimal? = null,        // 3학년 1학기 점수
    val thirdScore: BigDecimal? = null,             // 3학년 2학기 점수
    
    // 계산된 점수들
    val totalGradeScore: BigDecimal,            // 교과 총점
    val extraScore: BigDecimal = BigDecimal.ZERO,  // 가산점
    val totalScore: BigDecimal                  // 최종 총점
) : EntityBase<ReceiptCode>() {

    val id: ReceiptCode
        @JvmName("getReceiptCodeForScore")
        get() = receiptCode

    init {
        attendanceScore?.let { 
            if (it < 0 || it > 30) {
                throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
            }
        }
        volunteerScore?.let { 
            if (it < BigDecimal.ZERO || it > BigDecimal(30)) {
                throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
            }
        }
        if (extraScore < BigDecimal.ZERO || extraScore > BigDecimal(10)) {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
        if (totalScore < BigDecimal.ZERO || totalScore > BigDecimal(360)) {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
    }

    override fun getId(): ReceiptCode = receiptCode
    override fun getType(): String = "Score"
    override fun isValid(): Boolean {
        return (attendanceScore == null || attendanceScore >= 0) &&
               (volunteerScore == null || volunteerScore >= BigDecimal.ZERO) &&
               totalGradeScore >= BigDecimal.ZERO &&
               extraScore >= BigDecimal.ZERO &&
               totalScore >= BigDecimal.ZERO
    }

    /**
     * 내신 성적 계산 (졸업/졸업예정자용)
     */
    fun calculateGradeScore(): BigDecimal {
        val scores = listOfNotNull(thirdGradeScore, thirdBeforeScore, thirdBeforeBeforeScore, thirdScore)
        return if (scores.isEmpty()) {
            BigDecimal.ZERO
        } else {
            val average = scores.reduce { acc, score -> acc + score }.divide(
                BigDecimal(scores.size), 
                2, 
                RoundingMode.HALF_UP
            )
            average
        }
    }

    /**
     * 총점 재계산
     */
    fun recalculateTotal(): Score {
        val attendanceValue = attendanceScore?.toBigDecimal() ?: BigDecimal.ZERO
        val volunteerValue = volunteerScore ?: BigDecimal.ZERO
        val newTotal = attendanceValue + volunteerValue + totalGradeScore + extraScore
        return copy(totalScore = newTotal)
    }

    /**
     * 가산점 적용
     */
    fun applyExtraScore(extraScore: BigDecimal): Score {
        if (extraScore < BigDecimal.ZERO || extraScore > BigDecimal(10)) {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
        return copy(extraScore = extraScore).recalculateTotal()
    }

    companion object {
        fun create(
            receiptCode: ReceiptCode,
            attendanceScore: Int?,
            volunteerScore: BigDecimal?,
            totalGradeScore: BigDecimal
        ): Score {
            val attendanceValue = attendanceScore?.toBigDecimal() ?: BigDecimal.ZERO
            val volunteerValue = volunteerScore ?: BigDecimal.ZERO
            val totalScore = attendanceValue + volunteerValue + totalGradeScore
            return Score(
                receiptCode = receiptCode,
                attendanceScore = attendanceScore,
                volunteerScore = volunteerScore,
                totalGradeScore = totalGradeScore,
                totalScore = totalScore
            )
        }

        /**
         * 검정고시용 성적 생성
         */
        fun createForQualificationExam(
            receiptCode: ReceiptCode,
            attendanceScore: Int?,
            volunteerScore: BigDecimal?,
            qualificationScore: BigDecimal
        ): Score {
            val attendanceValue = attendanceScore?.toBigDecimal() ?: BigDecimal.ZERO
            val volunteerValue = volunteerScore ?: BigDecimal.ZERO
            val totalScore = attendanceValue + volunteerValue + qualificationScore
            return Score(
                receiptCode = receiptCode,
                attendanceScore = attendanceScore,
                volunteerScore = volunteerScore,
                totalGradeScore = qualificationScore,
                totalScore = totalScore
            )
        }

        /**
         * 졸업/졸업예정자용 성적 생성
         */
        fun createForGraduation(
            receiptCode: ReceiptCode,
            attendanceScore: Int?,
            volunteerScore: BigDecimal?,
            // 학기별 과목별 성적
            korean_3_1: String? = null,
            social_3_1: String? = null,
            history_3_1: String? = null,
            math_3_1: String? = null,
            science_3_1: String? = null,
            english_3_1: String? = null,
            techAndHome_3_1: String? = null,
            koreanGrade: String? = null,
            socialGrade: String? = null,
            historyGrade: String? = null,
            mathGrade: String? = null,
            scienceGrade: String? = null,
            englishGrade: String? = null,
            techAndHomeGrade: String? = null,
            korean_2_2: String? = null,
            social_2_2: String? = null,
            history_2_2: String? = null,
            math_2_2: String? = null,
            science_2_2: String? = null,
            english_2_2: String? = null,
            techAndHome_2_2: String? = null,
            korean_2_1: String? = null,
            social_2_1: String? = null,
            history_2_1: String? = null,
            math_2_1: String? = null,
            science_2_1: String? = null,
            english_2_1: String? = null,
            techAndHome_2_1: String? = null,
            // 학기별 점수 (기존)
            thirdGradeScore: BigDecimal?,
            thirdBeforeScore: BigDecimal?,
            thirdBeforeBeforeScore: BigDecimal?,
            thirdScore: BigDecimal?
        ): Score {
            val scores = listOfNotNull(thirdGradeScore, thirdBeforeScore, thirdBeforeBeforeScore, thirdScore)
            val totalGradeScore = if (scores.isEmpty()) {
                BigDecimal.ZERO
            } else {
                scores.reduce { acc, score -> acc + score }.divide(
                    BigDecimal(scores.size), 
                    2, 
                    RoundingMode.HALF_UP
                )
            }
            
            val attendanceValue = attendanceScore?.toBigDecimal() ?: BigDecimal.ZERO
            val volunteerValue = volunteerScore ?: BigDecimal.ZERO
            val totalScore = attendanceValue + volunteerValue + totalGradeScore
            
            return Score(
                receiptCode = receiptCode,
                attendanceScore = attendanceScore,
                volunteerScore = volunteerScore,
                // 학기별 과목별 성적
                korean_3_1 = korean_3_1,
                social_3_1 = social_3_1,
                history_3_1 = history_3_1,
                math_3_1 = math_3_1,
                science_3_1 = science_3_1,
                english_3_1 = english_3_1,
                techAndHome_3_1 = techAndHome_3_1,
                koreanGrade = koreanGrade,
                socialGrade = socialGrade,
                historyGrade = historyGrade,
                mathGrade = mathGrade,
                scienceGrade = scienceGrade,
                englishGrade = englishGrade,
                techAndHomeGrade = techAndHomeGrade,
                korean_2_2 = korean_2_2,
                social_2_2 = social_2_2,
                history_2_2 = history_2_2,
                math_2_2 = math_2_2,
                science_2_2 = science_2_2,
                english_2_2 = english_2_2,
                techAndHome_2_2 = techAndHome_2_2,
                korean_2_1 = korean_2_1,
                social_2_1 = social_2_1,
                history_2_1 = history_2_1,
                math_2_1 = math_2_1,
                science_2_1 = science_2_1,
                english_2_1 = english_2_1,
                techAndHome_2_1 = techAndHome_2_1,
                // 학기별 점수
                thirdGradeScore = thirdGradeScore,
                thirdBeforeScore = thirdBeforeScore,
                thirdBeforeBeforeScore = thirdBeforeBeforeScore,
                thirdScore = thirdScore,
                totalGradeScore = totalGradeScore,
                totalScore = totalScore
            )
        }
    }
}