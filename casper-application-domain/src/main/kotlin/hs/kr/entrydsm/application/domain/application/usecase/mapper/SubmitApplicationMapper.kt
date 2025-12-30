package hs.kr.entrydsm.application.domain.application.usecase.mapper

import hs.kr.entrydsm.application.domain.application.event.dto.ScoreData
import hs.kr.entrydsm.application.domain.application.event.dto.SubmissionData
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmitApplicationRequest
import hs.kr.entrydsm.application.domain.user.model.User
import java.util.UUID

object SubmitApplicationMapper {

    fun toApplication(request: SubmitApplicationRequest, user: User): Application {
        return request.run {
            Application(
                applicantName = user.name,
                applicantTel = user.phoneNumber,
                birthDate = birthDate,
                sex = applicantGender,
                streetAddress = streetAddress,
                postalCode = postalCode,
                detailAddress = detailAddress,
                isDaejeon = isDaejeon,
                parentName = parentName,
                parentTel = parentTel,
                parentRelation = parentRelation,
                educationalStatus = educationalStatus,
                applicationType = applicationType,
                studyPlan = studyPlan,
                selfIntroduce = selfIntroduce,
                userId = user.id
            )
        }
    }

    fun toSubmissionData(
        request: SubmitApplicationRequest,
        application: Application,
        userId: UUID
    ): SubmissionData {
        return SubmissionData(
            receiptCode = application.receiptCode,
            userId = userId,
            educationalStatus = request.educationalStatus,
            graduationDate = request.graduationDate,
            gradeNumber = request.studentId.substring(0, 1),
            classNumber = request.studentId.substring(1, 2),
            studentNumber = request.studentId.substring(2),
            schoolCode = request.schoolCode,
            teacherName = request.teacherName,
            schoolPhone = request.schoolPhone,
            scoreData = ScoreData(
                koreanGrade = request.koreanGrade,
                socialGrade = request.socialGrade,
                historyGrade = request.historyGrade,
                mathGrade = request.mathGrade,
                scienceGrade = request.scienceGrade,
                englishGrade = request.englishGrade,
                techAndHomeGrade = request.techAndHomeGrade,
                gedKorean = request.gedKorean,
                gedSocial = request.gedSocial,
                //gedHistory = request.gedHistory,
                gedOpt = request.gedOpt,
                gedMath = request.gedMath,
                gedScience = request.gedScience,
                gedEnglish = request.gedEnglish,
                absence = request.absence,
                tardiness = request.tardiness,
                earlyLeave = request.earlyLeave,
                classExit = request.classExit,
                volunteer = request.volunteer,
                algorithmAward = request.algorithmAward,
                infoProcessingCert = request.infoProcessingCert
            )
        )
    }
}
