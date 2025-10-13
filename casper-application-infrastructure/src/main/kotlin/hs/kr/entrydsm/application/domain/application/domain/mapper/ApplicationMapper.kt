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

        // 추가 정보 필드들
        model.streetAddress?.let { scoresMap["streetAddress"] = it }
        model.applicantGender?.let { scoresMap["applicantGender"] = it.name }
        model.guardianGender?.let { scoresMap["guardianGender"] = it.name }
        model.schoolName?.let { scoresMap["schoolName"] = it }
        model.studentId?.let { scoresMap["studentId"] = it }
        model.schoolPhone?.let { scoresMap["schoolPhone"] = it }
        model.teacherName?.let { scoresMap["teacherName"] = it }
        model.nationalMeritChild?.let { scoresMap["nationalMeritChild"] = it }
        model.specialAdmissionTarget?.let { scoresMap["specialAdmissionTarget"] = it }
        model.graduationDate?.let { scoresMap["graduationDate"] = it }

        // 성적 필드들
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
            applicantGender = model.applicantGender,
            educationalStatus = model.educationalStatus,
            isDaejeon = model.isDaejeon ?: false,
            parentName = model.parentName,
            parentTel = model.parentTel,
            parentRelation = model.parentRelation,
            postalCode = model.postalCode,
            streetAddress = model.streetAddress,
            detailAddress = model.detailAddress,
            studyPlan = model.studyPlan,
            selfIntroduce = model.selfIntroduce,
            nationalMeritChild = model.nationalMeritChild,
            specialAdmissionTarget = model.specialAdmissionTarget,
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
            if (entity.scoresData.isNullOrBlank() || entity.scoresData == "null") {
                emptyMap()
            } else {
                objectMapper.readValue(entity.scoresData)
            }
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
            streetAddress = entity.streetAddress,
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
            nationalMeritChild = entity.nationalMeritChild,
            specialAdmissionTarget = entity.specialAdmissionTarget,
            graduationDate = null,
            applicantGender = entity.applicantGender,
            guardianGender = null,
            schoolName = null,
            studentId = null,
            schoolPhone = null,
            teacherName = null,
            korean_3_1 = (scoresMap["korean_3_1"] as? Number)?.toInt(),
            social_3_1 = (scoresMap["social_3_1"] as? Number)?.toInt(),
            history_3_1 = (scoresMap["history_3_1"] as? Number)?.toInt(),
            math_3_1 = (scoresMap["math_3_1"] as? Number)?.toInt(),
            science_3_1 = (scoresMap["science_3_1"] as? Number)?.toInt(),
            tech_3_1 = (scoresMap["tech_3_1"] as? Number)?.toInt(),
            english_3_1 = (scoresMap["english_3_1"] as? Number)?.toInt(),
            korean_2_2 = (scoresMap["korean_2_2"] as? Number)?.toInt(),
            social_2_2 = (scoresMap["social_2_2"] as? Number)?.toInt(),
            history_2_2 = (scoresMap["history_2_2"] as? Number)?.toInt(),
            math_2_2 = (scoresMap["math_2_2"] as? Number)?.toInt(),
            science_2_2 = (scoresMap["science_2_2"] as? Number)?.toInt(),
            tech_2_2 = (scoresMap["tech_2_2"] as? Number)?.toInt(),
            english_2_2 = (scoresMap["english_2_2"] as? Number)?.toInt(),
            korean_2_1 = (scoresMap["korean_2_1"] as? Number)?.toInt(),
            social_2_1 = (scoresMap["social_2_1"] as? Number)?.toInt(),
            history_2_1 = (scoresMap["history_2_1"] as? Number)?.toInt(),
            math_2_1 = (scoresMap["math_2_1"] as? Number)?.toInt(),
            science_2_1 = (scoresMap["science_2_1"] as? Number)?.toInt(),
            tech_2_1 = (scoresMap["tech_2_1"] as? Number)?.toInt(),
            english_2_1 = (scoresMap["english_2_1"] as? Number)?.toInt(),
            korean_3_2 = (scoresMap["korean_3_2"] as? Number)?.toInt(),
            social_3_2 = (scoresMap["social_3_2"] as? Number)?.toInt(),
            history_3_2 = (scoresMap["history_3_2"] as? Number)?.toInt(),
            math_3_2 = (scoresMap["math_3_2"] as? Number)?.toInt(),
            science_3_2 = (scoresMap["science_3_2"] as? Number)?.toInt(),
            tech_3_2 = (scoresMap["tech_3_2"] as? Number)?.toInt(),
            english_3_2 = (scoresMap["english_3_2"] as? Number)?.toInt(),
            gedKorean = (scoresMap["gedKorean"] as? Number)?.toInt(),
            gedSocial = (scoresMap["gedSocial"] as? Number)?.toInt(),
            gedHistory = (scoresMap["gedHistory"] as? Number)?.toInt(),
            gedMath = (scoresMap["gedMath"] as? Number)?.toInt(),
            gedScience = (scoresMap["gedScience"] as? Number)?.toInt(),
            gedTech = (scoresMap["gedTech"] as? Number)?.toInt(),
            gedEnglish = (scoresMap["gedEnglish"] as? Number)?.toInt(),
            absence = (scoresMap["absence"] as? Number)?.toInt(),
            tardiness = (scoresMap["tardiness"] as? Number)?.toInt(),
            earlyLeave = (scoresMap["earlyLeave"] as? Number)?.toInt(),
            classExit = (scoresMap["classExit"] as? Number)?.toInt(),
            //unexcused = scoresMap["unexcused"] as? Int,
            volunteer = (scoresMap["volunteer"] as? Number)?.toInt(),
            algorithmAward = scoresMap["algorithmAward"] as? Boolean,
            infoProcessingCert = scoresMap["infoProcessingCert"] as? Boolean,
            totalScore = entity.totalScore,
        )
    }
}
