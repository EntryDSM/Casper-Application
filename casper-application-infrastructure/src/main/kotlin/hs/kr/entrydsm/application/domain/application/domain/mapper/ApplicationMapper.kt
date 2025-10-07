package hs.kr.entrydsm.application.domain.application.domain.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.domain.application.aggregates.Application
import org.springframework.stereotype.Component

@Component
class ApplicationMapper(
    private val objectMapper: ObjectMapper
) {
    fun toEntity(model: Application): ApplicationJpaEntity {
        // 개별 필드를 JSON으로 직렬화
        val scoresMap = mutableMapOf<String, Any?>()

        model.korean_3_1?.let { scoresMap["korean_3_1"] = it }
        model.social_3_1?.let { scoresMap["social_3_1"] = it }
        model.history_3_1?.let { scoresMap["history_3_1"] = it }
        model.math_3_1?.let { scoresMap["math_3_1"] = it }
        model.science_3_1?.let { scoresMap["science_3_1"] = it }
        model.tech_3_1?.let { scoresMap["tech_3_1"] = it }
        model.english_3_1?.let { scoresMap["english_3_1"] = it }

        model.korean_2_2?.let { scoresMap["korean_2_2"] = it }
        model.social_2_2?.let { scoresMap["social_2_2"] = it }
        model.history_2_2?.let { scoresMap["history_2_2"] = it }
        model.math_2_2?.let { scoresMap["math_2_2"] = it }
        model.science_2_2?.let { scoresMap["science_2_2"] = it }
        model.tech_2_2?.let { scoresMap["tech_2_2"] = it }
        model.english_2_2?.let { scoresMap["english_2_2"] = it }

        model.korean_2_1?.let { scoresMap["korean_2_1"] = it }
        model.social_2_1?.let { scoresMap["social_2_1"] = it }
        model.history_2_1?.let { scoresMap["history_2_1"] = it }
        model.math_2_1?.let { scoresMap["math_2_1"] = it }
        model.science_2_1?.let { scoresMap["science_2_1"] = it }
        model.tech_2_1?.let { scoresMap["tech_2_1"] = it }
        model.english_2_1?.let { scoresMap["english_2_1"] = it }

        model.korean_3_2?.let { scoresMap["korean_3_2"] = it }
        model.social_3_2?.let { scoresMap["social_3_2"] = it }
        model.history_3_2?.let { scoresMap["history_3_2"] = it }
        model.math_3_2?.let { scoresMap["math_3_2"] = it }
        model.science_3_2?.let { scoresMap["science_3_2"] = it }
        model.tech_3_2?.let { scoresMap["tech_3_2"] = it }
        model.english_3_2?.let { scoresMap["english_3_2"] = it }

        model.gedKorean?.let { scoresMap["gedKorean"] = it }
        model.gedSocial?.let { scoresMap["gedSocial"] = it }
        model.gedHistory?.let { scoresMap["gedHistory"] = it }
        model.gedMath?.let { scoresMap["gedMath"] = it }
        model.gedScience?.let { scoresMap["gedScience"] = it }
        model.gedTech?.let { scoresMap["gedTech"] = it }
        model.gedEnglish?.let { scoresMap["gedEnglish"] = it }

        model.absence?.let { scoresMap["absence"] = it }
        model.tardiness?.let { scoresMap["tardiness"] = it }
        model.earlyLeave?.let { scoresMap["earlyLeave"] = it }
        model.classExit?.let { scoresMap["classExit"] = it }
        model.unexcused?.let { scoresMap["unexcused"] = it }
        model.volunteer?.let { scoresMap["volunteer"] = it }

        model.algorithmAward?.let { scoresMap["algorithmAward"] = it }
        model.infoProcessingCert?.let { scoresMap["infoProcessingCert"] = it }

        val scoresDataJson = objectMapper.writeValueAsString(scoresMap)

        return ApplicationJpaEntity(
            applicationId = model.applicationId,
            userId = model.userId,
            receiptCode = model.receiptCode,
            applicantName = model.applicantName,
            applicantTel = model.applicantTel,
            birthDate = model.birthDate,
            applicationType = model.applicationType,
            educationalStatus = model.educationalStatus,
            status = model.status,
            isDaejeon = model.isDaejeon ?: false,
            isArrived = false,
            parentName = model.parentName,
            parentTel = model.parentTel,
            parentRelation = model.parentRelation,
            postalCode = model.postalCode,
            detailAddress = model.detailAddress,
            studyPlan = model.studyPlan,
            selfIntroduce = model.selfIntroduce,
            schoolCode = model.schoolCode,
            scoresData = scoresDataJson,
            totalScore = model.totalScore,
            subjectScore = null,
            attendanceScore = null,
            volunteerScore = null,
            bonusScore = null,
            calculatedAt = null,
            calculationTimeMs = null,
            submittedAt = model.submittedAt,
            reviewedAt = model.reviewedAt,
        )
    }

    fun toModel(entity: ApplicationJpaEntity): Application {
        // JSON에서 성적 데이터 파싱
        val scoresMap: Map<String, Any> = try {
            objectMapper.readValue(entity.scoresData)
        } catch (e: Exception) {
            emptyMap()
        }

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
            streetAddress = null,
            submittedAt = entity.submittedAt,
            reviewedAt = entity.reviewedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isDaejeon = entity.isDaejeon,
            photoPath = null,
            parentRelation = entity.parentRelation,
            postalCode = entity.postalCode,
            detailAddress = entity.detailAddress,
            studyPlan = entity.studyPlan,
            selfIntroduce = entity.selfIntroduce,
            schoolCode = entity.schoolCode,
            nationalMeritChild = null,
            specialAdmissionTarget = null,
            graduationDate = null,
            applicantGender = null,
            guardianGender = null,
            schoolName = null,
            studentId = null,
            schoolPhone = null,
            teacherName = null,
            korean_3_1 = scoresMap["korean_3_1"] as? Int,
            social_3_1 = scoresMap["social_3_1"] as? Int,
            history_3_1 = scoresMap["history_3_1"] as? Int,
            math_3_1 = scoresMap["math_3_1"] as? Int,
            science_3_1 = scoresMap["science_3_1"] as? Int,
            tech_3_1 = scoresMap["tech_3_1"] as? Int,
            english_3_1 = scoresMap["english_3_1"] as? Int,
            korean_2_2 = scoresMap["korean_2_2"] as? Int,
            social_2_2 = scoresMap["social_2_2"] as? Int,
            history_2_2 = scoresMap["history_2_2"] as? Int,
            math_2_2 = scoresMap["math_2_2"] as? Int,
            science_2_2 = scoresMap["science_2_2"] as? Int,
            tech_2_2 = scoresMap["tech_2_2"] as? Int,
            english_2_2 = scoresMap["english_2_2"] as? Int,
            korean_2_1 = scoresMap["korean_2_1"] as? Int,
            social_2_1 = scoresMap["social_2_1"] as? Int,
            history_2_1 = scoresMap["history_2_1"] as? Int,
            math_2_1 = scoresMap["math_2_1"] as? Int,
            science_2_1 = scoresMap["science_2_1"] as? Int,
            tech_2_1 = scoresMap["tech_2_1"] as? Int,
            english_2_1 = scoresMap["english_2_1"] as? Int,
            korean_3_2 = scoresMap["korean_3_2"] as? Int,
            social_3_2 = scoresMap["social_3_2"] as? Int,
            history_3_2 = scoresMap["history_3_2"] as? Int,
            math_3_2 = scoresMap["math_3_2"] as? Int,
            science_3_2 = scoresMap["science_3_2"] as? Int,
            tech_3_2 = scoresMap["tech_3_2"] as? Int,
            english_3_2 = scoresMap["english_3_2"] as? Int,
            gedKorean = scoresMap["gedKorean"] as? Int,
            gedSocial = scoresMap["gedSocial"] as? Int,
            gedHistory = scoresMap["gedHistory"] as? Int,
            gedMath = scoresMap["gedMath"] as? Int,
            gedScience = scoresMap["gedScience"] as? Int,
            gedTech = scoresMap["gedTech"] as? Int,
            gedEnglish = scoresMap["gedEnglish"] as? Int,
            absence = scoresMap["absence"] as? Int,
            tardiness = scoresMap["tardiness"] as? Int,
            earlyLeave = scoresMap["earlyLeave"] as? Int,
            classExit = scoresMap["classExit"] as? Int,
            unexcused = scoresMap["unexcused"] as? Int,
            volunteer = scoresMap["volunteer"] as? Int,
            algorithmAward = scoresMap["algorithmAward"] as? Boolean,
            infoProcessingCert = scoresMap["infoProcessingCert"] as? Boolean,
            totalScore = entity.totalScore,
        )
    }
}
