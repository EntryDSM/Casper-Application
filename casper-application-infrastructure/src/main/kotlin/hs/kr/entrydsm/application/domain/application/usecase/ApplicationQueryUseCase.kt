package hs.kr.entrydsm.application.domain.application.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.PhotoJpaRepository
import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationDetailResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationListResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationScoresResponse
import hs.kr.entrydsm.application.domain.file.presentation.exception.FileExceptions
import hs.kr.entrydsm.application.global.security.SecurityAdapter
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.file.`object`.PathList
import hs.kr.entrydsm.domain.file.spi.GenerateFileUrlPort
import hs.kr.entrydsm.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 원서 조회 UseCase
 *
 * 단일 테이블 구조로 모든 데이터를 조회합니다.
 */
@Service
@Transactional(readOnly = true)
class ApplicationQueryUseCase(
    private val applicationRepository: ApplicationJpaRepository,
    private val objectMapper: ObjectMapper,
    private val photoJpaRepository: PhotoJpaRepository,
    private val securityAdapter: SecurityAdapter,
    private val generateFileUrlPort: GenerateFileUrlPort,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract,
) {
    fun getApplicationById(applicationId: String): ApplicationDetailResponse {
        val uuid = UUID.fromString(applicationId)
        val application =
            applicationRepository.findById(uuid)
                .orElseThrow { ApplicationNotFoundException("원서를 찾을 수 없습니다: $applicationId") }

        val status = applicationQueryStatusContract.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        val user = securityAdapter.getCurrentUserId()
        val photoPath = photoJpaRepository.findByUserId(user)?.photo

        return ApplicationDetailResponse(
            success = true,
            data =
                ApplicationDetailResponse.ApplicationDetailData(
                    applicationId = application.applicationId.toString(),
                    userId = application.userId.toString(),
                    receiptCode = application.receiptCode,
                    applicantName = application.applicantName,
                    applicantTel = application.applicantTel,
                    parentName = application.parentName,
                    parentTel = application.parentTel,
                    birthDate = application.birthDate,
                    applicationType = application.applicationType.name,
                    educationalStatus = application.educationalStatus.name,
                    status = application.status.name,
                    submittedAt = application.submittedAt,
                    reviewedAt = application.reviewedAt,
                    createdAt = application.createdAt,
                    updatedAt = application.updatedAt,
                    photoUrl = generateFileUrlPort.generateFileUrl(photoPath!!, PathList.PHOTO),
                    studyPlan = application.studyPlan,
                    selfIntroduce = application.selfIntroduce,
                    isDaejeon = application.isDaejeon,
                    scores =
                        ApplicationDetailResponse.ScoreInfo(
                            totalScore = application.totalScore?.toDouble(),
                            subjectScore = application.subjectScore?.toDouble(),
                            attendanceScore = application.attendanceScore?.toDouble(),
                            volunteerScore = application.volunteerScore?.toDouble(),
                            bonusScore = application.bonusScore?.toDouble(),
                        ),
                ),
        )
    }

    fun getApplications(
        applicationType: String?,
        educationalStatus: String?,
        isDaejeon: Boolean?,
        page: Int = 0,
        size: Int = 20,
    ): ApplicationListResponse {
        val applications =
            when {
                applicationType != null && educationalStatus != null && isDaejeon != null -> {
                    val typeEnum = ApplicationType.fromString(applicationType)
                    val statusEnum = EducationalStatus.fromString(educationalStatus)
                    applicationRepository.findByApplicationTypeAndEducationalStatusAndIsDaejeon(typeEnum, statusEnum, isDaejeon)
                }
                applicationType != null && educationalStatus != null -> {
                    val typeEnum = ApplicationType.fromString(applicationType)
                    val statusEnum = EducationalStatus.fromString(educationalStatus)
                    applicationRepository.findByApplicationTypeAndEducationalStatus(typeEnum, statusEnum)
                }
                applicationType != null && isDaejeon != null -> {
                    val typeEnum = ApplicationType.fromString(applicationType)
                    applicationRepository.findByApplicationTypeAndIsDaejeon(typeEnum, isDaejeon)
                }
                educationalStatus != null && isDaejeon != null -> {
                    val statusEnum = EducationalStatus.fromString(educationalStatus)
                    applicationRepository.findByEducationalStatusAndIsDaejeon(statusEnum, isDaejeon)
                }
                applicationType != null -> {
                    val typeEnum = ApplicationType.fromString(applicationType)
                    applicationRepository.findByApplicationType(typeEnum)
                }
                educationalStatus != null -> {
                    val statusEnum = EducationalStatus.fromString(educationalStatus)
                    applicationRepository.findByEducationalStatus(statusEnum)
                }
                isDaejeon != null -> {
                    applicationRepository.findByIsDaejeon(isDaejeon)
                }
                else -> applicationRepository.findAll()
            }

        val totalElements = applications.size
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        val pagedApplications =
            if (startIndex < totalElements) {
                applications.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

        return ApplicationListResponse(
            success = true,
            data =
                ApplicationListResponse.ApplicationListData(
                    applications =
                        pagedApplications.map { app ->
                            ApplicationListResponse.ApplicationSummary(
                                applicationId = app.applicationId.toString(),
                                receiptCode = app.receiptCode,
                                applicantName = app.applicantName,
                                applicationType = app.applicationType.name,
                                educationalStatus = app.educationalStatus.name,
                                status = app.status.toString(),
                                submittedAt = app.submittedAt,
                                isDaejeon = app.isDaejeon,
                                isSubmitted = true,
                                isArrived = isArrivedDocuments(app),
                            )
                        },
                    total = totalElements,
                    page = page,
                    size = size,
                    totalPages = (totalElements + size - 1) / size,
                ),
        )
    }

    fun getUserApplications(userId: String): ApplicationListResponse {
        val uuid = UUID.fromString(userId)
        val applications = applicationRepository.findAllByUserId(uuid)

        return ApplicationListResponse(
            success = true,
            data =
                ApplicationListResponse.ApplicationListData(
                    applications =
                        applications.map { app ->
                            ApplicationListResponse.ApplicationSummary(
                                applicationId = app.applicationId.toString(),
                                receiptCode = app.receiptCode,
                                applicantName = app.applicantName,
                                applicationType = app.applicationType.name,
                                educationalStatus = app.educationalStatus.name,
                                status = app.status.toString(),
                                submittedAt = app.submittedAt,
                                isDaejeon = app.isDaejeon,
                                isSubmitted = true,
                                isArrived = app.isArrived,
                            )
                        },
                    total = applications.size,
                    page = 0,
                    size = applications.size,
                    totalPages = 1,
                ),
        )
    }

    fun getApplicationScores(applicationId: String): ApplicationScoresResponse {
        val uuid = UUID.fromString(applicationId)
        val application =
            applicationRepository.findById(uuid)
                .orElseThrow { ApplicationNotFoundException("원서를 찾을 수 없습니다: $applicationId") }

        // JSON 필드에서 성적 데이터 파싱
        val scores = if (application.scoresData != null && application.scoresData.isNotBlank()) {
            try {
                objectMapper.readValue(application.scoresData, Map::class.java) as Map<String, Any>
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        return ApplicationScoresResponse(
            success = true,
            data =
                ApplicationScoresResponse.ScoresData(
                    applicationId = applicationId,
                    scores = scores,
                ),
        )
    }

    /**
     * Entity를 Domain Model로 변환합니다.
     */
    fun getApplicationDomainModel(applicationId: UUID): Application {
        val entity =
            applicationRepository.findById(applicationId)
                .orElseThrow { ApplicationNotFoundException("원서를 찾을 수 없습니다: $applicationId") }

        return entityToModel(entity)
    }

    /**
     * 현재 로그인한 사용자의 원서를 Domain Model로 조회합니다.
     */
    fun getCurrentUserApplication(): Application {
        val userId = securityAdapter.getCurrentUserId()
        val applications = applicationRepository.findAllByUserId(userId)

        if (applications.isEmpty()) {
            throw ApplicationNotFoundException("원서를 찾을 수 없습니다")
        }

        // 가장 최근 원서 반환
        val entity = applications.maxByOrNull { it.createdAt }
            ?: throw ApplicationNotFoundException("원서를 찾을 수 없습니다")

        return entityToModel(entity)
    }

    private fun entityToModel(entity: ApplicationJpaEntity): Application {
        // JSON 필드에서 성적 데이터 파싱
        val scores = if (entity.scoresData != null && entity.scoresData.isNotBlank()) {
            try {
                objectMapper.readValue(entity.scoresData, Map::class.java) as Map<String, Any>
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        // 증명사진 조회
        val userId = securityAdapter.getCurrentUserId()
        val photoKey = photoJpaRepository.findByUserId(userId)?.photo
        val photoPath = photoKey?.let { generateFileUrlPort.generateFileUrl(it, PathList.PHOTO) }

        return Application(
            applicationId = entity.applicationId,
            userId = entity.userId,
            receiptCode = entity.receiptCode,
            applicantName = entity.applicantName,
            applicantTel = entity.applicantTel,
            parentName = entity.parentName,
            parentTel = entity.parentTel,
            birthDate = entity.birthDate,
            applicationType = entity.applicationType,
            educationalStatus = entity.educationalStatus,
            status = entity.status,
            streetAddress = scores["streetAddress"] as? String,
            submittedAt = entity.submittedAt ?: entity.createdAt,
            reviewedAt = entity.reviewedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isDaejeon = entity.isDaejeon,
            photoPath = photoPath,
            parentRelation = entity.parentRelation,
            postalCode = entity.postalCode,
            detailAddress = entity.detailAddress,
            studyPlan = entity.studyPlan,
            selfIntroduce = entity.selfIntroduce,
            schoolCode = entity.schoolCode,
            nationalMeritChild = scores["nationalMeritChild"] as? Boolean,
            specialAdmissionTarget = scores["specialAdmissionTarget"] as? Boolean,
            graduationDate = scores["graduationDate"] as? String,
            applicantGender = (scores["applicantGender"] as? String)?.let { hs.kr.entrydsm.domain.application.values.Gender.fromString(it) },
            guardianGender = (scores["guardianGender"] as? String)?.let { hs.kr.entrydsm.domain.application.values.Gender.fromString(it) },
            schoolName = scores["schoolName"] as? String,
            studentId = scores["studentId"] as? String,
            schoolPhone = scores["schoolPhone"] as? String,
            teacherName = scores["teacherName"] as? String,
            korean_3_1 = scores["korean_3_1"] as? Int,
            social_3_1 = scores["social_3_1"] as? Int,
            history_3_1 = scores["history_3_1"] as? Int,
            math_3_1 = scores["math_3_1"] as? Int,
            science_3_1 = scores["science_3_1"] as? Int,
            tech_3_1 = scores["tech_3_1"] as? Int,
            english_3_1 = scores["english_3_1"] as? Int,
            korean_2_2 = scores["korean_2_2"] as? Int,
            social_2_2 = scores["social_2_2"] as? Int,
            history_2_2 = scores["history_2_2"] as? Int,
            math_2_2 = scores["math_2_2"] as? Int,
            science_2_2 = scores["science_2_2"] as? Int,
            tech_2_2 = scores["tech_2_2"] as? Int,
            english_2_2 = scores["english_2_2"] as? Int,
            korean_2_1 = scores["korean_2_1"] as? Int,
            social_2_1 = scores["social_2_1"] as? Int,
            history_2_1 = scores["history_2_1"] as? Int,
            math_2_1 = scores["math_2_1"] as? Int,
            science_2_1 = scores["science_2_1"] as? Int,
            tech_2_1 = scores["tech_2_1"] as? Int,
            english_2_1 = scores["english_2_1"] as? Int,
            korean_3_2 = scores["korean_3_2"] as? Int,
            social_3_2 = scores["social_3_2"] as? Int,
            history_3_2 = scores["history_3_2"] as? Int,
            math_3_2 = scores["math_3_2"] as? Int,
            science_3_2 = scores["science_3_2"] as? Int,
            tech_3_2 = scores["tech_3_2"] as? Int,
            english_3_2 = scores["english_3_2"] as? Int,
            gedKorean = scores["qualificationKorean"] as? Int,
            gedSocial = scores["qualificationSocial"] as? Int,
            gedHistory = scores["qualificationHistory"] as? Int,
            gedMath = scores["qualificationMath"] as? Int,
            gedScience = scores["qualificationScience"] as? Int,
            gedTech = scores["qualificationOpt"] as? Int,
            gedEnglish = scores["qualificationEnglish"] as? Int,
            absence = scores["absence"] as? Int,
            tardiness = scores["tardiness"] as? Int,
            earlyLeave = scores["earlyLeave"] as? Int,
            classExit = scores["classExit"] as? Int,
            unexcused = scores["unexcused"] as? Int,
            volunteer = scores["volunteer"] as? Int,
            algorithmAward = (scores["extraScore"] as? Int ?: 0) >= 3,
            infoProcessingCert = (scores["extraScore"] as? Int ?: 0) >= 2,
            totalScore = entity.totalScore,
        )
    }

    private fun isArrivedDocuments(application: ApplicationJpaEntity): Boolean {
        val status = applicationQueryStatusContract.queryStatusByReceiptCode(application.receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        return when(status.applicationStatus) {
            ApplicationStatus.DOCUMENTS_RECEIVED -> true
            ApplicationStatus.SCREENING_IN_PROGRESS -> true
            ApplicationStatus.RESULT_ANNOUNCED -> true
            else -> false
        }
    }
}
