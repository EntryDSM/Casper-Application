package hs.kr.entrydsm.application.domain.application.presentation.mapper

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.SubmitApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmitApplicationRequest

fun SubmitApplicationWebRequest.toSubmitApplicationRequest(): SubmitApplicationRequest =
    SubmitApplicationRequest(
        applicantName = applicantName,
        applicantTel = applicantTel,
        applicationType = applicationType,
        educationalStatus = educationalStatus,
        birthDate = birthDate,
        applicantGender = applicantGender,
        streetAddress = streetAddress,
        postalCode = postalCode,
        detailAddress = detailAddress,
        isDaejeon = isDaejeon,
        parentName = parentName,
        parentTel = parentTel,
        parentRelation = parentRelation,
        guardianGender = guardianGender,
        schoolCode = schoolCode,
        schoolName = schoolName,
        studentId = studentId,
        schoolPhone = schoolPhone,
        teacherName = teacherName,
        nationalMeritChild = nationalMeritChild,
        specialAdmissionTarget = specialAdmissionTarget,
        graduationDate = graduationDate,
        studyPlan = studyPlan,
        selfIntroduce = selfIntroduce,

        koreanGrade = koreanGrade,
        socialGrade = socialGrade,
        historyGrade = historyGrade,
        mathGrade = mathGrade,
        scienceGrade = scienceGrade,
        englishGrade = englishGrade,
        techAndHomeGrade = techAndHomeGrade,

        gedKorean = gedKorean,
        gedSocial = gedSocial,
        gedMath = gedMath,
        gedScience = gedScience,
        gedEnglish = gedEnglish,
        gedOpt = gedOpt,
        //gedHistory = gedHistory,
        //gedTech = gedTech,

        absence = absence,
        tardiness = tardiness,
        earlyLeave = earlyLeave,
        classExit = classExit,
        volunteer = volunteer,

        algorithmAward = algorithmAward,
        infoProcessingCert = infoProcessingCert
    )
