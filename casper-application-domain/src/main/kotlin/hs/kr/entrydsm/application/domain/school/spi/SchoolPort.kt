package hs.kr.entrydsm.application.domain.school.spi

import hs.kr.entrydsm.application.domain.application.spi.ApplicationQuerySchoolPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQuerySchoolPort

interface SchoolPort :
    GraduationInfoQuerySchoolPort,
    QuerySchoolPort,
    ApplicationQuerySchoolPort
