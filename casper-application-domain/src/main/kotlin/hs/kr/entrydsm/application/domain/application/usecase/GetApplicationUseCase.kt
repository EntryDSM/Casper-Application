package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryScorePort
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryStatusPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.ApplicationCommonInformationResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.ApplicationEvaluationResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.ApplicationMoreInformationResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationResponse
import hs.kr.entrydsm.application.domain.file.spi.GenerateFileUrlPort
import hs.kr.entrydsm.application.domain.file.usecase.`object`.PathList
import hs.kr.entrydsm.application.domain.photo.exception.PhotoExceptions
import hs.kr.entrydsm.application.domain.photo.spi.QueryPhotoPort
import hs.kr.entrydsm.application.domain.score.exception.ScoreExceptions
import hs.kr.entrydsm.application.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.application.domain.status.model.Status
import hs.kr.entrydsm.application.global.annotation.UseCase

@UseCase
class GetApplicationUseCase(
    private val queryApplicationPort: QueryApplicationPort,
    private val applicationQueryStatusPort: ApplicationQueryStatusPort,
    private val applicationQueryScorePort: ApplicationQueryScorePort,
    private val queryPhotoPort: QueryPhotoPort,
    private val generateFileUrlPort: GenerateFileUrlPort
) {
    suspend fun execute(receiptCode: Long): GetApplicationResponse {
        val application = queryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()
        val status = applicationQueryStatusPort.queryStatusByReceiptCode(receiptCode)
            ?: throw StatusExceptions.StatusNotFoundException()

        val commonInformationResponse = getCommonInformationResponse(application)
        val moreInformationResponse = getMoreInformationResponse(application, status)
        val evaluationResponse = getEvaluationResponse(application)

        return GetApplicationResponse(
            commonInformation = commonInformationResponse,
            moreInformation = moreInformationResponse,
            evaluation = evaluationResponse
        )
    }

    private fun getCommonInformationResponse(application: Application): ApplicationCommonInformationResponse {
        return ApplicationCommonInformationResponse(
            name = application.applicantName!!,
            applicantGender = application.sex!!,
            telephoneNumber = application.applicantTel!!,
            parentName = application.parentName!!,
            parentTel = application.parentTel!!
        )
    }

    private fun getMoreInformationResponse(
        application: Application,
        status: Status
    ): ApplicationMoreInformationResponse? {
        val photo = queryPhotoPort.queryPhotoByUserId(application.userId)
            ?: throw PhotoExceptions.PhotoNotFoundException()

        return ApplicationMoreInformationResponse(
            photoUrl = generateFileUrlPort.generateFileUrl(photo.photoPath, PathList.PHOTO),
            birthDay = application.birthDate!!,
            applicationStatus = status.applicationStatus,
            educationalStatus = application.educationalStatus!!,
            applicationType = application.applicationType!!,
            applicationRemark = application.applicationRemark ?: ApplicationRemark.NOTHING,
            isDaejeon = application.isDaejeon!!,
            streetAddress = application.streetAddress!!
        )
    }

    private fun getEvaluationResponse(application: Application): ApplicationEvaluationResponse? {
        val score = applicationQueryScorePort.queryScoreByReceiptCode(application.receiptCode)
            ?: throw ScoreExceptions.ScoreNotFoundException()

        return ApplicationEvaluationResponse(
            totalScore = score.totalScore!!,
            totalGradeScore = score.totalGradeScore!!,
            attendanceScore = score.attendanceScore!!,
            volunteerScore = score.volunteerScore!!,
            extraScore = score.extraScore!!,
            selfIntroduce = application.selfIntroduce,
            studyPlan = application.studyPlan
        )
    }
}
