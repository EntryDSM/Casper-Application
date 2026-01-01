package hs.kr.entrydsm.application.domain.application.presentation.mapper

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.SubmitApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.AddressInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.ApplicantInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.ApplicationInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.AttendanceInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.AwardAndCertificateInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.GradeInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SchoolInfo
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmitApplicationRequest

fun SubmitApplicationWebRequest.toSubmitApplicationRequest(): SubmitApplicationRequest =
    SubmitApplicationRequest(
        applicantInfo = ApplicantInfo(
            applicantName = applicantInfo.applicantName,
            applicantTel = applicantInfo.applicantTel,
            birthDate = applicantInfo.birthDate,
            applicantGender = applicantInfo.applicantGender,
            parentName = applicantInfo.parentName,
            parentTel = applicantInfo.parentTel,
            parentRelation = applicantInfo.parentRelation,
            guardianGender = applicantInfo.guardianGender
        ),
        addressInfo = AddressInfo(
            isDaejeon = addressInfo.isDaejeon,
            streetAddress = addressInfo.streetAddress,
            detailAddress = addressInfo.detailAddress,
            postalCode = addressInfo.postalCode
        ),
        applicationInfo = ApplicationInfo(
            applicationType = applicationInfo.applicationType,
            educationalStatus = applicationInfo.educationalStatus,
            studentId = applicationInfo.studentId,
            graduationDate = applicationInfo.graduationDate,
            studyPlan = applicationInfo.studyPlan,
            selfIntroduce = applicationInfo.selfIntroduce,
            nationalMeritChild = applicationInfo.nationalMeritChild,
            specialAdmissionTarget = applicationInfo.specialAdmissionTarget
        ),
        schoolInfo = SchoolInfo(
            schoolCode = schoolInfo.schoolCode,
            schoolName = schoolInfo.schoolName,
            schoolPhone = schoolInfo.schoolPhone,
            teacherName = schoolInfo.teacherName
        ),
        gradeInfo = GradeInfo(
            koreanGrade = gradeInfo.koreanGrade,
            socialGrade = gradeInfo.socialGrade,
            historyGrade = gradeInfo.historyGrade,
            mathGrade = gradeInfo.mathGrade,
            scienceGrade = gradeInfo.scienceGrade,
            englishGrade = gradeInfo.englishGrade,
            techAndHomeGrade = gradeInfo.techAndHomeGrade,
            gedKorean = gradeInfo.gedKorean,
            gedSocial = gradeInfo.gedSocial,
            gedMath = gradeInfo.gedMath,
            gedScience = gradeInfo.gedScience,
            gedEnglish = gradeInfo.gedEnglish,
            gedHistory = gradeInfo.gedHistory
        ),
        attendanceInfo = AttendanceInfo(
            absence = attendanceInfo.absence,
            tardiness = attendanceInfo.tardiness,
            earlyLeave = attendanceInfo.earlyLeave,
            classExit = attendanceInfo.classExit,
            volunteer = attendanceInfo.volunteer
        ),
        awardAndCertificateInfo = AwardAndCertificateInfo(
            algorithmAward = awardAndCertificateInfo.algorithmAward,
            infoProcessingCert = awardAndCertificateInfo.infoProcessingCert
        )
    )
