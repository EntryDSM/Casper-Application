package hs.kr.entrydsm.application.domain.application.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.PhotoJpaRepository
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationDetailResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationListResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationScoresResponse
import hs.kr.entrydsm.application.global.security.SecurityAdapter
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.file.`object`.PathList
import hs.kr.entrydsm.domain.file.spi.GenerateFileUrlPort
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
) {
    fun getApplicationById(applicationId: String): ApplicationDetailResponse {
        val uuid = UUID.fromString(applicationId)
        val application =
            applicationRepository.findById(uuid)
                .orElseThrow { IllegalArgumentException("원서를 찾을 수 없습니다: $applicationId") }

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
                    status = application.status.toString(),
                    submittedAt = application.submittedAt,
                    reviewedAt = application.reviewedAt,
                    createdAt = application.createdAt,
                    updatedAt = application.updatedAt,
                    photoUrl = generateFileUrlPort.generateFileUrl(photoPath!!, PathList.PHOTO),
                    studyPlan = application.studyPlan,
                    selfIntroduce = application.selfIntroduce,
                    isDaejeon = application.isDaejeon
                ),
        )
    }

    fun getApplications(
        applicationType: String?,
        educationalStatus: String?,
        page: Int = 0,
        size: Int = 20,
    ): ApplicationListResponse {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending())

        val applications =
            when {
                applicationType != null && educationalStatus != null -> {
                    val typeEnum = ApplicationType.fromString(applicationType)
                    val statusEnum = EducationalStatus.fromString(educationalStatus)
                    applicationRepository.findByApplicationTypeAndEducationalStatus(typeEnum, statusEnum)
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
                                isDaejeon = app.isDaejeon
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
                                isDaejeon = app.isDaejeon
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
                .orElseThrow { IllegalArgumentException("원서를 찾을 수 없습니다: $applicationId") }

        // JSON 필드에서 성적 데이터 파싱
        val scores = objectMapper.readValue(application.scoresData, Map::class.java) as Map<String, Any>

        return ApplicationScoresResponse(
            success = true,
            data =
                ApplicationScoresResponse.ScoresData(
                    applicationId = applicationId,
                    scores = scores,
                ),
        )
    }
}
