package hs.kr.entrydsm.application.domain.calculator.presentation.mapper

import hs.kr.entrydsm.application.domain.calculator.presentation.dto.request.CalculateScoreWebRequest
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.response.CalculateScoreWebResponse
import hs.kr.entrydsm.application.domain.calculator.usecase.dto.request.CalculateScoreRequest
import hs.kr.entrydsm.application.domain.calculator.usecase.dto.response.CalculateScoreResponse

fun CalculateScoreWebRequest.toCalculateScoreRequest(): CalculateScoreRequest =
    CalculateScoreRequest(
        applicationType = applicationType,
        educationalStatus = educationalStatus,
        gradeInfo =
            CalculateScoreRequest.GradeInfo(
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
                gedHistory = gradeInfo.gedHistory,
            ),
        attendanceInfo =
            CalculateScoreRequest.AttendanceInfo(
                absence = attendanceInfo.absence,
                tardiness = attendanceInfo.tardiness,
                earlyLeave = attendanceInfo.earlyLeave,
                classExit = attendanceInfo.classExit,
                volunteer = attendanceInfo.volunteer,
            ),
        awardAndCertificateInfo =
            CalculateScoreRequest.AwardAndCertificateInfo(
                algorithmAward = awardAndCertificateInfo.algorithmAward,
                infoProcessingCert = awardAndCertificateInfo.infoProcessingCert,
            ),
    )

fun CalculateScoreResponse.toCalculateScoreWebResponse(): CalculateScoreWebResponse =
    CalculateScoreWebResponse(
        totalScore = totalScore
    )
