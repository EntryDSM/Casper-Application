package hs.kr.entrydsm.application.domain.application.usecase.mapper

import hs.kr.entrydsm.application.domain.application.event.dto.ScoreData
import hs.kr.entrydsm.application.domain.application.event.dto.SubmissionData
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmitApplicationRequest
import hs.kr.entrydsm.application.domain.user.model.User
import java.util.UUID

object SubmitApplicationMapper {

    fun toApplication(request: SubmitApplicationRequest, user: User): Application {
        return Application(
            applicantName = user.name,
            applicantTel = user.phoneNumber,
            birthDate = request.applicantInfo.birthDate,
            sex = request.applicantInfo.applicantGender,
            streetAddress = request.addressInfo.streetAddress,
            postalCode = request.addressInfo.postalCode,
            detailAddress = request.addressInfo.detailAddress,
            isDaejeon = request.addressInfo.isDaejeon,
            parentName = request.applicantInfo.parentName,
            parentTel = request.applicantInfo.parentTel,
            parentRelation = request.applicantInfo.parentRelation,
            educationalStatus = request.applicationInfo.educationalStatus,
            applicationType = request.applicationInfo.applicationType,
            studyPlan = request.applicationInfo.studyPlan,
            selfIntroduce = request.applicationInfo.selfIntroduce,
            userId = user.id
        )
    }

    // 원서 제출 과정에서 세부적인 로직은 이벤트 처리가 아닌 동기적으로 호출하기 때문에 해당 로직은 미사용
//    fun toSubmissionData(
//        request: SubmitApplicationRequest,
//        application: Application,
//        userId: UUID
//    ): SubmissionData {
//        return SubmissionData(
//            receiptCode = application.receiptCode,
//            userId = userId,
//            educationalStatus = request.applicationInfo.educationalStatus,
//            graduationDate = request.applicationInfo.graduationDate,
//            gradeNumber = request.applicationInfo.studentId.substring(0, 1),
//            classNumber = request.applicationInfo.studentId.substring(1, 2),
//            studentNumber = request.applicationInfo.studentId.substring(2),
//            schoolCode = request.schoolInfo.schoolCode,
//            teacherName = request.schoolInfo.teacherName,
//            schoolPhone = request.schoolInfo.schoolPhone,
//            scoreData = ScoreData(
//                koreanGrade = request.gradeInfo.koreanGrade,
//                socialGrade = request.gradeInfo.socialGrade,
//                historyGrade = request.gradeInfo.historyGrade,
//                mathGrade = request.gradeInfo.mathGrade,
//                scienceGrade = request.gradeInfo.scienceGrade,
//                englishGrade = request.gradeInfo.englishGrade,
//                techAndHomeGrade = request.gradeInfo.techAndHomeGrade,
//                gedKorean = request.gradeInfo.gedKorean,
//                gedSocial = request.gradeInfo.gedSocial,
//                gedHistory = request.gradeInfo.gedHistory,
//                gedMath = request.gradeInfo.gedMath,
//                gedScience = request.gradeInfo.gedScience,
//                gedEnglish = request.gradeInfo.gedEnglish,
//                absence = request.attendanceInfo.absence,
//                tardiness = request.attendanceInfo.tardiness,
//                earlyLeave = request.attendanceInfo.earlyLeave,
//                classExit = request.attendanceInfo.classExit,
//                volunteer = request.attendanceInfo.volunteer,
//                algorithmAward = request.awardAndCertificateInfo.algorithmAward,
//                infoProcessingCert = request.awardAndCertificateInfo.infoProcessingCert
//            )
//        )
//    }
}
