package hs.kr.entrydsm.application.domain.graduationInfo.service

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo
import hs.kr.entrydsm.application.domain.graduationInfo.model.Qualification
import hs.kr.entrydsm.application.global.annotation.DomainService

@DomainService
class GraduationInfoService {
    fun hasEducationalStatusMismatch(application: Application, graduationInfo: GraduationInfo): Boolean {
        application.educationalStatus ?: throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()

        return when (application.educationalStatus) {
            EducationalStatus.GRADUATE, EducationalStatus.PROSPECTIVE_GRADUATE ->
                graduationInfo !is Graduation
            EducationalStatus.QUALIFICATION_EXAM ->
                graduationInfo !is Qualification
        }
    }
}