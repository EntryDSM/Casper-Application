package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.domain.mapper.ApplicationMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CreateApplicationResponse
import hs.kr.entrydsm.application.domain.application.service.ApplicationValidationService
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationSubmissionStatus
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * 원서 제출 유스케이스
 * 
 * 원서 생성 등 원서 제출과 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional
class ApplicationSubmissionUseCase(
    private val applicationRepository: ApplicationJpaRepository,
    private val applicationMapper: ApplicationMapper,
    private val validationService: ApplicationValidationService
) {
    
    /**
     * 새로운 원서를 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param request 원서 생성 요청 데이터
     * @return 생성된 원서 응답
     */
    fun createApplication(userId: UUID, request: CreateApplicationRequest): CreateApplicationResponse {
        // 입력 데이터 검증
        //validationService.validateCreateApplicationRequest(request)
        
        // 중복 원서 제출 방지
        val existingApplications = applicationRepository.findAllByUserId(userId)
        if (existingApplications.isNotEmpty()) {
            throw IllegalStateException("이미 제출된 원서가 있습니다")
        }
        
        // 임시 수험번호 (나중에 동적 생성으로 변경)
        val receiptCode = 1000L + System.currentTimeMillis() % 100000
        
        val now = LocalDateTime.now()
        
        // Domain Model 생성
        val application = Application(
            applicationId = UUID.randomUUID(),
            userId = userId,
            receiptCode = receiptCode,
            applicantName = request.applicantName,
            applicantTel = request.applicantTel,
            parentName = request.parentName,
            parentTel = request.parentTel,
            birthDate = request.birthDate,
            applicationType = ApplicationType.valueOf(request.applicationType),
            educationalStatus = EducationalStatus.valueOf(request.educationalStatus),
            status = ApplicationStatus.SUBMITTED,
            submissionStatus = ApplicationSubmissionStatus.SUBMITTED,
            streetAddress = request.streetAddress,
            submittedAt = now,
            reviewedAt = null,
            createdAt = now,
            updatedAt = now,
            isDaejeon = request.isDaejeon,
            //isOutOfHeadcount = request.isOutOfHeadcount,
            //photoPath = request.photoPath,
            parentRelation = request.parentRelation,
            postalCode = request.postalCode,
            detailAddress = request.detailAddress,
            studyPlan = request.studyPlan,
            selfIntroduce = request.selfIntroduce,
            //veteransNumber = request.veteransNumber,
            schoolCode = request.schoolCode,
            
            // Basic Info Fields
            nationalMeritChild = request.nationalMeritChild,
            specialAdmissionTarget = request.specialAdmissionTarget,
            graduationDate = request.graduationDate,
            
            // Personal Info Fields
            applicantGender = request.applicantGender,
            
            // Guardian Info Fields
            //guardianName = request.guardianName,
            //guardianNumber = request.guardianNumber,
            guardianGender = request.guardianGender,
            
            // School Info Fields
            schoolName = request.schoolName,
            studentId = request.studentId,
            schoolPhone = request.schoolPhone,
            teacherName = request.teacherName,
            
            // Grade 3-1 Score Fields
            korean_3_1 = request.korean_3_1,
            social_3_1 = request.social_3_1,
            history_3_1 = request.history_3_1,
            math_3_1 = request.math_3_1,
            science_3_1 = request.science_3_1,
            tech_3_1 = request.tech_3_1,
            english_3_1 = request.english_3_1,
            
            // Grade 2-2 Score Fields
            korean_2_2 = request.korean_2_2,
            social_2_2 = request.social_2_2,
            history_2_2 = request.history_2_2,
            math_2_2 = request.math_2_2,
            science_2_2 = request.science_2_2,
            tech_2_2 = request.tech_2_2,
            english_2_2 = request.english_2_2,
            
            // Grade 2-1 Score Fields
            korean_2_1 = request.korean_2_1,
            social_2_1 = request.social_2_1,
            history_2_1 = request.history_2_1,
            math_2_1 = request.math_2_1,
            science_2_1 = request.science_2_1,
            tech_2_1 = request.tech_2_1,
            english_2_1 = request.english_2_1,
            
            // Grade 3-2 Score Fields (for graduates)
            korean_3_2 = request.korean_3_2,
            social_3_2 = request.social_3_2,
            history_3_2 = request.history_3_2,
            math_3_2 = request.math_3_2,
            science_3_2 = request.science_3_2,
            tech_3_2 = request.tech_3_2,
            english_3_2 = request.english_3_2,
            
            // GED Score Fields
            gedKorean = request.gedKorean,
            gedSocial = request.gedSocial,
            gedHistory = request.gedHistory,
            gedMath = request.gedMath,
            gedScience = request.gedScience,
            gedTech = request.gedTech,
            gedEnglish = request.gedEnglish,
            
            // Additional Personal Info Fields
            specialNotes = request.specialNotes,
            
            // Attendance & Service Fields
            absence = request.absence,
            tardiness = request.tardiness,
            earlyLeave = request.earlyLeave,
            classExit = request.classExit,
            unexcused = request.unexcused,
            volunteer = request.volunteer,
            algorithmAward = request.algorithmAward,
            infoProcessingCert = request.infoProcessingCert,
            
            // Score Calculation Fields
            totalScore = null
        )
        
        // Entity로 변환 후 저장
        val entity = applicationMapper.toEntity(application)
        val savedEntity = applicationRepository.save(entity)
        
        return CreateApplicationResponse(
            success = true,
            data = CreateApplicationResponse.ApplicationData(
                applicationId = savedEntity.applicationId,
                receiptCode = savedEntity.receiptCode,
                applicantName = savedEntity.applicantName,
                applicationType = savedEntity.applicationType,
                educationalStatus = savedEntity.educationalStatus,
                status = savedEntity.status.toString(),
                submittedAt = savedEntity.submittedAt,
                createdAt = savedEntity.createdAt
            )
        )
    }
    
    /**
     * 원서 ID로 원서를 조회합니다.
     * 
     * @param applicationId 원서 ID
     * @return 조회된 원서 또는 null
     */
    @Transactional(readOnly = true)
    fun getApplicationById(applicationId: UUID): Application? {
        val entity = applicationRepository.findByApplicationId(applicationId)
        return entity?.let { applicationMapper.toModel(it) }
    }
}