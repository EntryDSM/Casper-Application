package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.domain.pdf.presentation.dto.request.PreviewPdfRequest
import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationPdfGeneratorContract
import hs.kr.entrydsm.domain.application.values.ApplicationSubmissionStatus
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.application.values.Gender
import hs.kr.entrydsm.domain.file.`object`.PathList
import hs.kr.entrydsm.domain.file.spi.GenerateFileUrlPort
import hs.kr.entrydsm.domain.security.interfaces.SecurityContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import java.time.LocalDateTime
import java.util.UUID

@ReadOnlyUseCase
class GetPreviewApplicationPdfUseCase(
    private val securityContract: SecurityContract,
    private val applicationPdfGeneratorContract: ApplicationPdfGeneratorContract,
    private val photoJpaRepository: hs.kr.entrydsm.application.domain.application.domain.repository.PhotoJpaRepository,
    private val generateFileUrlPort: GenerateFileUrlPort,
) {
    /**
     * 프론트에서 전달받은 임시저장 데이터로 미리보기 PDF 생성
     */
    fun execute(request: PreviewPdfRequest): ByteArray {
        val userId = securityContract.getCurrentUserId()

        val photoKey = photoJpaRepository.findByUserId(userId)?.photo

        val photoUrl = photoKey?.let { generateFileUrlPort.generateFileUrl(it, PathList.PHOTO) }

        val tempApplication = createTempApplication(userId, request, photoUrl)

        return applicationPdfGeneratorContract.generate(tempApplication)
    }

    /**
     * PreviewPdfRequest로부터 임시 Application 도메인 객체 생성
     */
    private fun createTempApplication(
        userId: UUID,
        request: PreviewPdfRequest,
        photoPath: String?,
    ): Application {
        val now = LocalDateTime.now()

        return Application(
            applicationId = UUID.randomUUID(),
            userId = userId,
            receiptCode = 0L,
            applicantName = request.applicantName,
            applicantTel = request.applicantTel,
            parentName = request.parentName,
            parentTel = request.parentTel,
            birthDate = request.birthDate,
            applicationType = parseApplicationType(request.applicationType),
            educationalStatus = parseEducationalStatus(request.educationalStatus),
            status = ApplicationStatus.WRITING,
            streetAddress = request.streetAddress,
            submittedAt = LocalDateTime.MIN,
            reviewedAt = null,
            createdAt = now,
            updatedAt = now,
            isDaejeon = request.isDaejeon,
            photoPath = photoPath, // 전체 URL
            parentRelation = request.parentRelation,
            postalCode = request.postalCode,
            detailAddress = request.detailAddress,
            studyPlan = request.studyPlan,
            selfIntroduce = request.selfIntroduce,
            schoolCode = request.schoolCode,
            nationalMeritChild = request.nationalMeritChild,
            specialAdmissionTarget = request.specialAdmissionTarget,
            graduationDate = request.graduationDate,
            applicantGender = Gender.fromString(request.applicantGender),
            guardianGender = Gender.fromString(request.guardianGender),
            schoolName = request.schoolName,
            studentId = request.studentId,
            schoolPhone = request.schoolPhone,
            teacherName = request.teacherName,
            korean_3_1 = request.korean_3_1,
            social_3_1 = request.social_3_1,
            history_3_1 = request.history_3_1,
            math_3_1 = request.math_3_1,
            science_3_1 = request.science_3_1,
            tech_3_1 = request.tech_3_1,
            english_3_1 = request.english_3_1,
            korean_2_2 = request.korean_2_2,
            social_2_2 = request.social_2_2,
            history_2_2 = request.history_2_2,
            math_2_2 = request.math_2_2,
            science_2_2 = request.science_2_2,
            tech_2_2 = request.tech_2_2,
            english_2_2 = request.english_2_2,
            korean_2_1 = request.korean_2_1,
            social_2_1 = request.social_2_1,
            history_2_1 = request.history_2_1,
            math_2_1 = request.math_2_1,
            science_2_1 = request.science_2_1,
            tech_2_1 = request.tech_2_1,
            english_2_1 = request.english_2_1,
            korean_3_2 = request.korean_3_2,
            social_3_2 = request.social_3_2,
            history_3_2 = request.history_3_2,
            math_3_2 = request.math_3_2,
            science_3_2 = request.science_3_2,
            tech_3_2 = request.tech_3_2,
            english_3_2 = request.english_3_2,
            gedKorean = request.gedKorean,
            gedSocial = request.gedSocial,
            gedHistory = request.gedHistory,
            gedMath = request.gedMath,
            gedScience = request.gedScience,
            gedTech = request.gedTech,
            gedEnglish = request.gedEnglish,
            absence = request.absence,
            tardiness = request.tardiness,
            earlyLeave = request.earlyLeave,
            classExit = request.classExit,
            unexcused = request.unexcused,
            volunteer = request.volunteer,
            algorithmAward = request.algorithmAward,
            infoProcessingCert = request.infoProcessingCert,
            totalScore = null,
        )
    }

    private fun parseApplicationType(typeStr: String): ApplicationType {
        return when (typeStr.uppercase()) {
            "COMMON" -> ApplicationType.COMMON
            "MEISTER" -> ApplicationType.MEISTER
            "SOCIAL" -> ApplicationType.SOCIAL
            else -> ApplicationType.COMMON
        }
    }

    private fun parseEducationalStatus(statusStr: String): EducationalStatus {
        return when (statusStr.uppercase()) {
            "PROSPECTIVE_GRADUATE" -> EducationalStatus.PROSPECTIVE_GRADUATE
            "GRADUATED" -> EducationalStatus.GRADUATE
            "GED" -> EducationalStatus.QUALIFICATION_EXAM
            else -> EducationalStatus.PROSPECTIVE_GRADUATE
        }
    }
}