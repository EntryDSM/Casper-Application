package hs.kr.entrydsm.application.domain.graduationInfo.spi

import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryGraduationInfoPort

interface GraduationInfoPort :
    CommandGraduationInfoPort,
    QueryGraduationInfoPort,
    ApplicationQueryGraduationInfoPort