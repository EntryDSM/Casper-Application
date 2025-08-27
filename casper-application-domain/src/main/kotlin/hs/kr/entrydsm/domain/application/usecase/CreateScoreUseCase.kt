package hs.kr.entrydsm.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.math.BigDecimal
import java.util.UUID

/**
 * 성적 생성/수정 UseCase
 */
interface CreateScoreUseCase {
    fun execute(command: CreateScoreCommand): Score
}

data class CreateScoreCommand(
    val userId: UUID,
    val receiptCode: ReceiptCode? = null, // 통합 API에서 사용할 때 직접 전달
    // 기본 점수
    val attendanceScore: Int?,
    val volunteerScore: BigDecimal?,
    
    // 학기별 과목별 성적 - 3학년 1학기
    val korean_3_1: String? = null,
    val social_3_1: String? = null,
    val history_3_1: String? = null,
    val math_3_1: String? = null,
    val science_3_1: String? = null,
    val english_3_1: String? = null,
    val techAndHome_3_1: String? = null,
    
    // 학기별 과목별 성적 - 3학년 2학기 (기존 필드명 유지)
    val koreanGrade: String? = null,
    val socialGrade: String? = null,
    val historyGrade: String? = null,
    val mathGrade: String? = null,
    val scienceGrade: String? = null,
    val englishGrade: String? = null,
    val techAndHomeGrade: String? = null,
    
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
    
    // 검정고시용
    val qualificationScore: BigDecimal? = null,
    val qualificationKorean: BigDecimal? = null,
    val qualificationSocial: BigDecimal? = null,
    val qualificationMath: BigDecimal? = null,
    val qualificationScience: BigDecimal? = null,
    val qualificationEnglish: BigDecimal? = null,
    val qualificationOpt: BigDecimal? = null,
    
    // 졸업/졸업예정자용
    val thirdGradeScore: BigDecimal? = null,
    val thirdBeforeScore: BigDecimal? = null,
    val thirdBeforeBeforeScore: BigDecimal? = null,
    val thirdScore: BigDecimal? = null,
    
    // 가산점
    val extraScore: BigDecimal = BigDecimal.ZERO
)

class CreateScoreUseCaseImpl(
    private val applicationPort: ApplicationPort
) : CreateScoreUseCase {
    
    override fun execute(command: CreateScoreCommand): Score {
        // receiptCode가 직접 전달된 경우와 그렇지 않은 경우를 구분
        val (receiptCode, educationalStatus) = if (command.receiptCode != null) {
            // 통합 API에서 호출한 경우: receiptCode를 직접 사용하고, 별도로 educationalStatus 조회
            val application = applicationPort.queryApplicationByReceiptCode(command.receiptCode)
                ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
            command.receiptCode to application.educationalStatus
        } else {
            // 개별 API에서 호출한 경우: 기존 방식대로 userId로 조회
            val application = applicationPort.queryApplicationByUserId(command.userId)
                ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
            application.id to application.educationalStatus
        }
        
        // 학력에 따라 적절한 성적 생성
        val score = when (educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> {
                if (command.qualificationScore == null) {
                    throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
                }
                Score.createForQualificationExam(
                    receiptCode = receiptCode,
                    attendanceScore = command.attendanceScore,
                    volunteerScore = command.volunteerScore,
                    qualificationScore = command.qualificationScore
                )
            }
            
            EducationalStatus.GRADUATE, EducationalStatus.PROSPECTIVE_GRADUATE -> {
                Score.createForGraduation(
                    receiptCode = receiptCode,
                    attendanceScore = command.attendanceScore,
                    volunteerScore = command.volunteerScore,
                    // 학기별 과목별 성적
                    korean_3_1 = command.korean_3_1,
                    social_3_1 = command.social_3_1,
                    history_3_1 = command.history_3_1,
                    math_3_1 = command.math_3_1,
                    science_3_1 = command.science_3_1,
                    english_3_1 = command.english_3_1,
                    techAndHome_3_1 = command.techAndHome_3_1,
                    koreanGrade = command.koreanGrade,
                    socialGrade = command.socialGrade,
                    historyGrade = command.historyGrade,
                    mathGrade = command.mathGrade,
                    scienceGrade = command.scienceGrade,
                    englishGrade = command.englishGrade,
                    techAndHomeGrade = command.techAndHomeGrade,
                    korean_2_2 = command.korean_2_2,
                    social_2_2 = command.social_2_2,
                    history_2_2 = command.history_2_2,
                    math_2_2 = command.math_2_2,
                    science_2_2 = command.science_2_2,
                    english_2_2 = command.english_2_2,
                    techAndHome_2_2 = command.techAndHome_2_2,
                    korean_2_1 = command.korean_2_1,
                    social_2_1 = command.social_2_1,
                    history_2_1 = command.history_2_1,
                    math_2_1 = command.math_2_1,
                    science_2_1 = command.science_2_1,
                    english_2_1 = command.english_2_1,
                    techAndHome_2_1 = command.techAndHome_2_1,
                    // 학기별 점수
                    thirdGradeScore = command.thirdGradeScore,
                    thirdBeforeScore = command.thirdBeforeScore,
                    thirdBeforeBeforeScore = command.thirdBeforeBeforeScore,
                    thirdScore = command.thirdScore
                )
            }
        }
        
        // 가산점 적용
        val finalScore = if (command.extraScore > BigDecimal.ZERO) {
            score.applyExtraScore(command.extraScore)
        } else {
            score
        }
        
        return applicationPort.saveScore(finalScore)
    }
}