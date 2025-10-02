package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationPdfGeneratorContract
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.application.values.ApplicationSubmissionStatus
import hs.kr.entrydsm.domain.security.interfaces.SecurityContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import java.time.LocalDateTime
import java.util.UUID

@ReadOnlyUseCase
class GetPreviewApplicationPdfUseCase(
    private val securityContract: SecurityContract,
    private val applicationPdfGeneratorContract: ApplicationPdfGeneratorContract,
) {

    /**
     * 프론트에서 전달받은 임시저장 데이터로 미리보기 PDF 생성
     */
    fun execute(applicationData: Map<String, Any>, scoresData: Map<String, Any>): ByteArray {
        val userId = securityContract.getCurrentUserId()
        
        val tempApplication = createTempApplication(userId, applicationData)
        
        return applicationPdfGeneratorContract.generate(tempApplication, emptyMap())
    }

    /**
     * 프론트에서 전달받은 데이터로 실제 Application 도메인 객체 생성
     */
    private fun createTempApplication(userId: UUID, data: Map<String, Any>): Application {
        val now = LocalDateTime.now()

        return Application(
            applicationId = UUID.randomUUID(),
            userId = userId,
            receiptCode = (data["receiptCode"] as? Number)?.toLong() ?: 0L,
            applicantName = data["applicantName"]?.toString() ?: "",
            applicantTel = data["applicantTel"]?.toString() ?: "",
            parentName = data["parentName"]?.toString(),
            parentTel = data["parentTel"]?.toString(),
            birthDate = data["birthDate"]?.toString(),
            applicationType = parseApplicationType(data["applicationType"]?.toString()),
            educationalStatus = parseEducationalStatus(data["educationalStatus"]?.toString()),
            status = ApplicationStatus.WRITING,
            submissionStatus = ApplicationSubmissionStatus.NOT_SUBMITTED,
            streetAddress = data["streetAddress"]?.toString(),
            submittedAt = LocalDateTime.MIN,
            reviewedAt = null,
            createdAt = now,
            updatedAt = now,
            isDaejeon = data["isDaejeon"] as? Boolean,
            //photoPath = data["photoPath"]?.toString(),
            parentRelation = data["parentRelation"]?.toString(),
            postalCode = data["postalCode"]?.toString(),
            detailAddress = data["detailAddress"]?.toString(),
            studyPlan = data["studyPlan"]?.toString(),
            selfIntroduce = data["selfIntroduce"]?.toString(),
            schoolCode = data["schoolCode"]?.toString(),

            nationalMeritChild = data["nationalMeritChild"] as? Boolean,
            specialAdmissionTarget = data["specialAdmissionTarget"] as? Boolean,



            graduationDate = data["graduationDate"]?.toString(),
            applicantGender = data["applicantGender"]?.toString(),
            guardianGender = data["guardianGender"]?.toString(),
            
            schoolName = data["schoolName"]?.toString(),
            studentId = data["studentId"]?.toString(),
            schoolPhone = data["schoolPhone"]?.toString(),
            teacherName = data["teacherName"]?.toString(),
            
            korean_3_1 = (data["korean_3_1"] as? Number)?.toInt(),
            social_3_1 = (data["social_3_1"] as? Number)?.toInt(),
            history_3_1 = (data["history_3_1"] as? Number)?.toInt(),
            math_3_1 = (data["math_3_1"] as? Number)?.toInt(),
            science_3_1 = (data["science_3_1"] as? Number)?.toInt(),
            tech_3_1 = (data["tech_3_1"] as? Number)?.toInt(),
            english_3_1 = (data["english_3_1"] as? Number)?.toInt(),
            
            korean_2_2 = (data["korean_2_2"] as? Number)?.toInt(),
            social_2_2 = (data["social_2_2"] as? Number)?.toInt(),
            history_2_2 = (data["history_2_2"] as? Number)?.toInt(),
            math_2_2 = (data["math_2_2"] as? Number)?.toInt(),
            science_2_2 = (data["science_2_2"] as? Number)?.toInt(),
            tech_2_2 = (data["tech_2_2"] as? Number)?.toInt(),
            english_2_2 = (data["english_2_2"] as? Number)?.toInt(),
            
            korean_2_1 = (data["korean_2_1"] as? Number)?.toInt(),
            social_2_1 = (data["social_2_1"] as? Number)?.toInt(),
            history_2_1 = (data["history_2_1"] as? Number)?.toInt(),
            math_2_1 = (data["math_2_1"] as? Number)?.toInt(),
            science_2_1 = (data["science_2_1"] as? Number)?.toInt(),
            tech_2_1 = (data["tech_2_1"] as? Number)?.toInt(),
            english_2_1 = (data["english_2_1"] as? Number)?.toInt(),
            
            korean_3_2 = (data["korean_3_2"] as? Number)?.toInt(),
            social_3_2 = (data["social_3_2"] as? Number)?.toInt(),
            history_3_2 = (data["history_3_2"] as? Number)?.toInt(),
            math_3_2 = (data["math_3_2"] as? Number)?.toInt(),
            science_3_2 = (data["science_3_2"] as? Number)?.toInt(),
            tech_3_2 = (data["tech_3_2"] as? Number)?.toInt(),
            english_3_2 = (data["english_3_2"] as? Number)?.toInt(),
            
            gedKorean = (data["gedKorean"] as? Number)?.toInt(),
            gedSocial = (data["gedSocial"] as? Number)?.toInt(),
            gedHistory = (data["gedHistory"] as? Number)?.toInt(),
            gedMath = (data["gedMath"] as? Number)?.toInt(),
            gedScience = (data["gedScience"] as? Number)?.toInt(),
            gedTech = (data["gedTech"] as? Number)?.toInt(),
            gedEnglish = (data["gedEnglish"] as? Number)?.toInt(),
            
            //specialNotes = data["specialNotes"]?.toString(),
            
            absence = (data["absence"] as? Number)?.toInt(),
            tardiness = (data["tardiness"] as? Number)?.toInt(),
            earlyLeave = (data["earlyLeave"] as? Number)?.toInt(),
            classExit = (data["classExit"] as? Number)?.toInt(),
            unexcused = (data["unexcused"] as? Number)?.toInt(),
            volunteer = (data["volunteer"] as? Number)?.toInt(),
            algorithmAward = data["algorithmAward"] as? Boolean,
            infoProcessingCert = data["infoProcessingCert"] as? Boolean,
            
            totalScore = null
        )
    }

    /**
     * 문자열을 ApplicationType enum으로 변환
     */
    private fun parseApplicationType(typeStr: String?): ApplicationType {
        return when (typeStr?.uppercase()) {
            "COMMON" -> ApplicationType.COMMON
            "MEISTER" -> ApplicationType.MEISTER  
            "SOCIAL" -> ApplicationType.SOCIAL
            else -> ApplicationType.COMMON
        }
    }

    /**
     * 문자열을 EducationalStatus enum으로 변환
     */
    private fun parseEducationalStatus(statusStr: String?): EducationalStatus {
        return when (statusStr?.uppercase()) {
            "PROSPECTIVE_GRADUATE" -> EducationalStatus.PROSPECTIVE_GRADUATE
            "GRADUATED" -> EducationalStatus.GRADUATED
            "GED" -> EducationalStatus.GED
            else -> EducationalStatus.PROSPECTIVE_GRADUATE
        }
    }
}
