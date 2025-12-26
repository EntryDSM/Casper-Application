package hs.kr.entrydsm.application.domain.graduationInfo.spi

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo

interface QueryGraduationInfoPort {
    fun queryGraduationInfoByApplication(application: Application): GraduationInfo?
    fun isExistsGraduationInfoByApplication(application: Application): Boolean
}
