package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.entities.User
import hs.kr.entrydsm.domain.application.services.ApplicationScoreService
import hs.kr.entrydsm.domain.application.services.ScoreCalculationResult
import hs.kr.entrydsm.domain.application.services.VariableNameGenerator
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.usecase.*
import hs.kr.entrydsm.domain.formula.spi.FormulaPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application UseCase 통합 구현체
 */
@Service
class ApplicationUseCase(
    private val applicationPort: ApplicationPort,
    private val formulaPort: FormulaPort
) : CreateApplicationUseCase,
    GetApplicationUseCase,
    UpdateApplicationUseCase,
    SubmitApplicationUseCase,
    CreateScoreUseCase,
    CalculateScoreUseCase {
    
    override fun execute(command: CreateApplicationCommand): Application {
        return CreateApplicationUseCaseImpl(applicationPort).execute(command)
    }
    
    override fun execute(query: GetApplicationQuery): Application {
        return GetApplicationUseCaseImpl(applicationPort).execute(query)
    }
    
    @Transactional
    override fun execute(command: UpdateApplicationCommand): Application {
        return UpdateApplicationUseCaseImpl(applicationPort).execute(command)
    }
    
    @Transactional
    override fun execute(command: SubmitApplicationCommand): Application {
        return SubmitApplicationUseCaseImpl(applicationPort).execute(command)
    }
    
    override fun execute(command: CreateScoreCommand): Score {
        return CreateScoreUseCaseImpl(applicationPort).execute(command)
    }
    
    /**
     * 사용자 생성
     */
    @Transactional
    fun createUser(
        phoneNumber: String,
        name: String,
        isParent: Boolean = false
    ): User {
        val existingUser = applicationPort.queryUserByPhoneNumber(phoneNumber)
        if (existingUser != null) {
            return existingUser
        }
        
        val user = User.generate(phoneNumber, name, isParent)
        return applicationPort.saveUser(user)
    }
    
    /**
     * 통합 원서 작성 + 성적 입력 + 자동 계산 (트랜잭션으로 묶어서 원자성 보장)
     */
    @Transactional
    fun createCompleteApplication(
        userId: UUID,
        applicationCommand: CreateApplicationCommand,
        scoreCommand: CreateScoreCommand
    ): CompleteApplicationResult {
        // 기존 사용자 조회
        val user = applicationPort.queryUserById(userId) 
            ?: throw IllegalArgumentException("User not found with id: $userId")
        
        val application = execute(applicationCommand)
        
        // CreateScoreUseCaseImpl을 직접 호출하여 Application 객체와 관련 정보를 직접 전달
        val score = createScoreDirectly(application, scoreCommand)
        
        val calculationResult = execute(CalculateScoreCommand(receiptCode = application.id))
        
        return CompleteApplicationResult(user, application, score, calculationResult)
    }
    
    /**
     * Application 엔티티를 다시 조회하지 않고 직접 성적을 생성
     */
    private fun createScoreDirectly(application: Application, scoreCommand: CreateScoreCommand): Score {
        val score = when (application.educationalStatus) {
            hs.kr.entrydsm.domain.application.values.EducationalStatus.QUALIFICATION_EXAM -> {
                val qualScore = scoreCommand.qualificationScore
                    ?: throw IllegalArgumentException("Qualification score is required for qualification exam students")
                Score.createForQualificationExam(
                    receiptCode = application.id,
                    attendanceScore = scoreCommand.attendanceScore,
                    volunteerScore = scoreCommand.volunteerScore,
                    qualificationScore = qualScore
                )
            }
            
            hs.kr.entrydsm.domain.application.values.EducationalStatus.GRADUATE, 
            hs.kr.entrydsm.domain.application.values.EducationalStatus.PROSPECTIVE_GRADUATE -> {
                Score.createForGraduation(
                    receiptCode = application.id,
                    attendanceScore = scoreCommand.attendanceScore,
                    volunteerScore = scoreCommand.volunteerScore,
                    // 학기별 과목별 성적
                    korean_3_1 = scoreCommand.korean_3_1,
                    social_3_1 = scoreCommand.social_3_1,
                    history_3_1 = scoreCommand.history_3_1,
                    math_3_1 = scoreCommand.math_3_1,
                    science_3_1 = scoreCommand.science_3_1,
                    english_3_1 = scoreCommand.english_3_1,
                    techAndHome_3_1 = scoreCommand.techAndHome_3_1,
                    koreanGrade = scoreCommand.koreanGrade,
                    socialGrade = scoreCommand.socialGrade,
                    historyGrade = scoreCommand.historyGrade,
                    mathGrade = scoreCommand.mathGrade,
                    scienceGrade = scoreCommand.scienceGrade,
                    englishGrade = scoreCommand.englishGrade,
                    techAndHomeGrade = scoreCommand.techAndHomeGrade,
                    korean_2_2 = scoreCommand.korean_2_2,
                    social_2_2 = scoreCommand.social_2_2,
                    history_2_2 = scoreCommand.history_2_2,
                    math_2_2 = scoreCommand.math_2_2,
                    science_2_2 = scoreCommand.science_2_2,
                    english_2_2 = scoreCommand.english_2_2,
                    techAndHome_2_2 = scoreCommand.techAndHome_2_2,
                    korean_2_1 = scoreCommand.korean_2_1,
                    social_2_1 = scoreCommand.social_2_1,
                    history_2_1 = scoreCommand.history_2_1,
                    math_2_1 = scoreCommand.math_2_1,
                    science_2_1 = scoreCommand.science_2_1,
                    english_2_1 = scoreCommand.english_2_1,
                    techAndHome_2_1 = scoreCommand.techAndHome_2_1,
                    // 학기별 점수
                    thirdGradeScore = scoreCommand.thirdGradeScore,
                    thirdBeforeScore = scoreCommand.thirdBeforeScore,
                    thirdBeforeBeforeScore = scoreCommand.thirdBeforeBeforeScore,
                    thirdScore = scoreCommand.thirdScore
                )
            }
        }
        
        // 가산점 적용
        val finalScore = if (scoreCommand.extraScore > java.math.BigDecimal.ZERO) {
            score.applyExtraScore(scoreCommand.extraScore)
        } else {
            score
        }
        
        return applicationPort.saveScore(finalScore)
    }
    
    /**
     * 사용자 조회 (전화번호로)
     */
    fun getUserByPhoneNumber(phoneNumber: String): User? {
        return applicationPort.queryUserByPhoneNumber(phoneNumber)
    }
    
    /**
     * 모든 원서 조회
     */
    fun getAllApplications(): List<Application> {
        return applicationPort.queryAllApplications()
    }
    
    /**
     * 성적 조회
     */
    fun getScore(receiptCode: hs.kr.entrydsm.domain.application.values.ReceiptCode): Score? {
        return applicationPort.queryScoreByReceiptCode(receiptCode)
    }
    
    /**
     * 성적 계산
     */
    override fun execute(command: CalculateScoreCommand): ScoreCalculationResult {
        val variableNameGenerator = VariableNameGenerator()
        val applicationScoreService = ApplicationScoreService(formulaPort, variableNameGenerator)
        return CalculateScoreUseCaseImpl(applicationPort, applicationScoreService).execute(command)
    }
}