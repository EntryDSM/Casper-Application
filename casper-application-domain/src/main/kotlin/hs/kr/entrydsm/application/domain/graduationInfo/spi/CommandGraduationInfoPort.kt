package hs.kr.entrydsm.application.domain.graduationInfo.spi

import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo

interface CommandGraduationInfoPort {
    fun save(graduationInfo: GraduationInfo): GraduationInfo

    fun delete(graduationInfo: GraduationInfo)
}
