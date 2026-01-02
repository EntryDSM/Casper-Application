package hs.kr.entrydsm.application.domain.application.usecase.mapper

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmitApplicationRequest
import hs.kr.entrydsm.application.domain.user.model.User

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
}
