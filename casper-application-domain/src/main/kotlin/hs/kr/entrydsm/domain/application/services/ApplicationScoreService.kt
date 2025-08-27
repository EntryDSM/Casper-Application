package hs.kr.entrydsm.domain.application.services

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.domain.formula.entities.FormulaExecution
import hs.kr.entrydsm.domain.formula.spi.FormulaPort
import hs.kr.entrydsm.domain.formula.values.FormulaExecutionId
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * 원서 성적 계산 도메인 서비스
 * 
 * 원서 제출 시점에 해당하는 수식을 자동으로 찾아 계산하는 핵심 비즈니스 로직
 */
@Service(
    name = "ApplicationScoreService - 원서 성적 계산 도메인 서비스",
    type = ServiceType.DOMAIN_SERVICE
)
class ApplicationScoreService(
    private val formulaPort: FormulaPort,
    private val variableNameGenerator: VariableNameGenerator
) {
    
    /**
     * 원서 제출 시 성적 계산
     * 
     * @param application 원서 정보
     * @param score 성적 정보
     * @return 계산된 성적 결과
     * @throws DomainException 수식을 찾을 수 없거나 계산에 실패한 경우
     */
    fun calculateScoreOnSubmission(application: Application, score: Score): ScoreCalculationResult {
        try {
            // 1. 해당 원서에 맞는 수식 집합 찾기
            val formulaSet = formulaPort.findByApplicationCriteria(
                applicationType = application.applicationType,
                educationalStatus = application.educationalStatus,
                isDaejeon = application.isDaejeon
            ) ?: throw DomainException(
                errorCode = ErrorCode.BUSINESS_RULE_VIOLATION,
                message = "해당 원서 조건에 맞는 수식이 존재하지 않습니다",
                context = mapOf(
                    "receiptCode" to application.getId().value,
                    "applicationType" to application.applicationType.name,
                    "educationalStatus" to application.educationalStatus.name,
                    "isDaejeon" to application.isDaejeon
                )
            )

            val variableMapping = variableNameGenerator.generateAllVariables(application)
            val variables = mapScoreToFormulaVariables(application, score, variableMapping)
            val executionId = FormulaExecutionId.generate()
            val executionResult = formulaPort.executeFormulas(
                formulaSetId = formulaSet.id,
                executionId = executionId,
                variables = variables
            ) ?: throw DomainException(
                errorCode = ErrorCode.BUSINESS_RULE_VIOLATION,
                message = "수식 실행에 실패했습니다",
                context = mapOf(
                    "receiptCode" to application.getId().value,
                    "formulaSetId" to formulaSet.id.value,
                    "executionId" to executionId.value
                )
            )
            
            return ScoreCalculationResult(
                receiptCode = application.getId(),
                finalScore = executionResult.getResult("final_score") ?: 0.0,
                detailScores = executionResult.getAllResults(),
                formulaExecution = executionResult,
                variableMapping = variableMapping
            )
            
        } catch (domainException: DomainException) {
            throw domainException
        } catch (exception: Exception) {
            throw DomainException(
                errorCode = ErrorCode.BUSINESS_RULE_VIOLATION,
                message = "성적 계산 중 예상치 못한 오류가 발생했습니다: ${exception.message}",
                cause = exception,
                context = mapOf(
                    "receiptCode" to application.getId().value,
                    "operation" to "calculateScoreOnSubmission"
                )
            )
        }
    }
    
    /**
     * Score 엔티티의 데이터를 수식 변수로 매핑
     * 
     * @param application 원서 정보
     * @param score 성적 정보
     * @param variableMapping 생성된 변수명 매핑
     * @return 수식 변수 맵
     */
    private fun mapScoreToFormulaVariables(
        application: Application,
        score: Score,
        variableMapping: Map<String, String>
    ): Map<String, Any> {
        return buildMap {
            // 기본 점수들
            score.attendanceScore?.let { 
                variableMapping["attendance_score"]?.let { varName ->
                    put(varName, it.toInt())
                }
            }
            score.volunteerScore?.let { 
                variableMapping["volunteer_score"]?.let { varName ->
                    put(varName, it.toDouble())
                }
            }
            variableMapping["extra_score"]?.let { varName ->
                put(varName, score.extraScore.toDouble())
            }
            variableMapping["total_score"]?.let { varName ->
                put(varName, score.totalScore.toDouble())
            }
            
            // 학기별 과목별 등급 매핑
            // 3학년 2학기 (기존 필드)
            score.koreanGrade?.let { 
                variableMapping["korean_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            score.socialGrade?.let { 
                variableMapping["social_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            score.historyGrade?.let { 
                variableMapping["history_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            score.mathGrade?.let { 
                variableMapping["math_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            score.scienceGrade?.let { 
                variableMapping["science_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            score.englishGrade?.let { 
                variableMapping["english_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            score.techAndHomeGrade?.let { 
                variableMapping["tech_home_grade_3_2"]?.let { varName -> put(varName, it) }
            }
            
            // 3학년 1학기
            score.korean_3_1?.let { 
                variableMapping["korean_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            score.social_3_1?.let { 
                variableMapping["social_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            score.history_3_1?.let { 
                variableMapping["history_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            score.math_3_1?.let { 
                variableMapping["math_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            score.science_3_1?.let { 
                variableMapping["science_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            score.english_3_1?.let { 
                variableMapping["english_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            score.techAndHome_3_1?.let { 
                variableMapping["tech_home_grade_3_1"]?.let { varName -> put(varName, it) }
            }
            
            // 2학년 2학기
            score.korean_2_2?.let { 
                variableMapping["korean_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            score.social_2_2?.let { 
                variableMapping["social_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            score.history_2_2?.let { 
                variableMapping["history_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            score.math_2_2?.let { 
                variableMapping["math_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            score.science_2_2?.let { 
                variableMapping["science_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            score.english_2_2?.let { 
                variableMapping["english_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            score.techAndHome_2_2?.let { 
                variableMapping["tech_home_grade_2_2"]?.let { varName -> put(varName, it) }
            }
            
            // 2학년 1학기
            score.korean_2_1?.let { 
                variableMapping["korean_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            score.social_2_1?.let { 
                variableMapping["social_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            score.history_2_1?.let { 
                variableMapping["history_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            score.math_2_1?.let { 
                variableMapping["math_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            score.science_2_1?.let { 
                variableMapping["science_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            score.english_2_1?.let { 
                variableMapping["english_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            score.techAndHome_2_1?.let { 
                variableMapping["tech_home_grade_2_1"]?.let { varName -> put(varName, it) }
            }
            
            // 검정고시 점수
            score.qualificationKorean?.let { 
                variableMapping["qualification_korean"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.qualificationSocial?.let { 
                variableMapping["qualification_social"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.qualificationMath?.let { 
                variableMapping["qualification_math"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.qualificationScience?.let { 
                variableMapping["qualification_science"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.qualificationEnglish?.let { 
                variableMapping["qualification_english"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.qualificationOpt?.let { 
                variableMapping["qualification_optional"]?.let { varName -> put(varName, it.toDouble()) }
            }
            
            // 학기별 점수
            score.thirdBeforeBeforeScore?.let { 
                variableMapping["semester_total_2_1"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.thirdBeforeScore?.let { 
                variableMapping["semester_total_2_2"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.thirdGradeScore?.let { 
                variableMapping["semester_total_3_1"]?.let { varName -> put(varName, it.toDouble()) }
            }
            score.thirdScore?.let { 
                variableMapping["semester_total_3_2"]?.let { varName -> put(varName, it.toDouble()) }
            }
            
            // 전형/학력/지역 플래그들
            variableMapping["is_common"]?.let { varName ->
                put(varName, if (application.isCommon()) 1.0 else 0.0)
            }
            variableMapping["is_social"]?.let { varName ->
                put(varName, if (application.isSocial()) 1.0 else 0.0)
            }
            variableMapping["is_meister"]?.let { varName ->
                put(varName, if (application.isMeister()) 1.0 else 0.0)
            }
            
            variableMapping["is_graduate"]?.let { varName ->
                put(varName, if (application.isGraduate()) 1.0 else 0.0)
            }
            variableMapping["is_prospective"]?.let { varName ->
                put(varName, if (application.isProspectiveGraduate()) 1.0 else 0.0)
            }
            variableMapping["is_qualification"]?.let { varName ->
                put(varName, if (application.isQualificationExam()) 1.0 else 0.0)
            }
            
            variableMapping["is_daejeon"]?.let { varName ->
                put(varName, if (application.isDaejeon) 1.0 else 0.0)
            }
            variableMapping["is_nationwide"]?.let { varName ->
                put(varName, if (!application.isDaejeon) 1.0 else 0.0)
            }
        }
    }
}

/**
 * 성적 계산 결과 값 객체
 */
data class ScoreCalculationResult(
    val receiptCode: ReceiptCode,
    val finalScore: Double,
    val detailScores: Map<String, Double>,
    val formulaExecution: FormulaExecution,
    val variableMapping: Map<String, String>
)