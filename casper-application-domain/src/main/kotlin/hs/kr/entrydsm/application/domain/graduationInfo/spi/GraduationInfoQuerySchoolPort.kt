package hs.kr.entrydsm.application.domain.graduationInfo.spi

import hs.kr.entrydsm.application.domain.school.model.School

interface GraduationInfoQuerySchoolPort {
    fun querySchoolBySchoolCode(schoolCode: String): School?

    fun isExistsSchoolBySchoolCode(schoolCode: String): Boolean
}
