package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationPdfGeneratorContract
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.security.interfaces.SecurityContract
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import java.time.LocalDateTime
import java.util.UUID

@ReadOnlyUseCase
class GetPreviewApplicationPdfUseCase(
    private val securityContract: SecurityContract,
    private val applicationPdfGeneratorContract: ApplicationPdfGeneratorContract
) {

    /**
     * 프론트에서 전달받은 임시저장 데이터로 미리보기 PDF 생성
     */
    fun execute(applicationData: Map<String, Any>, scoresData: Map<String, Any>): ByteArray {
        val userId = securityContract.getCurrentUserId()
        
        // 프론트에서 전달받은 데이터로 실제 Application 도메인 객체 생성
        val tempApplication = createTempApplication(userId, applicationData)
        
        return applicationPdfGeneratorContract.generate(tempApplication, scoresData)
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
            educationalStatus = data["educationalStatus"]?.toString() ?: "",
            status = ApplicationStatus.WRITING, // 미리보기는 작성 중 상태
            streetAddress = data["streetAddress"]?.toString(),
            submittedAt = LocalDateTime.MIN, // 미제출 상태 표시 (임시값)
            reviewedAt = null,
            createdAt = now,
            updatedAt = now,
            isDaejeon = data["isDaejeon"] as? Boolean,
            isOutOfHeadcount = data["isOutOfHeadcount"] as? Boolean ?: false,
            photoPath = data["photoPath"]?.toString(),
            parentRelation = data["parentRelation"]?.toString(),
            postalCode = data["postalCode"]?.toString(),
            detailAddress = data["detailAddress"]?.toString(),
            studyPlan = data["studyPlan"]?.toString(),
            selfIntroduce = data["selfIntroduce"]?.toString(),
            veteransNumber = (data["veteransNumber"] as? Number)?.toInt(),
            schoolCode = data["schoolCode"]?.toString()
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
            else -> ApplicationType.COMMON // 기본값
        }
    }
}
